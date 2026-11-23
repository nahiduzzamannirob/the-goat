package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ArgentinaSky
import com.example.ui.theme.RonaldoCrimson
import com.example.ui.theme.TrophyGold

enum class GamePhase {
  MENU,
  PLAYING,
  PAUSED,
  GAME_OVER,
}

enum class SleepReaction {
  PEACEFUL,
  NERVOUS,
  BITTEN,
}

enum class FlyerKind(
  val displayName: String,
  val basePoints: Int,
  val radiusDp: Float,
  val hitRadiusDp: Float,
  val accentColor: Color,
  val mustBeHit: Boolean,
) {
  RONALDO_MOSQUITO(
    displayName = "CR7 Mosquito",
    basePoints = 25,
    radiusDp = 38f,
    hitRadiusDp = 34f,
    accentColor = RonaldoCrimson,
    mustBeHit = true,
  ),
  RONALDO_GOLD_BOSS(
    displayName = "Golden CR7",
    basePoints = 80,
    radiusDp = 42f,
    hitRadiusDp = 38f,
    accentColor = TrophyGold,
    mustBeHit = true,
  ),
  ISHOWSPEED_MOSQUITO(
    displayName = "IShowSpeed Mosquito",
    basePoints = 40,
    radiusDp = 38f,
    hitRadiusDp = 34f,
    accentColor = ArgentinaSky,
    mustBeHit = true,
  ),
}

data class FlyingCharacter(
  val id: Long,
  val kind: FlyerKind,
  val x: Float,
  val y: Float,
  val baseY: Float,
  val vx: Float,
  val passesRemaining: Int = 2,
  val ageSeconds: Float = 0f,
  val phaseOffset: Float = 0f,
  val waveAmplitude: Float = 0.05f,
  val waveFrequency: Float = 4.6f,
  val dialogue: String? = null,
  val dialogueRemaining: Float = 0f,
  val facingRight: Boolean = true,
)

data class SandalProjectile(
  val id: Long,
  val x: Float,
  val y: Float,
  val vx: Float,
  val vy: Float,
  val spinAngleDeg: Float = 0f,
  val spinSpeedDegPerSec: Float = 960f,
  val isGoldSandal: Boolean = false,
)

data class SlapEffect(
  val id: Long,
  val x: Float,
  val y: Float,
  val label: String,
  val accentColor: Color,
  val isDirectHit: Boolean,
  val ageSeconds: Float = 0f,
  val durationSeconds: Float = 0.65f,
)

data class ZzzBubble(
  val id: Long,
  val x: Float,
  val y: Float,
  val ageSeconds: Float = 0f,
  val durationSeconds: Float = 2.2f,
)

data class GameUiState(
  val phase: GamePhase = GamePhase.MENU,
  val score: Int = 0,
  val highScore: Int = 0,
  val bestCombo: Int = 0,
  val wave: Int = 1,
  val combo: Int = 0,
  val runMaxCombo: Int = 0,
  val slapsLanded: Int = 0,
  val sleepReaction: SleepReaction = SleepReaction.PEACEFUL,
  val messiReactionBubble: String? = null,
  val messiReactionTimer: Float = 0f,
  val gameOverReason: String = "",
  val bedShakeIntensity: Float = 0f,
  val throwAimAngleDeg: Float = -90f,
  val throwArmSwing: Float = 0f,
  val throwCooldownRemaining: Float = 0f,
  val isAiming: Boolean = false,
  val aimNormX: Float = 0.5f,
  val aimNormY: Float = 0.35f,
  val soundEnabled: Boolean = true,
  val isNewHighScore: Boolean = false,
  val flyers: List<FlyingCharacter> = emptyList(),
  val sandals: List<SandalProjectile> = emptyList(),
  val slapEffects: List<SlapEffect> = emptyList(),
  val zzzBubbles: List<ZzzBubble> = emptyList(),
)
