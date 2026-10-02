package com.example.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.example.audio.SoundManager
import com.example.game.engine.GameMode
import com.example.game.engine.GameOverPhase
import com.example.game.engine.GameState
import com.example.game.engine.GameWorld
import com.example.game.engine.TutorialState
import com.example.game.model.MissionDefinition
import com.example.game.render.GameRenderer
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonCyan

@Composable
fun GameScreen(
    gameWorld: GameWorld,
    soundManager: SoundManager,
    selectedThemeId: String,
    hasPromptedNotification: Boolean,
    onSaveResult: (score: Int, combo: Int, merges: Int, highestLvl: Int) -> Unit,
    onMissionCompleted: (missionId: Int) -> Unit,
    onMarkFtueCompleted: () -> Unit,
    onMarkNotificationPrompted: () -> Unit,
    onReturnHome: () -> Unit,
    onOpenCorepedia: () -> Unit,
    onOpenMissions: () -> Unit,
    onOpenSettings: () -> Unit,
    initialMode: GameMode = GameMode.ENDLESS,
    initialMission: MissionDefinition? = null
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val renderer = remember { GameRenderer() }

    var isPaused by remember { mutableStateOf(false) }
    var uiTick by remember { mutableStateOf(0L) }
    var resultSaved by remember { mutableStateOf(false) }

    gameWorld.currentThemeId = selectedThemeId
    gameWorld.gameOverFsm.hasPromptedNotificationEver = hasPromptedNotification

    gameWorld.onPlaySfx = { sfxType, pitch, comboStep ->
        soundManager.playSfx(sfxType, pitch, comboStep)
        if (soundManager.isVibrationEnabled) {
            try {
                vibrator?.vibrate(VibrationEffect.createOneShot(35L, VibrationEffect.DEFAULT_AMPLITUDE))
            } catch (e: Exception) {
                // Ignore vibration missing permissions
            }
        }
    }

    LaunchedEffect(initialMode, initialMission) {
        resultSaved = false
        isPaused = false
        gameWorld.startNewGame(initialMode, initialMission)
    }

    LaunchedEffect(isPaused) {
        var lastTimeNanos = System.nanoTime()
        while (!isPaused) {
            withFrameNanos { nowNanos ->
                val rawDt = ((nowNanos - lastTimeNanos) / 1_000_000_000f).coerceAtMost(0.033f)
                lastTimeNanos = nowNanos

                gameWorld.update(rawDt)
                uiTick++

                if (initialMode == GameMode.TUTORIAL && gameWorld.tutorialFsm.state == TutorialState.COMPLETE) {
                    onMarkFtueCompleted()
                }

                if (!resultSaved && (gameWorld.state == GameState.GAME_OVER || gameWorld.state == GameState.MISSION_COMPLETE)) {
                    resultSaved = true;
                    onSaveResult(
                        gameWorld.score,
                        gameWorld.bestCombo,
                        gameWorld.totalMergesInGame,
                        gameWorld.highestLevelReached
                    )
                    if (gameWorld.state == GameState.MISSION_COMPLETE && gameWorld.activeMission != null) {
                        onMissionCompleted(gameWorld.activeMission!!.id)
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            HudHeader(
                world = gameWorld,
                onMenuClick = { isPaused = true },
                onOverchargeClick = { gameWorld.triggerOverchargePulse() }
            )
        },
        bottomBar = {
            Column {
                ControlPanel(world = gameWorld, onFlipClick = { gameWorld.flipCurrentCharge() }, onDropClick = { gameWorld.dropCurrentCore() })
                NavigationBar(containerColor = DarkSurface, tonalElevation = 0.dp, modifier = Modifier.testTag("game_bottom_nav")) {
                    NavigationBarItem(false, onOpenCorepedia, icon = { Icon(Icons.Default.AutoAwesome, "Corepedia") }, label = { Text("Corepedia") })
                    NavigationBarItem(false, onOpenMissions, icon = { Icon(Icons.Default.TaskAlt, "Missions") }, label = { Text("Missions") })
                    NavigationBarItem(false, onOpenSettings, icon = { Icon(Icons.Default.Settings, "Settings") }, label = { Text("Settings") })
                }
            }
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBg)
                .testTag("game_field_container")
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        val width = coordinates.size.width.toFloat()
                        val height = coordinates.size.height.toFloat()
                        gameWorld.setChamberBounds(
                            width = width,
                            height = height,
                            topOffset = 0f,
                            bottomMargin = 0f
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, _ ->
                                gameWorld.setAimPosition(change.position.x)
                            },
                            onDragEnd = {
                                gameWorld.dropCurrentCore()
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            gameWorld.setAimPosition(offset.x)
                            gameWorld.dropCurrentCore()
                        }
                    }
            ) {
                if (uiTick >= 0L) renderer.render(
                    canvas = drawContext.canvas.nativeCanvas,
                    world = gameWorld,
                    screenWidth = size.width,
                    screenHeight = size.height
                )
            }

            // Interactive Tutorial Guidance Overlay
            if (gameWorld.isTutorialActive) {
                TutorialOverlayBanner(
                    fsm = gameWorld.tutorialFsm,
                    onSkipClick = {
                        gameWorld.tutorialFsm.skip()
                        onMarkFtueCompleted()
                    },
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }

            // Game Over & 1-Time Notification Prompt Overlay
            if (gameWorld.state == GameState.GAME_OVER || gameWorld.state == GameState.MISSION_COMPLETE) {
                if (gameWorld.gameOverFsm.phase == GameOverPhase.RESULT_SCREEN ||
                    gameWorld.gameOverFsm.phase == GameOverPhase.PERMISSION_PROMPT ||
                    gameWorld.state == GameState.MISSION_COMPLETE
                ) {
                    GameOverDialog(
                        world = gameWorld,
                        onPlayAgain = {
                            gameWorld.gameOverFsm.retry()
                        },
                        onMenuClick = onReturnHome,
                        onNotificationPromptDismissed = {
                            gameWorld.gameOverFsm.onNotificationPromptDismissed()
                            onMarkNotificationPrompted()
                        }
                    )
                }
            }

            if (isPaused) {
                AlertDialog(
                    onDismissRequest = { isPaused = false },
                    title = { Text("GAME PAUSED", color = NeonCyan, fontWeight = FontWeight.Bold) },
                    text = { Text("Current Score: ${gameWorld.score}", color = Color.White) },
                    confirmButton = {
                        Button(onClick = { isPaused = false }) {
                            Text("RESUME")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            isPaused = false
                            onReturnHome()
                        }) {
                            Text("QUIT TO MENU", color = Color.Gray)
                        }
                    },
                    containerColor = DarkSurface
                )
            }
        }
    }
}
