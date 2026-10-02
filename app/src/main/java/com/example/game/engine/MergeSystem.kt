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

        // Flip charge for next tier
        val nextCharge = a.charge.flipped()
        val nextRadius = CoreLevelRegistry.calculateRadius(nextLevel, densityFactor)

        // Deactivate merged parents
        a.active = false
        b.active = false

        return Core(
            id = System.nanoTime(),
            position = Vec2(midX, midY),
            velocity = Vec2(0f, -120f), // Initial subtle upward pop momentum
            level = nextLevel,
            charge = nextCharge,
            radius = nextRadius,
            active = true,
            isSpawned = true,
            mergeAnimTime = 0.3f,
            pulseFlashTime = 0.4f,
            rotationSpeed = if (nextLevel % 2 == 0) 1.5f else -1.5f
        )
    }
}
