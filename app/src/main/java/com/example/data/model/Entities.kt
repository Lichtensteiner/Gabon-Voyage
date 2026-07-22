package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nom: String = "",
    val prenom: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isAgent: Boolean = false
) : Serializable

@Entity(tableName = "agencies")
data class Agency(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String = "",
    val description: String = "",
    val hotline: String = ""
) : Serializable

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val agencyId: Int = 0,
    val agencyName: String = "",
    val departure: String = "",
    val destination: String = "",
    val departureTime: String = "", // e.g. "07:30" or "14:00"
    val price: Double = 0.0,
    val type: String = "",          // "Bus" or "Voiture"
    val totalSeats: Int = 18,
    val availableSeats: Int = 0
) : Serializable

@Entity(tableName = "bookings")
data class Booking(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val userNom: String = "",
    val userPhone: String = "",
    val tripId: Int = 0,
    val agencyName: String = "",
    val departure: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val type: String = "",
    val travelDate: String = "",
    val pricePaid: Double = 0.0,
    val paymentMethod: String = "",  // "Moov Money" or "Airtel Money"
    val merchantNumber: String = "",
    val transactionNumber: String = "",
    val screenshotUriString: String? = null,
    val status: String = "Pending", // "Pending", "Confirmed", "Rejected", "Cancelled"
    val bookingDate: Long = System.currentTimeMillis(),
    val rejectionReason: String? = null
) : Serializable

data class ChatMessage(
    val id: String = "",
    val senderName: String = "",
    val senderId: Int = 0,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isAgentMessage: Boolean = false,
    val threadId: String = ""
) : Serializable

