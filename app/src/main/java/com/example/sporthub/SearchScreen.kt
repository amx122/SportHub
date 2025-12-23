package com.example.sporthub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SearchScreen(
    onTeamClick: (TeamModel) -> Unit,
    onPlayerClick: (PlayerInfo) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 - Teams, 1 - Players
    var teamResults by remember { mutableStateOf<List<TeamModel>>(emptyList()) }
    var playerResults by remember { mutableStateOf<List<PlayerInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F)).padding(16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                searchJob?.cancel()
                searchJob = scope.launch {
                    delay(500)
                    if (query.length > 2) {
                        isLoading = true
                        try {
                            if (selectedTab == 0) {
                                val resp = RetrofitInstance.api.searchTeams(query)
                                teamResults = resp.response.map { it.team }
                            } else {
                                val resp = RetrofitInstance.api.searchPlayers(query)
                                playerResults = resp.response.map { it.player }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {
                            isLoading = false
                        }
                    }
                }
            },
            label = { Text("Search...", color = Color.Gray) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color(0xFFFF3B30),
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
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
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0; teamResults = emptyList(); query = "" }, text = { Text("Teams") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1; playerResults = emptyList(); query = "" }, text = { Text("Players") })
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFFF3B30))
            }
        } else {
            LazyColumn {
                if (selectedTab == 0) {
                    items(teamResults) { team ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onTeamClick(team) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = team.logo, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(team.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Divider(color = Color(0xFF1C1C1E))
                    }
                } else {
                    items(playerResults) { player ->
                        Row(modifier = Modifier.fillMaxWidth().clickable { onPlayerClick(player) }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(model = player.photo, contentDescription = null, modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(player.name, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text("Age: ${player.age ?: "?"} | ${player.nationality ?: ""}", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Divider(color = Color(0xFF1C1C1E))
                    }
                }
            }
        }
    }
}