package com.example.data

data class Level(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String, // "Easy", "Medium", "Hard", "Expert"
    val targetDistance: Float, // x-coordinate of target in meters
    val targetHeight: Float, // y-coordinate of target in meters (from canon launch height)
    val targetRadius: Float = 3.5f, // radius of success margin in meters
    val gravity: Float = 10.0f, // m/s^2 (changed default to 10.0f)
    val windX: Float = 0f, // wind horizontal force in m/s^2
    val coinReward: Int = 100,
    val hints: List<String> = emptyList(),
    val challengeType: String = "Angle", // "Angle", "Speed", "Range"
    val fixedAngle: Float = 45f,
    val fixedVelocity: Float = 25f
)
