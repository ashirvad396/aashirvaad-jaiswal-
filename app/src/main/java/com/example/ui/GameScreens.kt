package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.*
import com.example.physics.PhysicsEngine
import com.example.physics.SolverResult
import com.example.physics.ProjectileState
import kotlin.math.*

@Composable
fun AppNavigation(viewModel: GameViewModel) {
    val profile by viewModel.playerProfile.collectAsState()
    val progressList by viewModel.levelsProgress.collectAsState()
    val activeTheme = viewModel.getSelectedThemeConfig()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = activeTheme.backgroundStart
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(activeTheme.backgroundStart, activeTheme.backgroundEnd)
                        )
                    )
                    // Draw soft atmospheric star spots
                    drawCircle(
                        color = activeTheme.gridColor.copy(alpha = 0.05f),
                        radius = size.width * 0.4f,
                        center = Offset(size.width * 0.2f, size.height * 0.2f)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.02f),
                        radius = size.width * 0.1f,
                        center = Offset(size.width * 0.8f, size.height * 0.7f)
                    )
                }
        ) {
            AnimatedContent(
                targetState = viewModel.currentScreen,
                transitionSpec = {
                    fadeIn(animationSpec = tween(250)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is AppScreen.Home -> HomeScreen(viewModel, profile)
                    is AppScreen.LevelSelect -> LevelSelectScreen(viewModel, progressList)
                    is AppScreen.GamePlay -> GamePlayScreen(viewModel, screen.level, profile)
                    is AppScreen.Shop -> ShopScreen(viewModel, profile)
                    is AppScreen.PhysicsFormulas -> FormulasScreen(viewModel)
                }
            }
        }
    }
}

