package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class GameRepository(private val gameDao: GameDao) {

    val predefinedLevels = listOf(
        Level(
            id = "lvl_1",
            name = "1. Basic Range Aim",
            description = "Welcome! Find the correct Angle to throw the ball to hit the target. Ground gravity is exactly 10.0 m/s².",
            difficulty = "Easy",
            targetDistance = 50f,
            targetHeight = 0f,
            targetRadius = 3.5f,
            gravity = 10.0f,
            windX = 0f,
            coinReward = 50,
            hints = listOf(
                "Launch Speed is fixed at 25.0 m/s.",
                "Calculate Angle using: sin(2θ) = (R * g) / v0^2 = (50 * 10) / 625 = 0.8. Thus, 2θ ≈ 53° => θ ≈ 26.5° or 63.5°!"
            ),
            challengeType = "Angle",
            fixedAngle = 45f,
            fixedVelocity = 25f
        ),
        Level(
            id = "lvl_2",
            name = "2. Plateau Pitch Speed",
            description = "A supply ball needs to land on a high plateau. Find the correct Speed at a fixed angle of 45°.",
            difficulty = "Easy",
            targetDistance = 60f,
            targetHeight = 10f,
            targetRadius = 3.5f,
            gravity = 10.0f,
            windX = 0f,
            coinReward = 80,
            hints = listOf(
                "Launch Angle is locked at 45.0°.",
                "Since you are aiming high, increase launch speed to overcome the gravitational pull of 10.0 m/s²."
            ),
            challengeType = "Speed",
            fixedAngle = 45f,
            fixedVelocity = 20f
        ),
        Level(
            id = "lvl_3",
            name = "3. Moon Landing Range",
            description = "Welcome to the Moon! Weak gravity (1.62 m/s²). Calculate/predict the horizontal landing range to place the target correctly!",
            difficulty = "Medium",
            targetDistance = 120f,
            targetHeight = 0f,
            targetRadius = 4.0f,
            gravity = 1.62f,
            windX = 0f,
            coinReward = 120,
            hints = listOf(
                "Launch Angle is 30° and Speed is 15.0 m/s.",
                "Formula: R = (v0^2 * sin(2θ)) / g = (225 * sin(60°)) / 1.62 ≈ (225 * 0.866) / 1.62 ≈ 120.3 meters!"
            ),
            challengeType = "Range",
            fixedAngle = 30f,
            fixedVelocity = 15f
        ),
        Level(
            id = "lvl_4",
            name = "4. Windy Pass Angle",
            description = "Gravity is 10.0 m/s² but a stiff headwind blows against you. Find the perfect Angle to cut through the breeze!",
            difficulty = "Medium",
            targetDistance = 75f,
            targetHeight = 8f,
            targetRadius = 3.5f,
            gravity = 10.0f,
            windX = -2.0f, // headwind pushing left
            coinReward = 150,
            hints = listOf(
                "Launch Speed is fixed at 30.0 m/s.",
                "Use a lower, punchier angle so the headwind has less time to blow the ball back."
            ),
            challengeType = "Angle",
            fixedAngle = 45f,
            fixedVelocity = 30f
        ),
        Level(
            id = "lvl_5",
            name = "5. Martian Ravine Velocity",
            description = "Throw downwards into a crater on Mars (gravity 3.71 m/s²). Find the precise launch Speed to hit the base.",
            difficulty = "Medium",
            targetDistance = 80f,
            targetHeight = -15f,
            targetRadius = 3.8f,
            gravity = 3.71f,
            windX = 1.0f, // gentle tailwind
            coinReward = 150,
            hints = listOf(
                "Launch Angle is locked at 35.0°.",
                "Mars gravity is weak and the tailwind helps carry the ball. Adjust your speed to compensate!"
            ),
            challengeType = "Speed",
            fixedAngle = 35f,
            fixedVelocity = 25f
        ),
        Level(
            id = "lvl_6",
            name = "6. Stormy Gorge Range",
            description = "Heavy Earth-like gravity (10.0 m/s²) and a severe headwind. Predict the landing distance (Range) to position the target!",
            difficulty = "Hard",
            targetDistance = 60f,
            targetHeight = -20f,
            targetRadius = 3.5f,
            gravity = 10.0f,
            windX = -3.0f, // strong headwind
            coinReward = 200,
            hints = listOf(
                "Angle is 45° and Speed is 28.0 m/s.",
                "Calculate or estimate where the ball will land. Adjust the Target Distance slider until the bullseye aligns, then fire!"
            ),
            challengeType = "Range",
            fixedAngle = 45f,
            fixedVelocity = 28f
        ),
        Level(
            id = "lvl_7",
            name = "7. Jovian Core Angle",
            description = "Jupiter's intense gravity (25.0 m/s²) pulls everything down instantly. Adjust your Angle to strike high up!",
            difficulty = "Hard",
            targetDistance = 75f,
            targetHeight = 10f,
            targetRadius = 3.0f,
            gravity = 25.0f,
            windX = 0f,
            coinReward = 220,
            hints = listOf(
                "Launch Speed is locked at 50.0 m/s.",
                "With gravity of 25.0 m/s², use a high angle (50° - 60°) to give the ball sufficient loft."
            ),
            challengeType = "Angle",
            fixedAngle = 45f,
            fixedVelocity = 50f
        ),
        Level(
            id = "lvl_8",
            name = "8. Titan Gale Force Speed",
            description = "The ultimate challenge. Low gravity (1.5 m/s²) but a severe gale tailwind (4.0 m/s²). Find the perfect Speed!",
            difficulty = "Expert",
            targetDistance = 140f,
            targetHeight = 30f,
            targetRadius = 3.5f,
            gravity = 1.5f,
            windX = 4.0f, // severe horizontal storm
            coinReward = 300,
            hints = listOf(
                "Launch Angle is locked at 50.0°.",
                "Titan's dense air pushes your ball significantly. Lower speed might be key to let the wind do the work."
            ),
            challengeType = "Speed",
            fixedAngle = 50f,
            fixedVelocity = 20f
        )
    )

    fun getPlayerProfile(): Flow<PlayerProfile> {
        return gameDao.getPlayerProfile().map { profile ->
            profile ?: PlayerProfile().also { gameDao.savePlayerProfile(it) }
        }
    }

    fun getAllLevelProgress(): Flow<List<LevelProgress>> {
        return gameDao.getAllLevelProgress()
    }

    suspend fun savePlayerProfile(profile: PlayerProfile) {
        gameDao.savePlayerProfile(profile)
    }

    suspend fun saveLevelProgress(progress: LevelProgress) {
        gameDao.saveLevelProgress(progress)
    }

    suspend fun addCoins(amount: Int) {
        val current = gameDao.getPlayerProfile().map { it ?: PlayerProfile() }.firstOrNull() ?: PlayerProfile()
        gameDao.savePlayerProfile(current.copy(coins = current.coins + amount))
    }

    suspend fun spendCoins(amount: Int): Boolean {
        val current = gameDao.getPlayerProfile().map { it ?: PlayerProfile() }.firstOrNull() ?: PlayerProfile()
        return if (current.coins >= amount) {
            gameDao.savePlayerProfile(current.copy(coins = current.coins - amount))
            true
        } else {
            false
        }
    }

    suspend fun unlockProjectile(itemId: String, price: Int): Boolean {
        val current = gameDao.getPlayerProfile().map { it ?: PlayerProfile() }.firstOrNull() ?: PlayerProfile()
        val unlockedList = current.unlockedProjectiles.split(",").map { it.trim() }.toMutableSet()
        if (itemId in unlockedList) return true // already unlocked
        
        if (current.coins >= price) {
            unlockedList.add(itemId)
            gameDao.savePlayerProfile(current.copy(
                coins = current.coins - price,
                unlockedProjectiles = unlockedList.joinToString(",")
            ))
            return true
        }
        return false
    }

    suspend fun unlockCannon(itemId: String, price: Int): Boolean {
        val current = gameDao.getPlayerProfile().map { it ?: PlayerProfile() }.firstOrNull() ?: PlayerProfile()
        val unlockedList = current.unlockedCannons.split(",").map { it.trim() }.toMutableSet()
        if (itemId in unlockedList) return true
        
        if (current.coins >= price) {
            unlockedList.add(itemId)
            gameDao.savePlayerProfile(current.copy(
                coins = current.coins - price,
                unlockedCannons = unlockedList.joinToString(",")
            ))
            return true
        }
        return false
    }

    suspend fun unlockTheme(themeId: String, price: Int): Boolean {
        val current = gameDao.getPlayerProfile().map { it ?: PlayerProfile() }.firstOrNull() ?: PlayerProfile()
        val unlockedList = current.unlockedThemes.split(",").map { it.trim() }.toMutableSet()
        if (themeId in unlockedList) return true
        
        if (current.coins >= price) {
            unlockedList.add(themeId)
            gameDao.savePlayerProfile(current.copy(
                coins = current.coins - price,
                unlockedThemes = unlockedList.joinToString(",")
            ))
            return true
        }
        return false
    }

    suspend fun completeLevel(levelId: String, stars: Int, attempts: Int) {
        val existing = gameDao.getLevelProgressById(levelId)
        val newStars = maxOf(existing?.stars ?: 0, stars)
        val newAttempts = (existing?.attempts ?: 0) + attempts
        
        val progress = LevelProgress(
            levelId = levelId,
            isCompleted = true,
            stars = newStars,
            attempts = newAttempts,
            bestTime = System.currentTimeMillis()
        )
        gameDao.saveLevelProgress(progress)
    }
}
