package com.example.game.engine

import androidx.compose.ui.graphics.Color
import com.example.game.model.Charge
import com.example.game.model.Core
import com.example.game.model.CoreLevelRegistry
import com.example.game.model.DailyMissionsManager
import com.example.game.model.FloatingText
import com.example.game.model.MissionDefinition
import com.example.game.model.MissionRegistry
import com.example.game.model.Particle
import com.example.game.model.Shockwave
import com.example.game.model.Vec2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class GameState {
    MENU,
    PLAYING,
    PAUSED,
    GAME_OVER,
    MISSION_COMPLETE
}

enum class GameMode {
    ENDLESS,
    MISSION,
    TUTORIAL
}

class GameWorld {

    var state: GameState = GameState.MENU
    var mode: GameMode = GameMode.ENDLESS
    var activeMission: MissionDefinition? = null

    var elapsedTime: Float = 0f
    private var secondTickAccumulator: Float = 0f

    val tutorialFsm = TutorialFSM(this)
    val isTutorialActive: Boolean
        get() = mode == GameMode.TUTORIAL && tutorialFsm.state != TutorialState.COMPLETE

    val overflowWatcher = OverflowWatcher(this)
    val gameOverFsm = GameOverFSM(this) {
        state = GameState.GAME_OVER
    }

    var missionsManager: DailyMissionsManager? = null

    // Chamber Dimensions
    var chamberLeft: Float = 40f
    var chamberRight: Float = 1040f
    var chamberFloorY: Float = 1600f
    var dangerLineY: Float = 320f
    var spawnLineY: Float = 180f

    var densityFactor: Float = 1.0f

    // Gameplay Stats
    var score: Int = 0
    var bestScore: Int = 0
    var comboCount: Int = 1
    var bestCombo: Int = 1
    var totalMergesInGame: Int = 0
    var highestLevelReached: Int = 1
    var overchargePercent: Float = 0f
    var usedFlipInGame: Boolean = false

    // Flip Energy Charge System
    var flipEnergy: Float = 3.0f
    val MAX_FLIP_ENERGY = 3.0f

    // Score Overdrive Ability
    var scoreOverdriveTimer: Float = 0f

    // Screen Shake Juice
    var screenShakeIntensity: Float = 0f

    // Selected Chamber Theme
    var currentThemeId: String = "default"

    // Timers
    var comboTimer: Float = 0f
    val COMBO_WINDOW_SECONDS = 1.3f

    val isDangerActive: Boolean
        get() = overflowWatcher.isBreaching

    // Entities
    val activeCores = mutableListOf<Core>()
    val particles = mutableListOf<Particle>()
    val shockwaves = mutableListOf<Shockwave>()
    val floatingTexts = mutableListOf<FloatingText>()

    var currentCore: Core? = null
    var nextCore: Core? = null

    val physicsEngine = PhysicsEngine()
    val spawnSystem = SpawnSystem()

    // Aiming state
    var aimX: Float = 540f
    var isDropCooldown: Boolean = false
    var dropCooldownTimer: Float = 0f

    // Audio SFX callbacks
    var onPlaySfx: ((SfxType, Float, Int) -> Unit)? = null

    enum class SfxType {
        DROP,
        BOUNCE,
        MERGE,
        COMBO,
        DANGER,
        OVERCHARGE,
        GAME_OVER,
        MISSION_WIN
    }

    fun setChamberBounds(width: Float, height: Float, topOffset: Float, bottomMargin: Float) {
        val margin = (width * 0.05f).coerceAtLeast(16f)
        chamberLeft = margin
        chamberRight = width - margin

        spawnLineY = topOffset + (height * 0.08f)
        dangerLineY = topOffset + (height * 0.18f)
        chamberFloorY = height - bottomMargin

        val targetWidth = 1000f
        densityFactor = ((width - margin * 2) / targetWidth).coerceIn(0.65f, 1.25f)

        aimX = (chamberLeft + chamberRight) / 2f
    }

