package com.example.sporthub

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@Composable
fun LineupView(homeTeamName: String, awayTeamName: String, homeId: String, awayId: String) {
    var isHomeTeam by remember { mutableStateOf(true) }
    var homeSquad by remember { mutableStateOf<List<SquadPlayer>>(emptyList()) }
    var awaySquad by remember { mutableStateOf<List<SquadPlayer>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(homeId, awayId) {
        scope.launch {
            try {
                val homeResp = RetrofitInstance.api.getTeamSquad(homeId)
                homeSquad = homeResp.response.firstOrNull()?.players ?: emptyList()
                val awayResp = RetrofitInstance.api.getTeamSquad(awayId)
                awaySquad = awayResp.response.firstOrNull()?.players ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }
    val currentSquadRaw = if (isHomeTeam) homeSquad else awaySquad
    val formation = getTacticalFormation(currentSquadRaw)

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth().background(Color(0xFF1C1C1E), RoundedCornerShape(12.dp)).padding(4.dp)) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if(isHomeTeam) Color(0xFFFF3B30) else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { isHomeTeam = true }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(homeTeamName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(if(!isHomeTeam) Color(0xFF2196F3) else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { isHomeTeam = false }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(awayTeamName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7f)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val stripes = 10
                val stripeHeight = h / stripes
                for (i in 0 until stripes) {
                    val color = if (i % 2 == 0) Color(0xFF2E7D32) else Color(0xFF388E3C)
                    drawRect(color = color, topLeft = Offset(0f, i * stripeHeight), size = Size(w, stripeHeight))
                }
                val linePaint = Stroke(width = 2.dp.toPx())
                val white = Color.White.copy(alpha = 0.7f)
                drawRect(color = white, style = linePaint)
                drawLine(white, Offset(0f, h/2), Offset(w, h/2), strokeWidth = 2.dp.toPx())
                drawCircle(white, radius = w * 0.15f, center = Offset(w/2, h/2), style = linePaint)
                drawCircle(white, radius = 3.dp.toPx(), center = Offset(w/2, h/2)) // Точка
                val penaltyW = w * 0.5f
                val penaltyH = h * 0.16f
                drawRect(white, topLeft = Offset((w - penaltyW)/2, 0f), size = Size(penaltyW, penaltyH), style = linePaint)
                drawRect(white, topLeft = Offset((w - penaltyW)/2, h - penaltyH), size = Size(penaltyW, penaltyH), style = linePaint)
                val cornerSize = 30f
                drawArc(white, 0f, 90f, false, topLeft = Offset(0f, 0f), size = Size(cornerSize, cornerSize), style = linePaint)
                drawArc(white, 90f, 90f, false, topLeft = Offset(w - cornerSize, 0f), size = Size(cornerSize, cornerSize), style = linePaint)
                drawArc(white, 180f, 90f, false, topLeft = Offset(w - cornerSize, h - cornerSize), size = Size(cornerSize, cornerSize), style = linePaint)
                drawArc(white, 270f, 90f, false, topLeft = Offset(0f, h - cornerSize), size = Size(cornerSize, cornerSize), style = linePaint)
            }

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
            } else {
                Column(
                    modifier = Modifier.fillMaxSize().padding(vertical = 20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    PlayerRow(formation["GK"] ?: emptyList(), isHomeTeam)
                    PlayerRow(formation["DEF"] ?: emptyList(), isHomeTeam)
                    PlayerRow(formation["MID"] ?: emptyList(), isHomeTeam)
                    PlayerRow(formation["FWD"] ?: emptyList(), isHomeTeam)
                }
            }
        }
    }
}

@Composable
fun PlayerRow(players: List<SquadPlayer>, isHome: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        players.forEach { player ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(if(isHome) Color(0xFFFF3B30) else Color(0xFF2196F3), CircleShape)
                        .border(1.5.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (player.photo != null) {
                        AsyncImage(model = player.photo, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape))
                    } else {
                        Text(player.number?.toString() ?: "", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.background(Color.Black.copy(alpha=0.6f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                    Text(
                        text = player.name.split(" ").last(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

fun getTacticalFormation(allPlayers: List<SquadPlayer>): Map<String, List<SquadPlayer>> {
    val gk = allPlayers.filter { it.position == "Goalkeeper" }.take(1)
    val def = allPlayers.filter { it.position == "Defender" }.take(4)
    val mid = allPlayers.filter { it.position == "Midfielder" }.take(4)
    val fwd = allPlayers.filter { it.position == "Attacker" }.take(2)
    return mapOf("GK" to gk, "DEF" to def, "MID" to mid, "FWD" to fwd)
}