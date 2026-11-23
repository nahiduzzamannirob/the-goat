package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.GamePhase
import com.example.game.GameViewModel
import com.example.ui.components.BedroomScene
import com.example.ui.components.FlyingEntitiesLayer
import com.example.ui.components.GameHudBar
import com.example.ui.components.GameOverOverlay
import com.example.ui.components.MenuOverlay
import com.example.ui.components.PauseOverlay

@Composable
fun GameScreen(viewModel: GameViewModel, modifier: Modifier = Modifier) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val density = LocalDensity.current.density
  val lifecycleOwner = LocalLifecycleOwner.current

  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_PAUSE -> viewModel.onAppPause()
        Lifecycle.Event.ON_RESUME -> viewModel.onAppResume()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
  }

  LaunchedEffect(Unit) {
    var lastFrameNanos = 0L
    while (true) {
      withFrameNanos { frameNanos ->
        if (lastFrameNanos != 0L) {
          val dt = (frameNanos - lastFrameNanos) / 1_000_000_000f
          viewModel.tick(dt)
        }
        lastFrameNanos = frameNanos
      }
    }
  }

  BackHandler(enabled = uiState.phase == GamePhase.PLAYING) {
    viewModel.togglePause()
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .onSizeChanged { size ->
          viewModel.updateViewportMetrics(
            widthPx = size.width.toFloat(),
            heightPx = size.height.toFloat(),
            density = density,
          )
        }
        .pointerInput(uiState.phase) {
          if (uiState.phase == GamePhase.PLAYING) {
            awaitEachGesture {
              val down = awaitFirstDown(requireUnconsumed = false)
              viewModel.throwSandalToward(down.position.x, down.position.y, isDragRepeat = false)
              while (true) {
                val event = awaitPointerEvent()
                val activeChange = event.changes.firstOrNull { it.pressed }
                if (activeChange == null) {
                  viewModel.endAim()
                  break
                }
                viewModel.throwSandalToward(
                  activeChange.position.x,
                  activeChange.position.y,
                  isDragRepeat = true,
                )
              }
            }
          }
        }
  ) {
    // Layer 1: Cozy Bedroom + Bed + World Cup Trophy + Lying Messi throwing Sandals
    BedroomScene(
      sleepReaction = uiState.sleepReaction,
      messiReactionBubble = uiState.messiReactionBubble,
      bedShakeIntensity = uiState.bedShakeIntensity,
      throwAimAngleDeg = uiState.throwAimAngleDeg,
      throwArmSwing = uiState.throwArmSwing,
      isAiming = uiState.isAiming,
      zzzBubbles = uiState.zzzBubbles,
    )

    // Layer 2: Aim Guide, Flying CR7 Mosquitoes, Meme Squad, Spinning Sandals & Impact FX
    FlyingEntitiesLayer(
      flyers = uiState.flyers,
      sandals = uiState.sandals,
      slapEffects = uiState.slapEffects,
      isAiming = uiState.isAiming,
      aimNormX = uiState.aimNormX,
      aimNormY = uiState.aimNormY,
    )

    // Layer 3: Safe-Area Floating HUD
    if (uiState.phase == GamePhase.PLAYING || uiState.phase == GamePhase.PAUSED) {
      GameHudBar(
        state = uiState,
        onToggleSound = viewModel::toggleSound,
        onTogglePause = viewModel::togglePause,
        modifier =
          Modifier.align(Alignment.TopCenter)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 14.dp, vertical = 8.dp),
      )
    }

    // Layer 4: State Overlays
    when (uiState.phase) {
      GamePhase.MENU -> {
        MenuOverlay(
          highScore = uiState.highScore,
          soundEnabled = uiState.soundEnabled,
          onStartGame = viewModel::startGame,
          onToggleSound = viewModel::toggleSound,
          modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        )
      }

      GamePhase.PAUSED -> {
        PauseOverlay(
          onResume = viewModel::togglePause,
          onRestart = viewModel::startGame,
          modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        )
      }

      GamePhase.GAME_OVER -> {
        GameOverOverlay(
          state = uiState,
          onPlayAgain = viewModel::startGame,
          modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
        )
      }

      GamePhase.PLAYING -> Unit
    }
  }
}
