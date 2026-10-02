package com.example.game.engine

import com.example.game.model.Core

object CollisionSystem {

    fun areColliding(a: Core, b: Core): Boolean {
        if (!a.active || !b.active || !a.isSpawned || !b.isSpawned) return false
        val dx = b.position.x - a.position.x
        val dy = b.position.y - a.position.y
        val distSq = dx * dx + dy * dy
        val radiusSum = a.radius + b.radius
        return distSq <= radiusSum * radiusSum
    }
}
