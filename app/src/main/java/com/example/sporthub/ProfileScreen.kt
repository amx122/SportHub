package com.example.sporthub

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

enum class ProfileSubScreen {
    MAIN, SETTINGS_MENU, EDIT_PROFILE, SECURITY, SAVED_MATCHES, LANGUAGE, THEME, HELP, ABOUT
}

@Composable
fun ProfileScreen(onLoginClick: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var currentScreen by remember { mutableStateOf(ProfileSubScreen.MAIN) }

    BackHandler(enabled = currentScreen != ProfileSubScreen.MAIN) {
        currentScreen = if (currentScreen == ProfileSubScreen.SETTINGS_MENU) ProfileSubScreen.MAIN else ProfileSubScreen.SETTINGS_MENU
    }

    if (currentUser == null) {
        GuestProfileView(onLoginClick)
    } else {
        AnimatedContent(
            targetState = currentScreen,
            label = "profile_nav",
            transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } }
        ) { screen ->
            when (screen) {
                ProfileSubScreen.MAIN -> UserProfileMain(currentUser) { currentScreen = it }
                ProfileSubScreen.SETTINGS_MENU -> SettingsMenuScreen({ currentScreen = ProfileSubScreen.MAIN }, { currentScreen = it }, { auth.signOut() })
                ProfileSubScreen.EDIT_PROFILE -> EditProfileScreen { currentScreen = ProfileSubScreen.SETTINGS_MENU }
                ProfileSubScreen.SECURITY -> SecurityScreen { currentScreen = ProfileSubScreen.SETTINGS_MENU }
                ProfileSubScreen.SAVED_MATCHES -> SavedMatchesScreen { currentScreen = ProfileSubScreen.MAIN }
                ProfileSubScreen.LANGUAGE -> LanguageScreen { currentScreen = ProfileSubScreen.SETTINGS_MENU }
                ProfileSubScreen.THEME -> ThemeScreen { currentScreen = ProfileSubScreen.SETTINGS_MENU }
                ProfileSubScreen.HELP -> HelpCenterScreen { currentScreen = ProfileSubScreen.SETTINGS_MENU }
                ProfileSubScreen.ABOUT -> AboutAppScreen { currentScreen = ProfileSubScreen.SETTINGS_MENU }
            }
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileMain(user: com.google.firebase.auth.FirebaseUser, onNavigate: (ProfileSubScreen) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    var followers by remember { mutableStateOf(0) }
    var following by remember { mutableStateOf(0) }
    var likes by remember { mutableStateOf(0) }
    var bio by remember { mutableStateOf("...") }
    var photoUri by remember { mutableStateOf<Uri?>(user.photoUrl) } // Аватарка
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }

    LaunchedEffect(user.uid) {
        db.collection("users").document(user.uid).addSnapshotListener { snapshot, _ ->
            if (snapshot != null && snapshot.exists()) {
                followers = snapshot.getLong("followers")?.toInt() ?: 0
                following = snapshot.getLong("following")?.toInt() ?: 0
                likes = snapshot.getLong("likes")?.toInt() ?: 0
                bio = snapshot.getString("bio") ?: "Sport Fan"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold, color = Color.White) },
                actions = {
                    IconButton(onClick = { onNavigate(ProfileSubScreen.SETTINGS_MENU) }) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F))
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color(0xFF1C1C1E))
                        .clickable { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUri != null) {
                        AsyncImage(model = photoUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    } else {
                        Text(user.displayName?.take(1)?.uppercase() ?: "U", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                    Box(modifier = Modifier.align(Alignment.BottomEnd).background(Color.Black, CircleShape).padding(4.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column {
                    Text(user.displayName ?: "User", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(user.email ?: "", color = Color.Gray, fontSize = 14.sp)
                    Text(bio, color = Color(0xFFFF3B30), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp)).padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(followers.toString(), "Followers")
                StatItem(following.toString(), "Following")
                StatItem(likes.toString(), "Likes")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("Dashboard", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            DashboardItem(Icons.Outlined.BookmarkBorder, "Saved Matches", "Your favorites", onClick = { onNavigate(ProfileSubScreen.SAVED_MATCHES) })
            DashboardItem(Icons.Outlined.Edit, "Edit Profile", "Update bio & details", onClick = { onNavigate(ProfileSubScreen.EDIT_PROFILE) })
            DashboardItem(Icons.Outlined.Security, "Security", "Password & Privacy", onClick = { onNavigate(ProfileSubScreen.SECURITY) })
        }
    }
}
@Composable
fun SavedMatchesScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var savedMatches by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).collection("saved_matches").get().addOnSuccessListener { result ->
                savedMatches = result.documents.map { it.data ?: emptyMap() }
                isLoading = false
            }
        }
    }

    Scaffold(topBar = { SimpleTopBar("Saved Matches", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        if (isLoading) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF3B30)) }
        } else if (savedMatches.isEmpty()) {
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.BookmarkBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Text("No saved matches yet", color = Color.Gray, modifier = Modifier.padding(top = 16.dp))
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
                items(savedMatches) { match ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp)).padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(match["league"] as? String ?: "", color = Color.Gray, fontSize = 10.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = match["homeLogo"], contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(match["homeTeam"] as? String ?: "", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AsyncImage(model = match["awayLogo"], contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(match["awayTeam"] as? String ?: "", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(match["score"] as? String ?: "", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
@Composable
fun SettingsMenuScreen(onBack: () -> Unit, onNavigate: (ProfileSubScreen) -> Unit, onLogout: () -> Unit) {
    Scaffold(topBar = { SimpleTopBar("Settings", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
            SettingsSectionTitle("Account")
            SettingsRow(Icons.Outlined.Person, "Personal Information") { onNavigate(ProfileSubScreen.EDIT_PROFILE) }
            SettingsRow(Icons.Outlined.Lock, "Password & Security") { onNavigate(ProfileSubScreen.SECURITY) }
            SettingsRow(Icons.Outlined.Delete, "Delete Account") { /* Logic */ }

            SettingsSectionTitle("Preferences")
            SettingsRow(Icons.Outlined.Language, "Language") { onNavigate(ProfileSubScreen.LANGUAGE) }
            SettingsRow(Icons.Outlined.DarkMode, "Theme") { onNavigate(ProfileSubScreen.THEME) }
            SettingsToggleRow("Push Notifications", true)
            SettingsToggleRow("Sound Effects", false)

            SettingsSectionTitle("Data")
            SettingsRow(Icons.Outlined.CleaningServices, "Clear Cache") { }
            SettingsRow(Icons.Outlined.Download, "Download My Data") { }

            SettingsSectionTitle("Support")
            SettingsRow(Icons.Outlined.Help, "Help Center") { onNavigate(ProfileSubScreen.HELP) }
            SettingsRow(Icons.Outlined.Description, "Terms of Service") { }
            SettingsRow(Icons.Outlined.Info, "About SportPulse") { onNavigate(ProfileSubScreen.ABOUT) }

            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onLogout, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2E)), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = Color(0xFFFF3B30))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Log Out", color = Color(0xFFFF3B30))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Version 1.2.0 (Build 45)", color = Color.DarkGray, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}

@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var bio by remember { mutableStateOf("") }
    val context = LocalContext.current
    Scaffold(topBar = { SimpleTopBar("Edit Profile", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            MyTextField(bio, "Short Bio") { bio = it }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                auth.currentUser?.let { db.collection("users").document(it.uid).set(mapOf("bio" to bio), SetOptions.merge()) }
                Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show()
                onBack()
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))) { Text("Save") }
        }
    }
}

@Composable
fun SecurityScreen(onBack: () -> Unit) {
    var pass by remember { mutableStateOf("") }
    Scaffold(topBar = { SimpleTopBar("Security", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Update Password", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            MyTextField(pass, "New Password", true) { pass = it }
            Button(onClick = { onBack() }, modifier = Modifier.fillMaxWidth().padding(top = 16.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))) { Text("Update") }
        }
    }
}

@Composable
fun LanguageScreen(onBack: () -> Unit) {
    val langs = listOf("English", "Ukrainian", "Polish", "German")
    Scaffold(topBar = { SimpleTopBar("Language", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) { items(langs) { SettingsRow(Icons.Default.Language, it) { } } }
    }
}

@Composable
fun ThemeScreen(onBack: () -> Unit) {
    Scaffold(topBar = { SimpleTopBar("Theme", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) { Text("Dark Mode Always On", color = Color.Gray) }
    }
}

@Composable
fun HelpCenterScreen(onBack: () -> Unit) {
    Scaffold(topBar = { SimpleTopBar("Help Center", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) { Text("Contact support@sportpulse.com", color = Color.Gray) }
    }
}

@Composable
fun AboutAppScreen(onBack: () -> Unit) {
    Scaffold(topBar = { SimpleTopBar("About", onBack) }, containerColor = Color(0xFF0F0F0F)) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("SportPulse v1.0", color = Color.Gray)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White) } },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0F0F))
    )
}

@Composable
fun SettingsToggleRow(title: String, initialChecked: Boolean) {
    var checked by remember { mutableStateOf(initialChecked) }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { checked = it }, colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFFFF3B30)))
    }
}

@Composable
fun GuestProfileView(onLoginClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.AccountCircle, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoginClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3B30))) { Text("Login / Register") }
        }
    }
}

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, color = Color(0xFFFF3B30), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun DashboardItem(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp)).clickable { onClick() }.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color(0xFFFF3B30), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
    }
}

@Composable
fun MyTextField(value: String, label: String, isPassword: Boolean = false, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label, color = Color.Gray) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text),
        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Color(0xFFFF3B30), unfocusedBorderColor = Color.Gray),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(12.dp))
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(title.uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
fun SettingsRow(icon: ImageVector, title: String, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
    }
}