package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [User::class, Agency::class, Trip::class, Booking::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val agencyDao: AgencyDao
    abstract val tripDao: TripDao
    abstract val bookingDao: BookingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gabon_voyage_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        suspend fun populateDatabase(db: AppDatabase) {
            // 1. Insert Super Admin User (No demo accounts)
            val users = listOf(
                User(
                    id = 3,
                    nom = "Mvezogo",
                    prenom = "Martinien",
                    email = "martinienmvezogo@gmail.com",
                    phone = "077000000",
                    password = "24.05.1995Ludo",
                    isAgent = true
                )
            )
            for (user in users) {
                db.userDao.insertUser(user)
            }

            // 2. Insert Default Agencies
            val agencies = listOf(
                Agency(id = 1, name = "Major Transport", description = "Le leader du transport terrestre rapide vers Oyem et Bitam. Service confort, climatisation.", hotline = "+241 77 12 34 56"),
                Agency(id = 2, name = "Transporteur Voyage", description = "Liaisons coordonnées quotidiennes vers tout le Nord du Gabon (Mitzic, Oyem, Bitam, Médouneu).", hotline = "+241 66 98 76 54")
            )
            db.agencyDao.insertAgencies(agencies)

            // 3. Insert Default Trips (between Libreville and Northern Towns)
            val trips = listOf(
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
            db.tripDao.insertTrips(trips)
        }
    }
}
