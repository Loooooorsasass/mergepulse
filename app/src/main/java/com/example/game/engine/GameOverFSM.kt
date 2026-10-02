package com.example.game.engine

import com.example.game.model.Core

enum class GameOverPhase {
    PLAYING,            // Normal gameplay
    OVERFLOW_DETECTED,  // Warning siren, pulsing danger zone
    FREEZING_SLOW_MO,   // Time scale decays from 1.0 down to 0.0, freezing chamber
    RESULT_SCREEN,      // Result scoreboard overlay
    PERMISSION_PROMPT   // Strictly 1-time lifetime notification prompt
}

class OverflowWatcher(
    private val world: GameWorld
) {
    var isBreaching: Boolean = false
        private set

    var currentOverflowTime: Float = 0f
        private set

    fun getDangerGraceTime(coreCount: Int): Float {
        return when {
            coreCount < 10 -> 2.0f
            coreCount <= 20 -> 1.5f
            else -> 1.0f
        }
    }

    fun update(dt: Float): Boolean {
        if (world.state != GameState.PLAYING) {
            currentOverflowTime = 0f
            isBreaching = false
            return false
        }

        val dangerLineY = world.dangerLineY
        var hasBreach = false

        for (core in world.activeCores) {
            if (core.active && core.isSpawned && core.velocity.y <= 20f) {
                if (core.position.y - core.radius < dangerLineY) {
                    hasBreach = true
                    break
                }
            }
        }

        isBreaching = hasBreach
        val graceTime = getDangerGraceTime(world.activeCores.size)

        if (hasBreach) {
            currentOverflowTime += dt
            if (currentOverflowTime >= graceTime && !world.isTutorialActive) {
                return true // Trigger Game Over!
            }
        } else {
            currentOverflowTime = (currentOverflowTime - dt * 2.5f).coerceAtLeast(0f)
        }

        return false
    }

    fun reset() {
        currentOverflowTime = 0f
        isBreaching = false
    }
}

class GameOverFSM(
    private val world: GameWorld,
    private val onGameOverTriggered: () -> Unit
) {
    var phase: GameOverPhase = GameOverPhase.PLAYING
        private set

    var timeScale: Float = 1.0f
        private set

    private var slowMoTimer: Float = 0f
    private val SLOW_MO_DURATION = 0.5f

    var hasPromptedNotificationEver: Boolean = false

    fun startOverflowWarning() {
        if (phase == GameOverPhase.PLAYING) {
            phase = GameOverPhase.OVERFLOW_DETECTED
        }
    }

    fun clearOverflowWarning() {
        if (phase == GameOverPhase.OVERFLOW_DETECTED) {
            phase = GameOverPhase.PLAYING
        }
    }

    fun triggerGameOver() {
        if (phase == GameOverPhase.FREEZING_SLOW_MO || phase == GameOverPhase.RESULT_SCREEN) return

        phase = GameOverPhase.FREEZING_SLOW_MO
        slowMoTimer = SLOW_MO_DURATION
        world.onPlaySfx?.invoke(GameWorld.SfxType.GAME_OVER, 1.0f, 1)
        onGameOverTriggered()
    }

    fun update(dt: Float) {
        when (phase) {
            GameOverPhase.FREEZING_SLOW_MO -> {
                slowMoTimer -= dt
                val progress = (slowMoTimer / SLOW_MO_DURATION).coerceIn(0f, 1f)
                timeScale = progress // Smooth deceleration to 0

                if (slowMoTimer <= 0f) {
                    timeScale = 0f
                    phase = if (!hasPromptedNotificationEver) {
                        GameOverPhase.PERMISSION_PROMPT
                    } else {
                        GameOverPhase.RESULT_SCREEN
                    }
                }
            }
            else -> {}
        }
    }

    fun onNotificationPromptDismissed() {
        hasPromptedNotificationEver = true
        phase = GameOverPhase.RESULT_SCREEN
    }

    fun retry() {
        phase = GameOverPhase.PLAYING
        timeScale = 1.0f
        world.resetForRetry()
    }

    fun reset() {
        phase = GameOverPhase.PLAYING
        timeScale = 1.0f
    }
}
