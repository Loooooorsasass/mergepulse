package com.example.game.model

data class Vec2(
    var x: Float = 0f,
    var y: Float = 0f
) {
    fun length(): Float = kotlin.math.sqrt(x * x + y * y)

    fun normalize(): Vec2 {
        val len = length()
        return if (len > 0.0001f) Vec2(x / len, y / len) else Vec2(0f, -1f)
    }
}
