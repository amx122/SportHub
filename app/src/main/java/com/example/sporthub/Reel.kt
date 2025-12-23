package com.example.sporthub.model

data class Reel(
    val id: String = "",
    val videoUrl: String = "",
    val description: String = "",
    val author: String = "User",
    val likes: Int = 0,
    val isExternal: Boolean = false
)