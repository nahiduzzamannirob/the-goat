package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameViewModel
import com.example.game.SleepReaction
import com.example.game.ZzzBubble
import com.example.ui.theme.ArgentinaBlue
import com.example.ui.theme.ArgentinaSky
import com.example.ui.theme.ComicCream
import com.example.ui.theme.ComicOutline
import com.example.ui.theme.DangerRed
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.ShieldCyan
import com.example.ui.theme.TrophyGold
import com.example.ui.theme.TurfEmerald
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun BedroomScene(
  sleepReaction: SleepReaction,
  messiReactionBubble: String?,
  bedShakeIntensity: Float,
  throwAimAngleDeg: Float,
  throwArmSwing: Float,
  isAiming: Boolean,
  zzzBubbles: List<ZzzBubble>,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "bedroom_anim")
  val breathPhase by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 2800, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "breath_phase",
    )
  val pulsePhase by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 550, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "danger_pulse",
    )

  val breathOffsetY = if (sleepReaction == SleepReaction.BITTEN) 0f else sin(breathPhase) * 3.5f
  val shakeX =
    if (bedShakeIntensity > 0.01f) {
      sin(breathPhase * 16f) * 14f * bedShakeIntensity
    } else 0f
  val shakeY =
    if (bedShakeIntensity > 0.01f) {
      sin(breathPhase * 21f) * 7f * bedShakeIntensity
    } else 0f

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val widthPx = with(density) { maxWidth.toPx() }
    val heightPx = with(density) { maxHeight.toPx() }

    // 1. Cozy Cartoon Bedroom Background
    Image(
      painter = painterResource(id = R.drawable.img_bedroom_bg),
      contentDescription = "Cozy bedroom at night",
      contentScale = ContentScale.Crop,
      modifier = Modifier.fillMaxSize(),
    )

    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.verticalGradient(
              0.0f to MidnightIndigo.copy(alpha = 0.62f),
              0.38f to MidnightIndigo.copy(alpha = 0.25f),
              0.68f to MidnightIndigo.copy(alpha = 0.12f),
              1.0f to MidnightIndigo.copy(alpha = 0.85f),
            )
          )
    )

    val headCenterX = widthPx * GameViewModel.MESSI_HEAD_X + shakeX
    val headCenterY = heightPx * GameViewModel.MESSI_HEAD_Y + breathOffsetY + shakeY
    val headSizeDp = (minOf(maxWidth.value, 460f) * 0.29f).coerceIn(96f, 136f).dp
    val headSizePx = with(density) { headSizeDp.toPx() }

    // 2. Bed, Pillow, Argentina Blanket, World Cup Trophy & Shield Dome
    Canvas(modifier = Modifier.fillMaxSize()) {
      val bedTopY = heightPx * 0.72f + shakeY * 0.6f
      val bedLeft = widthPx * 0.07f + shakeX * 0.5f
      val bedWidth = widthPx * 0.86f
      val bedHeight = heightPx - bedTopY + 32f

      // Wooden headboard
      drawRoundRect(
        color = Color(0xFF5D3A21),
        topLeft = Offset(bedLeft + bedWidth * 0.06f, bedTopY - heightPx * 0.045f),
        size = Size(bedWidth * 0.88f, heightPx * 0.09f),
        cornerRadius = CornerRadius(24f, 24f),
      )
      drawRoundRect(
        color = Color(0xFF7B4E2D),
        topLeft = Offset(bedLeft + bedWidth * 0.09f, bedTopY - heightPx * 0.032f),
        size = Size(bedWidth * 0.82f, heightPx * 0.055f),
        cornerRadius = CornerRadius(16f, 16f),
      )

      // Fluffy Cream Pillow behind Messi & his World Cup Trophy
      val pillowW = headSizePx * 2.15f
      val pillowH = headSizePx * 0.96f
      drawRoundRect(
        color = Color(0xFFE6DCCF),
        topLeft = Offset(headCenterX - pillowW * 0.56f, headCenterY - pillowH * 0.45f),
        size = Size(pillowW, pillowH),
        cornerRadius = CornerRadius(pillowH * 0.42f, pillowH * 0.42f),
      )
      drawRoundRect(
        color = ComicCream,
        topLeft = Offset(headCenterX - pillowW * 0.52f, headCenterY - pillowH * 0.40f),
        size = Size(pillowW * 0.92f, pillowH * 0.82f),
        cornerRadius = CornerRadius(pillowH * 0.36f, pillowH * 0.36f),
      )

      // Danger aura around Messi's head when mosquito is close
      if (sleepReaction == SleepReaction.NERVOUS) {
        drawCircle(
          color = DangerRed.copy(alpha = 0.16f + 0.22f * pulsePhase),
          radius = headSizePx * (0.72f + 0.12f * pulsePhase),
          center = Offset(headCenterX, headCenterY),
        )
        drawCircle(
          color = DangerRed.copy(alpha = 0.65f),
          radius = headSizePx * 0.72f,
          center = Offset(headCenterX, headCenterY),
          style = Stroke(width = 5f),
        )
      }

      // Argentina #10 Sky-Blue & White Striped Blanket covering lying Messi
      val blanketTopY = headCenterY + headSizePx * 0.32f
      drawRoundRect(
        color = Color(0xFF0A2442),
        topLeft = Offset(bedLeft, blanketTopY - 8f),
        size = Size(bedWidth, bedHeight),
        cornerRadius = CornerRadius(42f, 42f),
      )
      drawRoundRect(
        color = ArgentinaSky,
        topLeft = Offset(bedLeft + 6f, blanketTopY),
        size = Size(bedWidth - 12f, bedHeight),
        cornerRadius = CornerRadius(38f, 38f),
      )

      val stripeCount = 5
      val stripeSpacing = (bedWidth - 40f) / stripeCount
      for (i in 0 until stripeCount) {
        val sx = bedLeft + 24f + i * stripeSpacing
        drawRoundRect(
          color = Color.White.copy(alpha = 0.85f),
          topLeft = Offset(sx, blanketTopY + 18f),
          size = Size(stripeSpacing * 0.38f, bedHeight - 24f),
          cornerRadius = CornerRadius(12f, 12f),
        )
      }

      drawRoundRect(
        color = Color.White,
        topLeft = Offset(bedLeft + 4f, blanketTopY - 4f),
        size = Size(bedWidth - 8f, 32f),
        cornerRadius = CornerRadius(18f, 18f),
      )
      drawRoundRect(
        color = ArgentinaBlue.copy(alpha = 0.35f),
        topLeft = Offset(bedLeft + 4f, blanketTopY + 24f),
        size = Size(bedWidth - 8f, 8f),
        cornerRadius = CornerRadius(4f, 4f),
      )
      val badgeY = blanketTopY + 72f
      drawCircle(color = TrophyGold, radius = 26f, center = Offset(headCenterX, badgeY))
      drawCircle(color = ComicOutline, radius = 26f, center = Offset(headCenterX, badgeY), style = Stroke(width = 4f))

      // Golden World Cup Trophy tucked right beside Messi on the bed!
      drawWorldCupTrophy(
        baseCenter = Offset(headCenterX - headSizePx * 0.90f, blanketTopY + 18f),
        scalePx = headSizePx * 0.95f,
        shimmer = pulsePhase,
        tiltDeg = -10f + sin(breathPhase) * 2.5f,
      )
    }

    // 3. Messi's Head Sticker on the Pillow
    val messiDrawable =
      if (sleepReaction == SleepReaction.BITTEN) R.drawable.img_messi_awake
      else R.drawable.img_messi_sleep

    Image(
      painter = painterResource(id = messiDrawable),
      contentDescription = if (sleepReaction == SleepReaction.BITTEN) "Awake Messi" else "Sleeping Messi",
      contentScale = ContentScale.Fit,
      modifier =
        Modifier.offset {
            IntOffset(
              x = (headCenterX - headSizePx / 2f).roundToInt(),
              y = (headCenterY - headSizePx / 2f).roundToInt(),
            )
          }
          .size(headSizeDp)
          .rotate(if (sleepReaction == SleepReaction.BITTEN) sin(breathPhase * 12f) * 5f else -4f + sin(breathPhase) * 2f),
    )

    // 4. Lying Messi's Throwing Arm + Sandal (Chappal) in Hand above the Blanket!
    val throwOriginX = widthPx * GameViewModel.THROW_ORIGIN_X + shakeX
    val throwOriginY = heightPx * GameViewModel.THROW_ORIGIN_Y + breathOffsetY + shakeY
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawMessiSandalThrowArm(
        shoulder = Offset(throwOriginX, throwOriginY + 26f),
        aimAngleDeg = throwAimAngleDeg,
        throwSwing = throwArmSwing,
        isAiming = isAiming,
        idleBreath = sin(breathPhase) * 5f,
        densityScale = density.density,
      )
    }

    if (sleepReaction == SleepReaction.NERVOUS) {
      Text(
        text = "💦",
        fontSize = 24.sp,
        modifier =
          Modifier.offset {
            IntOffset(
              x = (headCenterX + headSizePx * 0.36f).roundToInt(),
              y = (headCenterY - headSizePx * 0.42f + pulsePhase * 8f).roundToInt(),
            )
          },
      )
    }

    // 5. Floating Zzz Sleep Bubbles
    for (z in zzzBubbles) {
      val progress = (z.ageSeconds / z.durationSeconds).coerceIn(0f, 1f)
      val alpha = if (progress < 0.2f) progress / 0.2f else (1f - progress) / 0.8f
      val fontSize = (18f + progress * 12f).sp
      Text(
        text = "Z",
        style = MaterialTheme.typography.displaySmall,
        fontSize = fontSize,
        color = ArgentinaSky,
        modifier =
          Modifier.offset {
              IntOffset(
                x = (z.x * widthPx).roundToInt(),
                y = (z.y * heightPx).roundToInt(),
              )
            }
            .alpha(alpha.coerceIn(0f, 1f)),
      )
    }

    // 6. Messi Wake-Up Shout Bubble
    if (!messiReactionBubble.isNullOrBlank()) {
      Box(
        modifier =
          Modifier.offset {
              IntOffset(
                x = (headCenterX - headSizePx * 0.95f).roundToInt().coerceAtLeast(16),
                y = (headCenterY - headSizePx * 1.12f).roundToInt(),
              )
            }
            .clip(RoundedCornerShape(16.dp))
            .background(TrophyGold)
            .border(2.5.dp, ComicOutline, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = messiReactionBubble,
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.ExtraBold,
          color = ComicOutline,
        )
      }
    }
  }
}

