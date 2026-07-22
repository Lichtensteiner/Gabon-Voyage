package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.User
import com.example.ui.viewmodel.VoyageViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: VoyageViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()

    // Screen-level state for selected agent-client conversation
    var selectedClientForAgent by remember { mutableStateOf<User?>(null) }
    var userSearchQuery by remember { mutableStateOf("") }

    val isAgent = currentUser?.isAgent == true

    if (isAgent) {
        // Agent UI layout: Thread Inbox vs Live Thread View
        AnimatedContent(
            targetState = selectedClientForAgent,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut())
                    .using(SizeTransform(clip = false))
            },
            label = "AgentChatNavigation"
        ) { targetClient ->
            if (targetClient == null) {
                // Thread Directory inbox
                AgentInboxScreen(
                    allUsers = allUsers,
                    chatMessages = chatMessages,
                    currentUser = currentUser,
                    searchQuery = userSearchQuery,
                    onSearchQueryChange = { userSearchQuery = it },
                    onSelectClient = { selectedClientForAgent = it },
                    modifier = modifier
                )
            } else {
                // Live Chat window with selected client
                val clientMessages = chatMessages.filter { it.threadId == targetClient.id.toString() }
                LiveConversationView(
                    viewModel = viewModel,
                    chatTitle = "${targetClient.prenom} ${targetClient.nom}",
                    chatSubtitle = targetClient.email,
                    threadId = targetClient.id.toString(),
                    messagesList = clientMessages,
                    currentUser = currentUser,
                    onBackClick = { selectedClientForAgent = null },
                    modifier = modifier
                )
            }
        }
    } else {
        // Traveler UI layout: Direct stream to Support Team
        currentUser?.let { traveler ->
            val myMessages = chatMessages.filter { it.threadId == traveler.id.toString() }
            LiveConversationView(
                viewModel = viewModel,
                chatTitle = "Assistance Voyageurs Gabon",
                chatSubtitle = "Agents d'assistance prêts en ligne",
                threadId = traveler.id.toString(),
                messagesList = myMessages,
                currentUser = currentUser,
                showQuickChips = true,
                onBackClick = null,
                modifier = modifier
            )
        }
    }
}

