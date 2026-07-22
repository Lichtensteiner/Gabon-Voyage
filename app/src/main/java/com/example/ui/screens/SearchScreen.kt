package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Trip
import com.example.ui.viewmodel.VoyageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: VoyageViewModel,
    onNavigateToPayment: () -> Unit,
    modifier: Modifier = Modifier
) {
    val departureQuery by viewModel.departureQuery.collectAsState()
    val destinationQuery by viewModel.destinationQuery.collectAsState()
    val travelDateQuery by viewModel.travelDateQuery.collectAsState()
    val selectedAgencyFilter by viewModel.selectedAgencyFilter.collectAsState()
    
    val filteredTrips by viewModel.filteredTrips.collectAsState()
    val allAgencies by viewModel.allAgencies.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    val destinationsList = listOf("Libreville", "Oyem", "Bitam", "Mitzic", "Médouneu")

    var showDepDropdown by remember { mutableStateOf(false) }
    var showDestDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Où voyagez-vous ?",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Trouvez votre bus ou voiture pour le Nord",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = { viewModel.refreshData() },
                        enabled = !isRefreshing
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Actualiser les trajets",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Selection Settings Panel
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Departure Row
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = departureQuery,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Départ de") },
                            leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = { IconButton(onClick = { showDepDropdown = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = showDepDropdown,
                            onDismissRequest = { showDepDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            destinationsList.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        viewModel.setSearchCriteria(city, destinationQuery, travelDateQuery)
                                        showDepDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Floating Interchanger Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(
                            onClick = {
                                viewModel.setSearchCriteria(destinationQuery, departureQuery, travelDateQuery)
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondary, CircleShape)
                                .size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Inverser",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Destination Row
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = destinationQuery,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Destination vers") },
                            leadingIcon = { Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            trailingIcon = { IconButton(onClick = { showDestDropdown = true }) { Icon(Icons.Default.ArrowDropDown, null) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = showDestDropdown,
                            onDismissRequest = { showDestDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            destinationsList.forEach { city ->
                                DropdownMenuItem(
                                    text = { Text(city) },
                                    onClick = {
                                        viewModel.setSearchCriteria(departureQuery, city, travelDateQuery)
                                        showDestDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // Travel Date with Quick Selector Chips
                    OutlinedTextField(
                        value = travelDateQuery,
                        onValueChange = { viewModel.setSearchCriteria(departureQuery, destinationQuery, it) },
                        label = { Text("Date de Voyage (JJ/MM/AAAA)") },
                        leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Tomorrow quick chip
                        val simpleFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val calendar = Calendar.getInstance()
                        
                        val days = listOf("Demain", "Dans 2 jours", "Dans 3 jours")
                        days.forEachIndexed { index, title ->
                            val tempCal = calendar.clone() as Calendar
                            tempCal.add(Calendar.DAY_OF_YEAR, index + 1)
                            val dateString = simpleFormat.format(tempCal.time)
                            val isSelected = travelDateQuery == dateString

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setSearchCriteria(departureQuery, destinationQuery, dateString)
                                },
                                label = { Text(title, fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }
        }

        // Agency Filter Category Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Filtrer par agence",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filterOptions = listOf("Toutes") + allAgencies.map { it.name }
                    filterOptions.distinct().forEach { agencyName ->
                        val isSelected = selectedAgencyFilter == agencyName
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setAgencyFilter(agencyName) },
                            label = { Text(agencyName) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }

        // Header Results Count
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${departureQuery} ➔ ${destinationQuery}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${filteredTrips.size} trajets disponibles",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List of Available Trips
        if (filteredTrips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsBus,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Text(
                            text = "Aucun départ trouvé pour aujourd'hui dans cette sélection",
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Conseil : Essayez la ligne classique Libreville ➔ Oyem ou Libreville ➔ Bitam en changeant la date.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTrips) { trip ->
                TripCard(
                    trip = trip,
                    onBookClicked = {
                        viewModel.selectTripForBooking(trip)
                        onNavigateToPayment()
                    }
                )
            }
        }
    }
}

@Composable
fun TripCard(
    trip: Trip,
    onBookClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookClicked() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Agency Name & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = trip.agencyName.first().toString(),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }
                    Text(
                        text = trip.agencyName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "${trip.price.toInt()} FCFA",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            // Departure time, transit style and available seats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Départ à ${trip.departureTime}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (trip.type == "Bus") Icons.Default.DirectionsBus else Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (trip.type == "Bus") "Autocar Confort Multi-places" else "Berline / 4x4 (Rapide)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val seatText = if (trip.availableSeats == 0) "Complet" else "${trip.availableSeats} places libres"
                    val seatColor = if (trip.availableSeats == 0) MaterialTheme.colorScheme.error 
                    else if (trip.availableSeats < 5) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.primary

                    Surface(
                        color = seatColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = seatText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = seatColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    
                    Text(
                        text = "Sans CB • Payez par Mobile",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            // Quick Reservation Button
            Button(
                onClick = onBookClicked,
                modifier = Modifier.fillMaxWidth(),
                enabled = trip.availableSeats > 0,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Choisir et payer par Mobile Money", fontWeight = FontWeight.Bold)
            }
        }
    }
}
