package com.example.ui.screens

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import java.io.File
import java.io.FileOutputStream

object TechnicalGuidePdfHelper {

    fun generateTechnicalGuidePdf(context: Context) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            
            // --- PAGE 1: Presentation & Functionalities ---
            val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page1 = pdfDocument.startPage(pageInfo1)
            var canvas = page1.canvas
            
            // Draw header band
            paint.color = android.graphics.Color.parseColor("#005C4B") // WhatsApp/Gabon Voyage Green
            canvas.drawRect(0f, 0f, 595f, 80f, paint)
            
            // Header text
            paint.color = android.graphics.Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GABON VOYAGE - FICHE TECHNIQUE", 30f, 48f, paint)
            
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Guide de Spécifications & Architecture de l'Application", 30f, 65f, paint)
            
            // Document Info
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            var yPos = 120f
            canvas.drawText("1. PRÉSENTATION GÉNÉRALE", 40f, yPos, paint)
            yPos += 15f
            
            paint.color = android.graphics.Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            val introLines = listOf(
                "Gabon Voyage est une plateforme numérique innovante facilitant la réservation de trajets",
                "interurbains de bus au Gabon. Elle permet aux voyageurs de consulter les agences disponibles,",
                "de planifier des trajets, d'effectuer des paiements mobiles sécurisés, et d'obtenir des billets",
                "électroniques instantanés. Elle intègre également une assistance client propulsée par l'IA."
            )
            for (line in introLines) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 16f
            }
            
            yPos += 15f
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("2. ARCHITECTURE TECHNIQUE", 40f, yPos, paint)
            yPos += 15f
            
            paint.color = android.graphics.Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            val archLines = listOf(
                "• Framework UI : Jetpack Compose (100% Kotlin asynchrone)",
                "• Gestion d'État : ViewModel avec StateFlow / Flow de Coroutines Kotlin",
                "• Base de Données Locale : Jetpack Room Database (SQLite) pour le mode hors-ligne",
                "• Base de Données Temps Réel : Firebase Realtime Database pour la synchronisation en direct",
                "• Intégration IA : Service Gemini-3.5-flash pour les analyses et les auto-réponses",
                "• Génération de Documents : API PdfDocument Android native"
            )
            for (line in archLines) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 18f
            }
            
            yPos += 15f
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("3. MODULES ET FONCTIONNALITÉS CLÉS", 40f, yPos, paint)
            yPos += 15f
            
            paint.color = android.graphics.Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            val featLines = listOf(
                "• Module de Réservation : Recherche intelligente par gare de départ, destination et prix.",
                "• Module de Paiement Mobile : Prise en charge d'Airtel Money et Moov Money avec saisie",
                "  sécurisée du code de transaction et génération instantanée du billet électronique au format PDF.",
                "• Chat d'Assistance Client : Interface en temps réel de type WhatsApp avec conseiller IA",
                "  intégré (réponses instantanées basées sur la base de données réelle des trajets disponibles).",
                "• Panneau Super Administrateur : Suivi des ventes, gestion des trajets, approbation des",
                "  réservations en attente, audit de sécurité et génération de rapports financiers par Gemini."
            )
            for (line in featLines) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 18f
            }
            
            // Footer page 1
            paint.color = android.graphics.Color.GRAY
            paint.textSize = 8f
            canvas.drawLine(40f, 800f, 555f, 800f, paint)
            canvas.drawText("Gabon Voyage • Fiche Technique • Page 1/2", 40f, 815f, paint)
            
            pdfDocument.finishPage(page1)
            
            // --- PAGE 2: Data Models & Deployment ---
            val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = pdfDocument.startPage(pageInfo2)
            canvas = page2.canvas
            
            // Header page 2
            paint.color = android.graphics.Color.parseColor("#128C7E") // WhatsApp Teal Green
            canvas.drawRect(0f, 0f, 595f, 80f, paint)
            
            paint.color = android.graphics.Color.WHITE
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("GABON VOYAGE - SCHÉMA & FLUX", 30f, 48f, paint)
            
            paint.textSize = 10f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Structures de données, sécurité et synchronisation", 30f, 65f, paint)
            
            yPos = 120f
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("4. MODÈLES DE DONNÉES ESSENTIELS", 40f, yPos, paint)
            yPos += 15f
            
            paint.color = android.graphics.Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            val dataLines = listOf(
                "• User : Gère les comptes voyageurs (clients) et d'administrateurs (id, nom, prenom, email,",
                "  phone, password, isAgent).",
                "• Trip : Représente les trajets de bus disponibles (id, agencyName, departure, destination,",
                "  departureTime, price, type, availableSeats).",
                "• Booking : Représente l'état des réservations et des paiements (id, userId, userNom, tripId,",
                "  departure, destination, travelDate, departureTime, pricePaid, paymentMethod, transactionNumber,",
                "  status = Pending/Confirmed, seatNumber).",
                "• ChatMessage : Supporte le chat d'assistance (id, senderName, senderId, content, timestamp,",
                "  isAgentMessage, threadId)."
            )
            for (line in dataLines) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 18f
            }
            
            yPos += 15f
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("5. STRATÉGIE DE SYNCHRONISATION", 40f, yPos, paint)
            yPos += 15f
            
            paint.color = android.graphics.Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 10f
            val syncLines = listOf(
                "• Mécanisme en temps réel bidirectionnel : L'application écoute en continu les changements",
                "  Firebase. Dès qu'une modification survient en ligne (par exemple, approbation d'un billet par un",
                "  administrateur), l'état local Compose est mis à jour et se recompose instantanément.",
                "• Mise à jour optimiste : Pour garantir une expérience sans aucune latence dans le chat, les",
                "  messages envoyés sont d'abord injectés immédiatement dans l'interface utilisateur locale",
                "  avant même la confirmation finale de l'écriture réseau Firebase."
            )
            for (line in syncLines) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 18f
            }
            
            yPos += 12f
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("6. AXES D'AMÉLIORATION", 40f, yPos, paint)
            yPos += 15f
            
            paint.color = android.graphics.Color.DKGRAY
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 9.5f
            val axesLines = listOf(
                "1. Accompagnement des utilisateurs : La richesse fonctionnelle de Gabon Voyage nécessite",
                "   une courte formation initiale et la mise à disposition d'un guide utilisateur interactif afin",
                "   de faciliter la prise en main des différents modules selon les rôles.",
                "2. Optimisation de la connectivité : Les fonctionnalités en temps réel (géolocalisation,",
                "   synchronisation des réservations et notifications) dépendent d'une connexion Internet stable.",
                "   Un mécanisme de synchronisation automatique synchronise les données dès le rétablissement.",
                "3. Automatisation des paiements : Prévoir l'intégration des API et Webhooks des opérateurs",
                "   Mobile Money et des passerelles de paiement afin d'automatiser la validation des transactions,",
                "   la génération des reçus et le rapprochement comptable en temps réel, sans intervention manuelle."
            )
            for (line in axesLines) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += 15f
            }
            
            // Footer page 2
            paint.color = android.graphics.Color.GRAY
            paint.textSize = 8f
            canvas.drawLine(40f, 800f, 555f, 800f, paint)
            canvas.drawText("Gabon Voyage • Fiche Technique • Page 2/2", 40f, 815f, paint)
            
            pdfDocument.finishPage(page2)
            
            // Save to Downloads directory
            val fileDirectory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(fileDirectory, "Fiche_Technique_Gabon_Voyage.pdf")
            val fileOutputStream = FileOutputStream(file)
            pdfDocument.writeTo(fileOutputStream)
            pdfDocument.close()
            fileOutputStream.close()
            
            Toast.makeText(context, "Fiche technique PDF téléchargée avec succès !\nChemin : ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("PDF_GEN", "Error creating technical guide PDF", e)
            Toast.makeText(context, "Erreur de génération PDF : ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }
}
