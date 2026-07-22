package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.ui.viewmodel.VoyageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: VoyageViewModel,
    modifier: Modifier = Modifier
) {
    val bookings by viewModel.userBookings.collectAsState()
    var selectedTab by remember { mutableStateOf("Tous") }
    var selectedTicketDetails by remember { mutableStateOf<Booking?>(null) }

    val filteredList = when (selectedTab) {
        "En Attente" -> bookings.filter { it.status == "Pending" }
        "Confirmés" -> bookings.filter { it.status == "Confirmed" }
        else -> bookings
    }

    if (selectedTicketDetails != null) {
        // DETAILED MODAL SHEET POPUP TICKET VIEW
        TicketDetailDialog(
            viewModel = viewModel,
            booking = selectedTicketDetails!!,
            onClose = { selectedTicketDetails = null }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mes Billets & Historique",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CloudQueue, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Mode Offline Capable", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Segmented state filter row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf("Tous", "En Attente", "Confirmés")
                tabs.forEach { tab ->
                    val isSelected = selectedTab == tab
                    val bg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    val tg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    
                    Button(
                        onClick = { selectedTab = tab },
                        colors = ButtonDefaults.buttonColors(containerColor = bg, contentColor = tg),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(tab, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (filteredList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("Aucun voyage trouvé", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            text = "Vous n'avez pas de ticket sous ce filtre. Vos billets d'autocar achetés apparaîtront ici.",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredList) { booking ->
                BookingHistoryCard(
                    booking = booking,
                    onViewTicketDetails = { selectedTicketDetails = booking }
                )
            }
        }
    }
}

@Composable
fun BookingHistoryCard(
    booking: Booking,
    onViewTicketDetails: () -> Unit
) {
    val statusColor = when (booking.status) {
        "Confirmed" -> MaterialTheme.colorScheme.primary
        "Pending" -> MaterialTheme.colorScheme.error // Amber / Red representing manual verify
        "Rejected" -> MaterialTheme.colorScheme.error
        else -> Color.Gray
    }

    val statusLabel = when (booking.status) {
        "Confirmed" -> "Confirmé (Prêt)"
        "Pending" -> "Validation en cours (30m)"
        "Rejected" -> "Rejeté (Erreur)"
        else -> booking.status
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewTicketDetails() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${booking.departure} ➔ ${booking.destination}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${booking.agencyName} | ${booking.travelDate}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = (if (booking.status == "Confirmed") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontWeight = FontWeight.Bold,
                        color = if (booking.status == "Confirmed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ID Transaction Mobile Money :",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = booking.transactionNumber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Button(
                    onClick = onViewTicketDetails,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (booking.status == "Confirmed") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (booking.status == "Confirmed") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = if (booking.status == "Confirmed") "Billet Numérique" else "Détails",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            // Reason notification if rejected
            if (booking.status == "Rejected" && !booking.rejectionReason.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f))
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Motif de rejet : ${booking.rejectionReason}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// Full screen Ticket Dialog detail render
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketDetailDialog(
    viewModel: VoyageViewModel,
    booking: Booking,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        confirmButton = {
            TextButton(onClick = onClose) {
                Text("Fermer")
            }
        },
        title = {
            Text(
                booking.agencyName,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Voyageur :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text(booking.userNom, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Trajet libre :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text("${booking.departure} ➔ ${booking.destination}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Date du voyage :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text(booking.travelDate, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Heure départ :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text(booking.departureTime, fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tarif payé :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text("${booking.pricePaid.toInt()} FCFA", fontWeight = FontWeight.Bold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Mobile Money :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text("${booking.paymentMethod} (N° ${booking.userPhone})", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Réf SMS transaction :", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline, modifier = Modifier.weight(1f))
                            Text(booking.transactionNumber, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp, textAlign = TextAlign.End, modifier = Modifier.weight(1.5f))
                        }
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

                if (booking.status == "Confirmed") {
                    // Validated visual QR Code check-in
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Code QR Actif",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(120.dp)
                        )
                        Text(
                            text = "Billet Officiel Électronique",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Présentez ce QR code à l'agence de ${booking.departure} 30 min avant le départ pour obtenir votre carte d'embarquement.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        var isDownloaded by remember { mutableStateOf(false) }
                        var reminderSent by remember { mutableStateOf(false) }

                        // 1. Download Billet button (Suivi réservation : téléchargement du billet numérique)
                        Button(
                            onClick = {
                                isDownloaded = true
                                viewModel.addNotification("Téléchargement Réussi : Le billet numérique alternatif #${booking.id} (${booking.departure} ➔ ${booking.destination}) a été enregistré dans votre dossier de téléchargements locaux.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isDownloaded) "Billet PDF Téléchargé ✔" else "Télécharger Billet Numérique (PDF)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        // 2. Departure Reminder simulator button (Notifications push : rappel de départ)
                        OutlinedButton(
                            onClick = {
                                reminderSent = true
                                viewModel.addNotification("Rappel de départ : Votre voyage de ${booking.departure} vers ${booking.destination} part à l'heure (${booking.departureTime}) le ${booking.travelDate}. Présentez-vous 30 minutes d'avance avec vos affaires !")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (reminderSent) "Alerte de rappel activée !" else "Activer Rappel de départ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else if (booking.status == "Pending") {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Vérification manuelle par l'Agent",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Aussi tôt vérifié, l'agent mettra à jour votre billet avec un QR Code d'accès aux bus.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Cancel, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Billet non validé",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                        Text(
                            text = booking.rejectionReason ?: "Votre paiement n'a pas pu être validé.",
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}