// ========================
// HOME SCREEN
// ========================
@Composable
fun HomeScreen(viewModel: GameViewModel, profile: PlayerProfile) {
    val infiniteTransition = rememberInfiniteTransition(label = "HomeLogoTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoPulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // TOP HEADER: Profile Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0x33000000), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Profile",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Player One",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Coin indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFE6A100).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE6A100).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("coin_indicator")
            ) {
                Icon(
                    imageVector = Icons.Default.MonetizationOn,
                    contentDescription = "Coins",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${profile.coins}",
                    color = Color(0xFFFFD54F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // MIDDLE HERO: Game Logo & Title
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f, fill = false).padding(vertical = 32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Color(0x4422E1FF), Color.Transparent)
                            ),
                            radius = size.width * 0.7f
                        )
                    }
                    .padding(8.dp)
            ) {
                // Load generated game logo
                Image(
                    painter = painterResource(id = R.drawable.img_game_logo_1782471450998),
                    contentDescription = "Trajectory Blaster Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .border(2.dp, Color(0x662196F3), RoundedCornerShape(24.dp))
                        .rotate(pulseScale * 2f - 2f),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "TRAJECTORY",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = "BLASTER 3D",
                color = Color(0xFF22C55E),
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 6.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.offset(y = (-4).dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Master the laws of gravity, wind, and angles to hit targets across 3D-shaded worlds.",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // BOTTOM CONTROLS: Play / Store / Formulas
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { viewModel.navigateTo(AppScreen.LevelSelect) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("play_campaign_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF22C55E)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play Campaign", tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY CAMPAIGN",
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.generateProceduralLevel() },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("infinite_mode_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EA)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.AllInclusive, contentDescription = "Endless", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ENDLESS",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Button(
                    onClick = { viewModel.navigateTo(AppScreen.Shop) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("weapon_shop_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0x22FFFFFF)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(Icons.Outlined.Storefront, contentDescription = "Shop", tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ARMORY",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            Button(
                onClick = { viewModel.navigateTo(AppScreen.PhysicsFormulas) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("formulas_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Outlined.Calculate, contentDescription = "Formulas", tint = Color.White.copy(alpha = 0.7f))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PHYSICS FORMULAS & SCRATCHPAD",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// LEVEL SELECT SCREEN
// ========================
@Composable
fun LevelSelectScreen(viewModel: GameViewModel, progressList: List<LevelProgress>) {
    val context = LocalContext.current
    val completedCount = progressList.count { it.isCompleted }
    val totalLevels = viewModel.predefinedLevels.size
    val progressPercent = if (totalLevels > 0) completedCount.toFloat() / totalLevels else 0f

    val playerRank = when {
        completedCount == 0 -> "Novice Gunner"
        completedCount in 1..2 -> "Trajectory Cadet"
        completedCount in 3..4 -> "Ballistic Sergeant"
        completedCount in 5..6 -> "Gravity Pioneer"
        completedCount == 7 -> "Quantum General"
        else -> "Master of Trajectories"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier
                    .background(Color(0x22FFFFFF), CircleShape)
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "LEVEL PROGRESSION",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Box(modifier = Modifier.size(40.dp)) // spacer for centering
        }

        // PROGRESSION METERS CARD (DIFFICULTY DYNAMICS)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x1F22D55E)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, Color(0x3322C55E)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "COMMAND RANK: ${playerRank.uppercase()}",
                            color = Color(0xFF22C55E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Cleared $completedCount of $totalLevels Predefined Stages",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        color = Color(0xFF22C55E),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressPercent },
                    color = Color(0xFF22C55E),
                    trackColor = Color(0x1AFFFFFF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Progression Info (Explaining how Gravity and Target Distance scale difficulty)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Gravity scale: 1.5 → 25.0 m/s²",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.TrendingFlat,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Distance scale: 50 → 140 m",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(viewModel.predefinedLevels) { level ->
                val progress = progressList.find { it.levelId == level.id }
                val isCompleted = progress?.isCompleted == true
                val stars = progress?.stars ?: 0

                val levelIndex = viewModel.predefinedLevels.indexOf(level)
                val isUnlocked = levelIndex == 0 || (levelIndex > 0 && progressList.any { 
                    it.levelId == viewModel.predefinedLevels[levelIndex - 1].id && it.isCompleted 
                })

                val diffColor = when (level.difficulty) {
                    "Easy" -> Color(0xFF22C55E)
                    "Medium" -> Color(0xFFFFB300)
                    "Hard" -> Color(0xFFFF2E93)
                    else -> Color(0xFF9D4EDD)
                }

                Card(
                    onClick = {
                        if (isUnlocked) {
                            viewModel.navigateTo(AppScreen.GamePlay(level))
                        } else {
                            android.widget.Toast.makeText(
                                context,
                                "Clear stage ${viewModel.predefinedLevels[levelIndex - 1].name} to unlock!",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("level_card_${level.id}")
                        .then(if (!isUnlocked) Modifier.alpha(0.45f) else Modifier),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Color(0x1A22C55E) else if (isUnlocked) Color(0x14FFFFFF) else Color(0x08FFFFFF)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(
                        1.dp,
                        if (isCompleted) Color(0x4422C55E) else if (isUnlocked) Color.White.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = level.difficulty.uppercase(),
                                    color = if (isUnlocked) diffColor else Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier
                                        .background(if (isUnlocked) diffColor.copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                if (level.windX != 0f) {
                                    Icon(
                                        imageVector = Icons.Default.Air,
                                        contentDescription = "Wind",
                                        tint = if (isUnlocked) Color(0xFF29B6F6) else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                if (level.gravity != 10.0f) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Low/High G",
                                        tint = if (isUnlocked) Color(0xFFB39DDB) else Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = level.name,
                                color = if (isUnlocked) Color.White else Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = level.description,
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Mini stats bar showing Gravity & Target Distance escalation clearly
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = if (isUnlocked) Color(0xFFB39DDB) else Color.Gray,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "g = ${level.gravity} m/s²",
                                        color = if (isUnlocked) Color(0xFFB39DDB).copy(alpha = 0.8f) else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = if (isUnlocked) Color(0xFF00E5FF) else Color.Gray,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Target: ${level.targetDistance.toInt()}m",
                                        color = if (isUnlocked) Color(0xFF00E5FF).copy(alpha = 0.8f) else Color.Gray,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Surface(
                                    color = if (isUnlocked) Color.White.copy(alpha = 0.08f) else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = " ${level.challengeType.uppercase()} ",
                                        color = if (isUnlocked) Color.White.copy(alpha = 0.7f) else Color.Gray,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            if (isUnlocked) {
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    repeat(3) { starIndex ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Star",
                                            tint = if (starIndex < stars) Color(0xFFFFD54F) else Color.White.copy(alpha = 0.15f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .background(Color(0xFFE6A100).copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+${level.coinReward}",
                                        color = Color(0xFFFFD54F),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked Stage",
                                    tint = Color.White.copy(alpha = 0.35f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "LOCKED",
                                    color = Color.White.copy(alpha = 0.35f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================
// GAMEPLAY PLAYGROUND
// ========================
@Composable
fun GamePlayScreen(viewModel: GameViewModel, level: Level, profile: PlayerProfile) {
    var isMuted by remember { mutableStateOf(false) }
    var showMathSolutions by remember { mutableStateOf(false) }

    val activeTheme = viewModel.getSelectedThemeConfig()
    val projState = viewModel.projectileState
    val launcherSkin = viewModel.getSelectedHighlightCannonSkin() ?: viewModel.getSelectedCannonSkin()
    val projSkin = viewModel.getSelectedProjectileSkin()

    // Interactive solver to display real live solutions
    val solverResult = remember(viewModel.velocityInput, level) {
        PhysicsEngine.solveAnglesForTarget(
            v0 = viewModel.velocityInput,
            d = level.targetDistance,
            h = level.targetHeight,
            g = level.gravity
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(12.dp)
    ) {
        // TOP CONTROLS & LEVEL INFO
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { viewModel.navigateTo(AppScreen.LevelSelect) },
                    modifier = Modifier.background(Color(0x14FFFFFF), CircleShape).size(38.dp)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = level.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Attempts: ${viewModel.attemptsThisLevel}",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Prediction Helper Toggle
                IconButton(
                    onClick = { viewModel.isPredictionEnabled = !viewModel.isPredictionEnabled },
                    modifier = Modifier
                        .background(
                            if (viewModel.isPredictionEnabled) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0x14FFFFFF),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (viewModel.isPredictionEnabled) Color(0xFF00E5FF) else Color.Transparent,
                            CircleShape
                        )
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Grain,
                        contentDescription = "Toggle Trajectory Predictor",
                        tint = if (viewModel.isPredictionEnabled) Color(0xFF00E5FF) else Color.White
                    )
                }

                // Mathematical formula assistant helper button
                IconButton(
                    onClick = { showMathSolutions = !showMathSolutions },
                    modifier = Modifier
                        .background(
                            if (showMathSolutions) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color(0x14FFFFFF),
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (showMathSolutions) Color(0xFF4CAF50) else Color.Transparent,
                            CircleShape
                        )
                        .size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Calculate,
                        contentDescription = "Calculations Solver",
                        tint = if (showMathSolutions) Color(0xFF4CAF50) else Color.White
                    )
                }
            }
        }

        // Live status metrics (Gravity / Wind / Distance)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0x0AFFFFFF)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TARGET DIST", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                    Text("${level.targetDistance}m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("TARGET HEIGHT", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                    Text("${level.targetHeight}m", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("GRAVITY", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                    Text("${level.gravity} m/s²", color = Color(0xFFB39DDB), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("WIND FORCE", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
                    val wLabel = if (level.windX == 0f) "0" else "${level.windX} m/s²"
                    Text(wLabel, color = if (level.windX < 0) Color(0xFFE57373) else if (level.windX > 0) Color(0xFF81C784) else Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // PHYSICAL GAME CANVAS
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF020408))
                .border(1.dp, activeTheme.gridColor.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
        ) {
            // Live simulation rendering canvas
            PhysicsBlasterCanvas(
                viewModel = viewModel,
                level = level,
                projState = projState,
                launcherSkin = launcherSkin,
                projSkin = projSkin,
                activeTheme = activeTheme
            )

            // Result popup banners
            androidx.compose.animation.AnimatedVisibility(
                visible = viewModel.gameResult == GameResult.HIT || viewModel.gameResult == GameResult.MISS,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(Color(0xEE0A0E1A), RoundedCornerShape(20.dp))
                        .border(1.dp, if (viewModel.gameResult == GameResult.HIT) Color(0xFF22C55E) else Color(0xFFFF2E93), RoundedCornerShape(20.dp))
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = if (viewModel.gameResult == GameResult.HIT) Icons.Default.CheckCircle else Icons.Default.Cancel,
                        contentDescription = null,
                        tint = if (viewModel.gameResult == GameResult.HIT) Color(0xFF22C55E) else Color(0xFFFF2E93),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (viewModel.gameResult == GameResult.HIT) "TARGET BLOCKED!" else "TRAJECTORY MISSED!",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (viewModel.gameResult == GameResult.HIT) "Excellent calculations, physics commander!" else "Gravity overcame the shell. Adjust power or angle!",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(220.dp)
                    )
                    
                    if (viewModel.gameResult == GameResult.HIT) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+${level.coinReward} Gold Credits",
                                color = Color(0xFFFFD54F),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { viewModel.resetSimulation() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Retry", color = Color.White)
                        }
                        if (viewModel.gameResult == GameResult.HIT) {
                            Button(
                                onClick = { viewModel.navigateTo(AppScreen.LevelSelect) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Continue", color = Color.White)
                            }
                        }
                    }
                }
            }

            // Quick live formulas helper panel overlay (Math solutions helper)
            androidx.compose.animation.AnimatedVisibility(
                visible = showMathSolutions,
                enter = slideInHorizontally() + fadeIn(),
                exit = slideOutHorizontally() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .width(230.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xEE090D1A)),
                    border = BorderStroke(1.dp, Color(0x444CAF50)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "💡 TRAJECTORY GUIDE",
                            color = Color(0xFF81C784),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "To strike (distance = ${level.targetDistance}m, height = ${level.targetHeight}m):",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (solverResult.isReachable) {
                            Text(
                                text = "Perfect Target Angles:",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            solverResult.solutions.forEachIndexed { idx, angle ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Path ${idx + 1}:",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                    Text(
                                        text = "${String.format("%.2f", angle)}°",
                                        color = Color(0xFF00FFCC),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = solverResult.reason,
                                color = Color(0xFFE57373),
                                fontSize = 10.sp,
                                lineHeight = 13.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Formula applied:\ny = x·tan(θ) - [g·x²(1+tan²θ)] / (2v0²)",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 11.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // DYNAMIC CONTROLS (CHALLENGE-AWARE SLIDERS)
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // Challenge Badge / Description
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (level.challengeType) {
                                "Angle" -> Color(0xFF22C55E).copy(alpha = 0.2f)
                                "Speed" -> Color(0xFF00E5FF).copy(alpha = 0.2f)
                                else -> Color(0xFFFFB300).copy(alpha = 0.2f)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = " CHALLENGE: ${level.challengeType.uppercase()} ",
                                color = when (level.challengeType) {
                                    "Angle" -> Color(0xFF22C55E)
                                    "Speed" -> Color(0xFF00E5FF)
                                    else -> Color(0xFFFFB300)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = when (level.challengeType) {
                            "Angle" -> "Speed is locked at ${level.fixedVelocity} m/s"
                            "Speed" -> "Angle is locked at ${level.fixedAngle}°"
                            else -> "Angle: ${level.fixedAngle}°, Speed: ${level.fixedVelocity} m/s"
                        },
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (level.challengeType == "Angle") {
                    // ANGLE SLIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RotateRight, contentDescription = null, tint = Color(0xFF22C55E), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LAUNCH ANGLE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${String.format("%.1f", viewModel.angleInput)}°",
                            color = Color(0xFF22C55E),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Slider(
                        value = viewModel.angleInput,
                        onValueChange = { viewModel.angleInput = it },
                        valueRange = 0f..90f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF22C55E),
                            activeTrackColor = Color(0xFF22C55E),
                            inactiveTrackColor = Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.testTag("angle_slider")
                    )
                }

                if (level.challengeType == "Speed") {
                    // VELOCITY SLIDER
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("LAUNCH SPEED", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${String.format("%.1f", viewModel.velocityInput)} m/s",
                            color = Color(0xFF00E5FF),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Slider(
                        value = viewModel.velocityInput,
                        onValueChange = { viewModel.velocityInput = it },
                        valueRange = 10f..75f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF00E5FF),
                            activeTrackColor = Color(0xFF00E5FF),
                            inactiveTrackColor = Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.testTag("velocity_slider")
                    )
                }

                if (level.challengeType == "Range") {
                    // TARGET RANGE SLIDER (User predicts where the ball lands!)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ESTIMATE TARGET RANGE", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = "${String.format("%.1f", viewModel.rangeInput)} m",
                            color = Color(0xFFFFB300),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    Slider(
                        value = viewModel.rangeInput,
                        onValueChange = { viewModel.updateRangeInput(it) },
                        valueRange = 10f..150f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFFB300),
                            activeTrackColor = Color(0xFFFFB300),
                            inactiveTrackColor = Color(0x22FFFFFF)
                        ),
                        modifier = Modifier.testTag("range_slider")
                    )
                    
                    Text(
                        text = "Calculate R = (v0² · sin(2θ)) / g and slide target to that spot!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 10.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons: FIRE / RESET
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { viewModel.resetSimulation() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                        modifier = Modifier.weight(1.5f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("RESET", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { viewModel.fireProjectile() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewModel.gameResult == GameResult.FLYING) Color(0xFFFF9800) else Color(0xFF22C55E)
                        ),
                        modifier = Modifier.weight(2.5f).height(48.dp).testTag("fire_button"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = viewModel.gameResult != GameResult.FLYING
                    ) {
                        Icon(
                            imageVector = if (viewModel.gameResult == GameResult.FLYING) Icons.Default.HourglassEmpty else Icons.Default.Launch,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (viewModel.gameResult == GameResult.FLYING) "SHELL FLYING..." else "LAUNCH FIRE!",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

// ========================
// CORE PHYSICS CANVAS
// ========================
@Composable
fun PhysicsBlasterCanvas(
    viewModel: GameViewModel,
    level: Level,
    projState: ProjectileState,
    launcherSkin: CannonSkin,
    projSkin: ProjectileSkin,
    activeTheme: ThemeConfig
) {
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .testTag("physics_simulation_canvas")
    ) {
        val width = size.width
        val height = size.height

        // Define scaling factor of meters to canvas pixels.
        val startX = 50f
        val startY = height - 80f

        val maxMetersX = maxOf(160f, level.targetDistance + 40f)
        val maxMetersY = maxOf(75f, maxOf(abs(level.targetHeight), 35f) + 30f)

        val scaleX = (width - 100f) / maxMetersX
        val scaleY = (height - 130f) / maxMetersY
        val scale = minOf(scaleX, scaleY)

        fun mapCoords(mx: Float, my: Float): Offset {
            val px = startX + mx * scale
            val py = startY - my * scale
            return Offset(px, py)
        }

        val gridInterval = 20f
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 10f), 0f)

        drawLine(
            color = activeTheme.gridColor.copy(alpha = 0.4f),
            start = Offset(0f, startY),
            end = Offset(width, startY),
            strokeWidth = 2f
        )

        drawLine(
            color = activeTheme.gridColor.copy(alpha = 0.2f),
            start = Offset(startX, 0f),
            end = Offset(startX, height),
            strokeWidth = 1f,
            pathEffect = pathEffect
        )

        for (i in 1..(maxMetersX / gridInterval).toInt() + 1) {
            val mx = i * gridInterval
            val pos = mapCoords(mx, 0f)
            if (pos.x < width) {
                drawLine(
                    color = activeTheme.gridColor.copy(alpha = 0.1f),
                    start = Offset(pos.x, 0f),
                    end = Offset(pos.x, height),
                    strokeWidth = 1f
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "${mx.toInt()}m",
                    pos.x - 15f,
                    startY + 25f,
                    android.graphics.Paint().apply {
                        color = activeTheme.labelColor.copy(alpha = 0.5f).hashCode()
                        textSize = 24f
                    }
                )
            }
        }

        for (i in -4..(maxMetersY / gridInterval).toInt() + 1) {
            if (i == 0) continue
            val my = i * gridInterval
            val pos = mapCoords(0f, my)
            if (pos.y > 0f && pos.y < height) {
                drawLine(
                    color = activeTheme.gridColor.copy(alpha = 0.1f),
                    start = Offset(0f, pos.y),
                    end = Offset(width, pos.y),
                    strokeWidth = 1f,
                    pathEffect = pathEffect
                )
                drawContext.canvas.nativeCanvas.drawText(
                    "${my.toInt()}m",
                    10f,
                    pos.y + 8f,
                    android.graphics.Paint().apply {
                        color = activeTheme.labelColor.copy(alpha = 0.5f).hashCode()
                        textSize = 24f
                    }
                )
            }
        }

        if (viewModel.isPredictionEnabled) {
            val predictedPoints = PhysicsEngine.generateTheoreticalTrajectory(
                v0 = viewModel.velocityInput,
                angleDeg = viewModel.angleInput,
                g = level.gravity,
                windX = level.windX
            )

            for (i in 0 until predictedPoints.size - 1) {
                val pt1 = mapCoords(predictedPoints[i].first, predictedPoints[i].second)
                val pt2 = mapCoords(predictedPoints[i + 1].first, predictedPoints[i + 1].second)
                
                if (pt1.x in 0f..width && pt1.y in 0f..height && pt2.x in 0f..width && pt2.y in 0f..height) {
                    drawLine(
                        color = Color(0x7700E5FF),
                        start = pt1,
                        end = pt2,
                        strokeWidth = 2.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                    )
                }
            }
        }

        if (level.targetHeight > 0f) {
            val targetBase = mapCoords(level.targetDistance, 0f)
            val targetTopLeft = mapCoords(level.targetDistance - 8f, level.targetHeight)
            val targetTopRight = mapCoords(level.targetDistance + 8f, level.targetHeight)
            val plateauPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(targetTopLeft.x, targetTopLeft.y)
                lineTo(targetTopRight.x, targetTopRight.y)
                lineTo(targetBase.x + 15f * scale, startY)
                lineTo(targetBase.x - 15f * scale, startY)
                close()
            }
            drawPath(
                path = plateauPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                )
            )
            drawPath(
                path = plateauPath,
                color = Color(0x3300E5FF),
                style = Stroke(width = 1.5f)
            )
        } else if (level.targetHeight < 0f) {
            val targetBase = mapCoords(level.targetDistance, level.targetHeight)
            val leftCliffEdge = mapCoords(level.targetDistance - 25f, 0f)
            val rightCliffEdge = mapCoords(level.targetDistance + 25f, 0f)

            val ravinePath = androidx.compose.ui.graphics.Path().apply {
                moveTo(leftCliffEdge.x, leftCliffEdge.y)
                lineTo(targetBase.x - 8f * scale, targetBase.y)
                lineTo(targetBase.x + 8f * scale, targetBase.y)
                lineTo(rightCliffEdge.x, rightCliffEdge.y)
                lineTo(width, startY)
                lineTo(leftCliffEdge.x, startY)
                close()
            }
            drawPath(
                path = ravinePath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0A0F1D), Color(0xFF1E293B))
                )
            )
            drawPath(
                path = ravinePath,
                color = Color(0x22FF2E93),
                style = Stroke(width = 1.5f)
            )
        }

        val targetPos = mapCoords(level.targetDistance, level.targetHeight)
        val targetSizePx = level.targetRadius * scale
        
        drawCircle(
            color = Color(0x1800FFCC),
            radius = targetSizePx,
            center = targetPos
        )
        drawCircle(
            color = Color(0x8800FFCC),
            radius = targetSizePx,
            center = targetPos,
            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFF5252), Color(0xFFD32F2F))
            ),
            radius = 14f,
            center = targetPos
        )
        drawCircle(
            color = Color(0xFF00FFCC),
            radius = 20f,
            center = targetPos,
            style = Stroke(width = 3f)
        )

        if (projState.isFlying) {
            val points = projState.pathPoints
            for (i in 0 until points.size - 1) {
                val pt1 = mapCoords(points[i].first, points[i].second)
                val pt2 = mapCoords(points[i + 1].first, points[i + 1].second)
                
                if (pt1.x in 0f..width && pt1.y in 0f..height && pt2.x in 0f..width && pt2.y in 0f..height) {
                    val progressAlpha = i.toFloat() / maxOf(1f, points.size.toFloat())
                    drawLine(
                        color = projSkin.trailColor.copy(alpha = progressAlpha * 0.8f),
                        start = pt1,
                        end = pt2,
                        strokeWidth = 6f * projSkin.sizeMultiplier
                    )
                }
            }
        }

        viewModel.particles.forEach { p ->
            val pPos = mapCoords(p.x, p.y)
            if (pPos.x in 0f..width && pPos.y in 0f..height) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = pPos
                )
            }
        }

        val launchOrigin = mapCoords(0f, 0f)
        val angleRad = Math.toRadians(viewModel.angleInput.toDouble()).toFloat()
        val barrelLen = 50f * launcherSkin.barrelLengthMultiplier
        val endX = launchOrigin.x + barrelLen * cos(angleRad)
        val endY = launchOrigin.y - barrelLen * sin(angleRad)

        // Draw the aiming person (stylized physics athlete holding/aiming the ball)
        val shoulder = Offset(launchOrigin.x - 12f, launchOrigin.y - 8f)
        val torsoBase = Offset(launchOrigin.x - 16f, launchOrigin.y + 35f)
        val headCenter = Offset(launchOrigin.x - 14f, launchOrigin.y - 24f)

        // 1. Draw head (glowing neon sporty design)
        drawCircle(
            color = Color.White,
            radius = 11f,
            center = headCenter
        )
        // Draw face visor / details
        drawCircle(
            color = launcherSkin.accentColor,
            radius = 6f,
            center = Offset(headCenter.x + 4f, headCenter.y)
        )

        // 2. Draw torso
        drawLine(
            color = Color.White,
            start = Offset(launchOrigin.x - 14f, launchOrigin.y - 13f),
            end = torsoBase,
            strokeWidth = 6f
        )

        // 3. Draw legs standing on the ground terrain (startY)
        drawLine(
            color = Color.White,
            start = torsoBase,
            end = Offset(launchOrigin.x - 28f, startY),
            strokeWidth = 5f
        )
        drawLine(
            color = Color.White,
            start = torsoBase,
            end = Offset(launchOrigin.x - 6f, startY),
            strokeWidth = 5f
        )

        // 4. Draw back arm (balancing/idle)
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = Offset(launchOrigin.x - 14f, launchOrigin.y - 2f),
            end = Offset(launchOrigin.x - 30f, launchOrigin.y + 15f),
            strokeWidth = 4f
        )

        // 5. Draw throwing arm (stretches and aims towards the ball's release position endX, endY)
        drawLine(
            color = launcherSkin.accentColor,
            start = shoulder,
            end = Offset(endX, endY),
            strokeWidth = 6f
        )
        drawCircle(
            color = launcherSkin.accentColor,
            radius = 5f,
            center = Offset(endX, endY)
        )

        // If projectile is not yet flying, show the ball in the character's hand ready to launch
        if (!projState.isFlying) {
            drawCircle(
                color = projSkin.trailColor.copy(alpha = 0.4f),
                radius = 15f * projSkin.sizeMultiplier,
                center = Offset(endX, endY)
            )
            drawCircle(
                color = projSkin.color,
                radius = 9f * projSkin.sizeMultiplier,
                center = Offset(endX, endY)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 3.5f * projSkin.sizeMultiplier,
                center = Offset(endX - 3f, endY - 3f)
            )
        }

        if (projState.isFlying) {
            val pPos = mapCoords(projState.x, projState.y)
            if (pPos.x in 0f..width && pPos.y in 0f..height) {
                drawCircle(
                    color = projSkin.trailColor.copy(alpha = 0.4f),
                    radius = 16f * projSkin.sizeMultiplier,
                    center = pPos
                )
                drawCircle(
                    color = projSkin.color,
                    radius = 9f * projSkin.sizeMultiplier,
                    center = pPos
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 3f * projSkin.sizeMultiplier,
                    center = Offset(pPos.x - 3f, pPos.y - 3f)
                )
            }
        }
    }
}

// Ext helper for preview skin
fun GameViewModel.getSelectedHighlightCannonSkin(): CannonSkin? {
    val selectedId = playerProfile.value.selectedCannon
    return Skins.cannons.find { it.id == selectedId }
}

// ========================
// SHOP SCREEN (ARMORY)
// ========================
@Composable
fun ShopScreen(viewModel: GameViewModel, profile: PlayerProfile) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier.background(Color(0x22FFFFFF), CircleShape).size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "ARMORY & SKINS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(Color(0xFFE6A100).copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE6A100).copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Default.MonetizationOn, contentDescription = "Coins", tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${profile.coins}",
                    color = Color(0xFFFFD54F),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        var selectedTab by remember { mutableStateOf(0) }
        val tabs = listOf("PROJECTILES", "CANNONS", "THEMES")
        
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = Color(0xFF22C55E)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = label,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (selectedTab == 0) {
                items(Skins.projectiles) { skin ->
                    val unlockedList = profile.unlockedProjectiles.split(",").map { it.trim() }
                    val isUnlocked = skin.id in unlockedList
                    val isEquipped = profile.selectedProjectile == skin.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) Color(0x2222C55E) else Color(0x14FFFFFF)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isEquipped) Color(0xFF22C55E) else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .border(1.dp, skin.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(skin.color, CircleShape)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(skin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(skin.description, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isEquipped) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            } else if (isUnlocked) {
                                Button(
                                    onClick = { viewModel.equipProjectileSkin(skin.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("EQUIP", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buyProjectileSkin(skin.id, skin.price) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6A100)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    enabled = profile.coins >= skin.price
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MonetizationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${skin.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                items(Skins.cannons) { skin ->
                    val unlockedList = profile.unlockedCannons.split(",").map { it.trim() }
                    val isUnlocked = skin.id in unlockedList
                    val isEquipped = profile.selectedCannon == skin.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) Color(0x2222C55E) else Color(0x14FFFFFF)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isEquipped) Color(0xFF22C55E) else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .border(1.dp, skin.accentColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Row {
                                    Box(modifier = Modifier.size(12.dp).background(skin.primaryColor, CircleShape))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Box(modifier = Modifier.size(12.dp).background(skin.accentColor, CircleShape))
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(skin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(skin.description, color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isEquipped) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            } else if (isUnlocked) {
                                Button(
                                    onClick = { viewModel.equipCannonSkin(skin.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("EQUIP", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buyCannonSkin(skin.id, skin.price) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6A100)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    enabled = profile.coins >= skin.price
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MonetizationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${skin.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                items(Skins.themes) { skin ->
                    val unlockedList = profile.unlockedThemes.split(",").map { it.trim() }
                    val isUnlocked = skin.id in unlockedList
                    val isEquipped = profile.selectedTheme == skin.id

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isEquipped) Color(0x2222C55E) else Color(0x14FFFFFF)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isEquipped) Color(0xFF22C55E) else Color.White.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                                            colors = listOf(skin.backgroundStart, skin.backgroundEnd)
                                        ),
                                        shape = CircleShape
                                    )
                                    .border(1.dp, skin.gridColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Palette, null, tint = skin.labelColor, modifier = Modifier.size(20.dp))
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(skin.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("A unique atmospheric theme for coordinate space canvas drawings.", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp, lineHeight = 14.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            if (isEquipped) {
                                Text(
                                    text = "ACTIVE",
                                    color = Color(0xFF22C55E),
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                            } else if (isUnlocked) {
                                Button(
                                    onClick = { viewModel.equipTheme(skin.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x22FFFFFF)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("EQUIP", fontSize = 11.sp)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.buyTheme(skin.id, skin.price) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE6A100)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    enabled = profile.coins >= skin.price
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MonetizationOn, null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("${skin.price}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================
// PHYSICS FORMULAS SCREEN
// ========================
@Composable
fun FormulasScreen(viewModel: GameViewModel) {
    var v0Val by remember { mutableStateOf("25") }
    var distVal by remember { mutableStateOf("60") }
    var heightVal by remember { mutableStateOf("0") }
    var gravityVal by remember { mutableStateOf("10.0") }
    var calculatorResult by remember { mutableStateOf<SolverResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.Home) },
                modifier = Modifier.background(Color(0x22FFFFFF), CircleShape).size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Text(
                text = "PHYSICS COMPASS",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x14FFFFFF)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📐 The Projectile Equation",
                    color = Color(0xFF00FFCC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The vertical position (y) at a horizontal distance (x) is given by substituting time (t) into gravity drag equations:",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "y = x · tan(θ) - [g · x²] / [2 · v₀² · cos²(θ)]",
                        color = Color(0xFF22C55E),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Replacing 1/cos²(θ) with (1 + tan²θ) converts this into a quadratic in terms of tan(θ), allowing us to solve the exact firing angles mathematically!",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0x1A6200EA)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFF6200EA).copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🧪 Interactive Physics Solver",
                    color = Color(0xFFB39DDB),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Input custom launch parameters and targets to compute the corresponding solutions instantly.",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = v0Val,
                        onValueChange = { v0Val = it },
                        label = { Text("v₀ (m/s)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFB39DDB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = distVal,
                        onValueChange = { distVal = it },
                        label = { Text("Distance (m)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFB39DDB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = heightVal,
                        onValueChange = { heightVal = it },
                        label = { Text("Height (m)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFB39DDB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = gravityVal,
                        onValueChange = { gravityVal = it },
                        label = { Text("g (m/s²)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFB39DDB),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedLabelColor = Color(0xFFB39DDB),
                            unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val v = v0Val.toFloatOrNull() ?: 25f
                        val d = distVal.toFloatOrNull() ?: 60f
                        val h = heightVal.toFloatOrNull() ?: 0f
                        val g = gravityVal.toFloatOrNull() ?: 10.0f
                        calculatorResult = PhysicsEngine.solveAnglesForTarget(v, d, h, g)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EA))
                ) {
                    Text("SOLVE QUADRATIC COUPLING", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                calculatorResult?.let { result ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        if (result.isReachable) {
                            Text(
                                "✅ SOLVABLE TARGET DETECTED!",
                                color = Color(0xFF22C55E),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            result.solutions.forEachIndexed { idx, angle ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Trajectory ${idx + 1} Launch Angle:", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                    Text("${String.format("%.3f", angle)}°", color = Color(0xFF00FFCC), fontWeight = FontWeight.Black, fontSize = 13.sp)
                                }
                            }
                        } else {
                            Text(
                                "❌ TARGET UNREACHABLE",
                                color = Color(0xFFFF2E93),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(result.reason, color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, lineHeight = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
