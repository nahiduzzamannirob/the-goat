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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.game.FlyerKind
import com.example.game.FlyingCharacter
import com.example.game.GameViewModel
import com.example.game.SandalProjectile
import com.example.game.SlapEffect
import com.example.ui.theme.ArgentinaSky
import com.example.ui.theme.ComicCream
import com.example.ui.theme.ComicOutline
import com.example.ui.theme.DangerRed
import com.example.ui.theme.RonaldoCrimson
import com.example.ui.theme.TrophyGold
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun FlyingEntitiesLayer(
  flyers: List<FlyingCharacter>,
  sandals: List<SandalProjectile>,
  slapEffects: List<SlapEffect>,
  isAiming: Boolean,
  aimNormX: Float,
  aimNormY: Float,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "wing_anim")
  val wingCycle by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = (2f * PI).toFloat(),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 120, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "wing_flap",
    )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val density = LocalDensity.current
    val widthPx = with(density) { maxWidth.toPx() }
    val heightPx = with(density) { maxHeight.toPx() }

    // 0. Parabolic Aim Guide + Reticle when holding/dragging
    if (isAiming) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val originXPx = GameViewModel.THROW_ORIGIN_X * widthPx
        val originYPx = GameViewModel.THROW_ORIGIN_Y * heightPx
        val targetXPx = aimNormX * widthPx
        val targetYPx = aimNormY * heightPx
        val dxPx = targetXPx - originXPx
        val dyPx = (targetYPx - originYPx).coerceAtMost(-heightPx * 0.10f)
        val distPx = hypot(dxPx, dyPx).coerceAtLeast(1f)
        val speedPxPerSec = heightPx * GameViewModel.SANDAL_SPEED_HEIGHTS_PER_SEC
        val vxNorm = (dxPx / distPx) * speedPxPerSec / widthPx
        val vyNorm = (dyPx / distPx) * speedPxPerSec / heightPx

        for (step in 1..9) {
          val t = step * 0.040f
          val px = (GameViewModel.THROW_ORIGIN_X + vxNorm * t) * widthPx
          val py =
            (GameViewModel.THROW_ORIGIN_Y + vyNorm * t + 0.5f * GameViewModel.SANDAL_GRAVITY_NORM * t * t) * heightPx
          if (py < 0f || px < 0f || px > widthPx) break
          drawCircle(
            color = TrophyGold.copy(alpha = (1f - step * 0.08f).coerceAtLeast(0.2f)),
            radius = (4.5f - step * 0.25f) * density.density,
            center = Offset(px, py),
          )
        }

        drawCircle(
          color = TrophyGold.copy(alpha = 0.85f),
          radius = 15f * density.density,
          center = Offset(targetXPx, targetYPx),
          style = Stroke(width = 2.5f * density.density),
        )
        drawCircle(
          color = DangerRed,
          radius = 3.5f * density.density,
          center = Offset(targetXPx, targetYPx),
        )
      }
    }

    // 1. Render Ronaldo & IShowSpeed Mosquitoes
    for (flyer in flyers) {
      val cx = flyer.x * widthPx
      val cy = flyer.y * heightPx
      val sizeDp = (flyer.kind.radiusDp * 2f).dp
      val sizePx = with(density) { sizeDp.toPx() }
      val flapAngle = sin(wingCycle + flyer.phaseOffset) * 28f
      val isFinalPass = flyer.passesRemaining == 1

      Box(
        modifier =
          Modifier.offset {
              IntOffset(
                x = (cx - sizePx / 2f).roundToInt(),
                y = (cy - sizePx / 2f).roundToInt(),
              )
            }
            .size(sizeDp),
        contentAlignment = Alignment.Center,
      ) {
        CelebrityMosquitoSprite(
          headResId =
            if (flyer.kind == FlyerKind.ISHOWSPEED_MOSQUITO) R.drawable.img_speed_head
            else R.drawable.img_ronaldo_head,
          contentDesc = flyer.kind.displayName,
          isBoss = flyer.kind == FlyerKind.RONALDO_GOLD_BOSS,
          stripeColor = flyer.kind.accentColor,
          flapAngle = flapAngle,
          facingRight = flyer.facingRight,
          isUrgentReturnPass = isFinalPass,
        )
      }

      if (!flyer.dialogue.isNullOrBlank()) {
        val bubbleWidthPx = with(density) { 142.dp.toPx() }
        val bx = (cx - bubbleWidthPx / 2f).coerceIn(12f, (widthPx - bubbleWidthPx - 12f).coerceAtLeast(12f))
        val by = (cy - sizePx * 0.85f).coerceAtLeast(8f)

        Box(
          modifier =
            Modifier.offset { IntOffset(bx.roundToInt(), by.roundToInt()) }
              .clip(RoundedCornerShape(12.dp))
              .background(
                when {
                  isFinalPass -> DangerRed
                  flyer.kind == FlyerKind.RONALDO_GOLD_BOSS -> TrophyGold
                  else -> ComicCream
                }
              )
              .border(2.dp, ComicOutline, RoundedCornerShape(12.dp))
              .padding(horizontal = 8.dp, vertical = 4.dp),
        ) {
          Text(
            text = flyer.dialogue,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = if (isFinalPass) Color.White else ComicOutline,
            maxLines = 1,
          )
        }
      }
    }

    // 2. Render Spinning Sandals (Chappals) flung by lying Messi
    Canvas(modifier = Modifier.fillMaxSize()) {
      val sWidth = 22f * density.density
      val sHeight = 40f * density.density
      for (sandal in sandals) {
        val sx = sandal.x * widthPx
        val sy = sandal.y * heightPx
        val center = Offset(sx, sy)

        rotate(degrees = sandal.spinAngleDeg, pivot = center) {
          drawArc(
            color = (if (sandal.isGoldSandal) TrophyGold else Color.White).copy(alpha = 0.55f),
            startAngle = 15f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = Offset(sx - sHeight * 0.65f, sy - sHeight * 0.65f),
            size = Size(sHeight * 1.3f, sHeight * 1.3f),
            style = Stroke(width = 3.5f * density.density, cap = StrokeCap.Round),
          )
          drawArc(
            color = (if (sandal.isGoldSandal) TrophyGold else ArgentinaSky).copy(alpha = 0.55f),
            startAngle = 195f,
            sweepAngle = 105f,
            useCenter = false,
            topLeft = Offset(sx - sHeight * 0.65f, sy - sHeight * 0.65f),
            size = Size(sHeight * 1.3f, sHeight * 1.3f),
            style = Stroke(width = 3.5f * density.density, cap = StrokeCap.Round),
          )

          val soleTopLeft = Offset(sx - sWidth / 2f, sy - sHeight / 2f)
          drawRoundRect(
            color = if (sandal.isGoldSandal) TrophyGold else ArgentinaSky,
            topLeft = soleTopLeft,
            size = Size(sWidth, sHeight),
            cornerRadius = CornerRadius(sWidth * 0.48f, sWidth * 0.48f),
          )
          drawRoundRect(
            color = Color.White.copy(alpha = 0.35f),
            topLeft = Offset(sx - sWidth * 0.25f, sy - sHeight * 0.35f),
            size = Size(sWidth * 0.5f, sHeight * 0.70f),
            cornerRadius = CornerRadius(sWidth * 0.25f, sWidth * 0.25f),
          )
          drawRoundRect(
            color = ComicOutline,
            topLeft = soleTopLeft,
            size = Size(sWidth, sHeight),
            cornerRadius = CornerRadius(sWidth * 0.48f, sWidth * 0.48f),
            style = Stroke(width = 2.8f * density.density),
          )
          val strapColor = if (sandal.isGoldSandal) RonaldoCrimson else Color.White
          val toePost = Offset(sx, sy - sHeight * 0.24f)
          drawLine(
            color = strapColor,
            start = toePost,
            end = Offset(sx - sWidth * 0.38f, sy + sHeight * 0.14f),
            strokeWidth = 3.8f * density.density,
            cap = StrokeCap.Round,
          )
          drawLine(
            color = strapColor,
            start = toePost,
            end = Offset(sx + sWidth * 0.38f, sy + sHeight * 0.14f),
            strokeWidth = 3.8f * density.density,
            cap = StrokeCap.Round,
          )
          drawCircle(color = ComicOutline, radius = 2.5f * density.density, center = toePost)
        }
      }
    }

    // 3. Render Comic Impact Starbursts & Floating Score Popups
    for (fx in slapEffects) {
      val progress = (fx.ageSeconds / fx.durationSeconds).coerceIn(0f, 1f)
      val alpha = (1f - progress * progress).coerceIn(0f, 1f)
      val popScale = if (progress < 0.25f) 0.7f + (progress / 0.25f) * 0.5f else 1.2f - progress * 0.2f
      val fxSizeDp = if (fx.isDirectHit) 92.dp else 66.dp
      val fxSizePx = with(density) { fxSizeDp.toPx() }

      Box(
        modifier =
          Modifier.offset {
              IntOffset(
                x = (fx.x * widthPx - fxSizePx / 2f).roundToInt(),
                y = (fx.y * heightPx - fxSizePx / 2f).roundToInt(),
              )
            }
            .size(fxSizeDp)
            .alpha(alpha)
            .scale(popScale),
        contentAlignment = Alignment.Center,
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val center = Offset(size.width / 2f, size.height / 2f)
          val spikes = 10
          val outerR = size.minDimension * 0.48f
          val innerR = size.minDimension * 0.24f
          val star = Path()
          for (i in 0 until spikes * 2) {
            val r = if (i % 2 == 0) outerR else innerR
            val angle = (i * PI / spikes).toFloat()
            val px = center.x + cos(angle) * r
            val py = center.y + sin(angle) * r
            if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
          }
          star.close()
          drawPath(star, color = fx.accentColor.copy(alpha = 0.92f))
          drawPath(star, color = ComicOutline, style = Stroke(width = 4f))
        }

        Text(
          text = fx.label,
          style = MaterialTheme.typography.labelLarge,
          fontSize = if (fx.isDirectHit) 15.sp else 12.sp,
          fontWeight = FontWeight.Black,
          color = if (fx.isDirectHit) Color.White else ComicCream,
          modifier =
            Modifier.offset(y = (-24).dp)
              .background(ComicOutline.copy(alpha = 0.78f), RoundedCornerShape(8.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp),
        )
      }
    }
  }
}

