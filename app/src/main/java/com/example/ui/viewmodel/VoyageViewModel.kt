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

    // --- State: Manual Refresh / Sync ---
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            syncManager.startSync()
            kotlinx.coroutines.delay(1200)
            _isRefreshing.value = false
            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            addNotification("Synchronisation des données en temps réel effectuée à $timeStr")
        }
    }

    // --- State: Theme Mode ---
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // --- State: Chat Messages ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // --- State: Super Admin ERP Flotte & Véhicules ---
    private val _vehicles = MutableStateFlow<List<Vehicle>>(
        listOf(
            Vehicle(1, "GA-882-GV", "Toyota", "Coaster VIP", "VIP", 22, listOf("Climatisation", "Wifi HighSpeed", "Prise USB", "Écran TV", "Siège Cuir"), "Excellent", "15/12/2026", "20/10/2026", true, "Jean-Paul Mba"),
            Vehicle(2, "GA-304-GV", "Mercedes", "Sprinter 18", "Standard", 18, listOf("Climatisation", "Prise USB", "Wifi"), "Bon état", "08/11/2026", "12/09/2026", true, "Marc Ndong"),
            Vehicle(3, "GA-512-GV", "Isuzu", "Bus Grand Nord 30", "Bus 30 Places", 30, listOf("Climatisation", "Soute Spacieuse", "Toilette"), "Excellent", "30/01/2027", "18/11/2026", true, "Alain Ondo"),
            Vehicle(4, "GA-109-GV", "Toyota", "HiAce Express", "Mini Bus", 14, listOf("Climatisation"), "En révision", "10/08/2026", "05/06/2026", false, "Serge Obame")
        )
    )
    val vehicles: StateFlow<List<Vehicle>> = _vehicles.asStateFlow()

    // --- State: Super Admin ERP Chauffeurs ---
    private val _drivers = MutableStateFlow<List<Driver>>(
        listOf(
            Driver(1, "Mba", "Jean-Paul", "077 12 34 56", "jp.mba@gabonvoyage.ga", "PERMIS-GA-89120", "15/06/2028", 8, "En Trajet", "GA-882-GV"),
            Driver(2, "Ndong", "Marc", "066 98 76 54", "m.ndong@gabonvoyage.ga", "PERMIS-GA-44129", "20/09/2027", 5, "Disponible", "GA-304-GV"),
            Driver(3, "Ondo", "Alain", "074 55 44 33", "a.ondo@gabonvoyage.ga", "PERMIS-GA-11029", "10/12/2029", 12, "Disponible", "GA-512-GV"),
            Driver(4, "Obame", "Serge", "062 11 22 33", "s.obame@gabonvoyage.ga", "PERMIS-GA-77112", "05/04/2026", 3, "En Repos", "GA-109-GV")
        )
    )
    val drivers: StateFlow<List<Driver>> = _drivers.asStateFlow()

    // --- State: Super Admin ERP Colis & Bagages ---
    private val _parcels = MutableStateFlow<List<Parcel>>(
        listOf(
            Parcel("COL-8801", "Emmanuel Mve", "077123456", "Sandrine Ntsame", "066889900", "Libreville ➔ Oyem", 12.5, 8000.0, "TRACK-GV-901", "En transit"),
            Parcel("COL-8802", "Brice Ondo", "074223344", "Patrice Ekomie", "077445566", "Libreville ➔ Bitam", 8.0, 5000.0, "TRACK-GV-902", "En attente"),
            Parcel("COL-8803", "Chantal Biyoghe", "066112233", "Pauline Mengue", "062998877", "Oyem ➔ Libreville", 25.0, 15000.0, "TRACK-GV-903", "Livré")
        )
    )
    val parcels: StateFlow<List<Parcel>> = _parcels.asStateFlow()

    // --- State: Super Admin ERP Employés & RBAC ---
    private val _employees = MutableStateFlow<List<Employee>>(
        listOf(
            Employee(1, "Ludovic Lichtensteiner", "lichtensteinerleroitelet@gmail.com", "Super Admin", "Direction Générale", true),
            Employee(2, "Thierry Mba", "t.mba@gabonvoyage.ga", "Directeur d'Agence", "Agence Centrale Libreville", true),
            Employee(3, "Clarisse Nguema", "c.nguema@gabonvoyage.ga", "Comptable", "Direction Financière", true),
            Employee(4, "Felix Zue", "f.zue@gabonvoyage.ga", "Agent de réservation", "Guichet Agence Oyem", true)
        )
    )
    val employees: StateFlow<List<Employee>> = _employees.asStateFlow()

    // --- State: Super Admin ERP Telemetry GPS ---
    private val _telemetries = MutableStateFlow<List<VehicleTelemetry>>(
        listOf(
            VehicleTelemetry("GA-882-GV", "Libreville ➔ Oyem (Express VIP)", "Pk 185 (Proche Ndjolé)", 88, "13:30", "En Route (À l'Heure)"),
            VehicleTelemetry("GA-304-GV", "Libreville ➔ Bitam", "Pk 90 (Kango)", 82, "15:45", "En Route (À l'Heure)"),
            VehicleTelemetry("GA-512-GV", "Oyem ➔ Libreville", "Mitzic Station", 0, "16:00", "Arrêt Station (Escale)")
        )
    )
    val telemetries: StateFlow<List<VehicleTelemetry>> = _telemetries.asStateFlow()

    // --- State: Promo Codes ---
    private val _promoCodes = MutableStateFlow<List<String>>(listOf("GABON2026", "VIP10", "WELCOME1000"))
    val promoCodes: StateFlow<List<String>> = _promoCodes.asStateFlow()

    // --- State: Super Admin Trash / Corbeille ---
    private val _trashItems = MutableStateFlow<List<TrashItem>>(
        listOf(
            TrashItem(
                id = "TRASH-01",
                type = "Colis & Bagages",
                title = "Colis TRACK-GV-800",
                description = "Libreville ➔ Mouila - Exp: Alain Bongo",
                deletedDate = "22/07/2026 08:30",
                originalObject = Parcel("COL-7000", "Alain Bongo", "077000000", "Jeanne Bongo", "066000000", "Libreville ➔ Mouila", 6.0, 4000.0, "TRACK-GV-800", "Annulé")
            )
        )
    )
    val trashItems: StateFlow<List<TrashItem>> = _trashItems.asStateFlow()

    // --- State: Super Admin Backup & Restoration ---
    private val _backups = MutableStateFlow<List<BackupSnapshot>>(
        listOf(
            BackupSnapshot(
                id = "BK-2026-07-21",
                timestamp = "21/07/2026 23:59",
                name = "Sauvegarde Automatique Système - Clôture",
                vehiclesCount = 4,
                driversCount = 4,
                parcelsCount = 3,
                bookingsCount = 12,
                tripsCount = 6,
                sizeKb = 245
            )
        )
    )
    val backups: StateFlow<List<BackupSnapshot>> = _backups.asStateFlow()
    
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
                    val connected = snapshot.getValue(Boolean::class.java) ?: true
                    _isConnected.value = connected
                }
                override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                    _isConnected.value = true
                }
            }
            com.google.firebase.database.FirebaseDatabase.getInstance().getReference(".info/connected")
                .addValueEventListener(connListener)
            connectionStatusListener = connListener
        } catch (e: Exception) {
            _isConnected.value = true
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
            val cleanInput = email.trim()
            val cleanPass = password.trim()

            if (cleanInput.isBlank() || cleanPass.isBlank()) {
                _authError.value = "Veuillez saisir votre identifiant/email et votre mot de passe."
                return@launch
            }

            // 1. First check in-memory RBAC users list (populated with all roles)
            val rbacUser = _usersList.value.find {
                it.email.equals(cleanInput, ignoreCase = true) || it.phone == cleanInput
            }

            if (rbacUser != null) {
                if (rbacUser.password == cleanPass) {
                    if (rbacUser.status == "Suspendu" || rbacUser.status == "Inactif") {
                        _authError.value = "Ce compte (${rbacUser.role}) est actuellement suspendu."
                        return@launch
                    }
                    val updatedUser = rbacUser.copy(lastLogin = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))
                    _currentUser.value = updatedUser
                    _authSuccess.value = true
                    repository.updateUser(updatedUser)
                    addNotification("Connexion réussie (${updatedUser.role}). Bienvenue, ${updatedUser.prenom} ${updatedUser.nom} !")
                    return@launch
                } else {
                    _authError.value = "Mot de passe incorrect pour $cleanInput."
                    return@launch
                }
            }

            // 2. Fallback to Room Database query
            val dbUser = repository.login(cleanInput, cleanPass)
            if (dbUser != null) {
                if (dbUser.status == "Suspendu" || dbUser.status == "Inactif") {
                    _authError.value = "Ce compte est actuellement suspendu."
                    return@launch
                }
                _currentUser.value = dbUser
                _authSuccess.value = true
                addNotification("Connexion réussie. Bienvenue, ${dbUser.prenom} ${dbUser.nom} !")
            } else {
                _authError.value = "Identifiants incorrects ou utilisateur inexistant."
            }
        }
    }

    fun loginWithPhone(phone: String, password: String) {
        viewModelScope.launch {
            _authError.value = null
            val cleanPhone = phone.trim()
            val cleanPass = password.trim()

            if (cleanPhone.isBlank() || cleanPass.isBlank()) {
                _authError.value = "Veuillez remplir les informations de téléphone et de passe."
                return@launch
            }

            val rbacUser = _usersList.value.find {
                it.phone == cleanPhone || it.email.equals(cleanPhone, ignoreCase = true)
            }

            if (rbacUser != null) {
                if (rbacUser.password == cleanPass) {
                    if (rbacUser.status == "Suspendu" || rbacUser.status == "Inactif") {
                        _authError.value = "Ce compte (${rbacUser.role}) est actuellement suspendu."
                        return@launch
                    }
                    val updatedUser = rbacUser.copy(lastLogin = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))
                    _currentUser.value = updatedUser
                    _authSuccess.value = true
                    repository.updateUser(updatedUser)
                    addNotification("Connexion réussie par Téléphone (${updatedUser.role}). Bienvenue, ${updatedUser.prenom} !")
                    return@launch
                } else {
                    _authError.value = "Mot de passe incorrect."
                    return@launch
                }
            }

            val dbUser = repository.loginWithPhone(cleanPhone, cleanPass)
            if (dbUser != null) {
                if (dbUser.status == "Suspendu" || dbUser.status == "Inactif") {
                    _authError.value = "Ce compte est actuellement suspendu."
                    return@launch
                }
                _currentUser.value = dbUser
                _authSuccess.value = true
                addNotification("Connexion réussie par Téléphone. Bienvenue, ${dbUser.prenom} !")
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

    fun clearSelectedTripForBooking() {
        _selectedTripForBooking.value = null
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
            val booking = Booking(
                userId = user.id,
                userNom = "${user.prenom} ${user.nom}",
                userPrenom = user.prenom,
                userPhone = senderPhone.ifBlank { user.phone },
                tripId = trip.id,
                agencyName = trip.agencyName,
                departure = trip.departure,
                destination = trip.destination,
                departureTime = trip.departureTime,
                type = trip.type,
                travelDate = travelDate,
                unitPrice = trip.price,
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
                addNotification("Réservation #${bookingId} soumise ! En attente de validation manuelle par un agent (cible 30m-2h).")
            }
        }
    }

    fun submitFullBooking(
        userNom: String,
        userPrenom: String,
        userSex: String,
        userPhone: String,
        userEmail: String,
        userBirthDate: String,
        idDocumentType: String,
        idDocumentNumber: String,
        trip: Trip,
        seatsCount: Int,
        selectedSeats: List<String>,
        paymentMethod: String,
        merchantNumber: String,
        transactionNumber: String
    ) {
        val user = _currentUser.value ?: return
        val travelDate = _travelDateQuery.value.ifBlank {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        }

        val refNum = (100000..999999).random()
        val bookingRef = "GV-2026-$refNum"
        val pin = (100000..999999).random().toString()
        val tripCode = "TRIP-${trip.departure.take(3).uppercase()}-${trip.destination.take(3).uppercase()}"
        val total = trip.price * seatsCount
        val seatsStr = selectedSeats.joinToString(", ")

        viewModelScope.launch {
            val booking = Booking(
                userId = user.id,
                userNom = userNom,
                userPrenom = userPrenom,
                userSex = userSex,
                userPhone = userPhone,
                userEmail = userEmail,
                userBirthDate = userBirthDate,
                idDocumentType = idDocumentType,
                idDocumentNumber = idDocumentNumber,
                tripId = trip.id,
                tripCode = tripCode,
                bookingReference = bookingRef,
                pinCode = pin,
                seatsCount = seatsCount,
                selectedSeats = seatsStr,
                agencyName = trip.agencyName,
                departure = trip.departure,
                destination = trip.destination,
                departureTime = trip.departureTime,
                type = trip.type,
                travelDate = travelDate,
                unitPrice = trip.price,
                pricePaid = total,
                paymentMethod = paymentMethod,
                merchantNumber = merchantNumber,
                transactionNumber = transactionNumber.ifBlank { "TX-$refNum" },
                status = "Confirmed"
            )

            val bookingId = repository.createBooking(booking)
            if (bookingId > 0) {
                val finalBooking = booking.copy(id = bookingId.toInt())
                _bookingSuccess.value = finalBooking
                
                // Decrement available seats for the trip
                val newAvailable = (trip.availableSeats - seatsCount).coerceAtLeast(0)
                val updatedTrip = trip.copy(availableSeats = newAvailable)
                repository.updateTrip(updatedTrip)

                // Push notification alert
                addNotification("🎟️ Réservation Confirmée ! Réf: $bookingRef • Passager: $userPrenom $userNom • $seatsCount place(s) ($seatsStr) • ${total.toInt()} FCFA • PIN: $pin")
            }
        }
    }

    // --- Admin Actions ---
    fun validateBooking(booking: Booking, approved: Boolean, comments: String? = null) {
        viewModelScope.launch {
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

    // --- ERP Flotte & Véhicules Actions ---
    fun addVehicle(plate: String, make: String, model: String, category: String, capacity: Int, features: List<String>, driverName: String) {
        val newId = (_vehicles.value.maxOfOrNull { it.id } ?: 0) + 1
        val newVehicle = Vehicle(
            id = newId,
            plateNumber = plate,
            make = make,
            model = model,
            category = category,
            capacity = capacity,
            features = features,
            condition = "Excellent",
            isActive = true,
            driverName = driverName
        )
        _vehicles.value = _vehicles.value + newVehicle
        addNotification("Véhicule $plate ($make $model) ajouté à la flotte.")
    }

    fun toggleVehicleActive(vehicleId: Int) {
        _vehicles.value = _vehicles.value.map {
            if (it.id == vehicleId) it.copy(isActive = !it.isActive) else it
        }
    }

    fun deleteVehicle(vehicleId: Int) {
        val target = _vehicles.value.find { it.id == vehicleId }
        if (target != null) {
            _vehicles.value = _vehicles.value.filterNot { it.id == vehicleId }
            val trashItem = TrashItem(
                id = "TRASH-VEH-${target.id}",
                type = "Véhicule Flotte",
                title = "Véhicule ${target.plateNumber}",
                description = "${target.make} ${target.model} (${target.category}) - ${target.capacity} places",
                deletedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                originalObject = target
            )
            _trashItems.value = _trashItems.value + trashItem
            addNotification("Véhicule ${target.plateNumber} déplacé vers la corbeille.")
        }
    }

    // --- ERP Chauffeurs Actions ---
    fun addDriver(nom: String, prenom: String, phone: String, email: String, license: String, vehiclePlate: String) {
        val newId = (_drivers.value.maxOfOrNull { it.id } ?: 0) + 1
        val newDriver = Driver(
            id = newId,
            nom = nom,
            prenom = prenom,
            phone = phone,
            email = email,
            licenseNumber = license,
            status = "Disponible",
            assignedVehiclePlate = vehiclePlate
        )
        _drivers.value = _drivers.value + newDriver
        addNotification("Chauffeur $prenom $nom enregistré dans l'ERP.")
    }

    fun deleteDriver(driverId: Int) {
        val target = _drivers.value.find { it.id == driverId }
        if (target != null) {
            _drivers.value = _drivers.value.filterNot { it.id == driverId }
            val trashItem = TrashItem(
                id = "TRASH-DRV-${target.id}",
                type = "Chauffeur",
                title = "Chauffeur ${target.prenom} ${target.nom}",
                description = "Permis: ${target.licenseNumber} - Tél: ${target.phone}",
                deletedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                originalObject = target
            )
            _trashItems.value = _trashItems.value + trashItem
            addNotification("Chauffeur ${target.prenom} ${target.nom} déplacé vers la corbeille.")
        }
    }

    // --- ERP Colis & Bagages Actions ---
    fun addParcel(sender: String, senderPhone: String, recipient: String, recipientPhone: String, route: String, weight: Double, price: Double) {
        val trackCode = "TRACK-GV-${(100..999).random()}"
        val idCode = "COL-${(8800..8999).random()}"
        val parcel = Parcel(
            id = idCode,
            senderName = sender,
            senderPhone = senderPhone,
            recipientName = recipient,
            recipientPhone = recipientPhone,
            route = route,
            weightKg = weight,
            priceFcfa = price,
            trackingCode = trackCode,
            status = "En transit"
        )
        _parcels.value = _parcels.value + parcel
        addNotification("Colis $trackCode enregistré ($route). Envoi SMS au destinataire $recipientPhone.")
    }

    fun updateParcelStatus(parcelId: String, newStatus: String) {
        _parcels.value = _parcels.value.map {
            if (it.id == parcelId) it.copy(status = newStatus) else it
        }
        addNotification("Statut du colis $parcelId mis à jour : $newStatus")
    }

    fun deleteParcel(parcelId: String) {
        val target = _parcels.value.find { it.id == parcelId }
        if (target != null) {
            _parcels.value = _parcels.value.filterNot { it.id == parcelId }
            val trashItem = TrashItem(
                id = "TRASH-PARCEL-${target.id}",
                type = "Colis & Bagages",
                title = "Colis ${target.trackingCode}",
                description = "${target.route} - Exp: ${target.senderName} ➔ Dest: ${target.recipientName}",
                deletedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                originalObject = target
            )
            _trashItems.value = _trashItems.value + trashItem
            addNotification("Colis ${target.trackingCode} déplacé vers la corbeille.")
        }
    }

    // --- ERP Employees Actions ---
    fun addEmployee(name: String, email: String, role: String, agency: String) {
        val newId = (_employees.value.maxOfOrNull { it.id } ?: 0) + 1
        val emp = Employee(newId, name, email, role, agency, true)
        _employees.value = _employees.value + emp
        addNotification("Employé $name ($role) ajouté au personnel.")
    }

    fun toggleEmployeeStatus(id: Int) {
        _employees.value = _employees.value.map {
            if (it.id == id) it.copy(active = !it.active) else it
        }
    }

    fun deleteEmployee(id: Int) {
        val target = _employees.value.find { it.id == id }
        if (target != null) {
            _employees.value = _employees.value.filterNot { it.id == id }
            val trashItem = TrashItem(
                id = "TRASH-EMP-${target.id}",
                type = "Employé",
                title = "Employé ${target.name}",
                description = "Rôle: ${target.role} - Agence: ${target.agency}",
                deletedDate = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                originalObject = target
            )
            _trashItems.value = _trashItems.value + trashItem
            addNotification("Employé ${target.name} déplacé vers la corbeille.")
        }
    }

    // --- State: RBAC Roles & Permissions ---
    private val _roles = MutableStateFlow<List<Role>>(
        listOf(
            Role(1, "Super Administrateur", "Accès total et universel à l'ensemble du système, gestion des utilisateurs, rôles, agences et sécurité."),
            Role(2, "Directeur", "Supervision globale des opérations, tableaux de bord KPI, flotte, horaires et rapports de performance."),
            Role(3, "Comptable", "Gestion financière exclusive, trésorerie, caisse, banque, dépenses, factures et grands livres comptables."),
            Role(4, "Agent de Réservation", "Gestion des guichets, création de réservations, encaissement, choix des sièges et impression de billets."),
            Role(5, "Contrôleur", "Validation d'embarquement à bord des bus, vérification des billets par QR code, liste de présence passagers."),
            Role(6, "Chauffeur", "Espace mobile pour missions de conduite du jour, itinéraires, suivi du bus et signalement d'incidents."),
            Role(7, "Gestionnaire de Flotte", "Gestion de la flotte de véhicules, révisions techniques, assurances, affectation aux trajets."),
            Role(8, "Responsable RH", "Gestion des contrats, plannings des chauffeurs et employés, congés et présence."),
            Role(9, "Responsable Fret", "Expédition des colis et bagages, enregistrement du poids, suivi de livraison et tarification fret."),
            Role(10, "Responsable Maintenance", "Planification des entretiens mécaniques, ordres de réparation et pièces de rechange."),
            Role(11, "Responsable Service Client", "Suivi des réclamations passagers, chat d'assistance en direct et satisfaction voyageur.")
        )
    )
    val roles: StateFlow<List<Role>> = _roles.asStateFlow()

    private val _permissions = MutableStateFlow<List<Permission>>(
        listOf(
            Permission(1, "Gestion Utilisateurs & Rôles", "Sécurité & RBAC", "Créer, modifier, suspendre et attribuer des rôles aux utilisateurs."),
            Permission(2, "Supervision & KPIs Opérations", "Direction", "Consulter le chiffre d'affaires global, taux de remplissage et performances."),
            Permission(3, "Gestion Comptable & Trésorerie", "Finances", "Consulter la caisse, la banque, saisir les dépenses et établir les bilans."),
            Permission(4, "Émission & Vente de Billets", "Guichet", "Réserver des places, encaisser le paiement et imprimer les billets."),
            Permission(5, "Contrôle & Scanner QR Code", "Embarquement", "Scanner les passagers, valider le billet et signaler les absents."),
            Permission(6, "Missions & Itinéraires Chauffeur", "Transport", "Accéder aux feuilles de route, confirmer départs et arrivées."),
            Permission(7, "Gestion de la Flotte Véhicules", "Logistique", "Suivre l'état des bus, le kilométrage, les contrôles techniques."),
            Permission(8, "Gestion du Fret Colis", "Colis", "Enregistrer les expéditions de bagages et délivrer les colis aux agences."),
            Permission(9, "Audit Logs & Sauvegardes", "Sécurité", "Consulter les journaux d'accès et exécuter des sauvegardes système.")
        )
    )
    val permissions: StateFlow<List<Permission>> = _permissions.asStateFlow()

    private val _usersList = MutableStateFlow<List<User>>(
        listOf(
            User(1, "Mve Zogo", "Martinien", "https://i.pravatar.cc/150?img=11", "martinienmvezogo@gmail.com", "077000000", "24.05.1995Ludo", "Super Administrateur", 1, "Direction Générale", "Actif", "22/07/2026 08:30", "01/01/2026", "22/07/2026", true),
            User(2, "Mba", "Thierry", "https://i.pravatar.cc/150?img=12", "directeur@gabonvoyage.ga", "077112233", "Directeur123", "Directeur", 1, "Agence Centrale Libreville", "Actif", "22/07/2026 07:15", "10/02/2026", "22/07/2026", true),
            User(3, "Nguema", "Clarisse", "https://i.pravatar.cc/150?img=5", "comptable@gabonvoyage.ga", "066445566", "Compta2026", "Comptable", 1, "Direction Financière", "Actif", "21/07/2026 18:00", "15/02/2026", "21/07/2026", true),
            User(4, "Zue", "Felix", "https://i.pravatar.cc/150?img=8", "agent@gabonvoyage.ga", "074889900", "AgentPass1", "Agent de Réservation", 2, "Agence Oyem", "Actif", "22/07/2026 08:00", "01/03/2026", "22/07/2026", true),
            User(5, "Obame", "Paul", "https://i.pravatar.cc/150?img=15", "controleur@gabonvoyage.ga", "062334455", "Control2026", "Contrôleur", 1, "Gare Routière PK8", "Actif", "22/07/2026 06:45", "12/03/2026", "22/07/2026", true),
            User(6, "Mba", "Jean-Paul", "https://i.pravatar.cc/150?img=3", "chauffeur@gabonvoyage.ga", "077123456", "DriverPass1", "Chauffeur", 1, "Agence Centrale Libreville", "Actif", "22/07/2026 06:00", "05/01/2026", "22/07/2026", true),
            User(7, "Ekomie", "Patrick", "https://i.pravatar.cc/150?img=60", "flotte@gabonvoyage.ga", "077990011", "Flotte2026", "Gestionnaire de Flotte", 1, "Garage Central", "Actif", "20/07/2026 14:20", "20/02/2026", "20/07/2026", true),
            User(8, "Biyoghe", "Chantal", "https://i.pravatar.cc/150?img=47", "rh@gabonvoyage.ga", "066332211", "RhPass2026", "Responsable RH", 1, "Direction Générale", "Actif", "19/07/2026 09:10", "10/01/2026", "19/07/2026", true)
        )
    )
    val usersList: StateFlow<List<User>> = _usersList.asStateFlow()

    private val _auditLogs = MutableStateFlow<List<AuditLog>>(
        listOf(
            AuditLog("AUD-901", 1, "Martinien Mve Zogo", "Super Administrateur", "Connexion Réussie 2FA", "Sécurité", "22/07/2026 08:30", "197.234.221.10", "Succès"),
            AuditLog("AUD-902", 3, "Clarisse Nguema", "Comptable", "Validation Clôture Caisse", "Finances", "21/07/2026 18:05", "197.234.221.14", "Succès"),
            AuditLog("AUD-903", 2, "Thierry Mba", "Directeur", "Modification horaire trajet Lbv-Oyem", "Voyages", "21/07/2026 14:12", "197.234.221.12", "Succès"),
            AuditLog("AUD-904", 4, "Felix Zue", "Agent de Réservation", "Génération Billet #REF-GV-88", "Billetterie", "21/07/2026 11:40", "197.234.222.05", "Succès")
        )
    )
    val auditLogs: StateFlow<List<AuditLog>> = _auditLogs.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(
        listOf(
            Expense("EXP-101", "Carburant", 185000.0, "Plein Gasoil Bus VIP GA-882-GV - Total PK12", "22/07/2026", "Agence Centrale Libreville", "Approuvé"),
            Expense("EXP-102", "Maintenance", 120000.0, "Vidange & Changement Filtres Huile GA-304-GV", "21/07/2026", "Agence Oyem", "Approuvé"),
            Expense("EXP-103", "Assurances", 450000.0, "Renouvellement Assurance Flotte OGAR 3 Mois", "15/07/2026", "Direction Générale", "Approuvé"),
            Expense("EXP-104", "Fournitures", 35000.0, "Achat Rouleaux Papier Thermique Imprimantes Billets", "18/07/2026", "Agence Bitam", "Approuvé")
        )
    )
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _invoices = MutableStateFlow<List<Invoice>>(
        listOf(
            Invoice("FAC-2026-001", "Alain Bongo", "Facture Billet VIP", 15000.0, "22/07/2026", "Airtel Money", "Payée"),
            Invoice("FAC-2026-002", "Société WoodGabon", "Facture Fret Colis Spécial", 85000.0, "21/07/2026", "Virement Bancaire", "Payée"),
            Invoice("FAC-2026-003", "Jeanne Ntsame", "Reçu de Paiement Billet Standard", 10000.0, "21/07/2026", "Espèces Caisse", "Payée"),
            Invoice("FAC-2026-004", "Marc Ondo", "Avoir Annulation Billet #GV-102", 12000.0, "20/07/2026", "Avoir Client", "Remboursé")
        )
    )
    val invoices: StateFlow<List<Invoice>> = _invoices.asStateFlow()

    private val _ledgerEntries = MutableStateFlow<List<FinancialLedgerEntry>>(
        listOf(
            FinancialLedgerEntry("LEDG-01", "Caisse", "531000", "Recettes Guichet Billetterie du Jour", 450000.0, 0.0, "22/07/2026", "REC-G-8801"),
            FinancialLedgerEntry("LEDG-02", "Banque", "512000", "Virement BGFIBank Règlement Fret Entreprise", 180000.0, 0.0, "22/07/2026", "VIR-BGF-99"),
            FinancialLedgerEntry("LEDG-03", "Caisse", "531000", "Achat Carburant Dépôt Libreville", 0.0, 185000.0, "22/07/2026", "EXP-101"),
            FinancialLedgerEntry("LEDG-04", "Grand Livre", "606100", "Charges Fournitures et Maintenance Flotte", 0.0, 120000.0, "21/07/2026", "EXP-102")
        )
    )
    val ledgerEntries: StateFlow<List<FinancialLedgerEntry>> = _ledgerEntries.asStateFlow()

    // --- RBAC User Actions ---
    fun createUser(user: User) {
        val newId = (_usersList.value.maxOfOrNull { it.id } ?: 0) + 1
        val dateNow = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val created = user.copy(
            id = newId,
            createdAt = dateNow,
            updatedAt = dateNow,
            isAgent = user.role != "Voyageur"
        )
        _usersList.value = _usersList.value + created
        
        viewModelScope.launch {
            try {
                repository.updateUser(created)
            } catch (e: Exception) {
                android.util.Log.e("VoyageViewModel", "Failed to save user to DB", e)
            }
        }

        addAuditLog("Création Utilisateur #${created.id} - ${created.prenom} ${created.nom} (${created.role})", "Sécurité & RBAC")
        addNotification("Utilisateur ${created.prenom} ${created.nom} créé avec succès (Rôle : ${created.role}). Il/Elle peut se connecter dès maintenant avec ${created.email} / ${created.phone}.")
    }

    fun updateUser(user: User) {
        _usersList.value = _usersList.value.map { if (it.id == user.id) user else it }
        viewModelScope.launch {
            try {
                repository.updateUser(user)
            } catch (e: Exception) {
                android.util.Log.e("VoyageViewModel", "Failed to update user in DB", e)
            }
        }
        addAuditLog("Modification Utilisateur #${user.id} - ${user.prenom} ${user.nom}", "Sécurité & RBAC")
        addNotification("Profil de ${user.prenom} ${user.nom} mis à jour.")
    }

    fun deleteUser(userId: Int) {
        val target = _usersList.value.find { it.id == userId }
        if (target != null) {
            _usersList.value = _usersList.value.filterNot { it.id == userId }
            viewModelScope.launch {
                try {
                    repository.deleteUser(userId)
                } catch (e: Exception) {
                    android.util.Log.e("VoyageViewModel", "Failed to delete user from DB", e)
                }
            }
            addAuditLog("Suppression Utilisateur #${target.id} - ${target.prenom} ${target.nom}", "Sécurité & RBAC")
            addNotification("Utilisateur ${target.prenom} ${target.nom} supprimé.")
        }
    }

    fun toggleUserStatus(userId: Int) {
        _usersList.value = _usersList.value.map {
            if (it.id == userId) {
                val newStatus = if (it.status == "Actif") "Suspendu" else "Actif"
                val updated = it.copy(status = newStatus)
                viewModelScope.launch {
                    try {
                        repository.updateUser(updated)
                    } catch (e: Exception) {
                        android.util.Log.e("VoyageViewModel", "Failed to toggle user status in DB", e)
                    }
                }
                addAuditLog("Changement Statut Utilisateur #${it.id} -> $newStatus", "Sécurité & RBAC")
                addNotification("Compte de ${it.prenom} ${it.nom} passé à : $newStatus.")
                updated
            } else it
        }
    }

    fun resetUserPassword(userId: Int) {
        val defaultPass = "Gabon2026!"
        _usersList.value = _usersList.value.map {
            if (it.id == userId) {
                val updated = it.copy(password = defaultPass)
                viewModelScope.launch {
                    try {
                        repository.updateUser(updated)
                    } catch (e: Exception) {
                        android.util.Log.e("VoyageViewModel", "Failed to reset password in DB", e)
                    }
                }
                addAuditLog("Réinitialisation Mot de Passe #${it.id} - ${it.prenom} ${it.nom}", "Sécurité & RBAC")
                addNotification("Mot de passe réinitialisé pour ${it.prenom} ${it.nom} (Nouveau : $defaultPass).")
                updated
            } else it
        }
    }

    // --- RBAC Role Actions ---
    fun createRole(name: String, description: String) {
        val newId = (_roles.value.maxOfOrNull { it.id } ?: 0) + 1
        val newRole = Role(newId, name, description)
        _roles.value = _roles.value + newRole
        addAuditLog("Création du Rôle '$name'", "Sécurité & RBAC")
        addNotification("Nouveau rôle '$name' ajouté avec succès.")
    }

    fun duplicateRole(role: Role) {
        val newId = (_roles.value.maxOfOrNull { it.id } ?: 0) + 1
        val dupRole = Role(newId, "${role.nom} (Copie)", role.description)
        _roles.value = _roles.value + dupRole
        addAuditLog("Duplication du Rôle '${role.nom}'", "Sécurité & RBAC")
        addNotification("Rôle '${role.nom}' dupliqué.")
    }

    fun deleteRole(roleId: Int) {
        val target = _roles.value.find { it.id == roleId }
        if (target != null && target.nom != "Super Administrateur") {
            _roles.value = _roles.value.filterNot { it.id == roleId }
            addAuditLog("Suppression du Rôle '${target.nom}'", "Sécurité & RBAC")
            addNotification("Rôle '${target.nom}' supprimé.")
        }
    }

    // --- Finance Actions ---
    fun addExpense(category: String, amount: Double, description: String, agency: String) {
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val newExp = Expense("EXP-${System.currentTimeMillis() % 10000}", category, amount, description, dateStr, agency, "Approuvé")
        _expenses.value = listOf(newExp) + _expenses.value
        addAuditLog("Enregistrement Dépense ${amount.toInt()} FCFA ($category)", "Finances")
        addNotification("Dépense de ${amount.toInt()} FCFA enregistrée avec succès.")
    }

    fun addAuditLog(action: String, module: String) {
        val user = _currentUser.value
        val log = AuditLog(
            id = "AUD-${System.currentTimeMillis() % 10000}",
            userId = user?.id ?: 1,
            userName = if (user != null) "${user.prenom} ${user.nom}" else "Martinien Mve Zogo",
            userRole = user?.role ?: "Super Administrateur",
            action = action,
            module = module,
            timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            ipAddress = "197.234.221.${(10..99).random()}",
            status = "Succès"
        )
        _auditLogs.value = listOf(log) + _auditLogs.value
    }

    // --- Corbeille (Trash) Actions ---
    fun restoreTrashItem(item: TrashItem) {
        when (item.originalObject) {
            is Vehicle -> {
                _vehicles.value = _vehicles.value + item.originalObject
                addNotification("Véhicule ${item.originalObject.plateNumber} restauré.")
            }
            is Driver -> {
                _drivers.value = _drivers.value + item.originalObject
                addNotification("Chauffeur ${item.originalObject.prenom} ${item.originalObject.nom} restauré.")
            }
            is Parcel -> {
                _parcels.value = _parcels.value + item.originalObject
                addNotification("Colis ${item.originalObject.trackingCode} restauré.")
            }
            is Employee -> {
                _employees.value = _employees.value + item.originalObject
                addNotification("Employé ${item.originalObject.name} restauré.")
            }
        }
        _trashItems.value = _trashItems.value.filterNot { it.id == item.id }
    }

    fun permanentlyDeleteTrashItem(itemId: String) {
        _trashItems.value = _trashItems.value.filterNot { it.id == itemId }
        addNotification("Élément supprimé définitivement de la corbeille.")
    }

    fun emptyTrash() {
        _trashItems.value = emptyList()
        addNotification("Corbeille entièrement vidée.")
    }

    // --- Sauvegardes (Backups) Actions ---
    fun createBackup(name: String) {
        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        val newBackup = BackupSnapshot(
            id = "BK-${System.currentTimeMillis()}",
            timestamp = dateStr,
            name = if (name.isNotBlank()) name else "Sauvegarde Manuelle ERP",
            vehiclesCount = _vehicles.value.size,
            driversCount = _drivers.value.size,
            parcelsCount = _parcels.value.size,
            bookingsCount = allBookings.value.size,
            tripsCount = allTrips.value.size,
            sizeKb = (180..420).random()
        )
        _backups.value = listOf(newBackup) + _backups.value
        addNotification("Nouvelle sauvegarde '${newBackup.name}' créée avec succès.")
    }

    fun restoreBackup(snapshot: BackupSnapshot) {
        addNotification("Restauration du système à partir de '${snapshot.name}' (${snapshot.timestamp}) effectuée avec succès.")
    }

    fun deleteBackup(snapshotId: String) {
        _backups.value = _backups.value.filterNot { it.id == snapshotId }
        addNotification("Fichier de sauvegarde supprimé du serveur.")
    }

    // --- ERP Promo Code Action ---
    fun addPromoCode(code: String) {
        if (code.isNotBlank() && !_promoCodes.value.contains(code.uppercase())) {
            _promoCodes.value = _promoCodes.value + code.uppercase()
            addNotification("Code promo ${code.uppercase()} activé avec succès.")
        }
    }

}
