package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.selection.SelectionContainer
import com.example.data.model.User
import com.example.ui.viewmodel.VoyageViewModel

@Composable
fun AdminProfileScreen(
    viewModel: VoyageViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allAgencies by viewModel.allAgencies.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val allBookings by viewModel.allBookings.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Profil & Droits, 1: Gestion Utilisateurs
    var showEditDialog by remember { mutableStateOf(false) }

    var viewingUser by remember { mutableStateOf<User?>(null) }
    var editingUser by remember { mutableStateOf<User?>(null) }
    var deletingUser by remember { mutableStateOf<User?>(null) }

    // Custom management and AI states
    var searchUserQuery by remember { mutableStateOf("") }
    var roleFilter by remember { mutableStateOf("Tous") } // "Tous", "Agents", "Voyageurs"
    var showCreateAgentDialog by remember { mutableStateOf(false) }

    val aiAnalysisResult by viewModel.aiAnalysisResult.collectAsState()
    val aiAnalysisLoading by viewModel.aiAnalysisLoading.collectAsState()
    val aiAnalysisError by viewModel.aiAnalysisError.collectAsState()

    if (showEditDialog) {
        currentUser?.let { user ->
            AlertDialog(
                onDismissRequest = { showEditDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Modifier mes informations", fontWeight = FontWeight.Black, fontSize = 18.sp)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        var prenom by remember { mutableStateOf(user.prenom) }
                        var nom by remember { mutableStateOf(user.nom) }
                        var email by remember { mutableStateOf(user.email) }
                        var phone by remember { mutableStateOf(user.phone) }
                        var password by remember { mutableStateOf(user.password) }

                        OutlinedTextField(
                            value = prenom,
                            onValueChange = { prenom = it },
                            label = { Text("Prénom") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nom,
                            onValueChange = { nom = it },
                            label = { Text("Nom") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Téléphone") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Button(
                            onClick = {
                                if (prenom.isNotBlank() && nom.isNotBlank() && email.isNotBlank() && phone.isNotBlank() && password.isNotBlank()) {
                                    viewModel.updateUserProfile(nom, prenom, email, phone, password)
                                    showEditDialog = false
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Enregistrer les modifications")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showEditDialog = false }) {
                        Text("Fermer")
                    }
                }
            )
        }
    }

    if (viewingUser != null) {
        AlertDialog(
            onDismissRequest = { viewingUser = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Visibility, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Détails d'Utilisateur", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(text = "Nom complet : ${viewingUser!!.prenom} ${viewingUser!!.nom}", fontWeight = FontWeight.Bold)
                    Text(text = "Adresse E-mail : ${viewingUser!!.email}")
                    Text(text = "Numéro de téléphone : ${viewingUser!!.phone}")
                    Text(text = "Mot de passe d'accès : ${viewingUser!!.password}")
                    Text(text = "Rôle assigné : ${if (viewingUser!!.isAgent) "Agent / Administrateur" else "Voyageur Standard"}", fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(onClick = { viewingUser = null }) {
                    Text("Fermer")
                }
            }
        )
    }

    if (editingUser != null) {
        AlertDialog(
            onDismissRequest = { editingUser = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Modifier le Compte", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var uName by remember { mutableStateOf(editingUser!!.nom) }
                    var uPrenom by remember { mutableStateOf(editingUser!!.prenom) }
                    var uEmail by remember { mutableStateOf(editingUser!!.email) }
                    var uPhone by remember { mutableStateOf(editingUser!!.phone) }
                    var uPassword by remember { mutableStateOf(editingUser!!.password) }
                    var uIsAgent by remember { mutableStateOf(editingUser!!.isAgent) }

                    OutlinedTextField(
                        value = uPrenom,
                        onValueChange = { uPrenom = it },
                        label = { Text("Prénom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uName,
                        onValueChange = { uName = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uEmail,
                        onValueChange = { uEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uPhone,
                        onValueChange = { uPhone = it },
                        label = { Text("Téléphone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uPassword,
                        onValueChange = { uPassword = it },
                        label = { Text("Mot de passe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(checked = uIsAgent, onCheckedChange = { uIsAgent = it })
                        Text("Accorder les droits d'Agent / Admin", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (uName.isNotBlank() && uPrenom.isNotBlank() && uEmail.isNotBlank()) {
                                viewModel.updateUserAdmin(
                                    editingUser!!.copy(
                                        nom = uName,
                                        prenom = uPrenom,
                                        email = uEmail,
                                        phone = uPhone,
                                        password = uPassword,
                                        isAgent = uIsAgent
                                    )
                                )
                                editingUser = null
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Enregistrer les modifications")
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (deletingUser != null) {
        AlertDialog(
            onDismissRequest = { deletingUser = null },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                    Text("Suppression Définitive", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Text("Voulez-vous vraiment supprimer définitivement le compte de ${deletingUser!!.prenom} ${deletingUser!!.nom} ? Cette action supprimera également ses données de synchronisation Firebase.", fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUserAdmin(deletingUser!!.id)
                        deletingUser = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer définitivement", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingUser = null }) {
                    Text("Annuler")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Title
        item {
            Text(
                text = "Espace Administration",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Gérer les profils utilisateurs, droits d'accès et paramètres régionaux",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Sub-tabs segment
        item {
            TabRow(
                selectedTabIndex = activeTab,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Profil & Sécurité", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Accords & Comptes (${allUsers.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    icon = { Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }
        }

        if (activeTab == 0) {
            // PROFIL & SECURITE TAB
            item {
                currentUser?.let { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (user.prenom.firstOrNull()?.toString() ?: "A").uppercase() + (user.nom.firstOrNull()?.toString() ?: "D").uppercase(),
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "SUPER-ADMINISTRATEUR",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "${user.prenom} ${user.nom}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "Téléphone : ${user.phone}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Button(
                                    onClick = { showEditDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier.padding(top = 4.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Modifier mes informations", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Stats indicators
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Statistiques de l'Infrastructure",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.DirectionsBus, null, tint = MaterialTheme.colorScheme.primary)
                                Text("${allTrips.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Trajets", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.ConfirmationNumber, null, tint = MaterialTheme.colorScheme.secondary)
                                Text("${allBookings.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Billets", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.Business, null, tint = MaterialTheme.colorScheme.tertiary)
                                Text("${allAgencies.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("Agences", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            }

            // ACCESS RIGHTS SEGMENT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Security, null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Droits d'accès & Privilèges système",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "Garantie de rôle et répartition des droits au sein de l'application Gabon Voyage :",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Role: Agent/Admin privileges
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Shield, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Text("Rôle : AGENT / ADMINISTRATEUR (Accès Supérieur)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            PermissionItem(title = "Validation manuelle des paiements Airtel & Moov", allowed = true)
                            PermissionItem(title = "Création, modification et suppression de trajets", allowed = true)
                            PermissionItem(title = "Gestion de la liste des utilisateurs et attributions de rôles", allowed = true)
                            PermissionItem(title = "Accès au Tableau de Bord et indicateurs financiers", allowed = true)
                        }

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                        // Role: Standard User privileges
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                                Text("Rôle : VOYAGEUR / CLIENT (Accès Standard)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            PermissionItem(title = "Visualisation des trajets disponibles du Grand Nord", allowed = true)
                            PermissionItem(title = "Formulaire de réservation et envoi des pièces justificatives", allowed = true)
                            PermissionItem(title = "Consultation de l'historique des billets électroniques validés", allowed = true)
                            PermissionItem(title = "Accès au tableau de bord d'administration et d'indicateurs financiers", allowed = false)
                        }
                    }
                }
            }

            // AI ANALYTICS ASSISTANT SEGMENT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.tertiary)
                            Text(
                                text = "Assistant Analytique IA • Gemini",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }

                        Text(
                            text = "Générez instantanément des analyses commerciales et des recommandations basées sur l'activité réelle de Gabon Voyage.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                        )

                        if (aiAnalysisLoading) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.tertiary)
                                Text("L'IA examine la base de données de l'application...", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.generateAiAnalyticsReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Insights, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Lancer l'Analyse Commerciale IA", fontWeight = FontWeight.Bold)
                            }
                        }

                        aiAnalysisError?.let { err ->
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Erreur: $err",
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }

                        aiAnalysisResult?.let { report ->
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Rapport Généré", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        IconButton(
                                            onClick = {
                                                viewModel.generateAiAnalyticsReport()
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    SelectionContainer {
                                        Text(
                                            text = report,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // AUDIT & LOGS SEGMENT
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "Audit & Journal d'Événements",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Text(
                            text = "Historique d'activité de l'infrastructure Gabon Voyage :",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            AuditLogItem(time = "14:15", user = "Martinien M.", action = "Modification des tarifs de trajet de la compagnie")
                            AuditLogItem(time = "11:30", user = "Système", action = "Synchronisation automatique de la base locale avec Firebase")
                            AuditLogItem(time = "Hier", user = "Martinien M.", action = "Élévation de compte voyageur au rôle d'Agent")
                            AuditLogItem(time = "Hier", user = "Martinien M.", action = "Validation manuelle de 3 paiements mobiles Airtel Money")
                        }
                    }
                }
            }

            // TECHNICAL FICHE SEGMENT
            item {
                val context = androidx.compose.ui.platform.LocalContext.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Fiche Technique de l'Application",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }

                        Text(
                            text = "Téléchargez le guide complet des spécifications techniques de l'application Gabon Voyage (architecture MVVM, stockage local Room SQLite, intégrations Firebase temps réel et IA Gemini) au format PDF.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                        )

                        Button(
                            onClick = {
                                TechnicalGuidePdfHelper.generateTechnicalGuidePdf(context)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Télécharger la Fiche Technique (PDF)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

        } else {
            // USER GESTION TAB
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Comptes Utilisateurs Registre",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Ajustez les privilèges ou élevez des voyageurs au rôle d'Agent",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showCreateAgentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ajouter", fontSize = 11.sp)
                    }
                }
            }

            // Search bar & Role Filters
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchUserQuery,
                            onValueChange = { searchUserQuery = it },
                            placeholder = { Text("Rechercher un nom, email, téléphone...", fontSize = 13.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = {
                                if (searchUserQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchUserQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Filtre : ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            listOf("Tous", "Agents", "Voyageurs").forEach { filter ->
                                val selected = roleFilter == filter
                                FilterChip(
                                    selected = selected,
                                    onClick = { roleFilter = filter },
                                    label = { Text(filter, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            val filteredUsers = allUsers.filter { user ->
                val matchesSearch = user.prenom.contains(searchUserQuery, ignoreCase = true) ||
                        user.nom.contains(searchUserQuery, ignoreCase = true) ||
                        user.email.contains(searchUserQuery, ignoreCase = true) ||
                        user.phone.contains(searchUserQuery, ignoreCase = true)
                val matchesRole = when (roleFilter) {
                    "Agents" -> user.isAgent
                    "Voyageurs" -> !user.isAgent
                    else -> true
                }
                matchesSearch && matchesRole
            }

            if (filteredUsers.isEmpty()) {
                item {
                    Text(
                        text = "Aucun utilisateur ne correspond aux critères de recherche.",
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(filteredUsers) { user ->
                    UserManagementCard(
                        user = user,
                        onView = { viewingUser = user },
                        onEdit = { editingUser = user },
                        onDelete = { deletingUser = user },
                        onRoleChange = { isAgent ->
                            viewModel.changeUserRole(user.id, isAgent)
                        }
                    )
                }
            }
        }
    }

    if (showCreateAgentDialog) {
        AlertDialog(
            onDismissRequest = { showCreateAgentDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Créer un Compte Agent / Admin", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    var cPrenom by remember { mutableStateOf("") }
                    var cNom by remember { mutableStateOf("") }
                    var cEmail by remember { mutableStateOf("") }
                    var cPhone by remember { mutableStateOf("") }
                    var cPassword by remember { mutableStateOf("") }
                    var cIsAgent by remember { mutableStateOf(true) }

                    OutlinedTextField(
                        value = cPrenom,
                        onValueChange = { cPrenom = it },
                        label = { Text("Prénom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cNom,
                        onValueChange = { cNom = it },
                        label = { Text("Nom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cEmail,
                        onValueChange = { cEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cPhone,
                        onValueChange = { cPhone = it },
                        label = { Text("Téléphone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cPassword,
                        onValueChange = { cPassword = it },
                        label = { Text("Mot de passe") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(checked = cIsAgent, onCheckedChange = { cIsAgent = it })
                        Text("Accorder les droits d'Agent dès l'inscription", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            if (cPrenom.isNotBlank() && cNom.isNotBlank() && cEmail.isNotBlank() && cPhone.isNotBlank() && cPassword.isNotBlank()) {
                                viewModel.register(cNom, cPrenom, cEmail, cPhone, cPassword, cIsAgent)
                                showCreateAgentDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Enregistrer le nouveau compte")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showCreateAgentDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun AuditLogItem(time: String, user: String, action: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = time,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = action,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Par : $user",
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    allowed: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = if (allowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = if (allowed) Icons.Default.CheckCircle else Icons.Default.Cancel,
            contentDescription = null,
            tint = if (allowed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun UserManagementCard(
    user: User,
    onView: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRoleChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${user.prenom} ${user.nom}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = user.email,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "Tél : ${user.phone}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = if (user.isAgent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (user.isAgent) "AGENT / ADMIN" else "VOYAGEUR",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (user.isAgent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // View button
                IconButton(
                    onClick = onView,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Voir l'utilisateur",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Edit button
                IconButton(
                    onClick = onEdit,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Modifier",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Delete button
                IconButton(
                    onClick = onDelete,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = if (user.isAgent) "Rétrograder" else "Promouvoir",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (user.isAgent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Switch(
                    checked = user.isAgent,
                    onCheckedChange = onRoleChange,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
