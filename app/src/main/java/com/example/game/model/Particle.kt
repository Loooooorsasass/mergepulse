package com.example.game.model

import androidx.compose.ui.graphics.Color

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    val color: Color,
    val maxLife: Float,
    var life: Float = maxLife,
    val isRing: Boolean = false
) {
    val isDead: Boolean
        get() = life <= 0f

    val alpha: Float
        get() = (life / maxLife).coerceIn(0f, 1f)
}
