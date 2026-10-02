package com.example.game.engine

import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import com.example.game.model.Shockwave
import kotlin.math.pow
import kotlin.math.sqrt

object PulseSystem {

    fun calcShockwaveImpulse(distance: Float, radius: Float, mergedLevel: Int): Float {
        if (distance >= radius) return 0f
        // Controlled, predictable pulse force: nudges rather than launches
        val basePulseForce = 115f
        val levelScale = 1.0f + (mergedLevel - 1) * 0.2f / 4.0f
        val falloff = (1.0f - distance / radius).pow(2)
        return basePulseForce * levelScale * falloff
    }

    fun applyPulse(
        source: Core,
        allCores: List<Core>
    ): Shockwave {
        // Tight, localized pulse radius (2.2x instead of 3.2x)
        val pulseRadius = source.radius * 2.2f

        for (other in allCores) {
            if (!other.active || !other.isSpawned || other.id == source.id) continue

            val dx = other.position.x - source.position.x
            val dy = other.position.y - source.position.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > 0.1f && distance <= pulseRadius) {
                val impulseMag = calcShockwaveImpulse(distance, pulseRadius, source.level)
                val nx = dx / distance
                val ny = dy / distance

                val massFactor = 90f / (other.mass + 15f)
                // Subdued horizontal kick to prevent chaotic sideways ejections
                other.velocity.x += nx * impulseMag * massFactor * 0.65f
                other.velocity.y += ny * impulseMag * massFactor * 0.85f

                other.triggerReaction()
            }
        }

        val levelInfo = CoreLevelRegistry.getInfo(source.level)
        return Shockwave(
            id = System.nanoTime(),
            x = source.position.x,
            y = source.position.y,
            maxRadius = pulseRadius,
            force = 120f + source.level * 30f,
            color = levelInfo.glowColor
        )
    }

    fun applyOverchargePulse(
        centerX: Float,
        centerY: Float,
        allCores: List<Core>
    ): Shockwave {
        val pulseRadius = 750f
        val force = 380f

        for (core in allCores) {
            if (!core.active || !core.isSpawned) continue

            val dx = core.position.x - centerX
            val dy = core.position.y - centerY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > 0.1f) {
                val nx = dx / distance
                val ny = dy / distance
                val impulse = force * (100f / (core.mass + 20f))

                core.velocity.x += nx * impulse * 0.6f
                core.velocity.y += ny * impulse - 140f
                core.triggerReaction()
            }
        }

        return Shockwave(
            id = System.nanoTime(),
            x = centerX,
            y = centerY,
            maxRadius = pulseRadius,
            force = force,
            color = androidx.compose.ui.graphics.Color(0xFF2979FF)
        )
    }
}
