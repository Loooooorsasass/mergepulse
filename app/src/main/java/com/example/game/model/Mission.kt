package com.example.game.model

enum class MissionCategory {
    THRESHOLD,   // Peak value reached (e.g. reach Level 6, score 10,000, 4x Combo)
    ACCUMULATIVE // Accumulated count (e.g. 25 merges, 120s survival, 15 flips, 2 overcharges)
}

data class MissionDefinition(
    val id: Int,
    val title: String,
    val description: String,
    val category: MissionCategory,
    val targetValue: Int,
    val rewardCredits: Int,
    val eventKey: String
)

object MissionRegistry {
    // 10+ Data-Driven Mission Templates
    val MISSION_DEFINITIONS = listOf(
        MissionDefinition(
            id = 1,
            title = "Reactor Synthesis",
            description = "Reach Core Level 4 (Core)",
            category = MissionCategory.THRESHOLD,
            targetValue = 4,
            rewardCredits = 100,
            eventKey = "level_reached"
        ),
        MissionDefinition(
            id = 2,
            title = "Resonant Chain",
            description = "Achieve a 3x Chain Combo",
            category = MissionCategory.THRESHOLD,
            targetValue = 3,
            rewardCredits = 150,
            eventKey = "combo_reached"
        ),
        MissionDefinition(
            id = 3,
            title = "High Energy Harvest",
            description = "Score 2,500 Points in a single run",
            category = MissionCategory.THRESHOLD,
            targetValue = 2500,
            rewardCredits = 200,
            eventKey = "score_reached"
        ),
        MissionDefinition(
            id = 4,
            title = "Fusion Specialist",
            description = "Perform 20 Total Merges",
            category = MissionCategory.ACCUMULATIVE,
            targetValue = 20,
            rewardCredits = 250,
            eventKey = "merge_count"
        ),
        MissionDefinition(
            id = 5,
            title = "Thermal Stability",
            description = "Survive in Chamber for 90 seconds",
            category = MissionCategory.ACCUMULATIVE,
            targetValue = 90,
            rewardCredits = 200,
            eventKey = "survive_time"
        ),
        MissionDefinition(
            id = 6,
            title = "Pulse Mastery",
            description = "Reach Core Level 6 (Pulse)",
            category = MissionCategory.THRESHOLD,
            targetValue = 6,
            rewardCredits = 350,
            eventKey = "level_reached"
        ),
        MissionDefinition(
            id = 7,
            title = "Hyper Chain",
            description = "Achieve a 5x Chain Combo",
            category = MissionCategory.THRESHOLD,
            targetValue = 5,
            rewardCredits = 400,
            eventKey = "combo_reached"
        ),
        MissionDefinition(
            id = 8,
            title = "Overcharge Blast",
            description = "Trigger 2 Overcharge Shockwaves",
            category = MissionCategory.ACCUMULATIVE,
            targetValue = 2,
            rewardCredits = 300,
            eventKey = "overcharge_used"
        ),
        MissionDefinition(
            id = 9,
            title = "Polarity Inversion",
            description = "Use FLIP 12 Times tactically",
            category = MissionCategory.ACCUMULATIVE,
            targetValue = 12,
            rewardCredits = 200,
            eventKey = "flip_used"
        ),
        MissionDefinition(
            id = 10,
            title = "Plasma Singularity",
            description = "Reach Core Level 7 (Plasma)",
            category = MissionCategory.THRESHOLD,
            targetValue = 7,
            rewardCredits = 600,
            eventKey = "level_reached"
        ),
        MissionDefinition(
            id = 11,
            title = "Mass Fusion",
            description = "Perform 40 Total Merges",
            category = MissionCategory.ACCUMULATIVE,
            targetValue = 40,
            rewardCredits = 500,
            eventKey = "merge_count"
        ),
        MissionDefinition(
            id = 12,
            title = "Grand Matrix",
            description = "Score 10,000 Points in a single run",
            category = MissionCategory.THRESHOLD,
            targetValue = 10000,
            rewardCredits = 750,
            eventKey = "score_reached"
        )
    )

