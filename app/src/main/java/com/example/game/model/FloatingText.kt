package com.example.game.model

import androidx.compose.ui.graphics.Color

data class FloatingText(
    val id: Long,
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    val duration: Float = 1.0f,
    var age: Float = 0f,
    val isCombo: Boolean = false
) {
    val isDead: Boolean
        get() = age >= duration

    val alpha: Float
        get() = (1f - (age / duration)).coerceIn(0f, 1f)

    val scale: Float
        get() = if (isCombo) 1.2f + 0.3f * kotlin.math.sin(age * 10f) else 1.0f
}