    fun startNewGame(gameMode: GameMode = GameMode.ENDLESS, mission: MissionDefinition? = null) {
        mode = gameMode
        activeMission = mission
        state = GameState.PLAYING

        elapsedTime = 0f
        secondTickAccumulator = 0f
        activeCores.clear()
        particles.clear()
        shockwaves.clear()
        floatingTexts.clear()

        score = 0
        comboCount = 1
        comboTimer = 0f
        overflowWatcher.reset()
        gameOverFsm.reset()
        overchargePercent = 0f
        flipEnergy = MAX_FLIP_ENERGY
        scoreOverdriveTimer = 0f
        screenShakeIntensity = 0f
        totalMergesInGame = 0
        highestLevelReached = 1
        usedFlipInGame = false
        isDropCooldown = false
        dropCooldownTimer = 0f

        if (mode == GameMode.TUTORIAL) {
            tutorialFsm.start()
        } else {
            nextCore = spawnSystem.generateCore(
                spawnX = (chamberLeft + chamberRight) / 2f,
                spawnY = spawnLineY,
                elapsedSeconds = elapsedTime,
                densityFactor = densityFactor
            )
            spawnNextCore()
        }
    }

    fun resetForRetry() {
        state = GameState.PLAYING
        elapsedTime = 0f
        secondTickAccumulator = 0f
        activeCores.clear()
        particles.clear()
        shockwaves.clear()
        floatingTexts.clear()

        score = 0
        comboCount = 1
        comboTimer = 0f
        overflowWatcher.reset()
        gameOverFsm.reset()
        overchargePercent = 0f
        flipEnergy = MAX_FLIP_ENERGY
        scoreOverdriveTimer = 0f
        screenShakeIntensity = 0f
        totalMergesInGame = 0
        highestLevelReached = 1
        usedFlipInGame = false
        isDropCooldown = false
        dropCooldownTimer = 0f

        nextCore = spawnSystem.generateCore(
            spawnX = (chamberLeft + chamberRight) / 2f,
            spawnY = spawnLineY,
            elapsedSeconds = 0f,
            densityFactor = densityFactor
        )
        spawnNextCore()
    }

    fun spawnFixedCore(level: Int, charge: Charge) {
        val fixed = spawnSystem.createFixedCore(
            spawnX = aimX,
            spawnY = spawnLineY,
            level = level,
            charge = charge,
            densityFactor = densityFactor
        )
        currentCore = fixed
        nextCore = spawnSystem.createFixedCore(
            spawnX = aimX,
            spawnY = spawnLineY,
            level = if (level == 1) 1 else 2,
            charge = charge.flipped(),
            densityFactor = densityFactor
        )
    }

    fun spawnNextCore() {
        val next = nextCore ?: spawnSystem.generateCore(aimX, spawnLineY, elapsedTime, densityFactor)
        next.position.x = aimX.coerceIn(chamberLeft + next.radius, chamberRight - next.radius)
        next.position.y = spawnLineY
        next.isSpawned = false

        currentCore = next

        nextCore = spawnSystem.generateCore(
            spawnX = (chamberLeft + chamberRight) / 2f,
            spawnY = spawnLineY,
            elapsedSeconds = elapsedTime,
            densityFactor = densityFactor
        )
    }

    fun flipCurrentCharge(): Boolean {
        if (state != GameState.PLAYING || gameOverFsm.timeScale <= 0f) return false
        val core = currentCore ?: return false
        if (core.isSpawned) return false

        if (isTutorialActive && !tutorialFsm.isFlipAllowed) return false

        if (!isTutorialActive && flipEnergy < 1.0f) {
            onPlaySfx?.invoke(SfxType.BOUNCE, 0.5f, 1)
            return false
        }

        if (!isTutorialActive) {
            flipEnergy -= 1.0f
        }

        core.flipCharge()
        usedFlipInGame = true
        onPlaySfx?.invoke(SfxType.BOUNCE, 1.3f, 1)

        missionsManager?.onEvent("flip_used", 1)

        if (isTutorialActive) {
            tutorialFsm.onEvent("flip_used")
        }
        return true
    }

