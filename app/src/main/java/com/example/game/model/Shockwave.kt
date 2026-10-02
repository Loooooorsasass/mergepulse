package com.example.game.model

import androidx.compose.ui.graphics.Color

data class Shockwave(
    val id: Long,
    val x: Float,
    val y: Float,
    val maxRadius: Float,
    var currentRadius: Float = 0f,
    val force: Float,
    val color: Color,
    val duration: Float = 0.4f,
    var age: Float = 0f
) {
    val isFinished: Boolean
        get() = age >= duration

    val alpha: Float
        get() = (1f - (age / duration)).coerceIn(0f, 1f)
}