private fun DrawScope.drawWorldCupTrophy(
  baseCenter: Offset,
  scalePx: Float,
  shimmer: Float,
  tiltDeg: Float,
) {
  rotate(degrees = tiltDeg, pivot = baseCenter) {
    val h = scalePx * 0.92f
    val topY = baseCenter.y - h

    // Soft golden champion glow behind trophy
    drawCircle(
      color = TrophyGold.copy(alpha = 0.22f + shimmer * 0.16f),
      radius = h * 0.56f,
      center = Offset(baseCenter.x, topY + h * 0.45f),
    )

    // Base plinth + iconic malachite green rings
    val baseW = h * 0.40f
    val baseH = h * 0.18f
    drawRoundRect(
      color = Color(0xFFB45309),
      topLeft = Offset(baseCenter.x - baseW / 2f, baseCenter.y - baseH),
      size = Size(baseW, baseH),
      cornerRadius = CornerRadius(6f, 6f),
    )
    drawRoundRect(
      color = TurfEmerald,
      topLeft = Offset(baseCenter.x - baseW * 0.46f, baseCenter.y - baseH * 0.68f),
      size = Size(baseW * 0.92f, baseH * 0.34f),
      cornerRadius = CornerRadius(4f, 4f),
    )
    drawRoundRect(
      color = ComicOutline,
      topLeft = Offset(baseCenter.x - baseW / 2f, baseCenter.y - baseH),
      size = Size(baseW, baseH),
      cornerRadius = CornerRadius(6f, 6f),
      style = Stroke(width = 3.5f),
    )

    // Sculpted Golden Body flaring into the Globe top
    val stemPath =
      Path().apply {
        moveTo(baseCenter.x - baseW * 0.34f, baseCenter.y - baseH)
        cubicTo(
          baseCenter.x - baseW * 0.16f,
          baseCenter.y - h * 0.45f,
          baseCenter.x - h * 0.26f,
          topY + h * 0.30f,
          baseCenter.x - h * 0.23f,
          topY + h * 0.18f,
        )
        lineTo(baseCenter.x + h * 0.23f, topY + h * 0.18f)
        cubicTo(
          baseCenter.x + h * 0.26f,
          topY + h * 0.30f,
          baseCenter.x + baseW * 0.16f,
          baseCenter.y - h * 0.45f,
          baseCenter.x + baseW * 0.34f,
          baseCenter.y - baseH,
        )
        close()
      }
    drawPath(
      path = stemPath,
      brush =
        Brush.horizontalGradient(
          colors = listOf(Color(0xFFD97706), TrophyGold, Color(0xFFFEF08A), Color(0xFFD97706)),
          startX = baseCenter.x - h * 0.25f,
          endX = baseCenter.x + h * 0.25f,
        ),
    )
    drawPath(path = stemPath, color = ComicOutline, style = Stroke(width = 3.5f))

    // World Cup Globe Top
    val globeR = h * 0.23f
    val globeCenter = Offset(baseCenter.x, topY + globeR + 2f)
    drawCircle(
      brush =
        Brush.radialGradient(
          colors = listOf(Color(0xFFFEF9C3), TrophyGold, Color(0xFFD97706)),
          center = globeCenter - Offset(globeR * 0.3f, globeR * 0.3f),
          radius = globeR * 1.4f,
        ),
      radius = globeR,
      center = globeCenter,
    )
    drawCircle(color = ComicOutline, radius = globeR, center = globeCenter, style = Stroke(width = 3.5f))

    // Subtle continent/swirl lines on the globe
    drawArc(
      color = Color(0xFFB45309).copy(alpha = 0.65f),
      startAngle = 200f,
      sweepAngle = 140f,
      useCenter = false,
      topLeft = Offset(globeCenter.x - globeR * 0.65f, globeCenter.y - globeR * 0.45f),
      size = Size(globeR * 1.3f, globeR * 0.9f),
      style = Stroke(width = 3f, cap = StrokeCap.Round),
    )

    // Animated sparkle glints near the trophy
    val glintPos = globeCenter + Offset(-globeR * 0.95f, -globeR * 0.7f)
    val glintSize = 6f + shimmer * 6f
    drawLine(
      color = Color.White,
      start = glintPos + Offset(-glintSize, 0f),
      end = glintPos + Offset(glintSize, 0f),
      strokeWidth = 3f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color.White,
      start = glintPos + Offset(0f, -glintSize),
      end = glintPos + Offset(0f, glintSize),
      strokeWidth = 3f,
      cap = StrokeCap.Round,
    )
  }
}

