package com.example.ui

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mergepulse.R
import com.example.audio.SoundManager
import com.example.game.engine.GameMode
import com.example.game.engine.GameOverPhase
import com.example.game.engine.GameState
import com.example.game.engine.GameWorld
import com.example.game.engine.TutorialState
import com.example.game.model.MissionDefinition
import com.example.game.render.GameRenderer

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
    onNavigateToCollection: () -> Unit,
    onNavigateToMissions: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onReturnHome: () -> Unit,
    initialMode: GameMode = GameMode.ENDLESS,
    initialMission: MissionDefinition? = null
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator }
    val renderer = remember { GameRenderer() }

    var isPaused by remember { mutableStateOf(false) }

    // Frame Invalidation Bridge: triggers Compose recomposition every frame without mutating engine
    var uiTick by remember { mutableLongStateOf(0L) }

    gameWorld.currentThemeId = selectedThemeId
    gameWorld.gameOverFsm.hasPromptedNotificationEver = hasPromptedNotification

    gameWorld.onPlaySfx = { sfxType, pitch, comboStep ->
        soundManager.playSfx(sfxType, pitch, comboStep)
        // 10. Vibration: no vibration on minor bounce, gentle on merge, distinct on major events
        if (soundManager.isVibrationEnabled && sfxType != GameWorld.SfxType.BOUNCE && sfxType != GameWorld.SfxType.DROP) {
            try {
                val vibrationDurationMs = when (sfxType) {
                    GameWorld.SfxType.MERGE -> 20L
                    GameWorld.SfxType.COMBO -> 35L
                    GameWorld.SfxType.OVERCHARGE -> 60L
                    GameWorld.SfxType.DANGER -> 40L
                    GameWorld.SfxType.GAME_OVER, GameWorld.SfxType.MISSION_WIN -> 70L
                    else -> 0L
                }
                if (vibrationDurationMs > 0L) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(vibrationDurationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                }
            } catch (e: Exception) {
                // Ignore vibration missing permissions
            }
        }
    }

    LaunchedEffect(initialMode, initialMission) {
        gameWorld.startNewGame(initialMode, initialMission)
    }

    // Main Game Loop with uiTick invalidation bridge
    LaunchedEffect(isPaused) {
        var lastTimeNanos = System.nanoTime()
        while (!isPaused) {
            withFrameNanos { nowNanos ->
                val rawDt = ((nowNanos - lastTimeNanos) / 1_000_000_000f).coerceAtMost(0.033f)
                lastTimeNanos = nowNanos

                gameWorld.update(rawDt)
                uiTick++ // Signal recomposition bridge

                if (initialMode == GameMode.TUTORIAL && gameWorld.tutorialFsm.state == TutorialState.COMPLETE) {
                    onMarkFtueCompleted()
                }

                if (gameWorld.state == GameState.GAME_OVER || gameWorld.state == GameState.MISSION_COMPLETE) {
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

    // SECTION I & II: SAFE VISUAL AREA — 7% TOP MARGIN, 86% GAMEPLAY/UI, 7% BOTTOM MARGIN
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        val safeMarginTop = maxHeight * 0.07f
        val safeMarginBottom = maxHeight * 0.07f

        Column(modifier = Modifier.fillMaxSize()) {
            // TOP 7% EMPTY BREATHING SPACE
            Spacer(modifier = Modifier.height(safeMarginTop))

            // MIDDLE 86% GAME UI & PLAY AREA
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.White)
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            HudHeader(
                                world = gameWorld,
                                uiTick = uiTick,
                                onMenuClick = { isPaused = true },
                                onOverchargeClick = { gameWorld.triggerOverchargePulse() }
                            )
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))
                        }
                    },
                    bottomBar = {
                        Column(modifier = Modifier.fillMaxWidth().background(Color.White)) {
                            HorizontalDivider(thickness = 1.dp, color = Color(0xFFEEEEEE))

                            // Control Panel (READY Preview, FLIP, DROP)
                            ControlPanel(
                                world = gameWorld,
                                uiTick = uiTick,
                                onFlipClick = { gameWorld.flipCurrentCharge() },
                                onDropClick = { gameWorld.dropCurrentCore() }
                            )

                            // Bottom Navigation: corepedia | missions | settings
                            NavigationBar(
                                modifier = Modifier.height(60.dp),
                                containerColor = Color.White,
                                tonalElevation = 0.dp
                            ) {
                                NavigationBarItem(
                                    selected = false,
                                    onClick = onNavigateToCollection,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = stringResource(R.string.cd_corepedia),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.corepedia), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        unselectedIconColor = Color(0xFF4B5563),
                                        unselectedTextColor = Color(0xFF4B5563),
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_corepedia")
                                )

                                NavigationBarItem(
                                    selected = false,
                                    onClick = onNavigateToMissions,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.TrackChanges,
                                            contentDescription = stringResource(R.string.cd_missions),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.missions), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        unselectedIconColor = Color(0xFF4B5563),
                                        unselectedTextColor = Color(0xFF4B5563),
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_missions")
                                )

                                NavigationBarItem(
                                    selected = false,
                                    onClick = onNavigateToSettings,
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.Settings,
                                            contentDescription = stringResource(R.string.cd_settings),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(stringResource(R.string.settings), fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                                    colors = NavigationBarItemDefaults.colors(
                                        unselectedIconColor = Color(0xFF4B5563),
                                        unselectedTextColor = Color(0xFF4B5563),
                                        indicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.testTag("nav_settings")
                                )
                            }
                        }
                    },
                    containerColor = Color.White
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .background(Color.White)
                            .testTag("game_field_container")
                    ) {
                        val dangerText = stringResource(R.string.danger).uppercase()
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
                                        bottomMargin = 6f
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
                            val currentTick = uiTick
                            renderer.render(
                                canvas = drawContext.canvas.nativeCanvas,
                                world = gameWorld,
                                screenWidth = size.width,
                                screenHeight = size.height,
                                dangerLabel = dangerText
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

                        // In-game Pause & Quick Navigation Menu (Localized + Large Fonts)
                        if (isPaused) {
                            AlertDialog(
                                onDismissRequest = { isPaused = false },
                                title = { Text(stringResource(R.string.pause_title), color = Color(0xFF111827), fontWeight = FontWeight.Black, fontSize = 22.sp) },
                                text = {
                                    Column {
                                        Text(stringResource(R.string.current_score, gameWorld.score), color = Color(0xFF1F2937), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                isPaused = false
                                                onNavigateToCollection()
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(stringResource(R.string.view_corepedia), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                isPaused = false
                                                onNavigateToMissions()
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(stringResource(R.string.daily_missions), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Button(
                                            onClick = {
                                                isPaused = false
                                                onNavigateToSettings()
                                            },
                                            modifier = Modifier.fillMaxWidth().height(48.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                                            shape = RoundedCornerShape(12.dp)
                                        ) {
                                            Text(stringResource(R.string.settings), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { isPaused = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(stringResource(R.string.resume), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = {
                                        isPaused = false
                                        onReturnHome()
                                    }) {
                                        Text(stringResource(R.string.quit_to_menu), color = Color(0xFF6B7280), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    }
                                },
                                containerColor = Color.White
                            )
                        }
                    }
                }
            }

            // BOTTOM 7% EMPTY BREATHING SPACE
            Spacer(modifier = Modifier.height(safeMarginBottom))
        }
    }
}
