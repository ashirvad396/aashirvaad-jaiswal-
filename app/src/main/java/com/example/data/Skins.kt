package com.example.data

import androidx.compose.ui.graphics.Color

data class ProjectileSkin(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val color: Color,
    val trailColor: Color,
    val particleType: String, // "smoke", "plasma", "fire", "void", "ice"
    val sizeMultiplier: Float = 1.0f
)

data class CannonSkin(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val primaryColor: Color,
    val accentColor: Color,
    val barrelLengthMultiplier: Float = 1.0f
)

data class ThemeConfig(
    val id: String,
    val name: String,
    val price: Int,
    val backgroundStart: Color,
    val backgroundEnd: Color,
    val gridColor: Color,
    val labelColor: Color
)

object Skins {
    val projectiles = listOf(
        ProjectileSkin(
            id = "standard",
            name = "Iron Shell",
            description = "A standard high-density military-grade iron shell.",
            price = 0,
            color = Color(0xFF90A4AE),
            trailColor = Color(0x66CFD8DC),
            particleType = "smoke"
        ),
        ProjectileSkin(
            id = "plasma",
            name = "Plasma Pulse",
            description = "Superheated plasma container. Leaves a blazing neon-pink trail.",
            price = 150,
            color = Color(0xFFFF2E93),
            trailColor = Color(0x88FF2E93),
            particleType = "plasma"
        ),
        ProjectileSkin(
            id = "meteor",
            name = "Golden Meteor",
            description = "Harnesses the destructive power of a falling meteor. Spits fire!",
            price = 250,
            color = Color(0xFFFFB300),
            trailColor = Color(0x88FF6F00),
            particleType = "fire"
        ),
        ProjectileSkin(
            id = "singularity",
            name = "Gravity Void",
            description = "A portable micro-black hole. Tears through air resistance.",
            price = 400,
            color = Color(0xFF6200EA),
            trailColor = Color(0x886200EA),
            particleType = "void",
            sizeMultiplier = 1.2f
        ),
        ProjectileSkin(
            id = "comet",
            name = "Glacial Comet",
            description = "Frozen absolute zero core that condenses moisture into ice particles.",
            price = 300,
            color = Color(0xFF00E5FF),
            trailColor = Color(0x8800B0FF),
            particleType = "ice"
        )
    )

    val cannons = listOf(
        CannonSkin(
            id = "steel",
            name = "Steel Patriot",
            description = "Standard double-reinforced dark carbon steel launcher.",
            price = 0,
            primaryColor = Color(0xFF37474F),
            accentColor = Color(0xFFFF5722)
        ),
        CannonSkin(
            id = "laser",
            name = "Laser Fusion Core",
            description = "Advanced magnetic accelerator. Fires with clean geometric styling.",
            price = 200,
            primaryColor = Color(0xFF1A237E),
            accentColor = Color(0xFF00E676)
        ),
        CannonSkin(
            id = "arc",
            name = "Arc Lightning Titan",
            description = "Tesla electromagnetic field coils drive the projectile with raw power.",
            price = 350,
            primaryColor = Color(0xFF311B92),
            accentColor = Color(0xFF00E5FF)
        )
    )

    val themes = listOf(
        ThemeConfig(
            id = "cosmic",
            name = "Cosmic Void",
            price = 0,
            backgroundStart = Color(0xFF070B19),
            backgroundEnd = Color(0xFF131A35),
            gridColor = Color(0x1E4F83C3),
            labelColor = Color(0xFF8C9EFF)
        ),
        ThemeConfig(
            id = "synthwave",
            name = "Neon Retro Synth",
            price = 150,
            backgroundStart = Color(0xFF1F0321),
            backgroundEnd = Color(0xFF050112),
            gridColor = Color(0x22FF007F),
            labelColor = Color(0xFFFF007F)
        ),
        ThemeConfig(
            id = "chrono",
            name = "Chrono Copper",
            price = 200,
            backgroundStart = Color(0xFF140C03),
            backgroundEnd = Color(0xFF2E1C0A),
            gridColor = Color(0x22FF9F1C),
            labelColor = Color(0xFFFF9F1C)
        )
    )
}
