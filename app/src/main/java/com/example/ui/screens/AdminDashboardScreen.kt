package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.VoyageViewModel

enum class ErpTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    DASHBOARD("Tableau de bord", Icons.Default.Dashboard),
    FLEET("Flotte Véhicules", Icons.Default.DirectionsBus),
    DRIVERS("Chauffeurs & RH", Icons.Default.Badge),
    PARCELS("Colis & Bagages", Icons.Default.LocalShipping),
    FINANCE("Comptabilité", Icons.Default.AccountBalance),
    AI_ASSISTANT("IA Gabon Voyage", Icons.Default.AutoAwesome),
    GPS_TRACKING("GPS & Direct", Icons.Default.GpsFixed),
    SEAT_MAP("Sièges & Billets", Icons.Default.AirlineSeatReclineExtra),
    PROMOS("Promos & Push", Icons.Default.Campaign),
    CORBEILLE("Corbeille", Icons.Default.DeleteSweep),
    BACKUPS("Sauvegardes", Icons.Default.Backup)
}

@Composable
fun AdminDashboardScreen(
    viewModel: VoyageViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(ErpTab.DASHBOARD) }

    val bookings by viewModel.allBookings.collectAsState()
    val trips by viewModel.allTrips.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val drivers by viewModel.drivers.collectAsState()
    val parcels by viewModel.parcels.collectAsState()
    val employees by viewModel.employees.collectAsState()
    val telemetries by viewModel.telemetries.collectAsState()
    val trashItems by viewModel.trashItems.collectAsState()
    val backups by viewModel.backups.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- Top Header Bar ---
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = "SUPER ADMIN ERP",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Gabon Voyage Transport Management System",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.refreshData() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Default.Refresh, "Actualiser", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = { TechnicalGuidePdfHelper.generateTechnicalGuidePdf(context) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF Fiche", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // --- Scrollable ERP Category Tabs ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(ErpTab.values()) { tab ->
                        val isSelected = selectedTab == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            leadingIcon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            },
                            label = {
                                Text(
                                    text = tab.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        Divider()

        // --- Main Content Switcher Based on ERP Tab ---
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                ErpTab.DASHBOARD -> DashboardModule(bookings, trips, vehicles, viewModel)
                ErpTab.FLEET -> FleetModule(vehicles, drivers, viewModel)
                ErpTab.DRIVERS -> DriversAndEmployeesModule(drivers, employees, viewModel)
                ErpTab.PARCELS -> ParcelsModule(parcels, viewModel)
                ErpTab.FINANCE -> FinanceModule(bookings, context, viewModel)
                ErpTab.AI_ASSISTANT -> AiAssistantErpModule(viewModel)
                ErpTab.GPS_TRACKING -> GpsTrackingModule(telemetries)
                ErpTab.SEAT_MAP -> SeatMapModule(trips, bookings)
                ErpTab.PROMOS -> PromosAndPushModule(viewModel)
                ErpTab.CORBEILLE -> CorbeilleModule(trashItems, viewModel)
                ErpTab.BACKUPS -> BackupsModule(backups, viewModel)
            }
        }
    }
}

