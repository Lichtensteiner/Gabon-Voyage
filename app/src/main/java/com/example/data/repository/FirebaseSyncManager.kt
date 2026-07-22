package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FirebaseSyncManager(
    private val db: AppDatabase,
    private val scope: CoroutineScope
) {
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")
    private val agenciesRef = database.getReference("agencies")
    private val tripsRef = database.getReference("trips")
    private val bookingsRef = database.getReference("bookings")

    private val tag = "FirebaseSyncManager"
    private val activeListeners = mutableMapOf<DatabaseReference, ValueEventListener>()

    fun startSync() {
        Log.d(tag, "Démarrage de la synchronisation en temps réel de Firebase...")
        
        // Eviter de multiplier les écoutes si startSync est rappelé
        stopSync()

        // 1. Initialiser la vérification de pré-population
        initializeFirebaseDataIfNeeded()

        // 2. Écouter les Utilisateurs
        val usersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val firebaseUsers = mutableListOf<User>()
                        for (child in snapshot.children) {
                            val user = child.getValue(User::class.java)
                            if (user != null) {
                                firebaseUsers.add(user)
                                db.userDao.insertUser(user)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur de sync des utilisateurs", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Erreur Firebase Utilisateurs: ${error.message}")
            }
        }
        usersRef.addValueEventListener(usersListener)
        activeListeners[usersRef] = usersListener

        // 3. Écouter les Agences
        val agenciesListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val firebaseAgencies = mutableListOf<Agency>()
                        for (child in snapshot.children) {
                            val agency = child.getValue(Agency::class.java)
                            if (agency != null) {
                                firebaseAgencies.add(agency)
                            }
                        }
                        if (firebaseAgencies.isNotEmpty()) {
                            db.agencyDao.insertAgencies(firebaseAgencies)
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur de sync des agences", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Erreur Firebase Agences: ${error.message}")
            }
        }
        agenciesRef.addValueEventListener(agenciesListener)
        activeListeners[agenciesRef] = agenciesListener

        // 4. Écouter les Voyages (Trips)
        val tripsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val firebaseTrips = mutableListOf<Trip>()
                        for (child in snapshot.children) {
                            val trip = child.getValue(Trip::class.java)
                            if (trip != null) {
                                firebaseTrips.add(trip)
                                db.tripDao.insertTrip(trip)
                            }
                        }

                        // Pruner les voyages locaux qui ne sont plus dans Firebase
                        val localTrips = db.tripDao.getAllTripsList()
                        val firebaseIds = firebaseTrips.map { it.id }.toSet()
                        for (localTrip in localTrips) {
                            if (!firebaseIds.contains(localTrip.id)) {
                                db.tripDao.deleteTripById(localTrip.id)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur de sync des voyages", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Erreur Firebase Voyages: ${error.message}")
            }
        }
        tripsRef.addValueEventListener(tripsListener)
        activeListeners[tripsRef] = tripsListener

        // 5. Écouter les Réservations (Bookings)
        val bookingsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                scope.launch(Dispatchers.IO) {
                    try {
                        val firebaseBookings = mutableListOf<Booking>()
                        for (child in snapshot.children) {
                            val booking = child.getValue(Booking::class.java)
                            if (booking != null) {
                                firebaseBookings.add(booking)
                                db.bookingDao.insertBooking(booking)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(tag, "Erreur de sync des réservations", e)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Erreur Firebase Réservations: ${error.message}")
            }
        }
        bookingsRef.addValueEventListener(bookingsListener)
        activeListeners[bookingsRef] = bookingsListener
    }

    fun stopSync() {
        Log.d(tag, "Arrêt de la synchronisation Firebase (nettoyage des activeListeners)...")
        activeListeners.forEach { (ref, listener) ->
            ref.removeEventListener(listener)
        }
        activeListeners.clear()
    }

    /**
     * Si Firebase est vide (par exemple premier démarrage de l'infrastructure),
     * on copie les données par défaut de Room vers Firebase.
     */
    private fun initializeFirebaseDataIfNeeded() {
        tripsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                    Log.d(tag, "Firebase est vide. Copie des données par défaut vers Firebase...")
                    scope.launch(Dispatchers.IO) {
                        try {
                            // Lire localement
                            val localUsers = db.userDao.getAllUsersList()
                            val localAgencies = db.agencyDao.getAllAgenciesList()
                            val localTrips = db.tripDao.getAllTripsList()
                            val localBookings = db.bookingDao.getAllBookingsList()

                            // Si Room est vide aussi, on attend ou on initialise des valeurs de base forcées
                            val usersToUpload = if (localUsers.isEmpty()) {
                                listOf(
                                    User(id = 3, nom = "Mvezogo", prenom = "Martinien", email = "martinienmvezogo@gmail.com", phone = "077000000", password = "24.05.1995Ludo", isAgent = true)
                                )
                            } else {
                                localUsers
                            }

                            val agenciesToUpload = if (localAgencies.isEmpty()) {
                                listOf(
                                    Agency(id = 1, name = "Major Transport", description = "Le leader du transport terrestre rapide vers Oyem et Bitam. Service confort, climatisation.", hotline = "+241 77 12 34 56"),
                                    Agency(id = 2, name = "Transporteur Voyage", description = "Liaisons coordonnées quotidiennes vers tout le Nord du Gabon (Mitzic, Oyem, Bitam, Médouneu).", hotline = "+241 66 98 76 54")
                                )
                            } else {
                                localAgencies
                            }

                            val tripsToUpload = if (localTrips.isEmpty()) {
                                listOf(
                                    Trip(id = 1, agencyId = 1, agencyName = "Major Transport", departure = "Libreville", destination = "Oyem", departureTime = "06:30", price = 15000.0, type = "Bus", totalSeats = 18, availableSeats = 18),
                                    Trip(id = 2, agencyId = 1, agencyName = "Major Transport", departure = "Libreville", destination = "Bitam", departureTime = "05:30", price = 18000.0, type = "Bus", totalSeats = 18, availableSeats = 18),
                                    Trip(id = 3, agencyId = 2, agencyName = "Transporteur Voyage", departure = "Libreville", destination = "Mitzic", departureTime = "07:30", price = 12000.0, type = "Voiture", totalSeats = 7, availableSeats = 7),
                                    Trip(id = 4, agencyId = 2, agencyName = "Transporteur Voyage", departure = "Libreville", destination = "Médouneu", departureTime = "08:15", price = 10000.0, type = "Voiture", totalSeats = 5, availableSeats = 5),
                                    Trip(id = 5, agencyId = 1, agencyName = "Major Transport", departure = "Oyem", destination = "Libreville", departureTime = "06:00", price = 15000.0, type = "Bus", totalSeats = 18, availableSeats = 18),
                                    Trip(id = 6, agencyId = 1, agencyName = "Major Transport", departure = "Bitam", destination = "Libreville", departureTime = "05:00", price = 18000.0, type = "Bus", totalSeats = 18, availableSeats = 18),
                                    Trip(id = 7, agencyId = 2, agencyName = "Transporteur Voyage", departure = "Mitzic", destination = "Libreville", departureTime = "12:00", price = 12000.0, type = "Voiture", totalSeats = 7, availableSeats = 7),
                                    Trip(id = 8, agencyId = 2, agencyName = "Transporteur Voyage", departure = "Médouneu", destination = "Libreville", departureTime = "10:30", price = 10000.0, type = "Voiture", totalSeats = 5, availableSeats = 5),
                                    Trip(id = 9, agencyId = 1, agencyName = "Major Transport", departure = "Libreville", destination = "Oyem", departureTime = "13:00", price = 15000.0, type = "Voiture", totalSeats = 7, availableSeats = 7),
                                    Trip(id = 10, agencyId = 2, agencyName = "Transporteur Voyage", departure = "Oyem", destination = "Libreville", departureTime = "14:00", price = 15000.0, type = "Bus", totalSeats = 18, availableSeats = 18)
                                )
                            } else {
                                localTrips
                            }

                            // Uploader tout vers Firebase en bloc
                            for (user in usersToUpload) {
                                usersRef.child(user.id.toString()).setValue(user)
                            }
                            for (agency in agenciesToUpload) {
                                agenciesRef.child(agency.id.toString()).setValue(agency)
                            }
                            for (trip in tripsToUpload) {
                                tripsRef.child(trip.id.toString()).setValue(trip)
                            }
                            for (booking in localBookings) {
                                bookingsRef.child(booking.id.toString()).setValue(booking)
                            }
                            Log.d(tag, "Migration initiale Firebase effectuée avec succès !")
                        } catch (e: Exception) {
                            Log.e(tag, "Erreur lors de la migration initiale", e)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "onCancelled vérification initiale", error.toException())
            }
        })
    }

    // --- Helpers d'écriture directe pour propager les écritures locales vers Firebase ---
    fun writeUser(user: User) {
        usersRef.child(user.id.toString()).setValue(user)
    }

    fun deleteUser(userId: Int) {
        usersRef.child(userId.toString()).removeValue()
    }

    fun writeAgency(agency: Agency) {
        agenciesRef.child(agency.id.toString()).setValue(agency)
    }

    fun writeTrip(trip: Trip) {
        tripsRef.child(trip.id.toString()).setValue(trip)
    }

    fun deleteTrip(tripId: Int) {
        tripsRef.child(tripId.toString()).removeValue()
    }

    fun writeBooking(booking: Booking) {
        bookingsRef.child(booking.id.toString()).setValue(booking)
    }
}
