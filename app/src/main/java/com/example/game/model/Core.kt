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
    var mergeAnimTime: Float = 0f, // 0 to 0.25s spawn pop scale
    var pulseFlashTime: Float = 0f,
    var reactionCount: Int = 0, // Max 2 reactions per tier to prevent endless jitter
    var collisionSoundCount: Int = 0, // Max 2 collision sounds per tier to prevent audio spam
    var rotationAngle: Float = 0f,
    var rotationSpeed: Float = 0.5f
) {
    val mass: Float
        get() = radius * radius * 0.08f

    fun flipCharge() {
        charge = charge.flipped()
    }

    fun triggerReaction(): Boolean {
        if (reactionCount < 2) {
            reactionCount++
            return true
        }
        return false
    }

    fun triggerCollisionSound(): Boolean {
        if (collisionSoundCount < 2) {
            collisionSoundCount++
            return true
        }
        return false
    }
}
