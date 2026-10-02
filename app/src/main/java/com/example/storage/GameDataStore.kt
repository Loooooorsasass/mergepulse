package com.example.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "merge_pulse_prefs")

data class UserGameStats(
    val bestScore: Int = 0,
    val bestCombo: Int = 1,
    val totalGames: Int = 0,
    val totalMerges: Int = 0,
    val highestLevelUnlocked: Int = 1,
    val completedMissionIds: Set<Int> = emptySet(),
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val credits: Int = 150,
    val dailyStreak: Int = 1,
    val lastLoginTimestamp: Long = 0L,
    val unlockedThemes: Set<String> = setOf("default"),
    val selectedTheme: String = "default",
    val hasCompletedFtue: Boolean = false,
    val hasPromptedNotification: Boolean = false,
    val selectedLanguage: String = "en"
)

class GameDataStore(private val context: Context) {

    private object Keys {
        val BEST_SCORE = intPreferencesKey("best_score")
        val BEST_COMBO = intPreferencesKey("best_combo")
        val TOTAL_GAMES = intPreferencesKey("total_games")
        val TOTAL_MERGES = intPreferencesKey("total_merges")
        val HIGHEST_LEVEL = intPreferencesKey("highest_level")
        val COMPLETED_MISSIONS = stringPreferencesKey("completed_missions")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val VIBRATION_ENABLED = booleanPreferencesKey("vibration_enabled")
        val CREDITS = intPreferencesKey("credits")
        val DAILY_STREAK = intPreferencesKey("daily_streak")
        val LAST_LOGIN = longPreferencesKey("last_login")
        val UNLOCKED_THEMES = stringPreferencesKey("unlocked_themes")
        val SELECTED_THEME = stringPreferencesKey("selected_theme")
        val HAS_COMPLETED_FTUE = booleanPreferencesKey("has_completed_ftue")
        val HAS_PROMPTED_NOTIFICATION = booleanPreferencesKey("has_prompted_notification")
        val SELECTED_LANGUAGE = stringPreferencesKey("selected_language")
    }

    val statsFlow: Flow<UserGameStats> = context.dataStore.data.map { prefs ->
        val missionsStr = prefs[Keys.COMPLETED_MISSIONS] ?: ""
        val missionIds = missionsStr.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()

        val themesStr = prefs[Keys.UNLOCKED_THEMES] ?: "default"
        val themes = themesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

        UserGameStats(
            bestScore = prefs[Keys.BEST_SCORE] ?: 0,
            bestCombo = prefs[Keys.BEST_COMBO] ?: 1,
            totalGames = prefs[Keys.TOTAL_GAMES] ?: 0,
            totalMerges = prefs[Keys.TOTAL_MERGES] ?: 0,
            highestLevelUnlocked = prefs[Keys.HIGHEST_LEVEL] ?: 1,
            completedMissionIds = missionIds,
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            vibrationEnabled = prefs[Keys.VIBRATION_ENABLED] ?: true,
            credits = prefs[Keys.CREDITS] ?: 150,
            dailyStreak = prefs[Keys.DAILY_STREAK] ?: 1,
            lastLoginTimestamp = prefs[Keys.LAST_LOGIN] ?: 0L,
            unlockedThemes = if (themes.isEmpty()) setOf("default") else themes,
            selectedTheme = prefs[Keys.SELECTED_THEME] ?: "default",
            hasCompletedFtue = prefs[Keys.HAS_COMPLETED_FTUE] ?: false,
            hasPromptedNotification = prefs[Keys.HAS_PROMPTED_NOTIFICATION] ?: false,
            selectedLanguage = prefs[Keys.SELECTED_LANGUAGE] ?: "en"
        )
    }

    suspend fun markFtueCompleted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_COMPLETED_FTUE] = true
        }
    }

    suspend fun markNotificationPrompted() {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_PROMPTED_NOTIFICATION] = true
        }
    }

    suspend fun saveGameResult(
        score: Int,
        combo: Int,
        mergesInGame: Int,
        highestLevelInGame: Int
    ) {
        context.dataStore.edit { prefs ->
            val currentBestScore = prefs[Keys.BEST_SCORE] ?: 0
            if (score > currentBestScore) {
                prefs[Keys.BEST_SCORE] = score
            }

            val currentBestCombo = prefs[Keys.BEST_COMBO] ?: 1
            if (combo > currentBestCombo) {
                prefs[Keys.BEST_COMBO] = combo
            }

            val currentGames = prefs[Keys.TOTAL_GAMES] ?: 0
            prefs[Keys.TOTAL_GAMES] = currentGames + 1

            val currentMerges = prefs[Keys.TOTAL_MERGES] ?: 0
            prefs[Keys.TOTAL_MERGES] = currentMerges + mergesInGame

            val currentHighestLevel = prefs[Keys.HIGHEST_LEVEL] ?: 1
            if (highestLevelInGame > currentHighestLevel) {
                prefs[Keys.HIGHEST_LEVEL] = highestLevelInGame
            }

            val earnedCredits = (score / 20) + (mergesInGame * 2)
            val currentCredits = prefs[Keys.CREDITS] ?: 150
            prefs[Keys.CREDITS] = currentCredits + earnedCredits
        }
    }

    suspend fun markMissionCompleted(missionId: Int, rewardPoints: Int) {
        context.dataStore.edit { prefs ->
            val missionsStr = prefs[Keys.COMPLETED_MISSIONS] ?: ""
            val currentIds = missionsStr.split(",")
                .mapNotNull { it.trim().toIntOrNull() }
                .toMutableSet()

            if (!currentIds.contains(missionId)) {
                currentIds.add(missionId)
                prefs[Keys.COMPLETED_MISSIONS] = currentIds.joinToString(",")

                val currentCredits = prefs[Keys.CREDITS] ?: 150
                prefs[Keys.CREDITS] = currentCredits + rewardPoints
            }
        }
    }

    suspend fun updateDailyStreak() {
        context.dataStore.edit { prefs ->
            val now = System.currentTimeMillis()
            val lastLogin = prefs[Keys.LAST_LOGIN] ?: 0L
            val oneDayMs = 24 * 60 * 60 * 1000L

            val currentStreak = prefs[Keys.DAILY_STREAK] ?: 1
            if (now - lastLogin in oneDayMs..(2 * oneDayMs)) {
                prefs[Keys.DAILY_STREAK] = currentStreak + 1
                val currentCredits = prefs[Keys.CREDITS] ?: 150
                prefs[Keys.CREDITS] = currentCredits + (currentStreak * 50)
            } else if (now - lastLogin > 2 * oneDayMs) {
                prefs[Keys.DAILY_STREAK] = 1
            }
            prefs[Keys.LAST_LOGIN] = now
        }
    }

    suspend fun unlockTheme(themeId: String, cost: Int): Boolean {
        var success = false
        context.dataStore.edit { prefs ->
            val currentCredits = prefs[Keys.CREDITS] ?: 150
            if (currentCredits >= cost) {
                prefs[Keys.CREDITS] = currentCredits - cost

                val themesStr = prefs[Keys.UNLOCKED_THEMES] ?: "default"
                val themes = themesStr.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
                themes.add(themeId)
                prefs[Keys.UNLOCKED_THEMES] = themes.joinToString(",")
                prefs[Keys.SELECTED_THEME] = themeId
                success = true
            }
        }
        return success
    }

    suspend fun setSelectedTheme(themeId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_THEME] = themeId
        }
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = enabled
        }
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.VIBRATION_ENABLED] = enabled
        }
    }

    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_LANGUAGE] = languageCode
        }
    }

    suspend fun resetAllProgress() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }
}
