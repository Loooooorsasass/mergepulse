package com.example.game.engine

import com.example.game.model.Core
import kotlin.math.sqrt

class PhysicsEngine(
    var gravity: Float = 2000f,
    var bounceRestitution: Float = 0.22f,
    var friction: Float = 0.985f
) {
    val MAX_SPEED = 1200f

    fun updateCore(core: Core, dt: Float) {
        if (!core.active || !core.isSpawned) return

        // Apply gravity
        core.velocity.y += gravity * dt

        // Speed Cap safety
        val currentSpeed = core.velocity.length()
        if (currentSpeed > MAX_SPEED) {
            val scale = MAX_SPEED / currentSpeed
            core.velocity.x *= scale
            core.velocity.y *= scale
        }

        // Update positions
        core.position.x += core.velocity.x * dt
        core.position.y += core.velocity.y * dt

        // Update rotation
        core.rotationAngle += core.rotationSpeed * dt
    }

    fun resolveBoundaries(
        core: Core,
        leftWall: Float,
        rightWall: Float,
        floorY: Float
    ): Boolean {
        if (!core.active || !core.isSpawned) return false
        var collided = false

        // Left Wall
        val minX = leftWall + core.radius
        if (core.position.x <= minX) {
            core.position.x = minX
            if (core.velocity.x < 0f) {
                core.velocity.x = -core.velocity.x * bounceRestitution
                collided = true
            }
        }

        // Right Wall
        val maxX = rightWall - core.radius
        if (core.position.x >= maxX) {
            core.position.x = maxX
            if (core.velocity.x > 0f) {
                core.velocity.x = -core.velocity.x * bounceRestitution
                collided = true
            }
        }

        // Floor
        val maxY = floorY - core.radius
        if (core.position.y >= maxY) {
            core.position.y = maxY
            if (core.velocity.y > 0f) {
                core.velocity.y = -core.velocity.y * bounceRestitution
                collided = true
            }
            core.velocity.x *= friction
        }

        return collided
    }

    fun resolveCoreCollision(a: Core, b: Core): Boolean {
        if (!a.active || !b.active || !a.isSpawned || !b.isSpawned) return false

        var dx = b.position.x - a.position.x
        var dy = b.position.y - a.position.y

        var distance = sqrt(dx * dx + dy * dy)
        val minDist = a.radius + b.radius

        if (distance >= minDist) return false

        if (distance < 0.001f) {
            distance = 0.001f
            dx = 0.01f
            dy = -0.999f
        }

        val nx = dx / distance
        val ny = dy / distance

        // Same charge electrostatic repulsion bonus force
        if (a.charge == b.charge) {
            val repulsionForce = 80f * (1f - (distance / minDist))
            a.velocity.x -= nx * repulsionForce
            a.velocity.y -= ny * repulsionForce
            b.velocity.x += nx * repulsionForce
            b.velocity.y += ny * repulsionForce
        }

        // Positional separation
        val overlap = minDist - distance
        val totalMass = a.mass + b.mass
        val ratioA = b.mass / totalMass
        val ratioB = a.mass / totalMass

        a.position.x -= nx * overlap * ratioA
        a.position.y -= ny * overlap * ratioA

        b.position.x += nx * overlap * ratioB
        b.position.y += ny * overlap * ratioB

        // Elastic momentum resolution
        val kx = a.velocity.x - b.velocity.x
        val ky = a.velocity.y - b.velocity.y
        val p = 2f * (nx * kx + ny * ky) / (a.mass + b.mass)

        if (p > 0f) {
            val impulse = p * (1f + bounceRestitution)
            a.velocity.x -= impulse * b.mass * nx
            a.velocity.y -= impulse * b.mass * ny

            b.velocity.x += impulse * a.mass * nx
            b.velocity.y += impulse * a.mass * ny
        }

        return true
    }
}