private fun DrawScope.drawMessiSandalThrowArm(
  shoulder: Offset,
  aimAngleDeg: Float,
  throwSwing: Float,
  isAiming: Boolean,
  idleBreath: Float,
  densityScale: Float,
) {
  val baseRot = aimAngleDeg + 90f
  val swingDelta =
    when {
      isAiming -> 28f // Cocked back ready to whip forward!
      throwSwing > 0f -> (1f - throwSwing) * 60f - 30f
      else -> idleBreath
    }
  val armLength = 34f * densityScale
  val armThickness = 11f * densityScale

  rotate(degrees = baseRot + swingDelta, pivot = shoulder) {
    val wrist = Offset(shoulder.x, shoulder.y - armLength)
    drawLine(
      color = ArgentinaSky,
      start = shoulder,
      end = Offset(shoulder.x, shoulder.y - armLength * 0.45f),
      strokeWidth = armThickness * 1.25f,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = Color(0xFFE5B48A),
      start = Offset(shoulder.x, shoulder.y - armLength * 0.40f),
      end = wrist,
      strokeWidth = armThickness,
      cap = StrokeCap.Round,
    )
    drawLine(
      color = ComicOutline,
      start = shoulder,
      end = wrist,
      strokeWidth = 2.5f,
      cap = StrokeCap.Round,
    )
    drawCircle(color = Color(0xFFE5B48A), radius = 7.5f * densityScale, center = wrist)
    drawCircle(color = ComicOutline, radius = 7.5f * densityScale, center = wrist, style = Stroke(width = 2.5f))

    if (throwSwing < 0.45f) {
      val sandalW = 16f * densityScale
      val sandalH = 30f * densityScale
      val sandalTopLeft = Offset(wrist.x - sandalW / 2f, wrist.y - sandalH * 0.72f)
      drawRoundRect(
        color = ArgentinaSky,
        topLeft = sandalTopLeft,
        size = Size(sandalW, sandalH),
        cornerRadius = CornerRadius(sandalW * 0.48f, sandalW * 0.48f),
      )
      drawRoundRect(
        color = ComicOutline,
        topLeft = sandalTopLeft,
        size = Size(sandalW, sandalH),
        cornerRadius = CornerRadius(sandalW * 0.48f, sandalW * 0.48f),
        style = Stroke(width = 2.5f),
      )
      drawLine(
        color = Color.White,
        start = Offset(wrist.x, wrist.y - sandalH * 0.50f),
        end = Offset(wrist.x - sandalW * 0.35f, wrist.y - sandalH * 0.15f),
        strokeWidth = 3f * densityScale,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = Color.White,
        start = Offset(wrist.x, wrist.y - sandalH * 0.50f),
        end = Offset(wrist.x + sandalW * 0.35f, wrist.y - sandalH * 0.15f),
        strokeWidth = 3f * densityScale,
        cap = StrokeCap.Round,
      )
    } else {
      drawArc(
        color = TrophyGold.copy(alpha = throwSwing),
        startAngle = 210f,
        sweepAngle = 120f,
        useCenter = false,
        topLeft = Offset(wrist.x - 22f * densityScale, wrist.y - 26f * densityScale),
        size = Size(44f * densityScale, 32f * densityScale),
        style = Stroke(width = 3.5f * densityScale, cap = StrokeCap.Round),
      )
    }
  }
}
