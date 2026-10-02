package com.example.game.model

data class Core(
    val id: Long,
    var position: Vec2,
    var velocity: Vec2,
    var level: Int,
    var charge: Charge,
    var radius: Float,
    var active: Boolean = true,
    var isSpawned: Boolean = true, // false while aiming before drop
    var mergeAnimTime: Float = 0f, // 0 to 0.3s spawn animation scale
    var pulseFlashTime: Float = 0f, // glow pulse duration
    var rotationAngle: Float = 0f,
    var rotationSpeed: Float = 1.0f // radians per second rotation for visual flair
) {
    val mass: Float
        get() = radius * radius * 0.05f

    fun flipCharge() {
        charge = charge.flipped()
    }
}
