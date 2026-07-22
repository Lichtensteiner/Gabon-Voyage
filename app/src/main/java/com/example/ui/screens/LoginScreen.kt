package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.VoyageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: VoyageViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateFlowOf("") }
    var password by remember { mutableStateFlowOf("") }
    var isPasswordVisible by remember { mutableStateFlowOf(false) }
    
    // Custom phone login states
    var phoneLogin by remember { mutableStateOf("") }
    var passwordLogin by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("email") } // "email" or "phone"

    var showGoogleDialog by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        // Floating Theme Switch Button at Top End
        val isDarkMode by viewModel.isDarkMode.collectAsState()
        IconButton(
            onClick = { viewModel.toggleDarkMode() },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), CircleShape)
        ) {
            Icon(
                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Mode Sombre/Clair",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // Back to Presentation Home Button
        IconButton(
            onClick = onNavigateToHome,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .statusBarsPadding()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Retour à l'accueil",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .widthIn(max = 450.dp)
                .padding(horizontal = 24.dp, vertical = 56.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High quality app brand logo replacing the directions bus icon
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.img_app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(22.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Gabon Voyage",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Réservez vos voyages terrestres vers le Grand Nord du Gabon",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Connexion",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Error Alert
                    AnimatedVisibility(visible = authError != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = authError ?: "",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Native Selector Tabs for connection methods (E-mail / Téléphone)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { selectedTab = "email" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == "email") MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selectedTab == "email") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("E-mail", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { selectedTab = "phone" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selectedTab == "phone") MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selectedTab == "phone") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Téléphone", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (selectedTab == "email") {
                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("E-mail") },
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = "Email Icon")
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("username_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { viewModel.login(email, password) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "Se Connecter par Email",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        // Phone login fields
                        OutlinedTextField(
                            value = phoneLogin,
                            onValueChange = { phoneLogin = it },
                            label = { Text("Numéro de Téléphone (ex: 077000000)") },
                            leadingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 8.dp)
                                ) {
                                    Text("🇬🇦", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+241", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.outline)
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_username_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Phone login Password field
                        OutlinedTextField(
                            value = passwordLogin,
                            onValueChange = { passwordLogin = it },
                            label = { Text("Mot de passe") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = "Password Icon")
                            },
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle Visibility"
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_password_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { viewModel.loginWithPhone(phoneLogin, passwordLogin) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("phone_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                text = "Se Connecter par Téléphone",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }

                    // Divider segment
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                        Text(
                            text = "ou connecter de façon rapide via",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    }

                    if (showGoogleDialog) {
                        AlertDialog(
                            onDismissRequest = { showGoogleDialog = false },
                            title = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                        Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                        Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                        Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                        Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                        Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 26.sp)
                                    }
                                    Text("Connexion Sécurisée", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                }
                            },
                            text = {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "Choisissez un compte Google pour vous connecter à l'application Gabon Voyage :",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    // Option 1
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        onClick = {
                                            showGoogleDialog = false
                                            viewModel.loginWithGoogle("martinienmvezogo@gmail.com", "Martinien Mve Zogo")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFFE8F5E9), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("M", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Column {
                                                Text("Martinien Mve Zogo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text("martinienmvezogo@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }

                                    // Option 2
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                        onClick = {
                                            showGoogleDialog = false
                                            viewModel.loginWithGoogle("ludovic.developer@gmail.com", "Ludovic Martinien")
                                        }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .background(Color(0xFFE3F2FD), CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("L", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            }
                                            Column {
                                                Text("M. Mve Zogo Ludovic Martinien", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                Text("ludovic.developer@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }

                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showGoogleDialog = false }) {
                                    Text("Annuler")
                                }
                            }
                        )
                    }

                    // Google Login Button
                    OutlinedButton(
                        onClick = { showGoogleDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("google_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Text("G", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("o", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("o", color = Color(0xFFFBBC05), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("g", color = Color(0xFF4285F4), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("l", color = Color(0xFF34A853), fontWeight = FontWeight.Black, fontSize = 15.sp)
                                Text("e", color = Color(0xFFEA4335), fontWeight = FontWeight.Black, fontSize = 15.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Continuer avec Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pas encore de compte ? ",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        text = "Créer un compte",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

// Helper to handle flow updates smoothly inside compose
private fun <T> mutableStateFlowOf(value: T) = mutableStateOf(value)