    fun setAimPosition(x: Float) {
        val core = currentCore ?: return
        if (core.isSpawned || gameOverFsm.timeScale <= 0f) return
        val clampedX = x.coerceIn(chamberLeft + core.radius, chamberRight - core.radius)
        aimX = clampedX
        core.position.x = clampedX
    }

    fun dropCurrentCore(): Boolean {
        if (state != GameState.PLAYING || isDropCooldown || gameOverFsm.timeScale <= 0f) return false
        val core = currentCore ?: return false
        if (core.isSpawned) return false

        if (isTutorialActive && !tutorialFsm.isDropAllowed) return false

        if (isTutorialActive && tutorialFsm.requireFlipBeforeDrop && !tutorialFsm.hasFlippedInStep) {
            tutorialFsm.onEvent("core_dropped", mapOf("hasFlipped" to false))
            onPlaySfx?.invoke(SfxType.BOUNCE, 0.5f, 1)
            return false
        }

        core.isSpawned = true
        val fallSpeedMult = spawnSystem.getFallSpeedMultiplier(elapsedTime)
        core.velocity.y = 120f * fallSpeedMult
        activeCores.add(core)

        onPlaySfx?.invoke(SfxType.DROP, 1.0f, 1)

        currentCore = null
        isDropCooldown = true
        dropCooldownTimer = 0.3f

        if (isTutorialActive) {
            tutorialFsm.onEvent("core_dropped", mapOf("hasFlipped" to tutorialFsm.hasFlippedInStep))
        }

        return true
    }

    fun triggerOverchargePulse(): Boolean {
        if (state != GameState.PLAYING || overchargePercent < 100f || gameOverFsm.timeScale <= 0f) return false

        overchargePercent = 0f
        screenShakeIntensity = 25f
        val centerX = (chamberLeft + chamberRight) / 2f
        val centerY = (dangerLineY + chamberFloorY) / 2f

        val wave = PulseSystem.applyOverchargePulse(centerX, centerY, activeCores)
        shockwaves.add(wave)

        for (i in 0 until 45) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * 800f + 200f
            particles.add(
                Particle(
                    x = centerX,
                    y = centerY,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = Random.nextFloat() * 8f + 4f,
                    color = Color(0xFF00E5FF),
                    maxLife = 0.65f
                )
            )
        }

        floatingTexts.add(
            FloatingText(
                id = System.nanoTime(),
                x = centerX,
                y = centerY - 100f,
                text = "OVERCHARGE SHOCKWAVE!",
                color = Color(0xFF00E5FF),
                duration = 1.5f,
                isCombo = true
            )
        )

