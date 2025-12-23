package com.example.sporthub

import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage

@OptIn(UnstableApi::class)
@Composable
fun MatchDetailScreen(
    matchId: String,
    homeTeam: String, awayTeam: String,
    homeId: String, awayId: String,
    leagueId: String,
    season: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var isFavorite by remember { mutableStateOf(false) }
    val activeSeason = if (season == "null" || season.toIntOrNull() == null) "2025" else season

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse("https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")))
            prepare()
            playWhenReady = false
        }
    }
    DisposableEffect(Unit) { onDispose { exoPlayer.release() } }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).background(Color.Black)) {
            AndroidView(factory = { PlayerView(context).apply { player = exoPlayer; useController = true } }, modifier = Modifier.fillMaxSize())
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBack, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                IconButton(onClick = { isFavorite = !isFavorite; Toast.makeText(context, if(isFavorite) "Saved" else "Removed", Toast.LENGTH_SHORT).show() }, modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), CircleShape)) {
                    Icon(if(isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder, contentDescription = null, tint = if(isFavorite) Color(0xFFFF3B30) else Color.White)
                }
            }
        }

        MatchInfoSection(homeTeam, awayTeam)

        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf("Timeline", "Lineups", "Standings", "H2H & Form")

        TabRow(selectedTabIndex = selectedTabIndex, containerColor = Color(0xFF0F0F0F), contentColor = Color.White, indicator = {
            TabRowDefaults.Indicator(modifier = Modifier.tabIndicatorOffset(it[selectedTabIndex]), color = Color(0xFFFF3B30))
        }) {
            tabs.forEachIndexed { index, title ->
                Tab(selected = selectedTabIndex == index, onClick = { selectedTabIndex = index }, text = { Text(title, fontSize = 12.sp) })
            }
        }

        when (selectedTabIndex) {
            0 -> MatchEventsView(matchId, homeId)
            1 -> LineupView(homeTeam, awayTeam, homeId, awayId)
            2 -> LeagueTable(leagueId, activeSeason)
            3 -> H2HAndFormView(homeId, awayId, homeTeam, awayTeam)
        }
    }
}

