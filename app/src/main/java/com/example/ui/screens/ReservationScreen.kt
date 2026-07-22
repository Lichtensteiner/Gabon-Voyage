package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.Trip
import com.example.ui.viewmodel.VoyageViewModel
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ReservationScreen(
    viewModel: VoyageViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedTrip by viewModel.selectedTripForBooking.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val bookingSuccess by viewModel.bookingSuccess.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()

    // 1. Client Info state
    var nom by remember { mutableStateOf(currentUser?.nom ?: "") }
    var prenom by remember { mutableStateOf(currentUser?.prenom ?: "") }
    var sex by remember { mutableStateOf("Homme") }
    var birthDate by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var email by remember { mutableStateOf(currentUser?.email ?: "") }
    var idDocumentType by remember { mutableStateOf("Carte Nationale d'Identité") }
    var idDocumentNumber by remember { mutableStateOf("") }
    var showIdTypeDropdown by remember { mutableStateOf(false) }

    // Synchronize user info if updated
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            if (nom.isBlank()) nom = currentUser!!.nom
            if (prenom.isBlank()) prenom = currentUser!!.prenom
            if (phone.isBlank()) phone = currentUser!!.phone
            if (email.isBlank()) email = currentUser!!.email
        }
    }

    // 2. Trip selection state
    var activeTrip by remember { mutableStateOf<Trip?>(selectedTrip ?: allTrips.firstOrNull()) }
    var showTripDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTrip, allTrips) {
        if (selectedTrip != null) {
            activeTrip = selectedTrip
        } else if (activeTrip == null && allTrips.isNotEmpty()) {
            activeTrip = allTrips.first()
        }
    }

    // 3. Seats Selection State
    var seatsCount by remember { mutableIntStateOf(1) }
    val selectedSeatNumbers = remember { mutableStateListOf<Int>() }

    // Ensure selected seats fit requested count
    LaunchedEffect(seatsCount) {
        while (selectedSeatNumbers.size > seatsCount) {
            selectedSeatNumbers.removeAt(selectedSeatNumbers.lastIndex)
        }
    }

    // Default pre-occupied seats simulation for realistic bus plan
    val occupiedSeats = remember(activeTrip?.id) {
        val count = activeTrip?.let { (it.totalSeats - it.availableSeats).coerceAtLeast(0) } ?: 3
        (1..count).toList()
    }

    // 4. Payment method state
    var paymentMethod by remember { mutableStateOf("Moov Money") }
    var transactionNumber by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // If booking was generated successfully, render the modern ticket view!
    if (bookingSuccess != null) {
        ModernTicketResultView(
            booking = bookingSuccess!!,
            onViewHistory = onNavigateToHistory,
            onNewBooking = { viewModel.clearSelectedTripForBooking() }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Title Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nouvelle Réservation",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Réservation instantanée de billets interurbains",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Gabon Voyage",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // ==========================================
        // SECTION 1: INFORMATIONS DU VOYAGE (TRIP INFO)
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Trajet & Informations du voyage",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    TextButton(onClick = { showTripDropdown = true }) {
                        Text("Changer le trajet", fontSize = 12.sp)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }

                // Dropdown to pick different trip if needed
                DropdownMenu(
                    expanded = showTripDropdown,
                    onDismissRequest = { showTripDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    allTrips.forEach { trip ->
                        DropdownMenuItem(
                            text = {
                                Text("${trip.departure} ➔ ${trip.destination} (${trip.departureTime}) - ${trip.agencyName} [${trip.price.toInt()} FCFA]")
                            },
                            onClick = {
                                activeTrip = trip
                                viewModel.selectTripForBooking(trip)
                                showTripDropdown = false
                                selectedSeatNumbers.clear()
                            }
                        )
                    }
                }

                if (activeTrip != null) {
                    val trip = activeTrip!!
                    val estimatedDistance = when {
                        trip.destination.contains("Oyem", ignoreCase = true) -> "600 km"
                        trip.destination.contains("Bitam", ignoreCase = true) -> "670 km"
                        trip.destination.contains("Mitzic", ignoreCase = true) -> "480 km"
                        trip.destination.contains("Médouneu", ignoreCase = true) -> "350 km"
                        else -> "450 km"
                    }
                    val estimatedDuration = when {
                        trip.destination.contains("Bitam", ignoreCase = true) -> "8h 30min"
                        trip.destination.contains("Oyem", ignoreCase = true) -> "7h 15min"
                        else -> "6h 00min"
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "${trip.departure} ➔ ${trip.destination}",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Agence : ${trip.agencyName} • ${trip.type}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${trip.price.toInt()} FCFA",
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Divider(color = MaterialTheme.colorScheme.outlineVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Départ", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(trip.departureTime, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Distance", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(estimatedDistance, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Durée estimée", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text(estimatedDuration, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("Prix Unitaire", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                                    Text("${trip.price.toInt()} FCFA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // SECTION 2: FORMULAIRE INFORMATIONS CLIENT
        // ==========================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "2. Informations du passager",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        label = { Text("Prénom *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Sexe Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Sexe :", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    listOf("Homme", "Femme").forEach { item ->
                        val isSelected = sex == item
                        FilterChip(
                            selected = isSelected,
                            onClick = { sex = item },
                            label = { Text(item) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone (ex: 077...) *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (optionnel)") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Identity Document Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(modifier = Modifier.weight(1.2f)) {
                        OutlinedTextField(
                            value = idDocumentType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pièce d'identité") },
                            trailingIcon = { IconButton(onClick = { showIdTypeDropdown = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = showIdTypeDropdown,
                            onDismissRequest = { showIdTypeDropdown = false }
                        ) {
                            listOf("Carte Nationale d'Identité", "Passeport", "Permis de conduire").forEach { doc ->
                                DropdownMenuItem(
                                    text = { Text(doc) },
                                    onClick = {
                                        idDocumentType = doc
                                        showIdTypeDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = idDocumentNumber,
                        onValueChange = { idDocumentNumber = it },
                        label = { Text("N° Pièce (optionnel)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // ==========================================
        // SECTION 3: CHOIX DU NOMBRE DE PLACES & CALCUL
        // ==========================================
        if (activeTrip != null) {
            val trip = activeTrip!!
            val remaining = trip.availableSeats
            val unitPrice = trip.price
            val totalPrice = unitPrice * seatsCount

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. Nombre de places & Calcul",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )

                        // Real-time seats availability badge
                        Surface(
                            color = if (remaining > 3) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (remaining > 0) "$remaining / ${trip.totalSeats} places disponibles" else "0 place disponible (COMPLET)",
                                fontWeight = FontWeight.Bold,
                                color = if (remaining > 3) Color(0xFF2E7D32) else Color(0xFFC62828),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Seat Count Stepper Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Nombre de places désirées :", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("Prix unitaire : ${unitPrice.toInt()} FCFA", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconButton(
                                onClick = { if (seatsCount > 1) seatsCount-- },
                                enabled = seatsCount > 1,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "Moins")
                            }

                            Text(
                                text = "$seatsCount",
                                fontWeight = FontWeight.Black,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            IconButton(
                                onClick = { if (seatsCount < remaining) seatsCount++ },
                                enabled = seatsCount < remaining,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Plus", tint = Color.White)
                            }
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    // Instant Dynamic Billing Breakdown
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Sous-total (${seatsCount} place(s)) :", fontSize = 13.sp)
                            Text("${(unitPrice * seatsCount).toInt()} FCFA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Frais de réservation :", fontSize = 13.sp, color = MaterialTheme.colorScheme.outline)
                            Text("0 FCFA", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                        }
                        Divider(color = MaterialTheme.colorScheme.outlineVariant)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("MONTANT TOTAL A PAYER :", fontWeight = FontWeight.Black, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                            Text("${totalPrice.toInt()} FCFA", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // ==========================================
            // SECTION 4: CHOIX DES SIÈGES (SEAT MAP)
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "4. Choix des sièges dans le véhicule",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${selectedSeatNumbers.size} / $seatsCount sélectionné(s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Legend Badges
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(14.dp).background(Color(0xFF2E7D32), CircleShape))
                            Text("🟢 Vert: Disponible", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(14.dp).background(Color(0xFFD32F2F), CircleShape))
                            Text("🔴 Rouge: Réservé", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(14.dp).background(Color(0xFFFBC02D), CircleShape))
                            Text("🟡 Jaune: Choisi", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    // Bus Plan Diagram Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Driver cabin header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary)
                                    Text("Avant du Véhicule (Chauffeur)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎮", fontSize = 14.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Grid of seats (1 to totalSeats)
                            val totalSeats = trip.totalSeats
                            val rows = (totalSeats + 3) / 4

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (r in 0 until rows) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        for (c in 1..4) {
                                            val seatNumber = r * 4 + c
                                            if (seatNumber <= totalSeats) {
                                                // Check seat status
                                                val isOccupied = occupiedSeats.contains(seatNumber)
                                                val isSelected = selectedSeatNumbers.contains(seatNumber)

                                                val seatBg = when {
                                                    isSelected -> Color(0xFFFBC02D) // Yellow
                                                    isOccupied -> Color(0xFFD32F2F) // Red
                                                    else -> Color(0xFF2E7D32)       // Green
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(seatBg)
                                                        .clickable {
                                                            if (isOccupied) {
                                                                Toast.makeText(context, "Ce siège n°$seatNumber est déjà réservé par un autre passager.", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                if (isSelected) {
                                                                    selectedSeatNumbers.remove(seatNumber)
                                                                } else {
                                                                    if (selectedSeatNumbers.size < seatsCount) {
                                                                        selectedSeatNumbers.add(seatNumber)
                                                                    } else {
                                                                        Toast.makeText(context, "Vous avez déjà choisi $seatsCount siège(s). Pour modifier, désélectionnez un siège.", Toast.LENGTH_SHORT).show()
                                                                    }
                                                                }
                                                            }
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                        Text(
                                                            text = "S$seatNumber",
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 12.sp
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.EventSeat,
                                                            contentDescription = null,
                                                            tint = Color.White.copy(alpha = 0.8f),
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                }

                                                // Corridor aisle between column 2 and 3
                                                if (c == 2) {
                                                    Spacer(modifier = Modifier.width(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (selectedSeatNumbers.isNotEmpty()) {
                        Text(
                            text = "Sièges choisis : ${selectedSeatNumbers.sorted().joinToString { "Siège $it" }}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 13.sp
                        )
                    } else {
                        Text(
                            text = "💡 Conseil : Cliquez directement sur les sièges verts ci-dessus pour choisir vos places.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // ==========================================
            // SECTION 5: CHOIX DU MODE DE PAIEMENT
            // ==========================================
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "5. Mode de paiement",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    val paymentModes = listOf("Moov Money", "Airtel Money", "Mobile Money", "Carte Visa", "Paiement en Agence")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        paymentModes.forEach { mode ->
                            val isSelected = paymentMethod == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { paymentMethod = mode },
                                label = { Text(mode, fontWeight = FontWeight.Bold) },
                                leadingIcon = if (isSelected) {
                                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }

                    OutlinedTextField(
                        value = transactionNumber,
                        onValueChange = { transactionNumber = it },
                        label = { Text("Numéro / Réf de transaction Mobile Money / Carte") },
                        placeholder = { Text("Ex: TX-2026-9912") },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Error Display Banner
            AnimatedVisibility(visible = errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // ==========================================
            // SUBMIT RESERVATION BUTTON
            // ==========================================
            Button(
                onClick = {
                    if (nom.isBlank()) {
                        errorMessage = "Veuillez saisir le nom du passager."
                    } else if (prenom.isBlank()) {
                        errorMessage = "Veuillez saisir le prénom du passager."
                    } else if (phone.isBlank()) {
                        errorMessage = "Veuillez saisir un numéro de téléphone valide."
                    } else if (selectedSeatNumbers.size < seatsCount) {
                        errorMessage = "Veuillez sélectionner exactement $seatsCount siège(s) sur le plan du véhicule."
                    } else {
                        errorMessage = null
                        val seatsFormatted = selectedSeatNumbers.sorted().map { "Siège $it" }
                        viewModel.submitFullBooking(
                            userNom = nom,
                            userPrenom = prenom,
                            userSex = sex,
                            userPhone = phone,
                            userEmail = email,
                            userBirthDate = birthDate,
                            idDocumentType = idDocumentType,
                            idDocumentNumber = idDocumentNumber,
                            trip = trip,
                            seatsCount = seatsCount,
                            selectedSeats = seatsFormatted,
                            paymentMethod = paymentMethod,
                            merchantNumber = if (paymentMethod == "Moov Money") "504030" else "902010",
                            transactionNumber = transactionNumber
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Valider la Réservation & Générer le Billet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Modern Ticket Result & Export View Component
@Composable
fun ModernTicketResultView(
    booking: Booking,
    onViewHistory: () -> Unit,
    onNewBooking: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "RÉSERVATION CONFIRMÉE",
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "GABON VOYAGE • BILLET NUMÉRIQUE",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Reference & PIN Highlight Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Numéro de Réservation", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.bookingReference.ifBlank { "GV-2026-${booking.id}" }, fontWeight = FontWeight.Black, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Code PIN Embarquement", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.pinCode.ifBlank { "849201" }, fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("PASSAGER", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text("${booking.userPrenom} ${booking.userNom}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TÉLÉPHONE", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.userPhone, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Divider(color = MaterialTheme.colorScheme.outlineVariant)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("TRAJET", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text("${booking.departure} ➔ ${booking.destination}", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("AGENCE", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.agencyName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("DATE DE VOYAGE", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.travelDate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("HEURE DÉPART", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.departureTime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("SIÈGES RÉSERVÉS", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(booking.selectedSeats.ifBlank { "${booking.seatsCount} place(s)" }, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("MONTANT TOTAL", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text("${booking.pricePaid.toInt()} FCFA", fontWeight = FontWeight.Black, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Canvas(
                        Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                    ) {
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.5f),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // QR Code Center Box
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Code QR du billet",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(110.dp)
                        )
                        Text("Billet électronique valide", fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 12.sp)
                        Text("Présentez ce QR Code ou le Code PIN à l'embarquement", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }

        // Action Buttons Row: Download PDF, Share WhatsApp, Email, Print
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = { exportTicketPdf(context, booking) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Télécharger / Imprimer le Billet (PDF)", fontWeight = FontWeight.Bold)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { shareViaWhatsApp(context, booking) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { shareViaEmail(context, booking) },
                    modifier = Modifier.weight(1f).height(46.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Email", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onViewHistory,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Voir 'Mes Réservations'", fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun exportTicketPdf(context: android.content.Context, booking: Booking) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(320, 520, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        paint.color = android.graphics.Color.parseColor("#1E6F3E")
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GABON VOYAGE", 20f, 40f, paint)

        paint.textSize = 10f
        paint.color = android.graphics.Color.DKGRAY
        canvas.drawText("Billet de Transport Interurbain Officiel", 20f, 56f, paint)

        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(20f, 68f, 300f, 68f, paint)

        paint.textSize = 11f
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Ref: ${booking.bookingReference.ifBlank { "GV-2026-${booking.id}" }}", 20f, 90f, paint)
        canvas.drawText("PIN: ${booking.pinCode.ifBlank { "849201" }}", 200f, 90f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("PASSAGER :", 20f, 120f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.userPrenom} ${booking.userNom} (${booking.userPhone})", 20f, 135f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("TRAJET & AGENCE :", 20f, 165f, paint)
        paint.color = android.graphics.Color.parseColor("#1E6F3E")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.departure} -> ${booking.destination} (${booking.agencyName})", 20f, 180f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("DATE & HEURE DÉPART :", 20f, 210f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.travelDate} a ${booking.departureTime}", 20f, 225f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("SIÈGES SELECTIONNÉS :", 20f, 255f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(booking.selectedSeats.ifBlank { "${booking.seatsCount} place(s)" }, 20f, 270f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("MONTANT TOTAL PAYÉ :", 20f, 300f, paint)
        paint.color = android.graphics.Color.parseColor("#1E6F3E")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.pricePaid.toInt()} FCFA via ${booking.paymentMethod}", 20f, 315f, paint)

        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(20f, 335f, 300f, 335f, paint)

        paint.textSize = 9f
        paint.color = android.graphics.Color.DKGRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Presentez ce billet digital ou imprimé lors de l'embarquement.", 20f, 360f, paint)
        canvas.drawText("Bon voyage avec Gabon Voyage !", 20f, 375f, paint)

        pdfDocument.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(dir, "Billet_${booking.bookingReference.ifBlank { "GV_${booking.id}" }}.pdf")
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        pdfDocument.close()
        fos.close()

        Toast.makeText(context, "Billet PDF sauvegardé dans Téléchargements : ${file.name}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        Toast.makeText(context, "Erreur export PDF : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}

private fun shareViaWhatsApp(context: android.content.Context, booking: Booking) {
    try {
        val text = "🎟️ *Billet Gabon Voyage*\n" +
                "Ref: ${booking.bookingReference.ifBlank { "GV-2026-${booking.id}" }}\n" +
                "Passager: ${booking.userPrenom} ${booking.userNom}\n" +
                "Trajet: ${booking.departure} ➔ ${booking.destination}\n" +
                "Date: ${booking.travelDate} à ${booking.departureTime}\n" +
                "Sièges: ${booking.selectedSeats.ifBlank { "${booking.seatsCount} place(s)" }}\n" +
                "PIN embarquement: ${booking.pinCode}\n" +
                "Total: ${booking.pricePaid.toInt()} FCFA"

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            setPackage("com.whatsapp")
        }
        context.startActivity(sendIntent)
    } catch (e: Exception) {
        // Fallback to standard share chooser
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "Billet Gabon Voyage Ref: ${booking.bookingReference}")
            type = "text/plain"
        }
        context.startActivity(Intent.createChooser(sendIntent, "Partager le billet"))
    }
}

private fun shareViaEmail(context: android.content.Context, booking: Booking) {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${booking.userEmail}")
            putExtra(Intent.EXTRA_SUBJECT, "Votre Billet Gabon Voyage - Ref: ${booking.bookingReference}")
            putExtra(
                Intent.EXTRA_TEXT,
                "Bonjour ${booking.userPrenom},\n\nVoici votre billet de transport Gabon Voyage pour le trajet ${booking.departure} vers ${booking.destination} le ${booking.travelDate} à ${booking.departureTime}.\n\nRef: ${booking.bookingReference}\nCode PIN: ${booking.pinCode}\nSièges: ${booking.selectedSeats}\nMontant: ${booking.pricePaid.toInt()} FCFA\n\nBon voyage !"
            )
        }
        context.startActivity(Intent.createChooser(intent, "Envoyer par email"))
    } catch (e: Exception) {
        Toast.makeText(context, "Aucune application email installée", Toast.LENGTH_SHORT).show()
    }
}