        onPlaySfx?.invoke(SfxType.OVERCHARGE, 1.0f, 1)
        missionsManager?.onEvent("overcharge_used", 1)
        return true
    }

    fun triggerScoreOverdrive(): Boolean {
        if (state != GameState.PLAYING || overchargePercent < 100f || gameOverFsm.timeScale <= 0f) return false

        overchargePercent = 0f
        scoreOverdriveTimer = 10f
        screenShakeIntensity = 15f

        floatingTexts.add(
            FloatingText(
                id = System.nanoTime(),
                x = (chamberLeft + chamberRight) / 2f,
                y = dangerLineY + 100f,
                text = "2X SCORE OVERDRIVE (10s)!",
                color = Color(0xFFFFD600),
                duration = 1.8f,
                isCombo = true
            )
        )

        onPlaySfx?.invoke(SfxType.OVERCHARGE, 1.4f, 1)
        missionsManager?.onEvent("overcharge_used", 1)
        return true
    }

    fun update(rawDt: Float) {
        gameOverFsm.update(rawDt)

        val dt = rawDt * gameOverFsm.timeScale
        if (dt <= 0f && state != GameState.PLAYING) return

        elapsedTime += dt

        // Realtime survival seconds event tick
        secondTickAccumulator += dt
        if (secondTickAccumulator >= 1.0f) {
            val wholeSecs = secondTickAccumulator.toInt()
            secondTickAccumulator -= wholeSecs
            missionsManager?.onEvent("survive_time", wholeSecs)
        }

        if (isTutorialActive) {
            tutorialFsm.update(dt)
        }

        flipEnergy = (flipEnergy + 0.35f * dt).coerceAtMost(MAX_FLIP_ENERGY)

        if (scoreOverdriveTimer > 0f) {
            scoreOverdriveTimer = (scoreOverdriveTimer - dt).coerceAtLeast(0f)
        }

        if (screenShakeIntensity > 0f) {
            screenShakeIntensity = (screenShakeIntensity - 30f * dt).coerceAtLeast(0f)
        }

        // Overflow Watcher checking
        val shouldGameOver = overflowWatcher.update(dt)
        if (shouldGameOver && !isTutorialActive && state == GameState.PLAYING) {
            gameOverFsm.triggerGameOver()
        }

        if (isDropCooldown) {
            dropCooldownTimer -= dt
            if (dropCooldownTimer <= 0f) {
                isDropCooldown = false
                if (!isTutorialActive || currentCore == null) {
                    spawnNextCore()
                }
            }
        }

        if (comboTimer > 0f) {
            comboTimer -= dt
            if (comboTimer <= 0f) {
                comboCount = 1
            }
        }

        // Physics updates
        for (core in activeCores) {
            if (core.active && core.isSpawned) {
                physicsEngine.updateCore(core, dt)
                val bounced = physicsEngine.resolveBoundaries(core, chamberLeft, chamberRight, chamberFloorY)
                if (bounced && core.velocity.length() > 240f && gameOverFsm.timeScale > 0.5f) {
                    if (core.triggerCollisionSound()) {
                        onPlaySfx?.invoke(SfxType.BOUNCE, 0.8f, 1)
                    }
                }

                if (core.mergeAnimTime > 0f) core.mergeAnimTime -= dt
                if (core.pulseFlashTime > 0f) core.pulseFlashTime -= dt
            }
        }

        // Collisions & merges
        val count = activeCores.size
        for (i in 0 until count) {
            val a = activeCores[i]
            if (!a.active) continue

            for (j in i + 1 until count) {
                val b = activeCores[j]
                if (!b.active) continue

                if (CollisionSystem.areColliding(a, b)) {
                    if (MergeSystem.canMerge(a, b)) {
                        val merged = MergeSystem.merge(a, b, densityFactor)
                        activeCores.add(merged)

                        // Grace Rescue check
                        if (overflowWatcher.currentOverflowTime > 0.4f) {
                            overflowWatcher.reset()
                            score += 200
                            floatingTexts.add(
                                FloatingText(
                                    id = System.nanoTime(),
                                    x = merged.position.x,
                                    y = merged.position.y - 40f,
                                    text = "GRACE RESCUE! +200",
                                    color = Color(0xFF00E5FF),
                                    duration = 1.5f,
                                    isCombo = true
                                )
                            )
                        }

                        flipEnergy = (flipEnergy + 0.8f).coerceAtMost(MAX_FLIP_ENERGY)

                        totalMergesInGame++
                        if (merged.level > highestLevelReached) {
                            highestLevelReached = merged.level
                        }

                        if (comboTimer > 0f) {
                            comboCount++
                        } else {
                            comboCount = 1
                        }
                        comboTimer = COMBO_WINDOW_SECONDS
                        if (comboCount > bestCombo) {
                            bestCombo = comboCount
                        }

                        val basePoints = CoreLevelRegistry.getInfo(merged.level).baseScore
                        var pointsEarned = basePoints * comboCount
                        if (scoreOverdriveTimer > 0f) pointsEarned *= 2

                        score += pointsEarned
                        if (score > bestScore) {
                            bestScore = score
                        }

                        screenShakeIntensity = (8f + merged.level * 2f + comboCount * 3f).coerceAtMost(30f)

                        // Overcharge meter gain = 2 x level (%)
                        val gain = (2f * merged.level).coerceAtMost(100f)
                        overchargePercent = (overchargePercent + gain).coerceAtMost(100f)

                        val wave = PulseSystem.applyPulse(merged, activeCores)
                        shockwaves.add(wave)

                        spawnMergeParticles(merged)

                        val overdriveStr = if (scoreOverdriveTimer > 0f) " [2X]" else ""
                        val comboStr = if (comboCount > 1) " (x$comboCount COMBO!)$overdriveStr" else overdriveStr
                        floatingTexts.add(
                            FloatingText(
                                id = System.nanoTime(),
                                x = merged.position.x,
                                y = merged.position.y - merged.radius - 20f,
                                text = "+$pointsEarned$comboStr",
                                color = CoreLevelRegistry.getInfo(merged.level).glowColor,
                                duration = 1.2f,
                                isCombo = comboCount > 1
                            )
                        )

                        val pitchFactor = 0.8f + (merged.level * 0.1f)
                        if (comboCount > 1) {
                            onPlaySfx?.invoke(SfxType.COMBO, pitchFactor, comboCount)
                        } else {
                            onPlaySfx?.invoke(SfxType.MERGE, pitchFactor, 1)
                        }

                        // Dispatch Realtime Gameplay Events to Daily Missions Manager
                        missionsManager?.let { mgr ->
                            mgr.onEvent("merge_count", 1)
                            mgr.onEvent("level_reached", merged.level)
                            mgr.onEvent("combo_reached", comboCount)
                            mgr.onEvent("score_reached", score)
                        }

                        if (isTutorialActive) {
                            tutorialFsm.onEvent("merge_success", mapOf("usedFlip" to usedFlipInGame))
                        }

                        break
                    } else {
                        val collided = physicsEngine.resolveCoreCollision(a, b)
                        if (collided && (a.triggerCollisionSound() || b.triggerCollisionSound())) {
                            onPlaySfx?.invoke(SfxType.BOUNCE, 1.0f, 1)
                        }
                    }
                }
            }
        }

        activeCores.removeAll { !it.active }

        for (wave in shockwaves) {
            wave.age += dt
            wave.currentRadius = wave.maxRadius * (wave.age / wave.duration).coerceIn(0f, 1f)
        }
        shockwaves.removeAll { it.isFinished }

        for (p in particles) {
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.life -= dt
        }
        particles.removeAll { it.isDead }

        for (ft in floatingTexts) {
            ft.y -= 30f * dt
            ft.age += dt
        }
        floatingTexts.removeAll { it.isDead }

        if (mode == GameMode.MISSION) {
            checkActiveMissionProgress()
        }
    }

    private fun checkActiveMissionProgress() {
        val mission = activeMission ?: return
        var completed = false
        when (mission.eventKey) {
            "level_reached" -> if (highestLevelReached >= mission.targetValue) completed = true
            "score_reached" -> if (score >= mission.targetValue) completed = true
            "combo_reached" -> if (comboCount >= mission.targetValue) completed = true
            "merge_count" -> if (totalMergesInGame >= mission.targetValue) completed = true
            "survive_time" -> if (elapsedTime >= mission.targetValue) completed = true
            "overcharge_used", "flip_used" -> {
                // Evaluated via missionsManager
            }
        }

        if (completed && state == GameState.PLAYING) {
            state = GameState.MISSION_COMPLETE
            onPlaySfx?.invoke(SfxType.MISSION_WIN, 1.0f, 1)
        }
    }

    private fun spawnMergeParticles(core: Core) {
        val levelInfo = CoreLevelRegistry.getInfo(core.level)
        val particleCount = 20 + core.level * 4
        for (i in 0 until particleCount) {
            val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
            val speed = Random.nextFloat() * (220f + core.level * 45f) + 120f
            particles.add(
                Particle(
                    x = core.position.x,
                    y = core.position.y,
                    vx = cos(angle) * speed,
                    vy = sin(angle) * speed,
                    radius = Random.nextFloat() * 5f + 3f,
                    color = if (Random.nextBoolean()) levelInfo.positiveColor else levelInfo.negativeColor,
                    maxLife = Random.nextFloat() * 0.45f + 0.3f
                )
            )
        }
    }
}