@Composable
fun MatchInfoSection(homeTeam: String, awayTeam: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
            Text(homeTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Text("VS", color = Color(0xFFFF3B30), fontSize = 24.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
            Text(awayTeam, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, maxLines = 1, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun MatchEventsView(matchId: String, homeTeamId: String) {
    var events by remember { mutableStateOf<List<MatchEvent>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(matchId) {
        try {
            val response = RetrofitInstance.api.getMatchEvents(matchId)
            events = response.response
        } catch (e: Exception) { e.printStackTrace() } finally { isLoading = false }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        if (isLoading) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF3B30)) }
        } else if (events.isEmpty()) {
            Text("No events or match not started", color = Color.Gray, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            events.forEach { event ->
                val isHomeEvent = event.team.id.toString() == homeTeamId
                EventItem(time = "${event.time.elapsed}'", player = event.player.name ?: "Unknown", detail = event.detail, type = event.type, isHome = isHomeEvent)
            }
        }
    }
}

@Composable
fun EventItem(time: String, player: String, detail: String, type: String, isHome: Boolean) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = if(isHome) Arrangement.Start else Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
        if(isHome) {
            Text(time, color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
            Spacer(modifier = Modifier.width(8.dp))
            EventIcon(type, detail)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(player, color = Color.White, fontWeight = FontWeight.Bold)
                Text(detail, color = Color.Gray, fontSize = 10.sp)
            }
        } else {
            Column(horizontalAlignment = Alignment.End) {
                Text(player, color = Color.White, fontWeight = FontWeight.Bold)
                Text(detail, color = Color.Gray, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
            EventIcon(type, detail)
            Spacer(modifier = Modifier.width(8.dp))
            Text(time, color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp), textAlign = TextAlign.End)
        }
    }
    Divider(color = Color(0xFF1C1C1E))
}

@Composable
fun EventIcon(type: String, detail: String) {
    when {
        type == "Goal" -> Text("⚽", fontSize = 16.sp)
        detail.contains("Yellow") -> Box(modifier = Modifier.size(12.dp, 16.dp).background(Color.Yellow, RoundedCornerShape(2.dp)))
        detail.contains("Red") -> Box(modifier = Modifier.size(12.dp, 16.dp).background(Color.Red, RoundedCornerShape(2.dp)))
        type == "subst" -> Icon(Icons.Default.Info, contentDescription = "Sub", tint = Color.Green, modifier = Modifier.size(16.dp))
        else -> Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}
@Composable
fun LeagueTable(leagueId: String, season: String) {
    var standingsGroups by remember { mutableStateOf<List<List<StandingEntry>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(leagueId, season) {
        try {
            Log.d("TABLE_DEBUG", "Try season: $season")
            var response = RetrofitInstance.api.getLeagueTable(leagueId, season)

            // Якщо пусто, пробуємо 2024
            if (response.response.isEmpty()) {
                Log.d("TABLE_DEBUG", "Empty. Try season: 2024")
                response = RetrofitInstance.api.getLeagueTable(leagueId, "2024")
            }

            if (response.response.isNotEmpty()) {
                standingsGroups = response.response[0].league.standings
            }
        } catch (e: Exception) { e.printStackTrace() } finally { isLoading = false }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF3B30)) }
    } else if (standingsGroups.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("Table not available", color = Color.Gray)
        }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            standingsGroups.forEachIndexed { index, group ->
                if (standingsGroups.size > 1) {
                    Text("Group ${index + 1}", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                } else if (group.isNotEmpty() && group[0].form != null) {
                    Text("Standings", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
                }

                Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Text("#", color = Color.Gray, modifier = Modifier.width(30.dp))
                    Text("Team", color = Color.Gray, modifier = Modifier.weight(1f))
                    Text("P", color = Color.Gray, modifier = Modifier.width(25.dp), textAlign = TextAlign.Center)
                    Text("Pts", color = Color.White, modifier = Modifier.width(35.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                }
                Divider(color = Color.Gray)

                group.forEach { entry ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(entry.rank.toString(), color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.width(30.dp))
                        AsyncImage(model = entry.team.logo, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(entry.team.name, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                        Text(entry.stats.played.toString(), color = Color.Gray, modifier = Modifier.width(25.dp), textAlign = TextAlign.Center)
                        Text(entry.points.toString(), color = Color(0xFFFF3B30), modifier = Modifier.width(35.dp), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    }
                    Divider(color = Color(0xFF1C1C1E))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
@Composable
fun H2HAndFormView(homeId: String, awayId: String, homeName: String, awayName: String) {
    var h2hMatches by remember { mutableStateOf<List<MatchResponse>>(emptyList()) }
    var homeLastMatches by remember { mutableStateOf<List<MatchResponse>>(emptyList()) }
    var awayLastMatches by remember { mutableStateOf<List<MatchResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(homeId, awayId) {
        try {
            val h2hResp = RetrofitInstance.api.getHeadToHead("$homeId-$awayId")
            h2hMatches = h2hResp.response
            val homeResp = RetrofitInstance.api.getTeamLastMatches(homeId)
            homeLastMatches = homeResp.response
            val awayResp = RetrofitInstance.api.getTeamLastMatches(awayId)
            awayLastMatches = awayResp.response
        } catch (e: Exception) { e.printStackTrace() } finally { isLoading = false }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFFFF3B30)) }
    } else {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Head-to-Head", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (h2hMatches.isEmpty()) Text("No match history", color = Color.Gray, fontSize = 12.sp)
            else h2hMatches.forEach { m -> MiniMatchItem(m) }

            Spacer(modifier = Modifier.height(24.dp))

            Text("$homeName Form (Last 5)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (homeLastMatches.isEmpty()) Text("No data found", color = Color.Gray)
            else homeLastMatches.forEach { m -> MiniMatchItem(m) }

            Spacer(modifier = Modifier.height(24.dp))

            Text("$awayName Form (Last 5)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (awayLastMatches.isEmpty()) Text("No data found", color = Color.Gray)
            else awayLastMatches.forEach { m -> MiniMatchItem(m) }
        }
    }
}

@Composable
fun MiniMatchItem(match: MatchResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(match.fixture.date.take(10), color = Color.Gray, fontSize = 10.sp, modifier = Modifier.width(70.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(match.teams.home.name, color = if(match.teams.home.winner==true) Color.Green else Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(match.goals.home.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(match.teams.away.name, color = if(match.teams.away.winner==true) Color.Green else Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text(match.goals.away.toString(), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}