@Composable
private fun CelebrityMosquitoSprite(
  headResId: Int,
  contentDesc: String,
  isBoss: Boolean,
  stripeColor: Color,
  flapAngle: Float,
  facingRight: Boolean,
  isUrgentReturnPass: Boolean,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val cx = size.width / 2f
      val cy = size.height / 2f
      val wingColor =
        if (isBoss) TrophyGold.copy(alpha = 0.62f) else Color(0xFFE0F7FA).copy(alpha = 0.72f)
      val bodyColor = if (isBoss) TrophyGold else Color(0xFF1F2937)

      if (isUrgentReturnPass) {
        drawCircle(
          color = DangerRed.copy(alpha = 0.30f),
          radius = size.minDimension * 0.46f,
          center = Offset(cx, cy),
          style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)),
        )
      } else if (isBoss) {
        drawCircle(
          color = TrophyGold.copy(alpha = 0.28f),
          radius = size.minDimension * 0.48f,
          center = Offset(cx, cy),
        )
      }

      rotate(degrees = -28f + flapAngle, pivot = Offset(cx - 8f, cy - 2f)) {
        drawOval(
          color = wingColor,
          topLeft = Offset(cx - size.width * 0.48f, cy - size.height * 0.30f),
          size = Size(size.width * 0.44f, size.height * 0.22f),
        )
        drawOval(
          color = ComicOutline.copy(alpha = 0.7f),
          topLeft = Offset(cx - size.width * 0.48f, cy - size.height * 0.30f),
          size = Size(size.width * 0.44f, size.height * 0.22f),
          style = Stroke(width = 2.5f),
        )
      }
      rotate(degrees = 28f - flapAngle, pivot = Offset(cx + 8f, cy - 2f)) {
        drawOval(
          color = wingColor,
          topLeft = Offset(cx + size.width * 0.04f, cy - size.height * 0.30f),
          size = Size(size.width * 0.44f, size.height * 0.22f),
        )
        drawOval(
          color = ComicOutline.copy(alpha = 0.7f),
          topLeft = Offset(cx + size.width * 0.04f, cy - size.height * 0.30f),
          size = Size(size.width * 0.44f, size.height * 0.22f),
          style = Stroke(width = 2.5f),
        )
      }

      val tailDir = if (facingRight) -1f else 1f
      rotate(degrees = tailDir * 24f, pivot = Offset(cx, cy + 12f)) {
        drawOval(
          color = bodyColor,
          topLeft = Offset(cx - 10f, cy + 12f),
          size = Size(20f, 28f),
        )
        drawOval(
          color = stripeColor,
          topLeft = Offset(cx - 7f, cy + 20f),
          size = Size(14f, 6f),
        )
        val stinger =
          Path().apply {
            moveTo(cx - 3.5f, cy + 38f)
            lineTo(cx + 3.5f, cy + 38f)
            lineTo(cx, cy + 50f)
            close()
          }
        drawPath(stinger, color = Color(0xFF94A3B8))
      }

      val legColor = ComicOutline
      for (side in listOf(-1f, 1f)) {
        drawLine(
          color = legColor,
          start = Offset(cx + side * 12f, cy + 16f),
          end = Offset(cx + side * 24f, cy + 30f),
          strokeWidth = 3f,
        )
        drawLine(
          color = legColor,
          start = Offset(cx + side * 24f, cy + 30f),
          end = Offset(cx + side * 16f, cy + 40f),
          strokeWidth = 2.5f,
        )
      }
    }

    Image(
      painter = painterResource(id = headResId),
      contentDescription = contentDesc,
      contentScale = ContentScale.Fit,
      modifier =
        Modifier.size(if (isBoss) 54.dp else 46.dp)
          .offset(y = (-6).dp)
          .rotate(if (facingRight) 6f else -6f),
    )

    if (isBoss) {
      Text(
        text = "👑",
        fontSize = 18.sp,
        modifier = Modifier.align(Alignment.TopCenter).offset(y = (-4).dp),
      )
    }
  }
}
