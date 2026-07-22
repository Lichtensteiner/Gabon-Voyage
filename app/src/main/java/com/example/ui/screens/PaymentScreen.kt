package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import com.example.data.model.Booking
import com.example.ui.viewmodel.VoyageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: VoyageViewModel,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val trip = viewModel.selectedTripForBooking.collectAsState().value
    val bookingSuccess = viewModel.bookingSuccess.collectAsState().value
    val isConnected by viewModel.isConnected.collectAsState()

    var paymentMethod by remember { mutableStateOf("Moov Money") }
    var senderPhone by remember { mutableStateOf("") }
    var transactionId by remember { mutableStateOf("") }
    var screenshotAttached by remember { mutableStateOf(false) }

    var showErrorMsg by remember { mutableStateOf<String?>(null) }

    val merchantNumbers = mapOf(
        "Moov Money" to "Code Marchand Moov : *111*2*4# (ID: 504030)",
        "Airtel Money" to "Code Marchand Airtel : *150*4*1# (ID: 902010)"
    )

    if (trip == null && bookingSuccess == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aucune réservation sélectionnée en ce moment.")
        }
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

        if (bookingSuccess != null) {
            // SUCCESS TICKET STATE
            SuccessTicketLayout(
                booking = bookingSuccess,
                onViewHistory = onNavigateToHistory
            )
        } else if (trip != null) {
            // STEP 1: FILL PAYMENT PROOF
            Text(
                text = "Paiement Mobile Money",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            // Dynamic Trip Recapitulative Summary
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Récapitulatif du trajet",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${trip.departure} ➔ ${trip.destination}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Départ : ${trip.departureTime} | Agence : ${trip.agencyName}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${trip.price.toInt()} FCFA",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                    }
                }
            }

            // Mobile Money Payment Options Tab Selector
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Choisissez votre opérateur",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val paymentModes = listOf("Moov Money", "Airtel Money")
                        paymentModes.forEach { mode ->
                            val isSelected = paymentMethod == mode
                            val btnBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val btnTg = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

                            Button(
                                onClick = { paymentMethod = mode },
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = btnBg, contentColor = btnTg),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (mode == "Moov Money") Icons.Default.MobileFriendly else Icons.Default.PhoneAndroid,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = mode,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Merchant info Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Instructions de Transfert :",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Composez sur votre téléphone portable le code USSD ci-dessous pour envoyer le montant de ${trip.price.toInt()} FCFA :",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = merchantNumbers[paymentMethod] ?: "",
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Input Fields Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "2. Saisie des informations de transaction",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    AnimatedVisibility(visible = showErrorMsg != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = showErrorMsg ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = senderPhone,
                        onValueChange = { senderPhone = it },
                        label = { Text("Votre numéro Mobile Money (ex: 077...)") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Numéro / ID de Transaction reçu par SMS") },
                        leadingIcon = { Icon(Icons.Default.ReceiptLong, null) },
                        placeholder = { Text("Ex: TX-991203X") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Screenshot Capture Proof simulation
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { screenshotAttached = !screenshotAttached }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = if (screenshotAttached) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                            contentDescription = null,
                            tint = if (screenshotAttached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (screenshotAttached) "Preuve d'achat chargée avec succès !" else "Capturer / Charger le reçu de paiement",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (screenshotAttached) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (screenshotAttached) "Fichier: screenshot_reçu_gabon_voyage.png (240 KB)" else "Appuyez pour simuler l'upload de photo",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (screenshotAttached) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Safe reminder alert
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Important : Tout paiement frauduleux ou ID de transaction falsifié entraînera le rejet de la réservation.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (!isConnected) {
                        showErrorMsg = "Une connexion Internet active est requise pour soumettre votre réservation et la synchroniser."
                    } else if (senderPhone.isBlank()) {
                        showErrorMsg = "Veuillez saisir votre numéro de téléphone émetteur."
                    } else if (transactionId.isBlank()) {
                        showErrorMsg = "Veuillez entrer la référence de transaction mobile money reçue."
                    } else {
                        showErrorMsg = null
                        val screenshotPath = if (screenshotAttached) "content://com.example/screenshot_reçu_gabon_voyage.png" else null
                        viewModel.submitBooking(
                            paymentMethod = paymentMethod,
                            merchantNumber = if (paymentMethod == "Moov Money") "504030" else "902010",
                            senderPhone = senderPhone,
                            transactionNumber = transactionId,
                            screenshotUriString = screenshotPath
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CheckCircleOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Vérifier et Transmettre la Preuve", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Visual layout simulating the digital traveler ticket!
@Composable
fun SuccessTicketLayout(
    booking: Booking,
    onViewHistory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // High-Contrast Green Header
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
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Réservation Transmise !",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "ID Ticket : #${booking.id}",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Ticket Details Body
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("VOYAGEUR", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(booking.userNom, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text("DATE DE DÉPART", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(booking.travelDate, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DÉPART ➔ DESTINATION", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text("${booking.departure} ➔ ${booking.destination}", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text("HEURE", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(booking.departureTime, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AGENCE ET TYPE", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text("${booking.agencyName} (${booking.type})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text("PRIX", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text("${booking.pricePaid.toInt()} FCFA", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("PAIEMENT VIA", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text(booking.paymentMethod, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text("STATUT ACTUEL", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "En Attente",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Dashed boundary line
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

                Spacer(modifier = Modifier.height(4.dp))

                // QR Bar Code placeholder
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Code QR du billet",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(100.dp)
                    )
                    Text(
                        text = "Billet non validé - En attente de paiement manuel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Vérification en cours sous d'un délai de 30 minutes.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }

    val context = LocalContext.current

    Button(
        onClick = {
            generateTicketPdf(context, booking)
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
    ) {
        Icon(Icons.Default.Download, contentDescription = "Télécharger le reçu PDF")
        Spacer(modifier = Modifier.width(8.dp))
        Text("Télécharger le Billet (PDF)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondary)
    }

    Spacer(modifier = Modifier.height(10.dp))

    Button(
        onClick = onViewHistory,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outline)
    ) {
        Icon(Icons.Default.Assignment, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Consulter l'historique de mes billets", fontWeight = FontWeight.Bold)
    }
}

private fun generateTicketPdf(context: android.content.Context, booking: Booking) {
    try {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(300, 500, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint()

        // Title
        paint.color = android.graphics.Color.parseColor("#1E6F3E")
        paint.textSize = 16f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("GABON VOYAGE", 20f, 40f, paint)

        paint.textSize = 9f
        paint.color = android.graphics.Color.DKGRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Recu & Billet de Reservation Numerique", 20f, 56f, paint)

        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(20f, 70f, 280f, 70f, paint)

        // Ticket ID
        paint.textSize = 11f
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Ticket ID: #${booking.id}", 20f, 95f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = android.graphics.Color.GRAY
        canvas.drawText("VOYAGEUR :", 20f, 125f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(booking.userNom, 20f, 140f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("DEPART -> DESTINATION :", 20f, 170f, paint)
        paint.color = android.graphics.Color.parseColor("#1E6F3E")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.departure} -> ${booking.destination}", 20f, 185f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("DATE & HEURE :", 20f, 215f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.travelDate} a ${booking.departureTime}", 20f, 230f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("COMPAGNIE / AGENCE :", 20f, 260f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.agencyName} (${booking.type})", 20f, 275f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("MONTANT PAYE :", 20f, 305f, paint)
        paint.color = android.graphics.Color.parseColor("#F39C12")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.pricePaid.toInt()} FCFA", 20f, 320f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("MODE DE PAIEMENT :", 20f, 350f, paint)
        paint.color = android.graphics.Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("${booking.paymentMethod} (Ref: ${booking.transactionNumber})", 20f, 365f, paint)

        paint.color = android.graphics.Color.GRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("STATUT :", 20f, 395f, paint)
        paint.color = android.graphics.Color.parseColor("#F44336")
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EN ATTENTE DE VALIDATION", 20f, 410f, paint)

        paint.color = android.graphics.Color.LTGRAY
        canvas.drawLine(20f, 435f, 280f, 435f, paint)

        paint.textSize = 8f
        paint.color = android.graphics.Color.DKGRAY
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Presentez ce recu en agence pour l'embarquement.", 20f, 455f, paint)
        canvas.drawText("Genere par Gabon Voyage. Bon voyage !", 20f, 470f, paint)

        pdfDocument.finishPage(page)

        val fileDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(fileDirectory, "Billet_Voyage_${booking.id}.pdf")
        val fileOutputStream = FileOutputStream(file)
        pdfDocument.writeTo(fileOutputStream)
        pdfDocument.close()
        fileOutputStream.close()

        Toast.makeText(context, "Billet PDF telecharge avec succes !\nChemin : ${file.absolutePath}", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        android.util.Log.e("PDF_GEN", "Error during PDF creation", e)
        Toast.makeText(context, "Erreur de generation de PDF : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
