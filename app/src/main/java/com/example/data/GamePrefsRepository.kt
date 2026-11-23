package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.gameDataStore: DataStore<Preferences> by preferencesDataStore(name = "messi_slap_prefs")

data class GameStatsPrefs(
  val highScore: Int = 0,
  val bestCombo: Int = 0,
  val totalSlaps: Int = 0,
  val soundEnabled: Boolean = true,
)

class GamePrefsRepository(private val context: Context) {
  private object Keys {
    val HIGH_SCORE = intPreferencesKey("high_score")
    val BEST_COMBO = intPreferencesKey("best_combo")
    val TOTAL_SLAPS = intPreferencesKey("total_slaps")
    val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
  }

  val prefsFlow: Flow<GameStatsPrefs> =
    context.gameDataStore.data.map { prefs ->
      GameStatsPrefs(
        highScore = prefs[Keys.HIGH_SCORE] ?: 0,
        bestCombo = prefs[Keys.BEST_COMBO] ?: 0,
        totalSlaps = prefs[Keys.TOTAL_SLAPS] ?: 0,
        soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
      )
    }

  suspend fun saveRunResult(score: Int, maxCombo: Int, slapsInRun: Int) {
    context.gameDataStore.edit { prefs ->
      val prevHigh = prefs[Keys.HIGH_SCORE] ?: 0
      if (score > prevHigh) {
        prefs[Keys.HIGH_SCORE] = score
      }
      val prevCombo = prefs[Keys.BEST_COMBO] ?: 0
      if (maxCombo > prevCombo) {
        prefs[Keys.BEST_COMBO] = maxCombo
      }
      val prevTotal = prefs[Keys.TOTAL_SLAPS] ?: 0
      prefs[Keys.TOTAL_SLAPS] = prevTotal + slapsInRun
    }
  }

  suspend fun setSoundEnabled(enabled: Boolean) {
    context.gameDataStore.edit { prefs ->
      prefs[Keys.SOUND_ENABLED] = enabled
    }
  }
}