// ==========================================
// 1. DASHBOARD MODULE (REAL-TIME KPIS)
// ==========================================
@Composable
fun DashboardModule(
    bookings: List<Booking>,
    trips: List<Trip>,
    vehicles: List<Vehicle>,
    viewModel: VoyageViewModel
) {
    val totalTravelers = bookings.map { it.userId }.distinct().size.coerceAtLeast(1)
    val totalVehicles = vehicles.size
    val activeVehicles = vehicles.count { it.isActive }
    val todayTrips = trips.size
    val todayBookings = bookings.size
    val confirmedBookings = bookings.filter { it.status == "Confirmed" }
    val pendingBookings = bookings.filter { it.status == "Pending" }
    val cancelledBookings = bookings.filter { it.status == "Rejected" || it.status == "Cancelled" }

    val revenueToday = confirmedBookings.sumOf { it.pricePaid }
    val revenueMonth = revenueToday * 3.2 + 420000.0 // Simulated cumulative for the month
    val revenueYear = revenueMonth * 11.5 + 2850000.0

    val fillRate = if (trips.isNotEmpty()) {
        val totalCapacity = trips.sumOf { it.totalSeats }
        val reservedSeats = trips.sumOf { it.totalSeats - it.availableSeats }
        if (totalCapacity > 0) ((reservedSeats.toDouble() / totalCapacity) * 100).toInt() else 68
    } else 68

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "📊 Indicateurs Clés de Performance (KPIs)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Row 1: Users & Fleet
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Total Voyageurs", "$totalTravelers inscrits", Icons.Default.People, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                MetricTile("Véhicules Flotte", "$activeVehicles / $totalVehicles actifs", Icons.Default.DirectionsBus, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
        }

        // Row 2: Trips & Bookings
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Voyages du Jour", "$todayTrips départs", Icons.Default.Schedule, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                MetricTile("Réservations Jour", "$todayBookings billets", Icons.Default.ConfirmationNumber, Color(0xFF00897B), Modifier.weight(1f))
            }
        }

        // Row 3: Revenue Breakdown
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("C.A. du Jour", "${revenueToday.toInt()} FCFA", Icons.Default.Payments, Color(0xFF2E7D32), Modifier.weight(1f))
                MetricTile("C.A. du Mois", "${revenueMonth.toInt()} FCFA", Icons.Default.TrendingUp, Color(0xFF1565C0), Modifier.weight(1f))
            }
        }

        // Row 4: Statuses & Fill Rate
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("Réservations Confirmées", "${confirmedBookings.size} ✅", Icons.Default.CheckCircle, Color(0xFF2E7D32), Modifier.weight(1f))
                MetricTile("En Attente / Annulées", "${pendingBookings.size} ⏳ / ${cancelledBookings.size} ❌", Icons.Default.Pending, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }

        // Fill Rate Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AirlineSeatReclineExtra, null, tint = MaterialTheme.colorScheme.primary)
                            Text("Taux de Remplissage Global des Bus", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("$fillRate %", fontWeight = FontWeight.Black, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    LinearProgressIndicator(
                        progress = { fillRate / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Supervision Temps Réel Overview
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🚍 Supervision Live Ops Direct", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        SupervisionStatusItem("Bus en Route", "2", Color(0xFF2E7D32))
                        SupervisionStatusItem("Bus à l'Heure", "2", Color(0xFF1565C0))
                        SupervisionStatusItem("Arrêt Station", "1", Color(0xFFF57C00))
                        SupervisionStatusItem("Incidents", "0", Color(0xFFD32F2F))
                    }
                }
            }
        }
    }
}

