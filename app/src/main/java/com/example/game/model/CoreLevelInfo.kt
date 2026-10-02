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
    val LEVELS = listOf(
        CoreLevelData(
            level = 1,
            name = "Spark",
            baseScore = 1,
            radiusDp = 24f,
            mass = 1.0f,
            dropWeightPct = 35,
            positiveColor = Color(0xFFFF4081),
            negativeColor = Color(0xFF00E5FF),
            glowColor = Color(0xFF00E5FF),
            description = "Micro-charge energy spark. Highly mobile base unit."
        ),
        CoreLevelData(
            level = 2,
            name = "Cell",
            baseScore = 3,
            radiusDp = 30f,
            mass = 1.8f,
            dropWeightPct = 30,
            positiveColor = Color(0xFFFF5252),
            negativeColor = Color(0xFF1DE9B6),
            glowColor = Color(0xFF1DE9B6),
            description = "Stabilized elemental core cell with electromagnetic polarity."
        ),
        CoreLevelData(
            level = 3,
            name = "Node",
            baseScore = 6,
            radiusDp = 38f,
            mass = 2.9f,
            dropWeightPct = 20,
            positiveColor = Color(0xFFFF9100),
            negativeColor = Color(0xFF00B0FF),
            glowColor = Color(0xFF00B0FF),
            description = "Network node core emitting pulse waves to nearby entities."
        ),
        CoreLevelData(
            level = 4,
            name = "Core",
            baseScore = 12,
            radiusDp = 46f,
            mass = 4.5f,
            dropWeightPct = 10,
            positiveColor = Color(0xFFFFD600),
            negativeColor = Color(0xFF7C4DFF),
            glowColor = Color(0xFF7C4DFF),
            description = "Dense energy core matrix with strong gravitational mass."
        ),
        CoreLevelData(
            level = 5,
            name = "Reactor",
            baseScore = 24,
            radiusDp = 56f,
            mass = 6.8f,
            dropWeightPct = 5,
            positiveColor = Color(0xFFC51162),
            negativeColor = Color(0xFF00E676),
            glowColor = Color(0xFF00E676),
            description = "High-output nuclear plasma reactor core."
        ),
        CoreLevelData(
            level = 6,
            name = "Pulse",
            baseScore = 48,
            radiusDp = 68f,
            mass = 10.2f,
            dropWeightPct = 0,
            positiveColor = Color(0xFF651FFF),
            negativeColor = Color(0xFFFFAB00),
            glowColor = Color(0xFFFFAB00),
            description = "Harmonic pulse sphere created purely through energy reaction."
        ),
        CoreLevelData(
            level = 7,
            name = "Plasma",
            baseScore = 96,
            radiusDp = 82f,
            mass = 15.5f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFD500F9),
            negativeColor = Color(0xFF00B8D4),
            glowColor = Color(0xFF00B8D4),
            description = "Superheated ionized plasma cluster."
        ),
        CoreLevelData(
            level = 8,
            name = "Nova",
            baseScore = 192,
            radiusDp = 98f,
            mass = 23.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFFF3D00),
            negativeColor = Color(0xFF3D5AFF),
            glowColor = Color(0xFFFF3D00),
            description = "Stellar nova fragment capable of massive chain merges."
        ),
        CoreLevelData(
            level = 9,
            name = "Star",
            baseScore = 384,
            radiusDp = 116f,
            mass = 34.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFFFC400),
            negativeColor = Color(0xFF64FFDA),
            glowColor = Color(0xFFFFC400),
            description = "Radiant star core possessing high thermal potential."
        ),
        CoreLevelData(
            level = 10,
            name = "Singularity",
            baseScore = 768,
            radiusDp = 138f,
            mass = 50.0f,
            dropWeightPct = 0,
            positiveColor = Color(0xFFE040FB),
            negativeColor = Color(0xFF18FFFF),
            glowColor = Color(0xFFE040FB),
            description = "Cosmic singularity. The ultimate energy entity."
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
