package com.example.game.model

import androidx.compose.ui.graphics.Color

data class CoreLevelData(
    val level: Int,
    val name: String,
    val baseScore: Int,
    val radiusDp: Float,
    val mass: Float,
    val dropWeightPct: Int,
    val positiveColor: Color,
    val negativeColor: Color,
    val glowColor: Color,
    val description: String
)

object CoreLevelRegistry {
    // Calibrated size progression (growth factor ~1.32–1.36 for early-mid tiers):
    // L1: 28dp (starts comfortable and clear)
    // L2: 38dp (1.357x - clear visual step)
    // L3: 51dp (1.342x - space pressure begins)
    // L4: 68dp (1.333x - significant spatial occupancy)
    // L5: 90dp (1.323x - takes half the chamber width)
    // L6: 117dp (1.300x)
    // L7: 144dp (1.230x)
    // L8: 170dp (1.180x)
    // L9: 194dp (1.141x)
    // L10: 216dp (apex singularity)
    val LEVELS = listOf(
        CoreLevelData(
            level = 1,
            name = "Spark",
            baseScore = 1,
            radiusDp = 28f,
            mass = 1.0f,
            dropWeightPct = 65,
            positiveColor = Color(0xFFA5D6A7), // Soft Sage Green
            negativeColor = Color(0xFF90CAF9), // Soft Sky Blue
            glowColor = Color(0xFF90CAF9),
            description = "Micro-charge energy spark."
        ),
        CoreLevelData(
            level = 2,
            name = "Cell",
            baseScore = 3,
            radiusDp = 38f,
            mass = 2.0f,
            dropWeightPct = 25,
            positiveColor = Color(0xFFFFCC80), // Soft Apricot
            negativeColor = Color(0xFFFFAB91), // Soft Peach/Salmon
            glowColor = Color(0xFFFFAB91),
            description = "Stabilized elemental core cell."
        ),
        CoreLevelData(
            level = 3,
            name = "Node",
            baseScore = 6,
            radiusDp = 51f,
            mass = 3.8f,
            dropWeightPct = 10,
            positiveColor = Color(0xFF80CBC4), // Soft Mint
            negativeColor = Color(0xFF81D4FA), // Soft Cyan
            glowColor = Color(0xFF80CBC4),
            description = "Network node core."
        ),
        CoreLevelData(
            level = 4,
            name = "Core",
            baseScore = 12,
            radiusDp = 68f,
            mass = 6.8f,
            dropWeightPct = 6,
            positiveColor = Color(0xFFCE93D8), // Soft Orchid
            negativeColor = Color(0xFFC5CAE9), // Soft Lavender
            glowColor = Color(0xFFC5CAE9),
            description = "Dense energy core matrix."
        ),
        CoreLevelData(
            level = 5,
            name = "Reactor",
            baseScore = 24,
            radiusDp = 90f,
            mass = 11.5f,
            dropWeightPct = 2,
            positiveColor = Color(0xFFFFF59D), // Butter Yellow
            negativeColor = Color(0xFFFFE082), // Soft Amber
            glowColor = Color(0xFFFFE082),
            description = "High-output reactor core."
        ),
        CoreLevelData(
            level = 6,
            name = "Pulse",
            baseScore = 48,
            radiusDp = 117f,
            mass = 18.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFF48FB1), // Soft Rose
            negativeColor = Color(0xFFB39DDB), // Soft Lilac
            glowColor = Color(0xFFB39DDB),
            description = "Harmonic pulse sphere."
        ),
        CoreLevelData(
            level = 7,
            name = "Plasma",
            baseScore = 96,
            radiusDp = 144f,
            mass = 27.5f,
            dropWeightPct = 0,
            positiveColor = Color(0xFF80DEEA), // Soft Aqua
            negativeColor = Color(0xFF4DD0E1), // Cyan Tint
            glowColor = Color(0xFF80DEEA),
            description = "Superheated plasma cluster."
        ),
        CoreLevelData(
            level = 8,
            name = "Nova",
            baseScore = 192,
            radiusDp = 170f,
            mass = 39.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFFF8A65), // Soft Tangerine
            negativeColor = Color(0xFFFF7043), // Coral
            glowColor = Color(0xFFFF8A65),
            description = "Stellar nova fragment."
        ),
        CoreLevelData(
            level = 9,
            name = "Star",
            baseScore = 384,
            radiusDp = 194f,
            mass = 54.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFFFF176), // Soft Sunshine
            negativeColor = Color(0xFFFFEE58), // Radiant Gold
            glowColor = Color(0xFFFFF176),
            description = "Radiant star core."
        ),
        CoreLevelData(
            level = 10,
            name = "Singularity",
            baseScore = 768,
            radiusDp = 216f,
            mass = 75.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFD1C4E9), // Cosmic Mist
            negativeColor = Color(0xFFB388FF), // Deep Violet
            glowColor = Color(0xFFB388FF),
            description = "Cosmic singularity."
        )
    )

    fun getInfo(level: Int): CoreLevelData {
        val index = (level - 1).coerceIn(0, LEVELS.size - 1)
        return LEVELS[index]
    }

    fun calculateRadius(level: Int, densityFactor: Float = 1.0f): Float {
        val info = getInfo(level)
        return info.radiusDp * densityFactor
    }
}
