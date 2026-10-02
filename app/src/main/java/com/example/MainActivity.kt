package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.audio.SoundManager
import com.example.game.engine.GameMode
import com.example.game.engine.GameWorld
import com.example.game.model.DailyMissionsManager
import com.example.game.model.MissionDefinition
import com.example.game.model.MissionRegistry
import com.example.storage.GameDataStore
import com.example.storage.UserGameStats
import com.example.ui.CollectionScreen
import com.example.ui.GameScreen
import com.example.ui.HomeScreen
import com.example.ui.MissionsScreen
import com.example.ui.SettingsScreen
import com.example.ui.ShopScreen
import com.example.ui.theme.MergePulseTheme
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    GAME,
    MISSIONS,
    COLLECTION,
    SHOP,
    SETTINGS
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val dataStore = GameDataStore(applicationContext)
        val soundManager = SoundManager()
        val gameWorld = GameWorld()

        setContent {
            MergePulseTheme {
                val stats by dataStore.statsFlow.collectAsState(initial = UserGameStats())
                val coroutineScope = rememberCoroutineScope()

                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                var currentGameMode by remember { mutableStateOf(GameMode.ENDLESS) }
                var selectedMission by remember { mutableStateOf<MissionDefinition?>(null) }
                var initialFtueChecked by remember { mutableStateOf(false) }

                // Initialize deterministic daily missions manager
                val missionsManager = remember {
                    val todayIds = MissionRegistry.getTodayMissionIds()
                    DailyMissionsManager(todayIds) { completedDef ->
                        coroutineScope.launch {
                            dataStore.markMissionCompleted(completedDef.id, completedDef.rewardCredits)
                        }
                    }
                }

                LaunchedEffect(stats.completedMissionIds) {
                    missionsManager.syncCompleted(stats.completedMissionIds)
                }

                gameWorld.missionsManager = missionsManager

                LaunchedEffect(stats.hasCompletedFtue) {
                    dataStore.updateDailyStreak()

                    if (!stats.hasCompletedFtue && !initialFtueChecked) {
                        initialFtueChecked = true
                        currentGameMode = GameMode.TUTORIAL
                        selectedMission = null
                        currentScreen = Screen.GAME
                    }
                }

                soundManager.isSoundEnabled = stats.soundEnabled
                soundManager.isVibrationEnabled = stats.vibrationEnabled

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (currentScreen) {
                        Screen.HOME -> {
                            HomeScreen(
                                stats = stats,
                                onPlayEndless = {
                                    currentGameMode = GameMode.ENDLESS
                                    selectedMission = null
                                    currentScreen = Screen.GAME
                                },
                                onMissionsClick = {
                                    currentScreen = Screen.MISSIONS
                                },
                                onCollectionClick = {
                                    currentScreen = Screen.COLLECTION
                                },
                                onShopClick = {
                                    currentScreen = Screen.SHOP
                                },
                                onSettingsClick = {
                                    currentScreen = Screen.SETTINGS
                                },
                                onReplayTutorialClick = {
                                    currentGameMode = GameMode.TUTORIAL
                                    selectedMission = null
                                    currentScreen = Screen.GAME
                                }
                            )
                        }

                        Screen.GAME -> {
                            BackHandler {
                                currentScreen = Screen.HOME
                            }
                            GameScreen(
                                gameWorld = gameWorld,
                                soundManager = soundManager,
                                selectedThemeId = stats.selectedTheme,
                                hasPromptedNotification = stats.hasPromptedNotification,
                                onSaveResult = { score, combo, merges, highestLvl ->
                                    coroutineScope.launch {
                                        dataStore.saveGameResult(score, combo, merges, highestLvl)
                                    }
                                },
                                onMissionCompleted = { missionId ->
                                    coroutineScope.launch {
                                        val reward = MissionRegistry.getDefinition(missionId)?.rewardCredits ?: 100
                                        dataStore.markMissionCompleted(missionId, reward)
                                    }
                                },
                                onMarkFtueCompleted = {
                                    coroutineScope.launch {
                                        dataStore.markFtueCompleted()
                                    }
                                },
                                onMarkNotificationPrompted = {
                                    coroutineScope.launch {
                                        dataStore.markNotificationPrompted()
                                    }
                                },
                                onReturnHome = {
                                    currentScreen = Screen.HOME
                                },
                                initialMode = currentGameMode,
                                initialMission = selectedMission
                            )
                        }

                        Screen.MISSIONS -> {
                            BackHandler {
                                currentScreen = Screen.HOME
                            }
                            MissionsScreen(
                                completedMissionIds = stats.completedMissionIds,
                                onStartMission = { mission ->
                                    currentGameMode = GameMode.MISSION
                                    selectedMission = mission
                                    currentScreen = Screen.GAME
                                },
                                onBackClick = {
                                    currentScreen = Screen.HOME
                                }
                            )
                        }

                        Screen.COLLECTION -> {
                            BackHandler {
                                currentScreen = Screen.HOME
                            }
                            CollectionScreen(
                                highestLevelUnlocked = stats.highestLevelUnlocked,
                                onBackClick = {
                                    currentScreen = Screen.HOME
                                }
                            )
                        }

                        Screen.SHOP -> {
                            BackHandler {
                                currentScreen = Screen.HOME
                            }
                            ShopScreen(
                                stats = stats,
                                onUnlockTheme = { themeId, cost ->
                                    coroutineScope.launch {
                                        dataStore.unlockTheme(themeId, cost)
                                    }
                                },
                                onSelectTheme = { themeId ->
                                    coroutineScope.launch {
                                        dataStore.setSelectedTheme(themeId)
                                    }
                                },
                                onBackClick = {
                                    currentScreen = Screen.HOME
                                }
                            )
                        }

                        Screen.SETTINGS -> {
                            BackHandler {
                                currentScreen = Screen.HOME
                            }
                            SettingsScreen(
                                stats = stats,
                                onToggleSound = { enabled ->
                                    coroutineScope.launch {
                                        dataStore.setSoundEnabled(enabled)
                                    }
                                },
                                onToggleVibration = { enabled ->
                                    coroutineScope.launch {
                                        dataStore.setVibrationEnabled(enabled)
                                    }
                                },
                                onResetProgress = {
                                    coroutineScope.launch {
                                        dataStore.resetAllProgress()
                                    }
                                },
                                onBackClick = {
                                    currentScreen = Screen.HOME
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