    // Mulberry32 32-bit deterministic PRNG
    class Mulberry32(private var state: Long) {
        fun nextFloat(): Float {
            state = (state + 0x6D2B79F5L) and 0xFFFFFFFFL
            var z = state
            z = ((z xor (z ushr 15)) * ((z or 1L) and 0xFFFFFFFFL)) and 0xFFFFFFFFL
            z = (z xor (z + (((z xor (z ushr 7)) * ((z or 61L) and 0xFFFFFFFFL)) and 0xFFFFFFFFL))) and 0xFFFFFFFFL
            return (((z xor (z ushr 14)) ushr 0).toDouble() / 4294967296.0).toFloat()
        }

        fun nextInt(bound: Int): Int {
            return (nextFloat() * bound).toInt().coerceIn(0, bound - 1)
        }
    }

    /**
     * Deterministically generates the exact 5 daily missions for the given Epoch day across all devices.
     */
    fun getTodayMissionIds(epochDay: Long = System.currentTimeMillis() / (24 * 60 * 60 * 1000L)): List<Int> {
        val rng = Mulberry32(epochDay xor 0x5DEECE66DL)
        val pool = MISSION_DEFINITIONS.map { it.id }.toMutableList()
        val selected = mutableListOf<Int>()

        // Pick 5 unique missions
        val count = 5.coerceAtMost(pool.size)
        repeat(count) {
            val idx = rng.nextInt(pool.size)
            selected.add(pool.removeAt(idx))
        }
        return selected
    }

    fun getDefinition(id: Int): MissionDefinition? {
        return MISSION_DEFINITIONS.find { it.id == id }
    }
}

/**
 * Realtime Event-Driven Daily Missions Manager.
 * Separates _checkThreshold (peak value) and _incrementProgress (accumulation).
 */
class DailyMissionsManager(
    val todayMissionIds: List<Int>,
    private val onMissionCompletedCallback: (MissionDefinition) -> Unit
) {
    // Mission Progress Map: missionId -> current progress
    val progressMap = mutableMapOf<Int, Int>()
    val completedSet = mutableSetOf<Int>()

    init {
        todayMissionIds.forEach { id ->
            progressMap[id] = 0
        }
    }

    fun syncCompleted(completedIds: Set<Int>) {
        completedSet.clear()
        completedSet.addAll(completedIds)
    }

    /**
     * Called when a realtime gameplay event occurs (e.g. merge, combo, score, tick).
     */
    fun onEvent(eventKey: String, value: Int = 1) {
        todayMissionIds.forEach { missionId ->
            if (completedSet.contains(missionId)) return@forEach

            val def = MissionRegistry.getDefinition(missionId) ?: return@forEach
            if (def.eventKey != eventKey) return@forEach

            when (def.category) {
                MissionCategory.THRESHOLD -> _checkThreshold(def, value)
                MissionCategory.ACCUMULATIVE -> _incrementProgress(def, value)
            }
        }
    }

    private fun _checkThreshold(def: MissionDefinition, currentValue: Int) {
        val prev = progressMap[def.id] ?: 0
        val updated = maxOf(prev, currentValue)
        progressMap[def.id] = updated

        if (updated >= def.targetValue && !completedSet.contains(def.id)) {
            completedSet.add(def.id)
            onMissionCompletedCallback(def)
        }
    }

    private fun _incrementProgress(def: MissionDefinition, delta: Int) {
        val prev = progressMap[def.id] ?: 0
        val updated = prev + delta
        progressMap[def.id] = updated

        if (updated >= def.targetValue && !completedSet.contains(def.id)) {
            completedSet.add(def.id)
            onMissionCompletedCallback(def)
        }
    }
}