/**
 * Inbox Directory Screen for Agent / Administrators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentInboxScreen(
    allUsers: List<User>,
    chatMessages: List<ChatMessage>,
    currentUser: User?,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectClient: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine active threads (users who wrote messages or registered users)
    val standardTravelers = allUsers.filter { !it.isAgent && it.id != currentUser?.id }
    
    // Group messages by threadId to find active chats
    val messagesByThread = chatMessages.groupBy { it.threadId }
    
    // Filter travelers by search query (name, email, or phone)
    val filteredTravelers = standardTravelers.filter { traveler ->
        searchQuery.isBlank() ||
                traveler.nom.contains(searchQuery, ignoreCase = true) ||
                traveler.prenom.contains(searchQuery, ignoreCase = true) ||
                traveler.email.contains(searchQuery, ignoreCase = true) ||
                traveler.phone.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Navigation Header
        Surface(
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Forum,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Messagerie Voyageurs (Admins)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                
                Text(
                    text = "Consultez, répondez et guidez les voyageurs gabonais en ligne en temps réel.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // Search bar to lookup clients and start conversations
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    placeholder = { Text("Rechercher un voyageur (nom, e-mail...)", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }

        // Travelers threads list
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (filteredTravelers.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonSearch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Aucun voyageur trouvé",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "Ajustez vos filtres ou vérifiez l'orthographe du nom ou e-mail.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outlineVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                items(filteredTravelers) { traveler ->
                    val travelerThreadMessages = messagesByThread[traveler.id.toString()] ?: emptyList()
                    val lastMessage = travelerThreadMessages.maxByOrNull { it.timestamp }
                    
                    val hasMessages = travelerThreadMessages.isNotEmpty()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectClient(traveler) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasMessages) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Initial Avatar
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (hasMessages) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (traveler.prenom.isNotEmpty()) traveler.prenom.first().uppercase() else "V",
                                    color = if (hasMessages) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Info details safely structured to prevent overflow
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${traveler.prenom} ${traveler.nom}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    
                                    if (lastMessage != null) {
                                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                                        Text(
                                            text = sdf.format(Date(lastMessage.timestamp)),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = lastMessage?.content ?: "Aucun message échangé pour le moment",
                                    fontSize = 12.sp,
                                    color = if (lastMessage != null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                if (traveler.phone.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Tél: ${traveler.phone}",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Navigate arrow indicator
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Shared Real-time Chat View Component (WhatsApp Style with 3-Dots Options)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveConversationView(
    viewModel: VoyageViewModel,
    chatTitle: String,
    chatSubtitle: String,
    threadId: String,
    messagesList: List<ChatMessage>,
    currentUser: User?,
    showQuickChips: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val isAiAutoReply by viewModel.isAiAutoReplyEnabled.collectAsState()
    val isAiTyping by viewModel.isAiTyping.collectAsState()
    val isCurrentlyTyping = isAiTyping == threadId

    // Keep scrolling to latest entry
    LaunchedEffect(messagesList.size, isCurrentlyTyping) {
        if (messagesList.isNotEmpty()) {
            listState.animateScrollToItem(messagesList.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFEFEAE2)) // Authentic WhatsApp chat background color (beige)
    ) {
        // App bar top header (WhatsApp styled)
        Surface(
            color = Color(0xFF005C4B), // WhatsApp Deep Green
            contentColor = Color.White,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onBackClick != null) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retourner",
                            tint = Color.White
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Header avatar icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF128C7E)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (onBackClick != null) Icons.Default.Person else Icons.Default.SupportAgent,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Headline text
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chatTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (isCurrentlyTyping) {
                        Text(
                            text = "Conseiller IA écrit...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF25D366), // Bright green for typing indicator
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(Color(0xFF25D366), CircleShape)
                            )
                            Text(
                                text = "En ligne",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 3-Dots Dropdown Menu
                var showDropdownMenu by remember { mutableStateOf(false) }

                Box {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Vider la discussion", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(20.dp)) },
                            onClick = {
                                showDropdownMenu = false
                                viewModel.clearChatMessages(threadId)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Demander à l'IA d'y répondre", fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary) },
                            onClick = {
                                showDropdownMenu = false
                                viewModel.triggerManualAiReply(threadId, messagesList)
                            }
                        )
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = if (isAiAutoReply) "Désactiver l'IA Auto" else "Activer l'IA Auto",
                                    fontSize = 14.sp
                                ) 
                            },
                            leadingIcon = { 
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = if (isAiAutoReply) Color(0xFF00A884) else Color.Gray
                                ) 
                            },
                            onClick = {
                                showDropdownMenu = false
                                viewModel.toggleAiAutoReply()
                            }
                        )
                    }
                }
            }
        }

        // Chat stream thread content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            if (messagesList.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = null,
                        tint = Color.Gray.copy(alpha = 0.7f),
                        modifier = Modifier.size(44.dp)
                    )
                    Text(
                        text = "Aucun message",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Envoyez un message pour commencer l'assistance en ligne Gabon Voyage !",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(horizontal = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 12.dp)
                ) {
                    items(messagesList) { message ->
                        val isMe = currentUser != null && message.senderId == currentUser.id
                        
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val formattedTime = try {
                            sdf.format(Date(message.timestamp))
                        } catch (e: Exception) {
                            ""
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                        ) {
                            // Sender identity header for incoming messages
                            if (!isMe) {
                                Text(
                                    text = if (message.senderId == -999) "Conseiller IA • Gabon Voyage" else message.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (message.senderId == -999) Color(0xFF008069) else Color(0xFFE91E63),
                                    modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
                                )
                            }

                            // WhatsApp styled bubble card
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isMe) 12.dp else 2.dp,
                                    bottomEnd = if (isMe) 2.dp else 12.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) {
                                        Color(0xFFD9FDD3) // Official WhatsApp Light Green sent bubble
                                    } else {
                                        Color.White // Official WhatsApp White received bubble
                                    }
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.widthIn(max = 290.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                                    Text(
                                        text = message.content,
                                        fontSize = 14.sp,
                                        color = Color(0xFF111B21) // Off-black WhatsApp text color
                                    )
                                    
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.End)
                                            .padding(top = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = formattedTime,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Normal,
                                            color = Color.Gray
                                        )
                                        if (isMe) {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = "Lu",
                                                tint = Color(0xFF53BDEB), // Blue double check ticks
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isCurrentlyTyping) {
                        item {
                            Row(
                                modifier = Modifier.padding(start = 6.dp, top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF128C7E).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SmartToy,
                                        contentDescription = null,
                                        tint = Color(0xFF005C4B),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Card(
                                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp, bottomStart = 2.dp, bottomEnd = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Le conseiller IA tape",
                                            fontSize = 12.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Medium
                                        )
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            strokeWidth = 1.5.dp,
                                            color = Color(0xFF005C4B)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick helper choice chips (Traveler support shortcuts)
        if (showQuickChips) {
            val chipsList = listOf(
                "Où est mon billet ?",
                "Comment payer sur l'app ?",
                "Départs de Libreville",
                "Annuler mon voyage"
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                chipsList.forEach { prompt ->
                    AssistChip(
                        onClick = {
                            messageText = prompt
                        },
                        label = { Text(prompt, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = Color.White,
                            labelColor = Color(0xFF005C4B)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Message input dock panel (WhatsApp Style with circular floating send button)
        Surface(
            color = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Input card
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier.weight(1f),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Emojis",
                            tint = Color(0xFF8696A0),
                            modifier = Modifier
                                .size(24.dp)
                                .clickable { /* Emoji Picker trigger */ }
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Box(modifier = Modifier.weight(1f)) {
                            if (messageText.isEmpty()) {
                                Text(
                                    text = "Message",
                                    fontSize = 15.sp,
                                    color = Color(0xFF8696A0)
                                )
                            }
                            BasicTextField(
                                value = messageText,
                                onValueChange = { messageText = it },
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontSize = 15.sp,
                                    color = Color(0xFF111B21)
                                ),
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Send,
                                    keyboardType = KeyboardType.Text
                                ),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        if (messageText.isNotBlank()) {
                                            viewModel.sendChatMessage(messageText, threadId)
                                            messageText = ""
                                            keyboardController?.hide()
                                        }
                                    }
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))

                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Fichier",
                            tint = Color(0xFF8696A0),
                            modifier = Modifier
                                .size(22.dp)
                                .clickable { /* File upload option */ }
                        )
                    }
                }
                
                // Green floating action circular send/mic button
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00A884)) // WhatsApp vibrant Green Accent
                        .clickable {
                            if (messageText.isNotBlank()) {
                                viewModel.sendChatMessage(messageText, threadId)
                                messageText = ""
                                keyboardController?.hide()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (messageText.isBlank()) Icons.Default.Mic else Icons.Default.Send,
                        contentDescription = "Action",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
