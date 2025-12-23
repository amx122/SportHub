package com.example.sporthub

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme(background = Color(0xFF0F0F0F), surface = Color(0xFF1C1C1E), primary = Color(0xFFFF3B30))) {
                MainAppStructure()
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Scores", Icons.Outlined.SportsSoccer)
    object Search : Screen("search", "Search", Icons.Default.Search)
    object Reels : Screen("reels", "Highlights", Icons.Outlined.VideoLibrary)
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Auth : Screen("auth", "Auth", Icons.Default.Lock)

    object MatchDetails : Screen("match_details/{matchId}/{hName}/{aName}/{hId}/{aId}/{lId}/{season}", "Details", Icons.Default.Info) {
        fun createRoute(matchId: String, hName: String, aName: String, hId: String, aId: String, lId: String, season: Int) =
            "match_details/$matchId/${Uri.encode(hName)}/${Uri.encode(aName)}/$hId/$aId/$lId/$season"
    }

    object TeamDetails : Screen("team_details/{id}/{name}/{logo}", "Team", Icons.Default.Info) {
        fun createRoute(id: Int, name: String, logo: String) = "team_details/$id/${Uri.encode(name)}/${Uri.encode(logo)}"
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainAppStructure() {
    val navController = rememberNavController()
    val bottomNavItems = listOf(Screen.Home, Screen.Search, Screen.Reels, Screen.Profile)

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            if (currentRoute in listOf(Screen.Home.route, Screen.Search.route, Screen.Reels.route, Screen.Profile.route)) {
                NavigationBar(containerColor = Color(0xFF121212), contentColor = Color.White) {
                    bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 10.sp) },
                            selected = currentRoute == screen.route,
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, indicatorColor = MaterialTheme.colorScheme.primary),
                            onClick = { navController.navigate(screen.route) { popUpTo(navController.graph.findStartDestination().id) { saveState = true }; launchSingleTop = true; restoreState = true } }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) {
                FlashscoreHomeScreen(onMatchClick = { match ->
                    navController.navigate(Screen.MatchDetails.createRoute(
                        match.fixture.id.toString(),
                        match.teams.home.name,
                        match.teams.away.name,
                        match.teams.home.id.toString(),
                        match.teams.away.id.toString(),
                        match.league.id.toString(),
                        match.league.season ?: 2025
                    ))
                })
            }
            composable(Screen.Search.route) { SearchScreen({ team -> navController.navigate(Screen.TeamDetails.createRoute(team.id, team.name, team.logo)) }, { }) }
            composable(Screen.Reels.route) { ReelsScreen() }
            composable(Screen.Profile.route) { ProfileScreen(onLoginClick = { navController.navigate(Screen.Auth.route) }) }
            composable(Screen.Auth.route) { AuthScreen({ navController.popBackStack() }, { navController.popBackStack() }) }

            composable("match_details/{matchId}/{hName}/{aName}/{hId}/{aId}/{lId}/{season}") { backStackEntry ->
                val args = backStackEntry.arguments
                MatchDetailScreen(
                    matchId = args?.getString("matchId") ?: "",
                    homeTeam = args?.getString("hName") ?: "",
                    awayTeam = args?.getString("aName") ?: "",
                    homeId = args?.getString("hId") ?: "",
                    awayId = args?.getString("aId") ?: "",
                    leagueId = args?.getString("lId") ?: "",
                    season = args?.getString("season") ?: "2025",
                    onBack = { navController.popBackStack() }
                )
            }

            composable("team_details/{id}/{name}/{logo}") { backStackEntry ->
                val args = backStackEntry.arguments
                TeamDetailScreen(args?.getString("id")?:"", args?.getString("name")?:"", args?.getString("logo")?:"", { navController.popBackStack() })
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun FlashscoreHomeScreen(onMatchClick: (MatchResponse) -> Unit) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var groupedMatches by remember { mutableStateOf<Map<Int, Pair<LeagueInfo, List<MatchResponse>>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var statusMessage by remember { mutableStateOf("") }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    var savedMatchIds by remember { mutableStateOf(setOf<Int>()) }
    val context = LocalContext.current

    LaunchedEffect(auth.currentUser) {
        auth.currentUser?.let { user ->
            db.collection("users").document(user.uid).collection("saved_matches")
                .addSnapshotListener { snapshot, _ -> if (snapshot != null) savedMatchIds = snapshot.documents.mapNotNull { it.id.toIntOrNull() }.toSet() }
        }
    }

    LaunchedEffect(selectedDate) {
        isLoading = true
        statusMessage = ""
        groupedMatches = emptyMap()

        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
            val dateStr = selectedDate.format(formatter)
            Log.d("API_DEBUG", "Request: $dateStr")

            val response = RetrofitInstance.api.getFixturesByDateRaw(dateStr, "Europe/Kiev")
            if (response.errors != null && response.errors.toString() != "[]") {
                statusMessage = "API Error: ${response.errors}\n(Check your request limit)"
                Log.e("API_ERROR", statusMessage)
            } else if (response.response.isEmpty()) {
                statusMessage = "No matches found on ${selectedDate.format(DateTimeFormatter.ofPattern("dd.MM"))}"
            } else {
                val grouped = response.response.groupBy { it.league.id }
                val result = mutableMapOf<Int, Pair<LeagueInfo, List<MatchResponse>>>()

                grouped.forEach { (leagueId, matches) ->
                    if (matches.isNotEmpty()) {
                        val sorted = matches.sortedBy { it.fixture.date }
                        result[leagueId] = Pair(matches[0].league, sorted)
                    }
                }

                val priorityIds = listOf(2, 3, 848, 39, 140, 135, 78, 61, 333)
                groupedMatches = result.toList().sortedBy { (id, _) ->
                    val index = priorityIds.indexOf(id)
                    if (index == -1) 999 else index
                }.toMap()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            statusMessage = "Connection Error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    fun toggleSave(match: MatchResponse) {
        val user = auth.currentUser
        if (user == null) { Toast.makeText(context, "Please login", Toast.LENGTH_SHORT).show(); return }
        val matchRef = db.collection("users").document(user.uid).collection("saved_matches").document(match.fixture.id.toString())
        if (savedMatchIds.contains(match.fixture.id)) matchRef.delete()
        else {
            matchRef.set(hashMapOf("id" to match.fixture.id, "homeTeam" to match.teams.home.name, "awayTeam" to match.teams.away.name, "score" to match.fixture.status.short))
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        DateStrip(selectedDate) { selectedDate = it }
        Divider(color = Color(0xFF1C1C1E))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF3B30)) }
        } else if (statusMessage.isNotEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = statusMessage,
                    color = if(statusMessage.contains("Error")) Color.Red else Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                groupedMatches.forEach { (_, pair) ->
                    item { LeagueHeader(pair.first) }
                    items(pair.second) { m ->
                        FlashscoreMatchItem(
                            match = m,
                            isSaved = savedMatchIds.contains(m.fixture.id),
                            onToggleSave = { toggleSave(m) },
                            onClick = { onMatchClick(m) }
                        )
                        Divider(color = Color(0xFF1C1C1E), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateStrip(selectedDate: LocalDate, onDateSelected: (LocalDate) -> Unit) {
    val listState = rememberLazyListState()
    val dates = remember { (-14..14).map { LocalDate.now().plusDays(it.toLong()) } }

    LaunchedEffect(Unit) { listState.scrollToItem(14) }

    LazyRow(state = listState, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val isToday = date == LocalDate.now()

            Column(
                modifier = Modifier
                    .width(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) Color(0xFFFF3B30) else Color(0xFF1C1C1E))
                    .clickable { onDateSelected(date) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("EEE", Locale.US)).uppercase(),
                    color = if (isSelected) Color.White else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date.dayOfMonth.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isToday && !isSelected) Text("Today", color = Color(0xFFFF3B30), fontSize = 8.sp)
            }
        }
    }
}

@Composable
fun LeagueHeader(league: LeagueInfo) {
    Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF121212)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = league.flag ?: league.logo, contentDescription = null, modifier = Modifier.size(20.dp).clip(CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(league.name.uppercase(), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FlashscoreMatchItem(match: MatchResponse, isSaved: Boolean, onToggleSave: () -> Unit, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 12.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onToggleSave, modifier = Modifier.size(24.dp)) { Icon(if (isSaved) Icons.Default.Star else Icons.Outlined.StarBorder, contentDescription = null, tint = if (isSaved) Color(0xFFFF3B30) else Color.Gray) }
        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.width(40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            when (match.fixture.status.short) {
                "FT", "AET", "PEN" -> Text("Fin", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                "NS" -> Text(
                    text = try { match.fixture.date.substring(11, 16) } catch (e: Exception) { "-" },
                    color = Color.White,
                    fontSize = 12.sp
                )
                else -> Text("${match.fixture.status.elapsed}'", color = Color(0xFFFF3B30), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            TeamLine(match.teams.home, match.goals.home, match.fixture.status.short != "NS")
            Spacer(modifier = Modifier.height(6.dp))
            TeamLine(match.teams.away, match.goals.away, match.fixture.status.short != "NS")
        }
    }
}

@Composable
fun TeamLine(team: TeamModel, score: Int?, showScore: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(model = team.logo, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(team.name, color = if (team.winner == true) Color(0xFFFF3B30) else Color.White, fontSize = 14.sp, fontWeight = if (team.winner == true) FontWeight.Bold else FontWeight.Normal)
        Spacer(modifier = Modifier.weight(1f))
        if (showScore) Text("${score ?: 0}", color = if (team.winner == true) Color(0xFFFF3B30) else Color.White, fontWeight = FontWeight.Bold)
    }
}