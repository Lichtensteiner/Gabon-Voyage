package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VoyageViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: VoyageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()
            MyApplicationTheme(darkTheme = isDarkMode, dynamicColor = false) {
                MainAppEntry(viewModel)
            }
        }
    }
}

enum class NavigationFlow {
    WELCOME, LOGIN, REGISTER, MAIN_VOTER, MAIN_AGENT
}

enum class TravelerScreen {
    SEARCH, PAYMENT, HISTORY, MESSAGING, PROFILE
}

enum class AgentScreen {
    DASHBOARD, VALIDATION, TRIPS, PROFILE, MESSAGING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppEntry(viewModel: VoyageViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var activeFlow by remember { mutableStateOf(NavigationFlow.WELCOME) }
    var travelerScreen by remember { mutableStateOf(TravelerScreen.SEARCH) }
    var agentScreen by remember { mutableStateOf(AgentScreen.DASHBOARD) }

    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showCongratsDialog by remember { mutableStateOf(false) }

    // Intercept physical or system back press
    BackHandler(enabled = activeFlow != NavigationFlow.WELCOME) {
        when (activeFlow) {
            NavigationFlow.LOGIN -> activeFlow = NavigationFlow.WELCOME
            NavigationFlow.REGISTER -> activeFlow = NavigationFlow.WELCOME
            NavigationFlow.MAIN_VOTER -> {
                if (travelerScreen != TravelerScreen.SEARCH) {
                    travelerScreen = TravelerScreen.SEARCH
                }
            }
            NavigationFlow.MAIN_AGENT -> {
                if (agentScreen != AgentScreen.DASHBOARD) {
                    agentScreen = AgentScreen.DASHBOARD
                }
            }
            else -> {}
        }
    }

    // Sync session state changes
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            val fromRegister = activeFlow == NavigationFlow.REGISTER
            activeFlow = if (currentUser!!.isAgent) NavigationFlow.MAIN_AGENT else NavigationFlow.MAIN_VOTER
            if (fromRegister) {
                showCongratsDialog = true
            }
        } else {
            // Check if they were previously logged-in, and redirect to Welcome page on logout
            if (activeFlow == NavigationFlow.MAIN_VOTER || activeFlow == NavigationFlow.MAIN_AGENT) {
                activeFlow = NavigationFlow.WELCOME
            }
        }
    }

    if (showCongratsDialog) {
        AlertDialog(
            onDismissRequest = { showCongratsDialog = false },
            title = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFFE8F5E9), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎉", fontSize = 32.sp)
                    }
                    Text(
                        "Félicitations !",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            },
            text = {
                Text(
                    text = "Votre compte a été créé avec succès sur Gabon Voyage !\n\nVous êtes à présent connecté de manière sécurisée et vous pouvez commencer à rechercher des trajets vers Bitam, Oyem, Libreville, etc.",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { showCongratsDialog = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Commencer l'expérience", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
                    Text("Confirmer la déconnexion", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Voulez-vous vraiment vous déconnecter de Gabon Voyage ? Vous devrez ressaisir vos identifiants pour accéder à vos billets.", fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Se déconnecter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.NotificationsActive, null, tint = MaterialTheme.colorScheme.primary)
                    Text("Pushes & Alertes reçues", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (notifications.isEmpty()) {
                        Text(
                            text = "Aucune alerte reçue.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Loop through notifications with clear/dismiss support
                        notifications.forEachIndexed { index, message ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = message,
                                        fontSize = 11.sp,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeNotification(index) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Supprimer l'alerte",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotificationsDialog = false }) {
                    Text("Fermer")
                }
            },
            dismissButton = {
                if (notifications.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearAllNotifications() }) {
                        Text("Tout effacer", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentUser != null) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = if (isConnected) Icons.Default.CloudQueue else Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = if (isConnected) Color(0xFF00E676) else Color(0xFFFF8A80),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (currentUser!!.isAgent) "Espace Agent" else "Voyages Gabon",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                // Real-time pulse dot (Lanterne de réseau verte vibrante)
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(
                                            if (isConnected) Color(0xFF00E676) else Color(0xFFF44336),
                                            CircleShape
                                        )
                                )
                            }
                            Text(
                                text = if (isConnected) "Temps Réel Actif • Bonjour, ${currentUser!!.prenom}" else "Mode Hors-ligne • Bonjour, ${currentUser!!.prenom}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        navigationIconContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    navigationIcon = {
                        // Quick Profile Avatar Icon Badge
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .size(34.dp)
                                .background(MaterialTheme.colorScheme.secondary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser!!.prenom.first().toString().uppercase() + currentUser!!.nom.first().toString().uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary,
                                fontSize = 12.sp
                            )
                        }
                    },
                    actions = {
                        // Theme mode switch (Dark/Light)
                        IconButton(onClick = { viewModel.toggleDarkMode() }) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Mode Sombre/Clair",
                                tint = Color.White
                            )
                        }

                        // Notifications push badge icon
                        IconButton(onClick = { showNotificationsDialog = true }) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Text("${notifications.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Simulated Push Logs",
                                    tint = Color.White
                                )
                            }
                        }

                        // Logout button
                        IconButton(onClick = { showLogoutDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Logout,
                                contentDescription = "Déconnexion",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (currentUser != null) {
                NavigationBar {
                    if (currentUser!!.isAgent) {
                        // Agent Specific Bottom Tabs (No labels/titles shown, only icons for Super Admin)
                        NavigationBarItem(
                            selected = agentScreen == AgentScreen.DASHBOARD,
                            onClick = { agentScreen = AgentScreen.DASHBOARD },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = "Indicateurs") }
                        )
                        NavigationBarItem(
                            selected = agentScreen == AgentScreen.VALIDATION,
                            onClick = { agentScreen = AgentScreen.VALIDATION },
                            icon = { Icon(Icons.Default.PendingActions, contentDescription = "Paiements") }
                        )
                        NavigationBarItem(
                            selected = agentScreen == AgentScreen.TRIPS,
                            onClick = { agentScreen = AgentScreen.TRIPS },
                            icon = { Icon(Icons.Default.Settings, contentDescription = "Trajets") }
                        )
                        NavigationBarItem(
                            selected = agentScreen == AgentScreen.MESSAGING,
                            onClick = { agentScreen = AgentScreen.MESSAGING },
                            icon = { Icon(Icons.Default.Forum, contentDescription = "Messagerie") }
                        )
                        NavigationBarItem(
                            selected = agentScreen == AgentScreen.PROFILE,
                            onClick = { agentScreen = AgentScreen.PROFILE },
                            icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Droits & Users") }
                        )
                    } else {
                        // Traveler Specific Bottom Tabs
                        NavigationBarItem(
                            selected = travelerScreen == TravelerScreen.SEARCH,
                            onClick = { travelerScreen = TravelerScreen.SEARCH },
                            icon = { Icon(Icons.Default.Search, contentDescription = null) },
                            label = { Text("Recherche", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = travelerScreen == TravelerScreen.PAYMENT,
                            onClick = { travelerScreen = TravelerScreen.PAYMENT },
                            icon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            label = { Text("Paiement", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = travelerScreen == TravelerScreen.HISTORY,
                            onClick = { travelerScreen = TravelerScreen.HISTORY },
                            icon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                            label = { Text("Billets", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = travelerScreen == TravelerScreen.MESSAGING,
                            onClick = { travelerScreen = TravelerScreen.MESSAGING },
                            icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                            label = { Text("Messagerie", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        NavigationBarItem(
                            selected = travelerScreen == TravelerScreen.PROFILE,
                            onClick = { travelerScreen = TravelerScreen.PROFILE },
                            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                            label = { Text("Profil", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ConnectionStatusBanner(isConnected = isConnected)

            Box(
                modifier = Modifier.weight(1f)
            ) {
                AnimatedContent(
                    targetState = activeFlow,
                    transitionSpec = {
                        fadeIn() togetherWith fadeOut()
                    },
                    modifier = Modifier.fillMaxSize()
                ) { flow ->
                    when (flow) {
                        NavigationFlow.WELCOME -> {
                            WelcomeScreen(
                                viewModel = viewModel,
                                onNavigateToLogin = { activeFlow = NavigationFlow.LOGIN },
                                onNavigateToRegister = { activeFlow = NavigationFlow.REGISTER }
                            )
                        }

                        NavigationFlow.LOGIN -> {
                            LoginScreen(
                                viewModel = viewModel,
                                onNavigateToRegister = { activeFlow = NavigationFlow.REGISTER },
                                onNavigateToHome = { activeFlow = NavigationFlow.WELCOME }
                            )
                        }

                        NavigationFlow.REGISTER -> {
                            RegisterScreen(
                                viewModel = viewModel,
                                onNavigateToLogin = { activeFlow = NavigationFlow.LOGIN },
                                onNavigateToHome = { activeFlow = NavigationFlow.WELCOME }
                            )
                        }

                        NavigationFlow.MAIN_VOTER -> {
                            // Render current traveler sub-screen
                            when (travelerScreen) {
                                TravelerScreen.SEARCH -> SearchScreen(
                                    viewModel = viewModel,
                                    onNavigateToPayment = { travelerScreen = TravelerScreen.PAYMENT }
                                )

                                TravelerScreen.PAYMENT -> PaymentScreen(
                                    viewModel = viewModel,
                                    onNavigateToHistory = { travelerScreen = TravelerScreen.HISTORY }
                                )

                                TravelerScreen.HISTORY -> HistoryScreen(
                                    viewModel = viewModel
                                )

                                TravelerScreen.MESSAGING -> ChatScreen(
                                    viewModel = viewModel
                                )

                                TravelerScreen.PROFILE -> TravelerProfileScreen(
                                    viewModel = viewModel
                                )
                            }
                        }

                        NavigationFlow.MAIN_AGENT -> {
                            // Render current agent sub-screen
                            when (agentScreen) {
                                AgentScreen.DASHBOARD -> AdminDashboardScreen(
                                    viewModel = viewModel
                                )

                                AgentScreen.VALIDATION -> AdminValidationScreen(
                                    viewModel = viewModel
                                )

                                AgentScreen.TRIPS -> AdminTripsScreen(
                                    viewModel = viewModel
                                )

                                AgentScreen.PROFILE -> AdminProfileScreen(
                                    viewModel = viewModel
                                )

                                AgentScreen.MESSAGING -> ChatScreen(
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ConnectionStatusBanner(isConnected: Boolean) {
    var showConnectedNotification by remember { mutableStateOf(false) }
    var previousConnectionState by remember { mutableStateOf(isConnected) }

    // Detect reconnection to briefly show a green banner
    LaunchedEffect(isConnected) {
        if (isConnected && !previousConnectionState) {
            showConnectedNotification = true
            kotlinx.coroutines.delay(3000)
            showConnectedNotification = false
        }
        previousConnectionState = isConnected
    }

    AnimatedVisibility(
        visible = isConnected && showConnectedNotification,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut()
    ) {
        val bgColor = Color(0xFFE8F5E9)
        val contentColor = Color(0xFF2E7D32)
        val icon = Icons.Default.CloudQueue
        val text = "Connexion rétablie • Vos réservations se synchronisent."

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgColor)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}
