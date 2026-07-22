package com.example.data.repository

import com.example.data.local.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VoyageRepository(private val db: AppDatabase) {
    private val userDao = db.userDao
    private val agencyDao = db.agencyDao
    private val tripDao = db.tripDao
    private val bookingDao = db.bookingDao

    var firebaseSyncManager: FirebaseSyncManager? = null

    // --- Users ---
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    fun getUserById(id: Int): Flow<User?> = userDao.getUserById(id)
    
    suspend fun login(email: String, password: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByEmail(email)
        if (user != null && user.password == password) {
            user
        } else {
            null
        }
    }

    suspend fun loginWithPhone(phone: String, password: String): User? = withContext(Dispatchers.IO) {
        val user = userDao.getUserByPhone(phone)
        if (user != null && user.password == password) {
            user
        } else {
            null
        }
    }

    suspend fun loginWithGoogle(email: String, name: String): User? = withContext(Dispatchers.IO) {
        var user = userDao.getUserByEmail(email)
        if (user == null) {
            // Generer un ID unique pour le nouvel utilisateur basé sur le timestamp ou auto-généré
            val newId = (System.currentTimeMillis() % 1000000).toInt()
            val newUser = User(
                id = newId,
                nom = "Google",
                prenom = name,
                email = email,
                phone = "077000000",
                password = "google_mock_password",
                isAgent = false
            )
            val resultId = userDao.insertUser(newUser)
            user = newUser.copy(id = resultId.toInt())
            firebaseSyncManager?.writeUser(user)
        }
        user
    }

    suspend fun register(user: User): Long = withContext(Dispatchers.IO) {
        val existingEmail = userDao.getUserByEmail(user.email)
        val existingPhone = userDao.getUserByPhone(user.phone)
        if (existingEmail != null || existingPhone != null) {
            -1L // error code or user exists
        } else {
            // Générer un ID numérique unique pour l'utilisateur
            val uniqueId = (System.currentTimeMillis() % 1000000).toInt()
            val finalUser = user.copy(id = uniqueId)
            val insertedRow = userDao.insertUser(finalUser)
            firebaseSyncManager?.writeUser(finalUser)
            insertedRow
        }
    }

    suspend fun updateUserRole(userId: Int, isAgent: Boolean) = withContext(Dispatchers.IO) {
        val list = userDao.getAllUsersList()
        val found = list.find { it.id == userId }
        if (found != null) {
            val updated = found.copy(isAgent = isAgent)
            userDao.insertUser(updated)
            firebaseSyncManager?.writeUser(updated)
        }
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.insertUser(user)
        firebaseSyncManager?.writeUser(user)
    }

    suspend fun deleteUser(userId: Int) = withContext(Dispatchers.IO) {
        userDao.deleteUserById(userId)
        firebaseSyncManager?.deleteUser(userId)
    }

    // --- Agencies ---
    val allAgencies: Flow<List<Agency>> = agencyDao.getAllAgencies()
    
    suspend fun addAgency(agency: Agency): Long = withContext(Dispatchers.IO) {
        val uniqueId = (System.currentTimeMillis() % 100000).toInt()
        val finalAgency = agency.copy(id = uniqueId)
        val resultId = agencyDao.insertAgency(finalAgency)
        firebaseSyncManager?.writeAgency(finalAgency)
        resultId
    }

    // --- Trips ---
    val allTrips: Flow<List<Trip>> = tripDao.getAllTrips()

    fun getTripsByFilter(departure: String, destination: String): Flow<List<Trip>> {
        return tripDao.getTripsByFilter(departure, destination)
    }

    suspend fun addTrip(trip: Trip): Long = withContext(Dispatchers.IO) {
        val uniqueId = (System.currentTimeMillis() % 100000).toInt()
        val finalTrip = trip.copy(id = uniqueId)
        val resultId = tripDao.insertTrip(finalTrip)
        firebaseSyncManager?.writeTrip(finalTrip)
        resultId
    }

    suspend fun updateTrip(trip: Trip) = withContext(Dispatchers.IO) {
        tripDao.updateTrip(trip)
        firebaseSyncManager?.writeTrip(trip)
    }

    suspend fun deleteTrip(id: Int) = withContext(Dispatchers.IO) {
        tripDao.deleteTripById(id)
        firebaseSyncManager?.deleteTrip(id)
    }

    // --- Bookings ---
    val allBookings: Flow<List<Booking>> = bookingDao.getAllBookings()

    fun getBookingsByUserId(userId: Int): Flow<List<Booking>> {
        return bookingDao.getBookingsByUserId(userId)
    }

    fun getBookingById(bookingId: Int): Flow<Booking?> {
        return bookingDao.getBookingById(bookingId)
    }

    suspend fun createBooking(booking: Booking): Long = withContext(Dispatchers.IO) {
        val uniqueId = (System.currentTimeMillis() % 1000000).toInt()
        val finalBooking = booking.copy(id = uniqueId)
        val resultId = bookingDao.insertBooking(finalBooking)
        firebaseSyncManager?.writeBooking(finalBooking)
        resultId
    }

    suspend fun updateBookingStatus(bookingId: Int, status: String, reason: String? = null) = withContext(Dispatchers.IO) {
        // Obtenir la liste actuelle des réservations
        val list = bookingDao.getAllBookingsList()
        val found = list.find { it.id == bookingId }
        if (found != null) {
            val updated = found.copy(status = status, rejectionReason = reason)
            bookingDao.insertBooking(updated)
            firebaseSyncManager?.writeBooking(updated)
        }
    }

    // Alternative simpler update: just take booking object and update it
    suspend fun updateBooking(booking: Booking) = withContext(Dispatchers.IO) {
        bookingDao.updateBooking(booking)
        firebaseSyncManager?.writeBooking(booking)
    }

    // Confirm a booking: decrement available seats of the trip if confirmed successfully
    suspend fun validateBooking(booking: Booking, confirm: Boolean, comments: String? = null) = withContext(Dispatchers.IO) {
        val newStatus = if (confirm) "Confirmed" else "Rejected"
        val updatedBooking = booking.copy(status = newStatus, rejectionReason = comments)
        
        bookingDao.updateBooking(updatedBooking)
        firebaseSyncManager?.writeBooking(updatedBooking)
        
        if (confirm) {
            val trip = tripDao.getTripById(booking.tripId)
            if (trip != null && trip.availableSeats > 0) {
                val updatedTrip = trip.copy(availableSeats = trip.availableSeats - 1)
                tripDao.updateTrip(updatedTrip)
                firebaseSyncManager?.writeTrip(updatedTrip)
            }
        }
    }
}
