package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.physics.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val alpha: Float,
    val maxLife: Int,
    val currentLife: Int = 0
)

enum class GameResult {
    IDLE,
    FLYING,
    HIT,
    MISS
}

sealed interface AppScreen {
    object Home : AppScreen
    object LevelSelect : AppScreen
    data class GamePlay(val level: Level) : AppScreen
    object Shop : AppScreen
    object PhysicsFormulas : AppScreen
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = GameRepository(database.gameDao())

    val predefinedLevels = repository.predefinedLevels

    // UI Navigation State
    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Home)
        private set

    // Profile & Levels flows
    val playerProfile: StateFlow<PlayerProfile> = repository.getPlayerProfile()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerProfile()
        )

    val levelsProgress: StateFlow<List<LevelProgress>> = repository.getAllLevelProgress()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Active Game State
    var activeLevel by mutableStateOf<Level?>(null)
        private set

    var angleInput by mutableStateOf(45f)
    var velocityInput by mutableStateOf(25f)
    var rangeInput by mutableStateOf(50f)

    var projectileState by mutableStateOf(ProjectileState(0f, 0f, 0f, 0f))
        private set

    var gameResult by mutableStateOf(GameResult.IDLE)
        private set

    var attemptsThisLevel by mutableStateOf(0)
        private set

    var showFormulaHint by mutableStateOf(false)

    // Visual particles list updated on every physics frame
    var particles by mutableStateOf<List<Particle>>(emptyList())
        private set

    // Theoretical path (prediction) toggled in game
    var isPredictionEnabled by mutableStateOf(false)

    private var gameLoopJob: Job? = null

    fun navigateTo(screen: AppScreen) {
        currentScreen = screen
        if (screen is AppScreen.GamePlay) {
            setupLevel(screen.level)
        } else {
            resetSimulation()
        }
    }

    fun updateRangeInput(newRange: Float) {
        rangeInput = newRange
        val lvl = activeLevel
        if (lvl != null && lvl.challengeType == "Range") {
            activeLevel = lvl.copy(targetDistance = newRange)
        }
    }

    private fun setupLevel(level: Level) {
        activeLevel = level
        if (level.challengeType == "Angle") {
            angleInput = 45f
            velocityInput = level.fixedVelocity
        } else if (level.challengeType == "Speed") {
            angleInput = level.fixedAngle
            velocityInput = 25f
        } else if (level.challengeType == "Range") {
            angleInput = level.fixedAngle
            velocityInput = level.fixedVelocity
            rangeInput = 40f // start at 40m, player has to find the landing range
            activeLevel = level.copy(targetDistance = rangeInput)
        } else {
            angleInput = 45f
            velocityInput = 25f
        }
        resetSimulation()
        attemptsThisLevel = 0
        showFormulaHint = false
    }

    fun generateProceduralLevel() {
        val randomNum = Random.nextInt(100, 999)
        val d = Random.nextInt(40, 160).toFloat()
        val h = Random.nextInt(-25, 45).toFloat()
        val g = listOf(1.62f, 3.71f, 10.0f, 15.0f, 25.0f).random()
        val w = if (Random.nextBoolean()) Random.nextDouble(-5.0, 5.0).toFloat() else 0f
        
        val modeName = when {
            g < 3.0f -> "Low G Orbit"
            g > 15.0f -> "Heavy Core"
            abs(w) > 3.0f -> "Turbulent Gale"
            else -> "Wild Arena"
        }

        val challType = listOf("Angle", "Speed", "Range").random()

        val level = Level(
            id = "procedural_$randomNum",
            name = "🌌 Random: $modeName",
            description = "Procedural physics challenge. Adapt to the local forces to score a bullseye!",
            difficulty = if (abs(w) > 3f || g > 15f) "Hard" else "Medium",
            targetDistance = if (challType == "Range") 40f else d,
            targetHeight = h,
            targetRadius = 3.8f,
            gravity = g,
            windX = w,
            coinReward = 150,
            hints = listOf(
                "Gravity is ${g} m/s² and wind acceleration is ${w} m/s².",
                "This is a $challType challenge!"
            ),
            challengeType = challType,
            fixedAngle = if (challType == "Range" || challType == "Speed") 45f else 0f,
            fixedVelocity = if (challType == "Range" || challType == "Angle") 25f else 0f
        )
        
        navigateTo(AppScreen.GamePlay(level))
    }

    fun resetSimulation() {
        gameLoopJob?.cancel()
        gameResult = GameResult.IDLE
        projectileState = ProjectileState(0f, 0f, 0f, 0f)
        particles = emptyList()
    }

    fun fireProjectile() {
        if (gameResult == GameResult.FLYING) return

        val level = activeLevel ?: return
        attemptsThisLevel++
        gameResult = GameResult.FLYING
        
        val angleRad = Math.toRadians(angleInput.toDouble()).toFloat()
        val vx0 = velocityInput * cos(angleRad)
        val vy0 = velocityInput * sin(angleRad)
        
        projectileState = ProjectileState(
            x = 0f,
            y = 0f,
            vx = vx0,
            vy = vy0,
            isFlying = true,
            pathPoints = listOf(Pair(0f, 0f))
        )

        // Launch muzzle particles
        spawnMuzzleFlash()

        val skin = getSelectedProjectileSkin()
        
        gameLoopJob = viewModelScope.launch {
            val dt = 0.016f // 16ms simulation tick
            var t = 0f
            
            while (gameResult == GameResult.FLYING) {
                delay(16)
                t += dt
                
                val currentProj = projectileState
                val newVx = currentProj.vx + level.windX * dt
                val newVy = currentProj.vy - level.gravity * dt
                val newX = currentProj.x + newVx * dt
                val newY = currentProj.y + newVy * dt
                
                val points = currentProj.pathPoints.toMutableList()
                // Append point every few steps to avoid bloating
                if (points.size < 500) {
                    points.add(Pair(newX, newY))
                }

                projectileState = ProjectileState(
                    x = newX,
                    y = newY,
                    vx = newVx,
                    vy = newVy,
                    isFlying = true,
                    pathPoints = points
                )

                // Spawn trails based on particle skin
                spawnTrailParticles(newX, newY, skin)

                // Update existing particles
                updateParticles()

                // Check collision
                val dx = newX - level.targetDistance
                val dy = newY - level.targetHeight
                val distanceToTarget = sqrt(dx * dx + dy * dy)

                if (distanceToTarget <= level.targetRadius) {
                    // HIT!
                    gameResult = GameResult.HIT
                    triggerSuccessExplosion(level.targetDistance, level.targetHeight, skin.color)
                    
                    // Reward coins
                    val firstTryBonus = if (attemptsThisLevel == 1) 50 else 0
                    val totalReward = level.coinReward + firstTryBonus
                    repository.addCoins(totalReward)
                    
                    // Save progress if a predefined level
                    if (!level.id.startsWith("procedural_")) {
                        val stars = when (attemptsThisLevel) {
                            1 -> 3
                            2 -> 2
                            else -> 1
                        }
                        repository.completeLevel(level.id, stars, attemptsThisLevel)
                    }
                    break
                }

                // Check boundary out of bounds
                // Floor limit or too far
                val floorLimit = minOf(-45f, level.targetHeight - 20f)
                if (newY < floorLimit || newX > level.targetDistance + 60f || newX < -30f) {
                    gameResult = GameResult.MISS
                    break
                }
            }
        }
    }

    // Purchase & Equip Customizations
    fun buyProjectileSkin(skinId: String, price: Int) {
        viewModelScope.launch {
            val success = repository.unlockProjectile(skinId, price)
            if (success) {
                equipProjectileSkin(skinId)
            }
        }
    }

    fun equipProjectileSkin(skinId: String) {
        viewModelScope.launch {
            val profile = playerProfile.value
            repository.savePlayerProfile(profile.copy(selectedProjectile = skinId))
        }
    }

    fun buyCannonSkin(skinId: String, price: Int) {
        viewModelScope.launch {
            val success = repository.unlockCannon(skinId, price)
            if (success) {
                equipCannonSkin(skinId)
            }
        }
    }

    fun equipCannonSkin(skinId: String) {
        viewModelScope.launch {
            val profile = playerProfile.value
            repository.savePlayerProfile(profile.copy(selectedCannon = skinId))
        }
    }

    fun buyTheme(themeId: String, price: Int) {
        viewModelScope.launch {
            val success = repository.unlockTheme(themeId, price)
            if (success) {
                equipTheme(themeId)
            }
        }
    }

    fun equipTheme(themeId: String) {
        viewModelScope.launch {
            val profile = playerProfile.value
            repository.savePlayerProfile(profile.copy(selectedTheme = themeId))
        }
    }

    // Helper functions to get skins
    fun getSelectedProjectileSkin(): ProjectileSkin {
        val selectedId = playerProfile.value.selectedProjectile
        return Skins.projectiles.find { it.id == selectedId } ?: Skins.projectiles.first()
    }

    fun getSelectedCannonSkin(): CannonSkin {
        val selectedId = playerProfile.value.selectedCannon
        return Skins.cannons.find { it.id == selectedId } ?: Skins.cannons.first()
    }

    fun getSelectedThemeConfig(): ThemeConfig {
        val selectedId = playerProfile.value.selectedTheme
        return Skins.themes.find { it.id == selectedId } ?: Skins.themes.first()
    }

    // Particle effect details
    private fun spawnMuzzleFlash() {
        val count = 20
        val angleRad = Math.toRadians(angleInput.toDouble()).toFloat()
        val mList = mutableListOf<Particle>()
        val cannonColor = getSelectedCannonSkin().accentColor
        
        for (i in 0 until count) {
            val spreadAngle = angleRad + Random.nextDouble(-0.2, 0.2).toFloat()
            val speed = Random.nextDouble(5.0, 15.0).toFloat()
            mList.add(
                Particle(
                    x = 0f,
                    y = 0f,
                    vx = speed * cos(spreadAngle),
                    vy = speed * sin(spreadAngle),
                    color = cannonColor,
                    size = Random.nextDouble(4.0, 8.0).toFloat(),
                    alpha = 1.0f,
                    maxLife = Random.nextInt(15, 30)
                )
            )
        }
        particles = particles + mList
    }

    private fun spawnTrailParticles(x: Float, y: Float, skin: ProjectileSkin) {
        val pColor = skin.color
        val trailColor = skin.trailColor
        val trailCount = when (skin.particleType) {
            "plasma" -> 3
            "fire" -> 4
            "void" -> 2
            "ice" -> 3
            else -> 1
        }
        val tList = mutableListOf<Particle>()
        for (i in 0 until trailCount) {
            val vx = Random.nextDouble(-1.5, 1.5).toFloat()
            val vy = Random.nextDouble(-1.5, 1.5).toFloat()
            val pSize = Random.nextDouble(2.0, 5.0).toFloat() * skin.sizeMultiplier
            tList.add(
                Particle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    color = if (Random.nextBoolean()) pColor else trailColor,
                    size = pSize,
                    alpha = 0.8f,
                    maxLife = Random.nextInt(20, 40)
                )
            )
        }
        particles = particles + tList
    }

    private fun updateParticles() {
        val currentList = particles.toMutableList()
        val iterator = currentList.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            if (p.currentLife >= p.maxLife) {
                iterator.remove()
            } else {
                val index = currentList.indexOf(p)
                if (index != -1) {
                    currentList[index] = p.copy(
                        x = p.x + p.vx * 0.05f,
                        y = p.y + p.vy * 0.05f,
                        currentLife = p.currentLife + 1,
                        alpha = 1.0f - (p.currentLife.toFloat() / p.maxLife.toFloat())
                    )
                }
            }
        }
        particles = currentList
    }

    private fun triggerSuccessExplosion(tx: Float, ty: Float, color: Color) {
        val count = 45
        val pList = mutableListOf<Particle>()
        for (i in 0 until count) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI).toFloat()
            val speed = Random.nextDouble(10.0, 25.0).toFloat()
            pList.add(
                Particle(
                    x = tx,
                    y = ty,
                    vx = speed * cos(angle),
                    vy = speed * sin(angle),
                    color = color,
                    size = Random.nextDouble(5.0, 12.0).toFloat(),
                    alpha = 1.0f,
                    maxLife = Random.nextInt(35, 65)
                )
            )
        }
        // Gold star particles for hit success
        for (i in 0 until 15) {
            val angle = Random.nextDouble(0.0, 2 * Math.PI).toFloat()
            val speed = Random.nextDouble(5.0, 15.0).toFloat()
            pList.add(
                Particle(
                    x = tx,
                    y = ty,
                    vx = speed * cos(angle),
                    vy = speed * sin(angle),
                    color = Color(0xFFFFD700), // Gold
                    size = Random.nextDouble(6.0, 10.0).toFloat(),
                    alpha = 1.0f,
                    maxLife = Random.nextInt(40, 70)
                )
            )
        }
        particles = particles + pList
    }
}
