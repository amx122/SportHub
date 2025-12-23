package com.example.sporthub

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun TeamDetailScreen(teamId: String, teamName: String, teamLogo: String, onBack: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    var nextMatches by remember { mutableStateOf<List<MatchResponse>>(emptyList()) }
    var lastMatches by remember { mutableStateOf<List<MatchResponse>>(emptyList()) }
    var squad by remember { mutableStateOf<List<SquadPlayer>>(emptyList()) }

    LaunchedEffect(teamId) {
        try {
            nextMatches = RetrofitInstance.api.getTeamFixtures(teamId, next = 5).response
            lastMatches = RetrofitInstance.api.getTeamResults(teamId, last = 5).response
            squad = RetrofitInstance.api.getTeamSquad(teamId).response.firstOrNull()?.players ?: emptyList()
        } catch (e: Exception) { e.printStackTrace() }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        Box(modifier = Modifier.fillMaxWidth().background(Color(0xFF1C1C1E)).padding(16.dp)) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(model = teamLogo, contentDescription = null, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(teamName, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF0F0F0F),
            contentColor = Color.White,
            indicator = { tabPositions ->
                if (selectedTab < tabPositions.size) {
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Color(0xFFFF3B30)
                    )
                }
            }
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Matches") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Squad") })
        }
        LazyColumn(modifier = Modifier.padding(16.dp)) {
            if (selectedTab == 0) {
                item {
                    Text("Upcoming Fixtures", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                if (nextMatches.isEmpty()) {
                    item { Text("No upcoming matches", color = Color.Gray, fontSize = 12.sp) }
                } else {
                    items(nextMatches) { match -> SmallMatchItem(match) }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Recent Results", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                }
                if (lastMatches.isEmpty()) {
                    item { Text("No recent results", color = Color.Gray, fontSize = 12.sp) }
                } else {
                    items(lastMatches) { match -> SmallMatchItem(match) }
                }

            } else {
                if (squad.isEmpty()) {
                    item { Text("Squad not available", color = Color.Gray, modifier = Modifier.padding(8.dp)) }
                } else {
                    items(squad) { player ->
                        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = player.photo, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(player.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(player.position ?: "", color = Color.Gray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Text("#${player.number ?: "-"}", color = Color(0xFFFF3B30), fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Color(0xFF1C1C1E))
                    }
                }
            }
        }
    }
}

@Composable
fun SmallMatchItem(match: MatchResponse) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = match.fixture.date.takeLast(14).take(5),
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.width(40.dp)
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 8.dp)) {
            Text(match.teams.home.name, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(match.teams.away.name, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Text(
            text = if(match.fixture.status.short == "NS" || match.fixture.status.short == "TBD") "?"
            else "${match.goals.home} : ${match.goals.away}",
            color = Color(0xFFFF3B30),
            fontWeight = FontWeight.Bold
        )
    }
    Divider(color = Color(0xFF1C1C1E))
}