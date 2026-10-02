package com.example.game.engine

import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import com.example.game.model.Shockwave
import kotlin.math.pow
import kotlin.math.sqrt

object PulseSystem {

    fun calcShockwaveImpulse(distance: Float, radius: Float, mergedLevel: Int): Float {
        if (distance >= radius) return 0f
        val basePulseForce = 320f
        val levelScale = 1.0f + (mergedLevel - 1) * 0.3f / 4.0f
        val falloff = (1.0f - distance / radius).pow(2)
        return basePulseForce * levelScale * falloff
    }

    fun applyPulse(
        source: Core,
        allCores: List<Core>
    ): Shockwave {
        val pulseRadius = source.radius * 3.2f

        for (other in allCores) {
            if (!other.active || !other.isSpawned || other.id == source.id) continue

            val dx = other.position.x - source.position.x
            val dy = other.position.y - source.position.y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > 0.1f && distance <= pulseRadius) {
                val impulseMag = calcShockwaveImpulse(distance, pulseRadius, source.level)
                val nx = dx / distance
                val ny = dy / distance

                val massFactor = 100f / (other.mass + 10f)
                other.velocity.x += nx * impulseMag * massFactor
                other.velocity.y += ny * impulseMag * massFactor
            }
        }

        val levelInfo = CoreLevelRegistry.getInfo(source.level)
        return Shockwave(
            id = System.nanoTime(),
            x = source.position.x,
            y = source.position.y,
            maxRadius = pulseRadius,
            force = 300f + source.level * 80f,
            color = levelInfo.glowColor
        )
    }

    fun applyOverchargePulse(
        centerX: Float,
        centerY: Float,
        allCores: List<Core>
    ): Shockwave {
        val pulseRadius = 850f
        val force = 650f

        for (core in allCores) {
            if (!core.active || !core.isSpawned) continue

            val dx = core.position.x - centerX
            val dy = core.position.y - centerY
            val distance = sqrt(dx * dx + dy * dy)

            if (distance > 0.1f) {
                val nx = dx / distance
                val ny = dy / distance
                val impulse = force * (120f / (core.mass + 10f))

                core.velocity.x += nx * impulse
                core.velocity.y += ny * impulse - 220f
            }
        }

        return Shockwave(
            id = System.nanoTime(),
            x = centerX,
            y = centerY,
            maxRadius = pulseRadius,
            force = force,
            color = androidx.compose.ui.graphics.Color(0xFF00E5FF)
        )
    }
}