@Composable
fun SupervisionStatusItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ==========================================
// 2. FLEET MODULE (GESTION DES VÉHICULES)
// ==========================================
@Composable
fun FleetModule(
    vehicles: List<Vehicle>,
    drivers: List<Driver>,
    viewModel: VoyageViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("🚌 Flotte Automobile Interurbaine", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("${vehicles.size} véhicules enregistrés dans le parc", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(
                    onClick = { showAddDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Ajouter", fontSize = 12.sp)
                }
            }
        }

        items(vehicles) { vehicle ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary)
                            Text(vehicle.plateNumber, fontWeight = FontWeight.Black, fontSize = 16.sp)
                            AssistChip(
                                onClick = {},
                                label = { Text(vehicle.category, fontSize = 10.sp) }
                            )
                        }

                        Switch(
                            checked = vehicle.isActive,
                            onCheckedChange = { viewModel.toggleVehicleActive(vehicle.id) }
                        )
                    }

                    Text("${vehicle.make} ${vehicle.model} • Capacité : ${vehicle.capacity} places • Chauffeur : ${vehicle.driverName}", fontSize = 12.sp, fontWeight = FontWeight.Medium)

                    // Amenities chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        vehicle.features.forEach { feature ->
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(feature, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Assurance : ${vehicle.insuranceExpiry} | CT : ${vehicle.techControlExpiry}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)

                        IconButton(
                            onClick = { viewModel.deleteVehicle(vehicle.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            drivers = drivers,
            onDismiss = { showAddDialog = false },
            onConfirm = { plate, make, model, cat, seats, features, driverName ->
                viewModel.addVehicle(plate, make, model, cat, seats, features, driverName)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddVehicleDialog(
    drivers: List<Driver>,
    onDismiss: () -> Unit,
    onConfirm: (plate: String, make: String, model: String, category: String, seats: Int, features: List<String>, driver: String) -> Unit
) {
    var plate by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("Toyota") }
    var model by remember { mutableStateOf("Coaster") }
    var category by remember { mutableStateOf("VIP") }
    var seats by remember { mutableStateOf("22") }
    var selectedDriver by remember { mutableStateOf(drivers.firstOrNull()?.let { "${it.prenom} ${it.nom}" } ?: "Jean-Paul Mba") }
    var ac by remember { mutableStateOf(true) }
    var wifi by remember { mutableStateOf(true) }
    var tv by remember { mutableStateOf(true) }
    var usb by remember { mutableStateOf(true) }
    var toilet by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Véhicule Flotte", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("Immatriculation (ex: GA-990-GV)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = make, onValueChange = { make = it }, label = { Text("Marque") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("Modèle") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Catégorie (VIP/Standard)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = seats, onValueChange = { seats = it }, label = { Text("Nombre Places") }, modifier = Modifier.weight(1f))
                }

                Text("Équipements & Confort :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = ac, onClick = { ac = !ac }, label = { Text("Clim") })
                    FilterChip(selected = wifi, onClick = { wifi = !wifi }, label = { Text("Wifi") })
                    FilterChip(selected = tv, onClick = { tv = !tv }, label = { Text("TV") })
                    FilterChip(selected = usb, onClick = { usb = !usb }, label = { Text("USB") })
                    FilterChip(selected = toilet, onClick = { toilet = !toilet }, label = { Text("WC") })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (plate.isNotBlank()) {
                        val feats = mutableListOf<String>()
                        if (ac) feats.add("Climatisation")
                        if (wifi) feats.add("Wifi")
                        if (tv) feats.add("Écran TV")
                        if (usb) feats.add("Prise USB")
                        if (toilet) feats.add("Toilette")
                        onConfirm(plate, make, model, category, seats.toIntOrNull() ?: 18, feats, selectedDriver)
                    }
                }
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

// ==========================================
// 3. DRIVERS & EMPLOYEES (RBAC) MODULE
// ==========================================
@Composable
fun DriversAndEmployeesModule(
    drivers: List<Driver>,
    employees: List<Employee>,
    viewModel: VoyageViewModel
) {
    var showAddDriverDialog by remember { mutableStateOf(false) }
    var showAddEmpDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👨‍✈️ Roster Chauffeurs & Conducteurs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Button(onClick = { showAddDriverDialog = true }, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Nouveau Chauffeur", fontSize = 11.sp)
                }
            }
        }

        items(drivers) { driver ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("${driver.prenom} ${driver.nom}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Permis : ${driver.licenseNumber} (Exp : ${driver.licenseExpiry})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Tél : ${driver.phone} • Ancienneté : ${driver.seniorityYears} ans", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        AssistChip(
                            onClick = {},
                            label = { Text(driver.status, fontSize = 10.sp) }
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Bus : ${driver.assignedVehiclePlate}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            IconButton(
                                onClick = { viewModel.deleteDriver(driver.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👥 Personnel & Droits d'Accès (RBAC)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Button(onClick = { showAddEmpDialog = true }, shape = RoundedCornerShape(8.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)) {
                    Icon(Icons.Default.PersonAdd, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Créer Compte", fontSize = 11.sp)
                }
            }
        }

        items(employees) { emp ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(emp.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${emp.role} • ${emp.agency}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(emp.email, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = emp.active,
                            onCheckedChange = { viewModel.toggleEmployeeStatus(emp.id) }
                        )
                        IconButton(
                            onClick = { viewModel.deleteEmployee(emp.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDriverDialog) {
        AddDriverDialog(
            onDismiss = { showAddDriverDialog = false },
            onConfirm = { nom, prenom, phone, email, license, plate ->
                viewModel.addDriver(nom, prenom, phone, email, license, plate)
                showAddDriverDialog = false
            }
        )
    }

    if (showAddEmpDialog) {
        AddEmployeeDialog(
            onDismiss = { showAddEmpDialog = false },
            onConfirm = { name, email, role, agency ->
                viewModel.addEmployee(name, email, role, agency)
                showAddEmpDialog = false
            }
        )
    }
}

@Composable
fun AddDriverDialog(
    onDismiss: () -> Unit,
    onConfirm: (nom: String, prenom: String, phone: String, email: String, license: String, plate: String) -> Unit
) {
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var license by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("GA-882-GV") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter un Chauffeur", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = prenom, onValueChange = { prenom = it }, label = { Text("Prénom") }, singleLine = true)
                OutlinedTextField(value = nom, onValueChange = { nom = it }, label = { Text("Nom") }, singleLine = true)
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Téléphone (ex: 077 00 11 22)") }, singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true)
                OutlinedTextField(value = license, onValueChange = { license = it }, label = { Text("N° de Permis de conduire") }, singleLine = true)
                OutlinedTextField(value = plate, onValueChange = { plate = it }, label = { Text("Plaque Bus Affecté") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (nom.isNotBlank() && prenom.isNotBlank()) {
                    onConfirm(nom, prenom, phone, email, license, plate)
                }
            }) { Text("Ajouter") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

@Composable
fun AddEmployeeDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, email: String, role: String, agency: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("Agent de réservation") }
    var agency by remember { mutableStateOf("Agence Centrale Libreville") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nouveau Membre du Personnel", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nom Complet") }, singleLine = true)
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Adresse Email") }, singleLine = true)
                OutlinedTextField(value = role, onValueChange = { role = it }, label = { Text("Rôle (Super Admin/Comptable/Agent)") }, singleLine = true)
                OutlinedTextField(value = agency, onValueChange = { agency = it }, label = { Text("Agence Rattachée") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onConfirm(name, email, role, agency)
                }
            }) { Text("Créer Droits") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ==========================================
// 4. PARCELS & LUGGAGE MODULE
// ==========================================
@Composable
fun ParcelsModule(
    parcels: List<Parcel>,
    viewModel: VoyageViewModel
) {
    var showAddDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("📦 Service Fret & Express Colis", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Suivi du transport de colis interurbains par bus", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Button(onClick = { showAddDialog = true }, shape = RoundedCornerShape(8.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Enregistrer Colis", fontSize = 11.sp)
                }
            }
        }

        items(parcels) { parcel ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.LocalShipping, null, tint = MaterialTheme.colorScheme.primary)
                            Text(parcel.trackingCode, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }

                        AssistChip(
                            onClick = {
                                val nextStatus = when (parcel.status) {
                                    "En attente" -> "En transit"
                                    "En transit" -> "Livré"
                                    else -> "Livré"
                                }
                                viewModel.updateParcelStatus(parcel.id, nextStatus)
                            },
                            label = { Text(parcel.status, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Text("${parcel.route} • Poids : ${parcel.weightKg} kg • Tarif : ${parcel.priceFcfa.toInt()} FCFA", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Expéditeur : ${parcel.senderName} (${parcel.senderPhone})", fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Destinataire : ${parcel.recipientName} (${parcel.recipientPhone})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        IconButton(
                            onClick = { viewModel.deleteParcel(parcel.id) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Supprimer", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddParcelDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { sender, sPhone, recipient, rPhone, route, weight, price ->
                viewModel.addParcel(sender, sPhone, recipient, rPhone, route, weight, price)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun AddParcelDialog(
    onDismiss: () -> Unit,
    onConfirm: (sender: String, sPhone: String, recipient: String, rPhone: String, route: String, weight: Double, price: Double) -> Unit
) {
    var sender by remember { mutableStateOf("") }
    var sPhone by remember { mutableStateOf("") }
    var recipient by remember { mutableStateOf("") }
    var rPhone by remember { mutableStateOf("") }
    var route by remember { mutableStateOf("Libreville ➔ Oyem") }
    var weightStr by remember { mutableStateOf("5.0") }
    var priceStr by remember { mutableStateOf("5000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enregistrer un Colis", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = sender, onValueChange = { sender = it }, label = { Text("Nom Expéditeur") }, singleLine = true)
                OutlinedTextField(value = sPhone, onValueChange = { sPhone = it }, label = { Text("Téléphone Expéditeur") }, singleLine = true)
                OutlinedTextField(value = recipient, onValueChange = { recipient = it }, label = { Text("Nom Destinataire") }, singleLine = true)
                OutlinedTextField(value = rPhone, onValueChange = { rPhone = it }, label = { Text("Téléphone Destinataire") }, singleLine = true)
                OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Trajet (ex: Libreville ➔ Oyem)") }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = weightStr, onValueChange = { weightStr = it }, label = { Text("Poids (kg)") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = priceStr, onValueChange = { priceStr = it }, label = { Text("Prix (FCFA)") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (sender.isNotBlank() && recipient.isNotBlank()) {
                    onConfirm(sender, sPhone, recipient, rPhone, route, weightStr.toDoubleOrNull() ?: 5.0, priceStr.toDoubleOrNull() ?: 5000.0)
                }
            }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

// ==========================================
// 5. FINANCE & ACCOUNTING MODULE
// ==========================================
@Composable
fun FinanceModule(
    bookings: List<Booking>,
    context: Context,
    viewModel: VoyageViewModel
) {
    val confirmedBookings = bookings.filter { it.status == "Confirmed" }
    val totalRevenue = confirmedBookings.sumOf { it.pricePaid }

    val airtelRevenue = confirmedBookings.filter { it.paymentMethod.contains("Airtel", ignoreCase = true) }.sumOf { it.pricePaid }
    val moovRevenue = confirmedBookings.filter { it.paymentMethod.contains("Moov", ignoreCase = true) }.sumOf { it.pricePaid }
    val agencyRevenue = confirmedBookings.filter { it.paymentMethod.contains("agence", ignoreCase = true) || it.paymentMethod.contains("Espèce", ignoreCase = true) }.sumOf { it.pricePaid }

    val operatingExpenses = totalRevenue * 0.35
    val netProfit = totalRevenue - operatingExpenses
    val tvaEstimated = totalRevenue * 0.18

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("💰 Comptabilité & Bilan Financier", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Recettes, dépenses, taxes et bénéfices nets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = { TechnicalGuidePdfHelper.generateTechnicalGuidePdf(context) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Rapport PDF", fontSize = 11.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Chiffre d'Affaires Brut Validé", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text("${totalRevenue.toInt()} FCFA", fontSize = 24.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Dépenses opérationnelles (Carburant, entretien) :", fontSize = 11.sp)
                        Text("${operatingExpenses.toInt()} FCFA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("TVA estimée (18%) :", fontSize = 11.sp)
                        Text("${tvaEstimated.toInt()} FCFA", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Bénéfice Net d'Exploitation :", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("${netProfit.toInt()} FCFA", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        item {
            Text("📊 Répartition des Encaissements par Canal", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FinancialChannelCard("Airtel Money Gabon", "${airtelRevenue.toInt()} FCFA", Color(0xFFE53935))
                FinancialChannelCard("Moov Money Flooz", "${moovRevenue.toInt()} FCFA", Color(0xFFFB8C00))
                FinancialChannelCard("Paiements Guichet / Agence", "${agencyRevenue.toInt()} FCFA", Color(0xFF1E88E5))
            }
        }
    }
}

@Composable
fun FinancialChannelCard(title: String, amount: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Text(amount, fontWeight = FontWeight.Black, fontSize = 15.sp, color = color)
        }
    }
}

// ==========================================
// 6. AI ASSISTANT ERP MODULE (GEMINI)
// ==========================================
@Composable
fun AiAssistantErpModule(viewModel: VoyageViewModel) {
    val aiResult by viewModel.aiAnalysisResult.collectAsState()
    val isAiLoading by viewModel.aiAnalysisLoading.collectAsState()
    val aiError by viewModel.aiAnalysisError.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        Text("Intelligence Artificielle Gabon Voyage ERP", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    }
                    Text(
                        "L'assistant Gemini analyse vos ventes, prédit le taux de remplissage des lignes vers le Grand Nord, détecte les opportunités tarifaires et génère le rapport stratégique mensuel.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Button(
                        onClick = { viewModel.generateAiAnalyticsReport() },
                        enabled = !isAiLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(Modifier.width(8.dp))
                            Text("Analyse Gemini en cours...")
                        } else {
                            Icon(Icons.Default.AutoAwesome, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Générer l'Analyse IA Stratégique", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        if (aiError != null) {
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Text(aiError ?: "", modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer, fontSize = 12.sp)
                }
            }
        }

        if (aiResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("💡 Rapport Stratégique de l'IA", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Divider()
                        Text(aiResult ?: "", fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        } else if (!isAiLoading) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Analytics, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.height(8.dp))
                        Text("Cliquez sur le bouton ci-dessus pour obtenir l'analyse prédictive IA.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

// ==========================================
// 7. GPS TELEMETRY & LIVE TRACKING
// ==========================================
@Composable
fun GpsTrackingModule(telemetries: List<VehicleTelemetry>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("📍 Géolocalisation GPS & Suivi de la Flotte Direct", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        // Live Simulated Map Container
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B))
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Map, null, modifier = Modifier.size(40.dp), tint = Color(0xFF38BDF8))
                        Text("Carte GPS Réseau Interurbain Gabon Voyage", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Axe Libreville ➔ Kango ➔ Ndjolé ➔ Mitzic ➔ Oyem ➔ Bitam", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                }
            }
        }

        items(telemetries) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Navigation, null, tint = MaterialTheme.colorScheme.primary)
                            Text(item.busId, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        }
                        AssistChip(onClick = {}, label = { Text(item.status, fontSize = 10.sp) })
                    }
                    Text(item.lineName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text("Position : ${item.currentPosition} • Vitesse : ${item.speedKmh} km/h • ETA : ${item.eta}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ==========================================
// 8. SEAT MAP & BOOKINGS MODULE
// ==========================================
@Composable
fun SeatMapModule(trips: List<Trip>, bookings: List<Booking>) {
    var selectedTripId by remember { mutableStateOf(trips.firstOrNull()?.id ?: 1) }
    val currentTrip = trips.find { it.id == selectedTripId } ?: trips.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("💺 Plan Interactif des Sièges du Bus", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            if (currentTrip != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("${currentTrip.departure} ➔ ${currentTrip.destination} (${currentTrip.agencyName} - ${currentTrip.departureTime})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        // Seats Grid Legend
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SeatLegendItem("Libre", Color(0xFF4CAF50))
                            SeatLegendItem("Occupé", Color(0xFFE53935))
                            SeatLegendItem("Réservé", Color(0xFFFFB300))
                            SeatLegendItem("Hors Service", Color(0xFF757575))
                        }

                        // Grid 2D 18 seats representation
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("🚌 Poste de Conduite / Chauffeur 👨‍✈️", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            for (row in 1..5) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    val seatA = (row - 1) * 4 + 1
                                    val seatB = (row - 1) * 4 + 2
                                    val seatC = (row - 1) * 4 + 3
                                    val seatD = (row - 1) * 4 + 4

                                    SeatBox(seatA, seatA <= 6)
                                    SeatBox(seatB, seatB % 3 == 0)
                                    Spacer(Modifier.width(16.dp)) // Aisle
                                    SeatBox(seatC, seatC <= 10)
                                    if (seatD <= 18) SeatBox(seatD, false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SeatLegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(color))
        Text(label, fontSize = 10.sp)
    }
}

@Composable
fun SeatBox(number: Int, isOccupied: Boolean) {
    val bgColor = if (isOccupied) Color(0xFFE53935) else Color(0xFF4CAF50)
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text("N°$number", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
    }
}

// ==========================================
// 9. PROMOS & PUSH NOTIFICATIONS MODULE
// ==========================================
@Composable
fun PromosAndPushModule(viewModel: VoyageViewModel) {
    val promoCodes by viewModel.promoCodes.collectAsState()
    var newCodeText by remember { mutableStateOf("") }

    var pushTitle by remember { mutableStateOf("") }
    var pushBody by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("📣 Gestion des Codes Promo & Notifications Push", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🎟️ Ajouter un Code Promo (ex: GABON10)", fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCodeText,
                            onValueChange = { newCodeText = it },
                            label = { Text("Code Promo") },
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                viewModel.addPromoCode(newCodeText)
                                newCodeText = ""
                            }
                        ) {
                            Text("Activer")
                        }
                    }

                    Text("Codes Actifs : ${promoCodes.joinToString()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🔔 Diffuser une Notification Push aux Voyageurs", fontWeight = FontWeight.Bold)
                    OutlinedTextField(value = pushTitle, onValueChange = { pushTitle = it }, label = { Text("Titre de l'alerte") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pushBody, onValueChange = { pushBody = it }, label = { Text("Message de la notification") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (pushTitle.isNotBlank()) {
                                viewModel.addNotification("PUSH ENVOYÉ : $pushTitle - $pushBody")
                                pushTitle = ""
                                pushBody = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Send, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Envoyer la notification broadcast")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricTile(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Black)
        }
    }
}

// ==========================================
// 10. CORBEILLE (TRASH BIN) MODULE
// ==========================================
@Composable
fun CorbeilleModule(
    trashItems: List<TrashItem>,
    viewModel: VoyageViewModel
) {
    var showEmptyConfirmDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🗑️ Corbeille & Historique des Suppressions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${trashItems.size} éléments en corbeille (restauration possible)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (trashItems.isNotEmpty()) {
                    Button(
                        onClick = { showEmptyConfirmDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Vider Corbeille", fontSize = 11.sp)
                    }
                }
            }
        }

        if (trashItems.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "La corbeille est actuellement vide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Tous les véhicules, chauffeurs, colis ou employés supprimés apparaîtront ici pour une restauration en 1 clic.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(trashItems) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = item.type,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = item.deletedDate,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Text(
                            text = item.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Text(
                            text = item.description,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.restoreTrashItem(item) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.RestoreFromTrash, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Restaurer l'élément", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            IconButton(
                                onClick = { viewModel.permanentlyDeleteTrashItem(item.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.DeleteForever,
                                    contentDescription = "Supprimer définitivement",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEmptyConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEmptyConfirmDialog = false },
            title = { Text("Vider la corbeille ?", fontWeight = FontWeight.Bold) },
            text = { Text("Attention : Cette action est irréversible et supprimera définitivement tous les éléments en corbeille.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.emptyTrash()
                        showEmptyConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Vider définitivement")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmptyConfirmDialog = false }) { Text("Annuler") }
            }
        )
    }
}

// ==========================================
// 11. BACKUPS & RESTORATION MODULE
// ==========================================
@Composable
fun BackupsModule(
    backups: List<BackupSnapshot>,
    viewModel: VoyageViewModel
) {
    var showCreateBackupDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "💾 Dossier de Sauvegarde & Secours ERP",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Snapshots de la base de données et restauration",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { showCreateBackupDialog = true },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Backup, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Créer Sauvegarde", fontSize = 11.sp)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "Sauvegarde Automatique Réseau Libreville",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Toutes les données (flotte, réservations, chauffeurs, fret colis) sont instantanément archivées en sécurité.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        items(backups) { snapshot ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Storage, null, tint = MaterialTheme.colorScheme.primary)
                            Text(snapshot.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${snapshot.sizeKb} KB",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "Date de création : ${snapshot.timestamp} | ID : ${snapshot.id}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )

                    // Stats row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🚍 Flotte : ${snapshot.vehiclesCount}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("👨‍✈️ Chauffeurs : ${snapshot.driversCount}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("📦 Colis : ${snapshot.parcelsCount}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        Text("🎟️ Billet : ${snapshot.bookingsCount}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.restoreBackup(snapshot) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.SettingsBackupRestore, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Restaurer ce fichier", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = { viewModel.deleteBackup(snapshot.id) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Delete, "Supprimer sauvegarde", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showCreateBackupDialog) {
        var backupName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateBackupDialog = false },
            title = { Text("Créer une Sauvegarde Globale", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Nommez votre snapshot de sauvegarde (optionnel) :", fontSize = 12.sp)
                    OutlinedTextField(
                        value = backupName,
                        onValueChange = { backupName = it },
                        label = { Text("ex: Clôture Mensuelle Juillet 2026") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createBackup(backupName)
                        showCreateBackupDialog = false
                    }
                ) {
                    Text("Sauvegarder Maintenant")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBackupDialog = false }) { Text("Annuler") }
            }
        )
    }
}
