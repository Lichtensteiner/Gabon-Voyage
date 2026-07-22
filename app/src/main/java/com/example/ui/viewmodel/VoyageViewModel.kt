package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.VoyageRepository
import com.example.data.repository.GeminiAnalyticsService
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class VoyageViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: VoyageRepository
    private val syncManager: com.example.data.repository.FirebaseSyncManager

    private var chatMessagesListener: com.google.firebase.database.ValueEventListener? = null
    private var connectionStatusListener: com.google.firebase.database.ValueEventListener? = null

    // --- State: Connection Status ---
    private val _isConnected = MutableStateFlow(true)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // --- State: Theme Mode ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- State: Chat Messages ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()
    
    init {
        val db = AppDatabase.getInstance(application, viewModelScope)
        repository = VoyageRepository(db)
        syncManager = com.example.data.repository.FirebaseSyncManager(db, viewModelScope)
        repository.firebaseSyncManager = syncManager
        syncManager.startSync()

        viewModelScope.launch {
            try {
                val superAdminEmail = "martinienmvezogo@gmail.com"
                val existing = db.userDao.getUserByEmail(superAdminEmail)
                val superAdminUser = User(
                    id = 3,
                    nom = "Mvezogo",
                    prenom = "Martinien",
                    email = superAdminEmail,
                    phone = "077000000",
                    password = "24.05.1995Ludo",
                    isAgent = true
                )
                if (existing == null) {
                    db.userDao.insertUser(superAdminUser)
                    syncManager.writeUser(superAdminUser)
                } else if (existing.password != "24.05.1995Ludo" || !existing.isAgent) {
                    val updated = existing.copy(password = "24.05.1995Ludo", isAgent = true)
                    db.userDao.insertUser(updated)
                    syncManager.writeUser(updated)
                }
            } catch (e: Exception) {
                android.util.Log.e("VoyageViewModel", "Erreur lors de l'initialisation forcée de l'administrateur", e)
            }
        }

        // --- Init real-time Chat listener ---
        try {
            val chatListener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val list = mutableListOf<ChatMessage>()
                    for (child in snapshot.children) {
                        val msg = child.getValue(ChatMessage::class.java)
                        if (msg != null) {
                            list.add(msg)
                        }
                    }
                    _chatMessages.value = list.sortedBy { it.timestamp }
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    android.util.Log.e("VoyageViewModel", "Firebase Chat Error: ${error.message}")
                }
            }
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference("messages")
                .addValueEventListener(chatListener)
            chatMessagesListener = chatListener
        } catch (e: Exception) {
            android.util.Log.e("VoyageViewModel", "Firebase Chat Init Fail", e)
        }

        // --- Init real-time Connection status tracker ---
        try {
            val connListener = object : com.google.firebase.database.ValueEventListener {
                override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                    val connected = snapshot.getValue(Boolean::class.java) ?: false
                    _isConnected.value = connected
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    _isConnected.value = false
                }
            }
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference(".info/connected")
                .addValueEventListener(connListener)
            connectionStatusListener = connListener
        } catch (e: Exception) {
            _isConnected.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Arrêter la synchronisation globale
        syncManager.stopSync()

        // Désabonner le listener des messages de discussion
        try {
            chatMessagesListener?.let { listener ->
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference("messages")
                    .removeEventListener(listener)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoyageViewModel", "Failed to remove messages listener", e)
        }

        // Désabonner le listener de statut de connexion
        try {
            connectionStatusListener?.let { listener ->
                com.google.firebase.database.FirebaseDatabase.getInstance().getReference(".info/connected")
                    .removeEventListener(listener)
            }
        } catch (e: Exception) {
            android.util.Log.e("VoyageViewModel", "Failed to remove connected listener", e)
        }
    }

    // --- State: Auth ---
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _authSuccess = MutableStateFlow<Boolean>(false)
    val authSuccess: StateFlow<Boolean> = _authSuccess.asStateFlow()

    // --- State: Search & Filters ---
    private val _departureQuery = MutableStateFlow("Libreville")
    val departureQuery: StateFlow<String> = _departureQuery.asStateFlow()

    private val _destinationQuery = MutableStateFlow("Oyem")
    val destinationQuery: StateFlow<String> = _destinationQuery.asStateFlow()

    private val _travelDateQuery = MutableStateFlow("")
    val travelDateQuery: StateFlow<String> = _travelDateQuery.asStateFlow()

    private val _selectedAgencyFilter = MutableStateFlow<String>("Toutes")
    val selectedAgencyFilter: StateFlow<String> = _selectedAgencyFilter.asStateFlow()

    init {
        // Set default travel date as tomorrow's date
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        _travelDateQuery.value = sdf.format(calendar.time)
    }

    // --- Trips ---
    val allTrips: StateFlow<List<Trip>> = repository.allTrips
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered trips depending on from, to and agency
    val filteredTrips: StateFlow<List<Trip>> = combine(
        allTrips,
        _departureQuery,
        _destinationQuery,
        _selectedAgencyFilter
    ) { trips, dep, dest, agency ->
        trips.filter { trip ->
            trip.departure.trim().lowercase() == dep.trim().lowercase() &&
            trip.destination.trim().lowercase() == dest.trim().lowercase() &&
            (agency == "Toutes" || trip.agencyName == agency)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Users ---
    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Agencies ---
    val allAgencies: StateFlow<List<Agency>> = repository.allAgencies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Bookings ---
    private val _userBookings = MutableStateFlow<List<Booking>>(emptyList())
    val userBookings: StateFlow<List<Booking>> = _userBookings.asStateFlow()

    val allBookings: StateFlow<List<Booking>> = repository.allBookings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Active Booking flow state ---
    private val _selectedTripForBooking = MutableStateFlow<Trip?>(null)
    val selectedTripForBooking: StateFlow<Trip?> = _selectedTripForBooking.asStateFlow()

    private val _bookingSuccess = MutableStateFlow<Booking?>(null)
    val bookingSuccess: StateFlow<Booking?> = _bookingSuccess.asStateFlow()

    // Observe user bookings reactively when current user changes
    init {
        viewModelScope.launch {
            _currentUser.collectLatest { user ->
                if (user != null) {
                    repository.getBookingsByUserId(user.id).collectLatest { bookings ->
                        _userBookings.value = bookings
                    }
                } else {
                    _userBookings.value = emptyList()
                }
            }
        }
    }

    // --- Notifications state (Simulated) ---
    private val _notifications = MutableStateFlow<List<String>>(
        listOf(
            "Bienvenue sur Gabon Voyage ! Préparez votre prochain trajet vers le Grand Nord.",
            "Info : Pensez à uploader des captures nettes pour une validation de paiement rapide."
        )
    )
    val notifications: StateFlow<List<String>> = _notifications.asStateFlow()

    fun addNotification(message: String) {
        val updated = ArrayList(_notifications.value)
        updated.add(0, message)
        _notifications.value = updated
    }

    fun removeNotification(index: Int) {
        val updated = ArrayList(_notifications.value)
        if (index in updated.indices) {
            updated.removeAt(index)
            _notifications.value = updated
        }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    // --- Authentication Actions ---
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.login(email.trim(), password)
            if (user != null) {
                _currentUser.value = user
                _authSuccess.value = true
                addNotification("Connexion réussie. Bienvenue, ${user.prenom} ${user.nom} !")
            } else {
                _authError.value = "Identifiants incorrects ou utilisateur inexistant."
            }
        }
    }

    fun loginWithPhone(phone: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            if (phone.isBlank() || password.isBlank()) {
                _authError.value = "Veuillez remplir les informations de téléphone et de passe."
                return@launch
            }
            val user = repository.loginWithPhone(phone.trim(), password)
            if (user != null) {
                _currentUser.value = user
                _authSuccess.value = true
                addNotification("Connexion réussie par Téléphone. Bienvenue, ${user.prenom} !")
            } else {
                _authError.value = "Compte de téléphone introuvable ou mot de passe incorrect."
            }
        }
    }

    fun loginWithGoogle(email: String, name: String) {
        viewModelScope.launch {
            _authError.value = null
            val user = repository.loginWithGoogle(email, name)
            if (user != null) {
                _currentUser.value = user
                _authSuccess.value = true
                addNotification("Connexion avec Google réussie. Bienvenue, ${user.prenom} !")
            } else {
                _authError.value = "Échec de connexion via l'API Google."
            }
        }
    }

    fun register(nom: String, prenom: String, email: String, phone: String, password: String, isAgent: Boolean = false) {
        viewModelScope.launch {
            _authError.value = null
            if (!_isConnected.value) {
                _authError.value = "Une connexion Internet active est requise pour s'enregistrer et synchroniser votre compte."
                return@launch
            }
            if (nom.isBlank() || prenom.isBlank() || email.isBlank() || phone.isBlank() || password.isBlank()) {
                _authError.value = "Veuillez remplir tous les champs requis."
                return@launch
            }
            val newUser = User(
                nom = nom,
                prenom = prenom,
                email = email.trim(),
                phone = phone.trim(),
                password = password,
                isAgent = isAgent
            )
            val result = repository.register(newUser)
            if (result > 0) {
                _currentUser.value = newUser.copy(id = result.toInt())
                _authSuccess.value = true
                addNotification("Votre compte a été créé avec succès !")
            } else {
                _authError.value = "Cet e-mail ou numéro de téléphone est déjà associé à un compte."
            }
        }
    }

    fun logout() {
        val user = _currentUser.value
        if (user != null) {
            addNotification("Au revoir, ${user.prenom} !")
        }
        _currentUser.value = null
        _authSuccess.value = false
        _authError.value = null
    }

    // --- Search setters ---
    fun setSearchCriteria(departure: String, destination: String, date: String) {
        _departureQuery.value = departure
        _destinationQuery.value = destination
        _travelDateQuery.value = date
    }

    fun setAgencyFilter(agency: String) {
        _selectedAgencyFilter.value = agency
    }

    fun selectTripForBooking(trip: Trip?) {
        _selectedTripForBooking.value = trip
        _bookingSuccess.value = null
    }

    // --- Booking Actions ---
    fun submitBooking(
        paymentMethod: String,
        merchantNumber: String,
        senderPhone: String,
        transactionNumber: String,
        screenshotUriString: String? = null
    ) {
        val user = _currentUser.value ?: return
        val trip = _selectedTripForBooking.value ?: return
        val travelDate = _travelDateQuery.value

        viewModelScope.launch {
            if (!_isConnected.value) {
                addNotification("Échec : Vous devez être connecté à Internet pour effectuer une réservation et la synchroniser.")
                return@launch
            }
            val booking = Booking(
                userId = user.id,
                userNom = "${user.prenom} ${user.nom}",
                userPhone = senderPhone.ifBlank { user.phone },
                tripId = trip.id,
                agencyName = trip.agencyName,
                departure = trip.departure,
                destination = trip.destination,
                departureTime = trip.departureTime,
                type = trip.type,
                travelDate = travelDate,
                pricePaid = trip.price,
                paymentMethod = paymentMethod,
                merchantNumber = merchantNumber,
                transactionNumber = transactionNumber,
                screenshotUriString = screenshotUriString,
                status = "Pending"
            )

            val bookingId = repository.createBooking(booking)
            if (bookingId > 0) {
                val finalBooking = booking.copy(id = bookingId.toInt())
                _bookingSuccess.value = finalBooking
                addNotification("Réservation #${bookingId} soumise ! En attente de validation manuelles par un agent (cible 30m-2h).")
            }
        }
    }

    // --- Admin Actions ---
    fun validateBooking(booking: Booking, approved: Boolean, comments: String? = null) {
        viewModelScope.launch {
            if (!_isConnected.value) {
                addNotification("Échec de mise à jour : Vous devez être connecté pour valider ou rejeter cette réservation.")
                return@launch
            }
            repository.validateBooking(booking, approved, comments)
            
            val clientName = booking.userNom
            val direction = "${booking.departure} ➔ ${booking.destination}"
            val dec = if (approved) "APPROUVÉE" else "REJETÉE"
            val info = if (approved) "Votre billet numérique est disponible." else "Motif : ${comments ?: "Non spécifié"}"
            
            addNotification("Réservation #${booking.id} pour $clientName ($direction) par l'agent : statut mis à jour vers $dec.")
        }
    }

    fun addTripAdmin(agencyId: Int, agencyName: String, departure: String, destination: String, time: String, price: Double, type: String, seats: Int) {
        viewModelScope.launch {
            if (!_isConnected.value) {
                addNotification("Échec : Vous devez être connecté pour ajouter des voyages et les synchroniser en temps réel.")
                return@launch
            }
            val trip = Trip(
                agencyId = agencyId,
                agencyName = agencyName,
                departure = departure,
                destination = destination,
                departureTime = time,
                price = price,
                type = type,
                totalSeats = seats,
                availableSeats = seats
            )
            repository.addTrip(trip)
            addNotification("Nouveau trajet ajouté : $agencyName, de $departure à $destination à $time ($price FCFA).")
        }
    }

    fun deleteTripAdmin(tripId: Int) {
        viewModelScope.launch {
            repository.deleteTrip(tripId)
            addNotification("Trajet supprimé du catalogue par un administrateur.")
        }
    }

    fun changeUserRole(userId: Int, isAgent: Boolean) {
        viewModelScope.launch {
            repository.updateUserRole(userId, isAgent)
            addNotification("Rôle de l'utilisateur #${userId} mis à jour (isAgent = $isAgent).")
        }
    }

    fun deleteUserAdmin(userId: Int) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            addNotification("L'utilisateur numéro #${userId} a été définitivement supprimé.")
        }
    }

    fun updateUserAdmin(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
            addNotification("Les informations de l'utilisateur ${user.prenom} ont été mises à jour.")
        }
    }

    fun updateUserProfile(nom: String, prenom: String, email: String, phone: String, password: String) {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(
            nom = nom,
            prenom = prenom,
            email = email,
            phone = phone,
            password = password
        )
        viewModelScope.launch {
            try {
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
                addNotification("Profil mis à jour avec succès !")
            } catch (e: Exception) {
                _authError.value = "Erreur lors de la mise à jour du profil : ${e.message}"
            }
        }
    }

    private val _isAiAutoReplyEnabled = MutableStateFlow(true)
    val isAiAutoReplyEnabled: StateFlow<Boolean> = _isAiAutoReplyEnabled.asStateFlow()

    private val _isAiTyping = MutableStateFlow<String?>(null)
    val isAiTyping: StateFlow<String?> = _isAiTyping.asStateFlow()

    fun toggleAiAutoReply() {
        _isAiAutoReplyEnabled.value = !_isAiAutoReplyEnabled.value
    }

    fun sendChatMessage(content: String, targetThreadId: String? = null) {
        val user = _currentUser.value ?: return
        if (content.isBlank()) return
        val messagesRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("messages")
        val msgId = messagesRef.push().key ?: return
        val determinedThreadId = targetThreadId ?: if (user.isAgent) "" else user.id.toString()
        val message = ChatMessage(
            id = msgId,
            senderName = "${user.prenom} ${user.nom}",
            senderId = user.id,
            content = content.trim(),
            timestamp = System.currentTimeMillis(),
            isAgentMessage = user.isAgent,
            threadId = determinedThreadId
        )

        // Optimistic UI updates to render messages instantly in the chat UI
        val currentList = _chatMessages.value.toMutableList()
        if (!currentList.any { it.id == msgId }) {
            currentList.add(message)
            _chatMessages.value = currentList.sortedBy { it.timestamp }
        }

        messagesRef.child(msgId).setValue(message)

        // Automatically trigger AI response if auto-reply is on and the message was NOT sent by an agent/AI
        if (!user.isAgent && _isAiAutoReplyEnabled.value) {
            triggerAiAutoReply(determinedThreadId, content.trim())
        }
    }

    fun triggerAiAutoReply(threadId: String, latestUserMessage: String) {
        viewModelScope.launch {
            _isAiTyping.value = threadId
            kotlinx.coroutines.delay(1800) // Realistic typing simulation
            
            val tripsList = allTrips.value
            val tripsSummary = tripsList.joinToString("; ") { 
                "${it.agencyName}: ${it.departure} -> ${it.destination} à ${it.departureTime} pour ${it.price.toInt()} FCFA"
            }

            val prompt = """
                Tu es un assistant de chat IA expert (Conseiller Client Virtuel) intégré à l'application "Gabon Voyage", une plateforme de réservation numérique de trajets de bus au Gabon.
                Réponds au voyageur de manière polie, concise (max 3 phrases), rassurante, chaleureuse et professionnelle en français.
                
                Voici la liste des trajets et agences actuellement disponibles sur notre réseau :
                $tripsSummary
                
                Dernier message du client : "$latestUserMessage"
                
                Donne une réponse précise et directe. N'invente pas de trajets ou de prix qui ne figurent pas dans la liste ci-dessus. Si tu n'as pas l'information ou si la demande est complexe, conseille-lui gentiment de s'adresser à un agent humain.
            """.trimIndent()

            try {
                val aiReplyText = GeminiAnalyticsService.generateAnalysis(prompt)
                val messagesRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("messages")
                val responseMsgId = messagesRef.push().key
                if (responseMsgId != null) {
                    val aiMessage = ChatMessage(
                        id = responseMsgId.orEmpty(),
                        senderName = "Conseiller IA",
                        senderId = -999, // Unique ID for Gemini AI
                        content = aiReplyText.trim(),
                        timestamp = System.currentTimeMillis(),
                        isAgentMessage = true,
                        threadId = threadId
                    )

                    // Optimistic update
                    val currentList = _chatMessages.value.toMutableList()
                    if (!currentList.any { it.id == responseMsgId }) {
                        currentList.add(aiMessage)
                        _chatMessages.value = currentList.sortedBy { it.timestamp }
                    }

                    messagesRef.child(responseMsgId).setValue(aiMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("VoyageViewModel", "Failed to generate AI auto reply", e)
            } finally {
                _isAiTyping.value = null
            }
        }
    }

    fun triggerManualAiReply(threadId: String, messages: List<ChatMessage>) {
        viewModelScope.launch {
            _isAiTyping.value = threadId
            val lastMessageText = messages.lastOrNull { !it.isAgentMessage }?.content ?: "Bonjour"
            
            val tripsList = allTrips.value
            val tripsSummary = tripsList.joinToString("; ") { 
                "${it.agencyName}: ${it.departure} -> ${it.destination} à ${it.departureTime} pour ${it.price.toInt()} FCFA"
            }

            val prompt = """
                Tu es un conseiller client d'assistance IA pour "Gabon Voyage". Réponds au message de l'utilisateur. 
                Reste concis, précis et chaleureux.
                
                Trajets configurés :
                $tripsSummary
                
                Message : "$lastMessageText"
            """.trimIndent()

            try {
                val aiReplyText = GeminiAnalyticsService.generateAnalysis(prompt)
                val messagesRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("messages")
                val responseMsgId = messagesRef.push().key
                if (responseMsgId != null) {
                    val aiMessage = ChatMessage(
                        id = responseMsgId.orEmpty(),
                        senderName = "Assistant IA",
                        senderId = -999,
                        content = aiReplyText.trim(),
                        timestamp = System.currentTimeMillis(),
                        isAgentMessage = true,
                        threadId = threadId
                    )

                    // Optimistic update
                    val currentList = _chatMessages.value.toMutableList()
                    if (!currentList.any { it.id == responseMsgId }) {
                        currentList.add(aiMessage)
                        _chatMessages.value = currentList.sortedBy { it.timestamp }
                    }

                    messagesRef.child(responseMsgId).setValue(aiMessage)
                }
            } catch (e: Exception) {
                android.util.Log.e("VoyageViewModel", "Failed to generate manual AI reply", e)
            } finally {
                _isAiTyping.value = null
            }
        }
    }

    fun clearChatMessages(threadId: String) {
        viewModelScope.launch {
            try {
                val messagesRef = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("messages")
                messagesRef.addListenerForSingleValueEvent(object : com.google.firebase.database.ValueEventListener {
                    override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                        for (child in snapshot.children) {
                            val msg = child.getValue(ChatMessage::class.java)
                            if (msg != null && msg.threadId == threadId) {
                                child.ref.removeValue()
                            }
                        }
                    }
                    override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
                })
                addNotification("Discussion vidée avec succès !")
            } catch (e: Exception) {
                android.util.Log.e("VoyageViewModel", "Clear Chat Error", e)
            }
        }
    }

    // --- State: AI Analytics via Gemini ---
    private val _aiAnalysisResult = MutableStateFlow<String?>(null)
    val aiAnalysisResult: StateFlow<String?> = _aiAnalysisResult.asStateFlow()

    private val _aiAnalysisLoading = MutableStateFlow(false)
    val aiAnalysisLoading: StateFlow<Boolean> = _aiAnalysisLoading.asStateFlow()

    private val _aiAnalysisError = MutableStateFlow<String?>(null)
    val aiAnalysisError: StateFlow<String?> = _aiAnalysisError.asStateFlow()

    fun generateAiAnalyticsReport() {
        viewModelScope.launch {
            _aiAnalysisLoading.value = true
            _aiAnalysisError.value = null
            _aiAnalysisResult.value = null
            
            val usersList = allUsers.value
            val tripsList = allTrips.value
            val bookingsList = allBookings.value
            val agenciesList = allAgencies.value

            val totalRevenue = bookingsList.filter { it.status == "Confirmed" }.sumOf { it.pricePaid }
            val pendingCount = bookingsList.count { it.status == "Pending" }
            val approvedCount = bookingsList.count { it.status == "Confirmed" }
            val rejectedCount = bookingsList.count { it.status == "Rejected" }

            val prompt = """
                Tu es un expert analyste d'entreprise pour l'application "Gabon Voyage", une plateforme de réservation numérique de trajets de bus reliant principalement Libreville au Grand Nord du Gabon (Oyem, Bitam, etc.).
                Voici un rapport statistique complet en temps réel issu de la base de données locale de l'application :
                
                - Nombre total d'utilisateurs inscrits : ${usersList.size}
                  * Rôle Agents/Admins : ${usersList.count { it.isAgent }}
                  * Rôle Voyageurs standard : ${usersList.count { !it.isAgent }}
                
                - Nombre d'agences de voyages partenaires intégrées : ${agenciesList.size}
                  * Liste des agences : ${agenciesList.joinToString { it.name }}
                
                - Nombre total de trajets de bus configurés : ${tripsList.size}
                  * Liste des trajets : ${tripsList.joinToString { "${it.agencyName} (${it.departure} -> ${it.destination} à ${it.departureTime} pour ${it.price} FCFA, ${it.availableSeats} places)" }}
                
                - Nombre total de réservations (billets) : ${bookingsList.size}
                  * Billets Validés/Approuvés : ${approvedCount}
                  * Billets Rejetés : ${rejectedCount}
                  * Billets En attente : ${pendingCount}
                  * Chiffre d'affaires cumulé validé : ${totalRevenue.toInt()} FCFA
                  * Détails des réservations : ${bookingsList.take(20).joinToString { "${it.userNom} payé via ${it.paymentMethod} pour ${it.departure}->${it.destination} (${it.pricePaid.toInt()} FCFA, statut: ${it.status})" }}
                
                Analyse ces données en détail et génère un rapport d'analyse commerciale stratégique et visuellement attrayant en français.
                Ton rapport doit comporter les sections suivantes structurées avec du formatage Markdown propre :
                1. 📊 **Résumé Exécutif de l'Activité** : Un résumé rapide de l'état de l'application.
                2. 💰 **Analyse Financière & Performance des Agences** : Performance des ventes par rapport aux trajets configurés, revenus par agence.
                3. 🚀 **Recommandations Stratégiques de l'IA** : Des propositions concrètes pour optimiser les prix, ajouter ou modifier des trajets ou des horaires, ou promouvoir l'application auprès des voyageurs.
                4. 📈 **Plan d'Action Immédiat** : Les 3 prochaines étapes concrètes recommandées pour le super-administrateur.
                
                Reste très professionnel, constructif et propose des analyses réelles basées sur ces chiffres exacts. Si les chiffres sont bas ou s'il n'y a pas encore beaucoup d'activité, explique comment stimuler les premières réservations et fidéliser les agences.
            """.trimIndent()

            try {
                val report = GeminiAnalyticsService.generateAnalysis(prompt)
                _aiAnalysisResult.value = report
            } catch (e: Exception) {
                _aiAnalysisError.value = e.localizedMessage ?: "Une erreur inconnue est survenue"
            } finally {
                _aiAnalysisLoading.value = false
            }
        }
    }
}
