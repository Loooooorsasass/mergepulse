package com.example.game.engine

import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import com.example.game.model.Vec2
import kotlin.random.Random

class ChargeGenerator {
    private val lastCharges = mutableListOf<Charge>()
    private val PITY_THRESHOLD = 4
    private val PITY_BOOST = 0.7f

    fun getNextCharge(): Charge {
        val streak = getCurrentStreak()
        var positiveChance = 0.5f

        if (streak != null && streak.count >= PITY_THRESHOLD) {
            positiveChance = if (streak.charge == Charge.POSITIVE) (1f - PITY_BOOST) else PITY_BOOST
        }

        val charge = if (Random.nextFloat() < positiveChance) Charge.POSITIVE else Charge.NEGATIVE
        lastCharges.add(charge)
        if (lastCharges.size > 10) {
            lastCharges.removeAt(0)
        }
        return charge
    }

    private fun getCurrentStreak(): StreakInfo? {
        if (lastCharges.isEmpty()) return null
        val last = lastCharges.last()
        var count = 0
        for (i in lastCharges.indices.reversed()) {
            if (lastCharges[i] == last) count++ else break
        }
        return StreakInfo(last, count)
    }

    private data class StreakInfo(val charge: Charge, val count: Int)
}

class SpawnSystem {

    // Early game (< 60s): 65% L1, 25% L2, 10% L3
    private val earlyGameWeights = mapOf(
        1 to 65,
        2 to 25,
        3 to 10
    )

    // Mid/Late game (>= 60s): 55% L1, 25% L2, 12% L3, 6% L4, 2% L5
    private val midGameWeights = mapOf(
        1 to 55,
        2 to 25,
        3 to 12,
        4 to 6,
        5 to 2
    )

    private val chargeGenerator = ChargeGenerator()

    fun getUnlockedLevels(elapsedSeconds: Float): List<Int> {
        val minutes = elapsedSeconds / 60f
        return if (minutes < 1f) {
            listOf(1, 2, 3)
        } else {
            listOf(1, 2, 3, 4, 5)
        }
    }

    fun getFallSpeedMultiplier(elapsedSeconds: Float): Float {
        val minutes = elapsedSeconds / 60f
        var mult = 1.0f
        if (minutes >= 1f) mult = 1.15f
        if (minutes >= 3f) {
            val extraMinutes = minutes - 3f
            mult = 1.15f + extraMinutes * 0.05f
        }
        return mult.coerceAtMost(1.5f)
    }

    fun pickSpawnLevel(elapsedSeconds: Float): Int {
        val minutes = elapsedSeconds / 60f
        val weightMap = if (minutes < 1f) earlyGameWeights else midGameWeights
        val levels = weightMap.keys.toList()
        val totalWeight = weightMap.values.sum()

        var roll = Random.nextFloat() * totalWeight
        for (lvl in levels) {
            val weight = weightMap[lvl] ?: 0
            roll -= weight
            if (roll <= 0) return lvl
        }
        return levels.first()
    }

    fun generateCore(
        spawnX: Float,
        spawnY: Float,
        elapsedSeconds: Float,
        densityFactor: Float = 1.0f
    ): Core {
        val level = pickSpawnLevel(elapsedSeconds)
        val charge = chargeGenerator.getNextCharge()
        val radius = CoreLevelRegistry.calculateRadius(level, densityFactor)

        return Core(
            id = System.nanoTime(),
            position = Vec2(spawnX, spawnY),
            velocity = Vec2(0f, 0f),
            level = level,
            charge = charge,
            radius = radius,
            active = true,
            isSpawned = false
        )
    }

    fun createFixedCore(
        spawnX: Float,
        spawnY: Float,
        level: Int,
        charge: Charge,
        densityFactor: Float = 1.0f
    ): Core {
        val radius = CoreLevelRegistry.calculateRadius(level, densityFactor)
        return Core(
            id = System.nanoTime(),
            position = Vec2(spawnX, spawnY),
            velocity = Vec2(0f, 0f),
            level = level,
            charge = charge,
            radius = radius,
            active = true,
            isSpawned = false
        )
    }
}
