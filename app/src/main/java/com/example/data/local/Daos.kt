package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<User>

    @Query("SELECT * FROM users WHERE id = :id")
    fun getUserById(id: Int): Flow<User?>

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE phone = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Int)
}

@Dao
interface AgencyDao {
    @Query("SELECT * FROM agencies ORDER BY name ASC")
    fun getAllAgencies(): Flow<List<Agency>>

    @Query("SELECT * FROM agencies")
    suspend fun getAllAgenciesList(): List<Agency>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgencies(agencies: List<Agency>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgency(agency: Agency): Long
}

@Dao
interface TripDao {
    @Query("SELECT * FROM trips ORDER BY agencyName ASC")
    fun getAllTrips(): Flow<List<Trip>>

    @Query("SELECT * FROM trips")
    suspend fun getAllTripsList(): List<Trip>

    @Query("SELECT * FROM trips WHERE departure = :departure AND destination = :destination ORDER BY price ASC")
    fun getTripsByFilter(departure: String, destination: String): Flow<List<Trip>>

    @Query("SELECT * FROM trips WHERE id = :id LIMIT 1")
    suspend fun getTripById(id: Int): Trip?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrips(trips: List<Trip>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: Int)
}

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY bookingDate DESC")
    fun getAllBookings(): Flow<List<Booking>>

    @Query("SELECT * FROM bookings")
    suspend fun getAllBookingsList(): List<Booking>

    @Query("SELECT * FROM bookings WHERE userId = :userId ORDER BY bookingDate DESC")
    fun getBookingsByUserId(userId: Int): Flow<List<Booking>>

    @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
    fun getBookingById(id: Int): Flow<Booking?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: Booking): Long

    @Update
    suspend fun updateBooking(booking: Booking)
}
