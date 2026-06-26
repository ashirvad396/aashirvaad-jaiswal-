package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfile(
    @PrimaryKey val id: Int = 1,
    val coins: Int = 500, // Starts with 500 coins so they can buy their first skin!
    val selectedProjectile: String = "standard",
    val selectedCannon: String = "steel",
    val unlockedProjectiles: String = "standard", // Comma-separated list: standard, plasma, meteor
    val unlockedCannons: String = "steel", // Comma-separated list: steel, laser, arc
    val unlockedThemes: String = "cosmic", // Comma-separated list: cosmic, synthwave, chrono
    val selectedTheme: String = "cosmic"
)

@Entity(tableName = "level_progress")
data class LevelProgress(
    @PrimaryKey val levelId: String,
    val isCompleted: Boolean = false,
    val stars: Int = 0,
    val attempts: Int = 0,
    val bestTime: Long = 0L
)
