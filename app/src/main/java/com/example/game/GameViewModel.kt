package com.example.game

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.GameSoundEngine
import com.example.data.GamePrefsRepository
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameViewModel(application: Application) : AndroidViewModel(application) {
  companion object {
    const val MESSI_HEAD_X = 0.52f
    const val MESSI_HEAD_Y = 0.77f
    const val THROW_ORIGIN_X = 0.62f
    const val THROW_ORIGIN_Y = 0.81f
    const val SANDAL_SPEED_HEIGHTS_PER_SEC = 1.98f
    const val SANDAL_GRAVITY_NORM = 0.32f
    private const val TAP_THROW_COOLDOWN_SEC = 0.10f
    private const val DRAG_THROW_COOLDOWN_SEC = 0.18f
  }

  private val prefsRepository = GamePrefsRepository(application.applicationContext)
  private val soundEngine = GameSoundEngine(application.applicationContext)

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  private var nextEntityId = 1L
  private var spawnCooldown = 0.5f
  private var zzzCooldown = 0.8f
  private var snoreCooldown = 3.2f
  private var nextEntryFromRight = true
  private var lastViewportWidthPx = 1080f
  private var lastViewportHeightPx = 1920f
  private var lastDensity = 2.75f

  private val ronaldoLines =
    listOf(
      "SIUUU! Bzzz!",
      "Catch me, Leo!",
      "7 > 10 bzzz!",
      "Ankara Mosquito!",
      "Bzzz-SIUUU!",
      "Too fast for u!",
    )

  private val ronaldoReturnLines =
    listOf(
      "LAST CHANCE, LEO!",
      "TURBO SIUUU!!",
      "BYE BYE LEO!",
    )

  private val ronaldoBossLines =
    listOf(
      "GOLDEN SIUUU!",
      "CR7 OVERDRIVE!",
    )

  private val speedLines =
    listOf(
      "SEWEYYY! BARK!",
      "CR7 IS THE GOAT!",
      "SERIOUS RIGHT NEOW?!",
      "ARF ARF! Bzzz!",
      "BEN?! BZZZ!",
    )

  private val speedReturnLines =
    listOf(
      "SPEED BOOST! BARK!",
      "TOO FAST NEOW!",
      "SEWEYYY RETURN!",
    )

  init {
    viewModelScope.launch {
      prefsRepository.prefsFlow.collect { stats ->
        soundEngine.soundEnabled = stats.soundEnabled
        _uiState.update { state ->
          state.copy(
            highScore = stats.highScore,
            bestCombo = stats.bestCombo,
            soundEnabled = stats.soundEnabled,
          )
        }
      }
    }
    seedMenuPreviewFlyers()
  }

  private fun seedMenuPreviewFlyers() {
    val preview =
      listOf(
        FlyingCharacter(
          id = nextEntityId++,
          kind = FlyerKind.RONALDO_MOSQUITO,
          x = 0.76f,
          y = 0.25f,
          baseY = 0.25f,
          vx = -0.32f,
          passesRemaining = 999,
          phaseOffset = 0.4f,
          dialogue = "Bzzz! SIUUU!",
          dialogueRemaining = 999f,
          facingRight = false,
        ),
        FlyingCharacter(
          id = nextEntityId++,
          kind = FlyerKind.ISHOWSPEED_MOSQUITO,
          x = 0.24f,
          y = 0.36f,
          baseY = 0.36f,
          vx = 0.35f,
          passesRemaining = 999,
          phaseOffset = 2.2f,
          dialogue = "SEWEYYY! BARK!",
          dialogueRemaining = 999f,
          facingRight = true,
        ),
      )
    _uiState.update { it.copy(flyers = preview) }
  }

  fun updateViewportMetrics(widthPx: Float, heightPx: Float, density: Float) {
    if (widthPx > 0f && heightPx > 0f) {
      lastViewportWidthPx = widthPx
      lastViewportHeightPx = heightPx
      lastDensity = density
    }
  }

  fun startGame() {
    spawnCooldown = 1.4f
    zzzCooldown = 0.5f
    snoreCooldown = 2.4f
    nextEntryFromRight = true
    soundEngine.setPaused(false)
    soundEngine.buzzIntensity = 0.26f

    // Start with a fast Ronaldo mosquito flying Right -> Left
    val starterMosquito =
      createCrossingMosquito(
        kind = FlyerKind.RONALDO_MOSQUITO,
        wave = 1,
        slapsLanded = 0,
        fromRight = true,
      )
    nextEntryFromRight = false

    _uiState.update { state ->
      state.copy(
        phase = GamePhase.PLAYING,
        score = 0,
        wave = 1,
        combo = 0,
        runMaxCombo = 0,
        slapsLanded = 0,
        sleepReaction = SleepReaction.PEACEFUL,
        messiReactionBubble = null,
        messiReactionTimer = 0f,
        gameOverReason = "",
        bedShakeIntensity = 0f,
        throwAimAngleDeg = -90f,
        throwArmSwing = 0f,
        throwCooldownRemaining = 0f,
        isAiming = false,
        isNewHighScore = false,
        flyers = listOf(starterMosquito),
        sandals = emptyList(),
        slapEffects = emptyList(),
        zzzBubbles = emptyList(),
      )
    }
    soundEngine.speakMemeLine("Bzzz! Siuuu!", force = true)
  }

  fun togglePause() {
    val current = _uiState.value.phase
    if (current == GamePhase.PLAYING) {
      soundEngine.buzzIntensity = 0f
      _uiState.update { it.copy(phase = GamePhase.PAUSED, isAiming = false) }
    } else if (current == GamePhase.PAUSED) {
      _uiState.update { it.copy(phase = GamePhase.PLAYING) }
    }
  }

  fun onAppPause() {
    soundEngine.setPaused(true)
    if (_uiState.value.phase == GamePhase.PLAYING) {
      _uiState.update { it.copy(phase = GamePhase.PAUSED, isAiming = false) }
    }
  }

  fun onAppResume() {
    soundEngine.setPaused(false)
  }

  fun toggleSound() {
    val newValue = !_uiState.value.soundEnabled
    soundEngine.soundEnabled = newValue
    if (!newValue) {
      soundEngine.buzzIntensity = 0f
    }
    _uiState.update { it.copy(soundEnabled = newValue) }
    viewModelScope.launch { prefsRepository.setSoundEnabled(newValue) }
  }

  fun throwSandalToward(targetXPx: Float, targetYPx: Float, isDragRepeat: Boolean = false) {
    val state = _uiState.value
    if (state.phase != GamePhase.PLAYING) return

    val w = lastViewportWidthPx.coerceAtLeast(1f)
    val h = lastViewportHeightPx.coerceAtLeast(1f)
    val normX = (targetXPx / w).coerceIn(0.04f, 0.96f)
    val normY = (targetYPx / h).coerceIn(0.08f, 0.68f)

    val dxPx = normX * w - THROW_ORIGIN_X * w
    val dyPx = (normY * h - THROW_ORIGIN_Y * h).coerceAtMost(-h * 0.10f)
    val aimDeg = (atan2(dyPx, dxPx) * 180.0 / PI).toFloat()

    if (state.throwCooldownRemaining > 0f) {
      _uiState.update { cur ->
        cur.copy(
          isAiming = true,
          aimNormX = normX,
          aimNormY = normY,
          throwAimAngleDeg = aimDeg,
        )
      }
      return
    }

    val distPx = hypot(dxPx, dyPx).coerceAtLeast(1f)
    val dirX = dxPx / distPx
    val dirY = dyPx / distPx
    val speedPxPerSec = h * SANDAL_SPEED_HEIGHTS_PER_SEC

    val vxNorm = (dirX * speedPxPerSec) / w
    val vyNorm = (dirY * speedPxPerSec) / h

    val startX = (THROW_ORIGIN_X + (dirX * 34f * lastDensity) / w).coerceIn(0.04f, 0.96f)
    val startY = (THROW_ORIGIN_Y + (dirY * 34f * lastDensity) / h).coerceIn(0.08f, 0.90f)

    val sandal =
      SandalProjectile(
        id = nextEntityId++,
        x = startX,
        y = startY,
        vx = vxNorm,
        vy = vyNorm,
        spinAngleDeg = aimDeg,
        spinSpeedDegPerSec = if (dxPx >= 0f) 1180f else -1180f,
        isGoldSandal = state.combo >= 5,
      )

    soundEngine.playSandalThrow()

    _uiState.update { cur ->
      cur.copy(
        isAiming = true,
        aimNormX = normX,
        aimNormY = normY,
        throwAimAngleDeg = aimDeg,
        throwArmSwing = 1.0f,
        throwCooldownRemaining = if (isDragRepeat) DRAG_THROW_COOLDOWN_SEC else TAP_THROW_COOLDOWN_SEC,
        sandals = (cur.sandals + sandal).takeLast(14),
      )
    }
  }

  fun endAim() {
    _uiState.update { it.copy(isAiming = false) }
  }

  fun tick(dtSeconds: Float) {
    val dt = dtSeconds.coerceIn(0f, 0.05f)
    if (dt <= 0f) return

    val state = _uiState.value
    if (state.phase == GamePhase.PAUSED || state.phase == GamePhase.GAME_OVER) {
      soundEngine.buzzIntensity = 0f
      return
    }

    zzzCooldown -= dt
    val nextZzz =
      state.zzzBubbles
        .mapNotNull { z ->
          val nextAge = z.ageSeconds + dt
          if (nextAge >= z.durationSeconds) null
          else z.copy(ageSeconds = nextAge, y = z.y - dt * 0.06f, x = z.x + sin(nextAge * 4f) * dt * 0.02f)
        }
        .toMutableList()

    if (zzzCooldown <= 0f && state.sleepReaction != SleepReaction.BITTEN) {
      zzzCooldown = 0.95f
      nextZzz.add(
        ZzzBubble(
          id = nextEntityId++,
          x = MESSI_HEAD_X + 0.06f + Random.nextFloat() * 0.04f,
          y = MESSI_HEAD_Y - 0.06f,
        )
      )
    }

    if (state.phase == GamePhase.MENU) {
      soundEngine.buzzIntensity = 0f
      val menuFlyers =
        state.flyers.map { flyer ->
          val age = flyer.ageSeconds + dt
          var nx = flyer.x + flyer.vx * dt
          var nvx = flyer.vx
          if (nx < 0.08f) {
            nx = 0.08f
            nvx = abs(nvx)
          } else if (nx > 0.92f) {
            nx = 0.92f
            nvx = -abs(nvx)
          }
          val ny = (flyer.baseY + sin(age * flyer.waveFrequency + flyer.phaseOffset) * flyer.waveAmplitude).coerceIn(0.14f, 0.48f)
          flyer.copy(x = nx, y = ny, vx = nvx, ageSeconds = age, facingRight = nvx >= 0f)
        }
      _uiState.update { it.copy(flyers = menuFlyers, zzzBubbles = nextZzz) }
      return
    }

    // --- PLAYING UPDATE ---
    snoreCooldown -= dt
    if (snoreCooldown <= 0f && state.sleepReaction == SleepReaction.PEACEFUL) {
      snoreCooldown = 3.6f + Random.nextFloat() * 1.5f
      soundEngine.playSnore()
    }

    var score = state.score
    var wave = state.wave
    var combo = state.combo
    var runMaxCombo = state.runMaxCombo
    var slapsLanded = state.slapsLanded
    var sleepReaction = state.sleepReaction
    val reactionTimer = (state.messiReactionTimer - dt).coerceAtLeast(0f)
    val reactionBubble = if (reactionTimer > 0f) state.messiReactionBubble else null
    val shake = (state.bedShakeIntensity - dt * 2.4f).coerceAtLeast(0f)
    val armSwing = (state.throwArmSwing - dt * 5.2f).coerceAtLeast(0f)
    val throwCd = (state.throwCooldownRemaining - dt).coerceAtLeast(0f)

    val nextFlyers = state.flyers.toMutableList()
    val nextEffects =
      state.slapEffects
        .mapNotNull { fx ->
          val nextAge = fx.ageSeconds + dt
          if (nextAge >= fx.durationSeconds) null else fx.copy(ageSeconds = nextAge, y = fx.y - dt * 0.045f)
        }
        .toMutableList()

    // Spawn ONLY Ronaldo & IShowSpeed mosquitoes, alternating Right->Left and Left->Right
    spawnCooldown -= dt
    val maxActive = if (wave >= 3) 2 else 1
    if (spawnCooldown <= 0f && nextFlyers.size < maxActive) {
      val roll = Random.nextFloat()
      val kind =
        when {
          slapsLanded > 0 && slapsLanded % 9 == 0 && nextFlyers.none { it.kind == FlyerKind.RONALDO_GOLD_BOSS } ->
            FlyerKind.RONALDO_GOLD_BOSS
          slapsLanded >= 2 && roll < 0.36f -> FlyerKind.ISHOWSPEED_MOSQUITO
          else -> FlyerKind.RONALDO_MOSQUITO
        }
      nextFlyers.add(
        createCrossingMosquito(
          kind = kind,
          wave = wave,
          slapsLanded = slapsLanded,
          fromRight = nextEntryFromRight,
        )
      )
      nextEntryFromRight = !nextEntryFromRight
      spawnCooldown = (1.30f - wave * 0.08f).coerceAtLeast(0.58f) + Random.nextFloat() * 0.35f
    }

    // Advance unlimited spinning Sandals
    val w = lastViewportWidthPx.coerceAtLeast(1f)
    val h = lastViewportHeightPx.coerceAtLeast(1f)
    val nextSandals = mutableListOf<SandalProjectile>()

    for (sandal in state.sandals) {
      val nvy = sandal.vy + SANDAL_GRAVITY_NORM * dt
      val sx = sandal.x + sandal.vx * dt
      val sy = sandal.y + nvy * dt

      if (sx < -0.08f || sx > 1.08f || sy < -0.10f || sy > 1.05f) {
        continue
      }

      var hitIndex = -1
      var bestDistPx = Float.MAX_VALUE
      for (i in nextFlyers.indices) {
        val flyer = nextFlyers[i]
        val dxPx = (flyer.x - sx) * w
        val dyPx = (flyer.y - sy) * h
        val distPx = hypot(dxPx, dyPx)
        val hitRadiusPx = flyer.kind.hitRadiusDp * lastDensity
        if (distPx <= hitRadiusPx && distPx < bestDistPx) {
          bestDistPx = distPx
          hitIndex = i
        }
      }

      if (hitIndex >= 0) {
        val hitFlyer = nextFlyers.removeAt(hitIndex)
        combo += 1
        val multiplier = 1 + (combo / 5).coerceAtMost(3)
        val earnedPoints = hitFlyer.kind.basePoints * multiplier
        score += earnedPoints
        slapsLanded += 1
        wave = maxOf(wave, 1 + slapsLanded / 5)
        runMaxCombo = maxOf(runMaxCombo, combo)

        val isSpecial = hitFlyer.kind != FlyerKind.RONALDO_MOSQUITO
        soundEngine.playMosquitoSlap(isBossOrSpecial = isSpecial)

        if (combo > 0 && combo % 5 == 0) {
          soundEngine.playComboFanfare()
          soundEngine.speakMemeLine("Siuuu! Combo $combo!", force = false)
        } else if (hitFlyer.kind == FlyerKind.ISHOWSPEED_MOSQUITO) {
          soundEngine.speakMemeLine("Sewey! Bark!")
        } else if (hitFlyer.kind == FlyerKind.RONALDO_GOLD_BOSS) {
          soundEngine.speakMemeLine("Golden Siuuu!", force = true)
        }

        val hitWord =
          when (hitFlyer.kind) {
            FlyerKind.ISHOWSPEED_MOSQUITO -> "SEWEY! +$earnedPoints"
            FlyerKind.RONALDO_GOLD_BOSS -> "GOLDEN SIUUU! +$earnedPoints"
            FlyerKind.RONALDO_MOSQUITO ->
              if (combo >= 5 && combo % 5 == 0) "CHAPPAL! +$earnedPoints" else "SMACK! +$earnedPoints"
          }

        nextEffects.add(
          SlapEffect(
            id = nextEntityId++,
            x = hitFlyer.x,
            y = hitFlyer.y,
            label = hitWord,
            accentColor = hitFlyer.kind.accentColor,
            isDirectHit = true,
          )
        )
        if (nextFlyers.isEmpty()) {
          spawnCooldown = minOf(spawnCooldown, 0.35f)
        }
        continue
      }

      nextSandals.add(
        sandal.copy(
          x = sx,
          y = sy,
          vy = nvy,
          spinAngleDeg = (sandal.spinAngleDeg + sandal.spinSpeedDegPerSec * dt) % 360f,
        )
      )
    }

    // Move mosquitoes Right -> Left and Left -> Right with gradually increasing speed
    var urgentWarning = false
    val iter = nextFlyers.listIterator()
    while (iter.hasNext()) {
      val f = iter.next()
      val age = f.ageSeconds + dt
      var diagTimer = (f.dialogueRemaining - dt).coerceAtLeast(0f)
      var diag = if (diagTimer > 0f) f.dialogue else null

      var nx = f.x + f.vx * dt
      var nvx = f.vx
      var passes = f.passesRemaining
      val ny = (f.baseY + sin(age * f.waveFrequency + f.phaseOffset) * f.waveAmplitude).coerceIn(0.14f, 0.56f)

      if (nvx < 0f && nx <= 0.05f && passes > 1) {
        nx = 0.05f
        nvx = abs(nvx) * 1.12f
        passes -= 1
        diag = if (f.kind == FlyerKind.ISHOWSPEED_MOSQUITO) speedReturnLines.random() else ronaldoReturnLines.random()
        diagTimer = 1.9f
      } else if (nvx > 0f && nx >= 0.95f && passes > 1) {
        nx = 0.95f
        nvx = -abs(nvx) * 1.12f
        passes -= 1
        diag = if (f.kind == FlyerKind.ISHOWSPEED_MOSQUITO) speedReturnLines.random() else ronaldoReturnLines.random()
        diagTimer = 1.9f
      } else if ((nvx < 0f && nx < -0.07f) || (nvx > 0f && nx > 1.07f)) {
        // Any mosquito escaping past the edge triggers LEVEL FAILED!
        val reason =
          if (f.kind == FlyerKind.ISHOWSPEED_MOSQUITO) "ISHOWSPEED MOSQUITO ESCAPED!"
          else "CR7 MOSQUITO ESCAPED!"
        val shout =
          if (f.kind == FlyerKind.ISHOWSPEED_MOSQUITO) "NOOO! SPEED GOT AWAY!"
          else "NOOO! RONALDO GOT AWAY!"
        triggerGameOver(
          score = score,
          wave = wave,
          runMaxCombo = runMaxCombo,
          slapsLanded = slapsLanded,
          reason = reason,
          messiShout = shout,
          flyers = nextFlyers,
          effects = nextEffects,
        )
        return
      }

      if (passes == 1) {
        urgentWarning = true
      }

      iter.set(
        f.copy(
          x = nx,
          y = ny,
          vx = nvx,
          passesRemaining = passes,
          ageSeconds = age,
          dialogue = diag,
          dialogueRemaining = diagTimer,
          facingRight = nvx >= 0f,
        )
      )
    }

    if (reactionTimer <= 0f) {
      sleepReaction = if (urgentWarning) SleepReaction.NERVOUS else SleepReaction.PEACEFUL
    }

    soundEngine.buzzIntensity =
      when {
        nextFlyers.isEmpty() -> 0f
        urgentWarning -> 0.88f
        else -> 0.38f
      }

    _uiState.update { cur ->
      cur.copy(
        score = score,
        wave = wave,
        combo = combo,
        runMaxCombo = runMaxCombo,
        slapsLanded = slapsLanded,
        isNewHighScore = score > cur.highScore,
        sleepReaction = sleepReaction,
        messiReactionBubble = reactionBubble,
        messiReactionTimer = reactionTimer,
        bedShakeIntensity = shake,
        throwArmSwing = armSwing,
        throwCooldownRemaining = throwCd,
        flyers = nextFlyers,
        sandals = nextSandals,
        slapEffects = nextEffects.takeLast(16),
        zzzBubbles = nextZzz,
      )
    }
  }

  private fun triggerGameOver(
    score: Int,
    wave: Int,
    runMaxCombo: Int,
    slapsLanded: Int,
    reason: String,
    messiShout: String,
    flyers: List<FlyingCharacter>,
    effects: List<SlapEffect>,
  ) {
    soundEngine.buzzIntensity = 0f
    soundEngine.playBiteWakeUp()
    soundEngine.playGameOver()
    soundEngine.speakMemeLine("Que miras!", force = true)
    viewModelScope.launch {
      prefsRepository.saveRunResult(score, runMaxCombo, slapsLanded)
    }
    _uiState.update { cur ->
      cur.copy(
        phase = GamePhase.GAME_OVER,
        score = score,
        wave = wave,
        combo = 0,
        runMaxCombo = runMaxCombo,
        slapsLanded = slapsLanded,
        isNewHighScore = score > cur.highScore,
        sleepReaction = SleepReaction.BITTEN,
        messiReactionBubble = messiShout,
        messiReactionTimer = 999f,
        gameOverReason = reason,
        bedShakeIntensity = 0.85f,
        throwArmSwing = 0f,
        throwCooldownRemaining = 0f,
        isAiming = false,
        flyers = flyers,
        sandals = emptyList(),
        slapEffects = effects.takeLast(16),
        zzzBubbles = emptyList(),
      )
    }
  }

  private fun createCrossingMosquito(
    kind: FlyerKind,
    wave: Int,
    slapsLanded: Int,
    fromRight: Boolean,
  ): FlyingCharacter {
    val startX = if (fromRight) 1.04f else -0.04f
    // Starts brisk and gradually ramps up faster with every hit!
    val gradualSpeedScale = (1f + slapsLanded * 0.036f + (wave - 1) * 0.04f).coerceAtMost(2.35f)

    val baseSpeedX =
      when (kind) {
        FlyerKind.ISHOWSPEED_MOSQUITO -> 0.37f + Random.nextFloat() * 0.08f
        FlyerKind.RONALDO_GOLD_BOSS -> 0.39f + Random.nextFloat() * 0.07f
        FlyerKind.RONALDO_MOSQUITO -> 0.32f + Random.nextFloat() * 0.08f
      }
    val speedX = (baseSpeedX * gradualSpeedScale).coerceAtMost(0.84f)

    val baseY = 0.20f + Random.nextFloat() * 0.24f

    val line =
      when (kind) {
        FlyerKind.RONALDO_MOSQUITO -> ronaldoLines.random()
        FlyerKind.RONALDO_GOLD_BOSS -> ronaldoBossLines.random()
        FlyerKind.ISHOWSPEED_MOSQUITO -> speedLines.random()
      }

    return FlyingCharacter(
      id = nextEntityId++,
      kind = kind,
      x = startX,
      y = baseY,
      baseY = baseY,
      vx = if (fromRight) -speedX else speedX,
      passesRemaining = 2,
      phaseOffset = Random.nextFloat() * (2f * PI.toFloat()),
      waveAmplitude = (0.048f + (wave * 0.004f).coerceAtMost(0.025f)),
      waveFrequency = 4.6f + Random.nextFloat() * 1.4f,
      dialogue = line,
      dialogueRemaining = 2.4f,
      facingRight = !fromRight,
    )
  }

  override fun onCleared() {
    soundEngine.release()
    super.onCleared()
  }
}
