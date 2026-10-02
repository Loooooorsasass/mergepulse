package com.example.game.engine

import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import com.example.game.model.Vec2

object MergeSystem {

    fun canMerge(a: Core, b: Core): Boolean {
        return a.active &&
                b.active &&
                a.isSpawned &&
                b.isSpawned &&
                a.level == b.level &&
                a.charge != b.charge
    }

    fun merge(a: Core, b: Core, densityFactor: Float = 1.0f): Core {
        val nextLevel = a.level + 1
        val midX = (a.position.x + b.position.x) * 0.5f
        val midY = (a.position.y + b.position.y) * 0.5f

        val nextCharge = a.charge.flipped()
        val nextRadius = CoreLevelRegistry.calculateRadius(nextLevel, densityFactor)

        a.active = false
        b.active = false

        return Core(
            id = System.nanoTime(),
            position = Vec2(midX, midY),
            velocity = Vec2(0f, -50f), // Soft, contained upward pop
            level = nextLevel,
            charge = nextCharge,
            radius = nextRadius,
            active = true,
            isSpawned = true,
            mergeAnimTime = 0.25f,
            pulseFlashTime = 0.3f,
            reactionCount = 0, // Reset for new tier
            collisionSoundCount = 0,
            rotationSpeed = 0f
        )
    }
}
