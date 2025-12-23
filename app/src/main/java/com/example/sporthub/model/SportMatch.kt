package com.example.sporthub.model

import androidx.compose.ui.graphics.Color

data class SportMatch(
    val id: Int,
    val league: String, // Наприклад "Premier League"
    val team1: String,
    val team2: String,
    val score: String, // "2 : 1" або час "19:45"
    val isLive: Boolean, // Чи йде матч зараз
    val sportType: SportType,
    val color: Color // Колір фону картки для краси
)

enum class SportType {
    FOOTBALL, HOCKEY, BASKETBALL, TENNIS
}