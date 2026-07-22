package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.ui.viewmodel.VoyageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTripsScreen(
    viewModel: VoyageViewModel,
    modifier: Modifier = Modifier
) {
    val trips by viewModel.allTrips.collectAsState()
    val agencies by viewModel.allAgencies.collectAsState()

    var showAddForm by remember { mutableStateOf(false) }

    // Form states
    var departure by remember { mutableStateOf("Libreville") }
    var destination by remember { mutableStateOf("Oyem") }
    var agencySelected by remember { mutableStateOf("Major Transport") }
    var departureTime by remember { mutableStateOf("07:00") }
    var priceText by remember { mutableStateOf("15000") }
    var typeSelected by remember { mutableStateOf("Bus") }
    var seatsText by remember { mutableStateOf("18") }

    var formMessage by remember { mutableStateOf<String?>(null) }

    val cities = listOf("Libreville", "Oyem", "Bitam", "Mitzic", "Médouneu")

    var showDepMenu by remember { mutableStateOf(false) }
    var showDestMenu by remember { mutableStateOf(false) }
    var showAgencyMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Catalogue des lignes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Gérez la disponibilité des départs vers le Nord",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showAddForm = !showAddForm },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (showAddForm) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (showAddForm) "Fermer" else "Ajouter")
                }
            }
        }

        // Animated Trip Addition Form
        if (showAddForm) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ajouter un nouveau trajet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        AnimatedVisibility(visible = formMessage != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = formMessage ?: "",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        // Departure Dropdown selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = departure,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ville de Départ") },
                                trailingIcon = { IconButton(onClick = { showDepMenu = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = showDepMenu, onDismissRequest = { showDepMenu = false }) {
                                cities.forEach { city ->
                                    DropdownMenuItem(text = { Text(city) }, onClick = { departure = city; showDepMenu = false })
                                }
                            }
                        }

                        // Destination Dropdown selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = destination,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Ville d'Arrivée") },
                                trailingIcon = { IconButton(onClick = { showDestMenu = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = showDestMenu, onDismissRequest = { showDestMenu = false }) {
                                cities.forEach { city ->
                                    DropdownMenuItem(text = { Text(city) }, onClick = { destination = city; showDestMenu = false })
                                }
                            }
                        }

                        // Agency Dropdown selection
                        Box(modifier = Modifier.fillMaxWidth()) {
                            val agencyNames = agencies.map { it.name }.ifEmpty { listOf("Major Transport", "Transporteur Voyage") }
                            OutlinedTextField(
                                value = agencySelected,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Agence partenaire") },
                                trailingIcon = { IconButton(onClick = { showAgencyMenu = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            )
                            DropdownMenu(expanded = showAgencyMenu, onDismissRequest = { showAgencyMenu = false }) {
                                agencyNames.forEach { name ->
                                    DropdownMenuItem(text = { Text(name) }, onClick = { agencySelected = name; showAgencyMenu = false })
                                }
                            }
                        }

                        // Departure Time input
                        OutlinedTextField(
                            value = departureTime,
                            onValueChange = { departureTime = it },
                            label = { Text("Heure de Départ (ex: 07:45)") },
                            leadingIcon = { Icon(Icons.Default.Schedule, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        // Price and Available Seats values row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = priceText,
                                onValueChange = { priceText = it },
                                label = { Text("Tarif (FCFA)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = seatsText,
                                onValueChange = { seatsText = it },
                                label = { Text("Places max") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        // Vehicle Type selector
                        Column {
                            Text(
                                text = "Type de transport véhicule :",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                listOf("Bus", "Voiture").forEach { vehicle ->
                                    val isSelected = typeSelected == vehicle
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            typeSelected = vehicle
                                            seatsText = if (vehicle == "Bus") "18" else "7"
                                        },
                                        label = { Text(vehicle) }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val priceVal = priceText.trim().toDoubleOrNull()
                                val seatsVal = seatsText.trim().toIntOrNull()

                                if (departure == destination) {
                                    formMessage = "Le point de départ ne peut pas être identique à la destination."
                                } else if (priceVal == null || priceVal <= 0) {
                                    formMessage = "Veuillez entrer un tarif supérieur à 0."
                                } else if (seatsVal == null || seatsVal <= 0) {
                                    formMessage = "Veuillez entrer un nombre de places valide."
                                } else {
                                    formMessage = null
                                    viewModel.addTripAdmin(
                                        agencyId = if (agencySelected == "Major Transport") 1 else 2,
                                        agencyName = agencySelected,
                                        departure = departure,
                                        destination = destination,
                                        time = departureTime,
                                        price = priceVal,
                                        type = typeSelected,
                                        seats = seatsVal
                                    )
                                    // Reset & Close
                                    showAddForm = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sauvegarder le trajet au catalogue", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Catalog List
        items(trips) { trip ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${trip.departure} ➔ ${trip.destination}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Ligne : ${trip.agencyName} | Départ : ${trip.departureTime}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (trip.type == "Bus") Icons.Default.DirectionsBus else Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "Tarif : ${trip.price.toInt()} FCFA | Capacité : ${trip.availableSeats}/${trip.totalSeats} places",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.deleteTripAdmin(trip.id) },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Supprimer")
                    }
                }
            }
        }
    }
}
