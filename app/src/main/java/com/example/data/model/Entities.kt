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
    val userPrenom: String = "",
    val userSex: String = "",
    val userPhone: String = "",
    val userEmail: String = "",
    val userBirthDate: String = "",
    val idDocumentType: String = "",
    val idDocumentNumber: String = "",
    val tripId: Int = 0,
    val tripCode: String = "",
    val bookingReference: String = "",
    val pinCode: String = "",
    val seatsCount: Int = 1,
    val selectedSeats: String = "",
    val agencyName: String = "",
    val departure: String = "",
    val destination: String = "",
    val departureTime: String = "",
    val type: String = "",
    val travelDate: String = "",
    val unitPrice: Double = 0.0,
    val pricePaid: Double = 0.0,
    val paymentMethod: String = "",  // "Moov Money", "Airtel Money", "Mobile Money", "Carte Visa", "Paiement en agence"
    val merchantNumber: String = "",
    val transactionNumber: String = "",
    val screenshotUriString: String? = null,
    val status: String = "Pending", // "Pending", "Confirmed", "Rejected", "Cancelled"
    val bookingDate: Long = System.currentTimeMillis(),
    val rejectionReason: String? = null
) : Serializable

data class Vehicle(
    val id: Int = 0,
    val plateNumber: String = "",
    val make: String = "Toyota",
    val model: String = "Coaster",
    val category: String = "Bus Standard", // "VIP", "Standard", "Mini Bus", "Bus 30 Places"
    val capacity: Int = 18,
    val features: List<String> = listOf("Climatisation", "USB", "Wifi"),
    val condition: String = "Excellent", // "Excellent", "En révision", "Hors service"
    val insuranceExpiry: String = "15/12/2026",
    val techControlExpiry: String = "20/10/2026",
    val isActive: Boolean = true,
    val driverName: String = "Jean-Paul Mba"
) : Serializable

data class Driver(
    val id: Int = 0,
    val nom: String = "",
    val prenom: String = "",
    val phone: String = "",
    val email: String = "",
    val licenseNumber: String = "",
    val licenseExpiry: String = "01/01/2028",
    val seniorityYears: Int = 5,
    val status: String = "Disponible", // "Disponible", "En Trajet", "En Repos"
    val assignedVehiclePlate: String = "G1-2026-GA"
) : Serializable

data class Parcel(
    val id: String = "",
    val senderName: String = "",
    val senderPhone: String = "",
    val recipientName: String = "",
    val recipientPhone: String = "",
    val route: String = "Libreville ➔ Oyem",
    val weightKg: Double = 5.0,
    val priceFcfa: Double = 5000.0,
    val trackingCode: String = "",
    val status: String = "En transit" // "En attente", "En transit", "Livré", "Annulé"
) : Serializable

data class Employee(
    val id: Int = 0,
    val name: String = "",
    val email: String = "",
    val role: String = "Agent de réservation", // "Super Admin", "Directeur", "Gestionnaire Flotte", "Comptable", "Contrôleur", "Agent de réservation"
    val agency: String = "Agence Centrale Libreville",
    val active: Boolean = true
) : Serializable

data class VehicleTelemetry(
    val busId: String = "",
    val lineName: String = "",
    val currentPosition: String = "",
    val speedKmh: Int = 85,
    val eta: String = "",
    val status: String = "En Route (À l'Heure)"
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

