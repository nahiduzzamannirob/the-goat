package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameUiState
import com.example.ui.theme.ArgentinaSky
import com.example.ui.theme.CardSlate
import com.example.ui.theme.ComicCream
import com.example.ui.theme.ComicOutline
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DeepBedroomNavy
import com.example.ui.theme.MidnightIndigo
import com.example.ui.theme.RonaldoCrimson
import com.example.ui.theme.SoftLavender
import com.example.ui.theme.TrophyGold
import com.example.ui.theme.TurfEmerald

@Composable
fun GameHudBar(
  state: GameUiState,
  onToggleSound: () -> Unit,
  onTogglePause: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Score & Wave Pill
      Row(
        modifier =
          Modifier.clip(RoundedCornerShape(20.dp))
            .background(MidnightIndigo.copy(alpha = 0.84f))
            .border(2.dp, ArgentinaSky.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Column {
          Text(
            text = stringResource(R.string.score_label),
            style = MaterialTheme.typography.labelSmall,
            color = SoftLavender,
          )
          Text(
            text = state.score.toString(),
            style = MaterialTheme.typography.headlineMedium,
            color = TrophyGold,
          )
        }
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(12.dp))
              .background(CardSlate)
              .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
          Text(
            text = "${stringResource(R.string.wave_label)} ${state.wave}",
            style = MaterialTheme.typography.labelLarge,
            color = ArgentinaSky,
          )
        }
      }

      // Sudden-Death Badge + Audio + Pause Controls
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Row(
          modifier =
            Modifier.clip(RoundedCornerShape(16.dp))
              .background(MidnightIndigo.copy(alpha = 0.84f))
              .border(1.5.dp, TurfEmerald.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
              .padding(horizontal = 10.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "∞ SANDALS",
            style = MaterialTheme.typography.labelSmall,
            color = TrophyGold,
            fontWeight = FontWeight.Black,
          )
        }

        IconButton(
          onClick = onToggleSound,
          modifier =
            Modifier.size(48.dp)
              .clip(CircleShape)
              .background(MidnightIndigo.copy(alpha = 0.84f))
              .border(1.5.dp, SoftLavender.copy(alpha = 0.4f), CircleShape)
              .testTag("sound_toggle_button"),
        ) {
          Icon(
            imageVector =
              if (state.soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
              else Icons.AutoMirrored.Rounded.VolumeOff,
            contentDescription =
              if (state.soundEnabled) stringResource(R.string.sound_on)
              else stringResource(R.string.sound_off),
            tint = if (state.soundEnabled) TrophyGold else SoftLavender,
          )
        }

        IconButton(
          onClick = onTogglePause,
          modifier =
            Modifier.size(48.dp)
              .clip(CircleShape)
              .background(MidnightIndigo.copy(alpha = 0.84f))
              .border(1.5.dp, SoftLavender.copy(alpha = 0.4f), CircleShape)
              .testTag("pause_button"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Pause,
            contentDescription = stringResource(R.string.pause_game),
            tint = ComicCream,
          )
        }
      }
    }

    if (state.combo >= 2) {
      Row(
        modifier =
          Modifier.padding(top = 8.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(TrophyGold)
            .border(2.dp, ComicOutline, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
      ) {
        Icon(
          imageVector = Icons.Rounded.Bolt,
          contentDescription = null,
          tint = ComicOutline,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = "${state.combo}x ${stringResource(R.string.combo_label)}",
          style = MaterialTheme.typography.labelLarge,
          color = ComicOutline,
        )
      }
    }
  }
}

@Composable
fun MenuOverlay(
  highScore: Int,
  soundEnabled: Boolean,
  onStartGame: () -> Unit,
  onToggleSound: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = DeepBedroomNavy.copy(alpha = 0.92f),
      tonalElevation = 8.dp,
      modifier =
        Modifier.widthIn(max = 380.dp)
          .padding(horizontal = 24.dp)
          .border(3.dp, TrophyGold, RoundedCornerShape(28.dp)),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 22.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_messi_sleep),
            contentDescription = "Sleeping Messi",
            modifier = Modifier.size(54.dp),
          )
          Text(
            text = "VS",
            style = MaterialTheme.typography.displaySmall,
            color = TrophyGold,
          )
          Image(
            painter = painterResource(id = R.drawable.img_ronaldo_head),
            contentDescription = "Buzzing Ronaldo Mosquito",
            modifier = Modifier.size(48.dp),
          )
          Image(
            painter = painterResource(id = R.drawable.img_speed_head),
            contentDescription = "Buzzing IShowSpeed Mosquito",
            modifier = Modifier.size(48.dp),
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = "THE GOAT",
          style = MaterialTheme.typography.displayLarge,
          color = ArgentinaSky,
          textAlign = TextAlign.Center,
        )
        Text(
          text = stringResource(R.string.tagline_subtitle),
          style = MaterialTheme.typography.bodyMedium,
          color = ComicCream,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (highScore > 0) {
          Row(
            modifier =
              Modifier.clip(RoundedCornerShape(14.dp))
                .background(CardSlate)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.EmojiEvents,
              contentDescription = null,
              tint = TrophyGold,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = "${stringResource(R.string.high_score_label)}: $highScore",
              style = MaterialTheme.typography.labelLarge,
              color = TrophyGold,
            )
          }
          Spacer(modifier = Modifier.height(14.dp))
        }

        Column(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(MidnightIndigo.copy(alpha = 0.72f))
              .padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "• Tap or drag to throw unlimited sandals from bed!",
            style = MaterialTheme.typography.bodyMedium,
            color = ComicCream,
          )
          Text(
            text = "• CR7 & IShowSpeed get faster—don't let them escape!",
            style = MaterialTheme.typography.bodyMedium,
            color = TrophyGold,
            fontWeight = FontWeight.Bold,
          )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onStartGame,
          shape = RoundedCornerShape(18.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = TrophyGold,
              contentColor = ComicOutline,
            ),
          modifier =
            Modifier.fillMaxWidth()
              .height(54.dp)
              .testTag("start_game_button"),
        ) {
          Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.start_game),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
          onClick = onToggleSound,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("menu_sound_button"),
        ) {
          Icon(
            imageVector =
              if (soundEnabled) Icons.AutoMirrored.Rounded.VolumeUp
              else Icons.AutoMirrored.Rounded.VolumeOff,
            contentDescription = null,
            tint = ArgentinaSky,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text =
              if (soundEnabled) stringResource(R.string.sound_on)
              else stringResource(R.string.sound_off),
            style = MaterialTheme.typography.labelLarge,
            color = ComicCream,
          )
        }
      }
    }
  }
}

@Composable
fun PauseOverlay(
  onResume: () -> Unit,
  onRestart: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier.fillMaxSize().background(MidnightIndigo.copy(alpha = 0.65f)),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(26.dp),
      color = DeepBedroomNavy,
      modifier =
        Modifier.widthIn(max = 330.dp)
          .padding(horizontal = 24.dp)
          .border(2.5.dp, ArgentinaSky, RoundedCornerShape(26.dp)),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Text(
          text = "TIME OUT!",
          style = MaterialTheme.typography.displaySmall,
          color = TrophyGold,
        )
        Button(
          onClick = onResume,
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TurfEmerald, contentColor = ComicOutline),
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("resume_button"),
        ) {
          Icon(imageVector = Icons.Rounded.PlayArrow, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.resume_game),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
        }
        OutlinedButton(
          onClick = onRestart,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("restart_button"),
        ) {
          Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null, tint = ComicCream)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.play_again),
            style = MaterialTheme.typography.labelLarge,
            color = ComicCream,
          )
        }
      }
    }
  }
}

@Composable
fun GameOverOverlay(
  state: GameUiState,
  onPlayAgain: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            0f to MidnightIndigo.copy(alpha = 0.78f),
            1f to MidnightIndigo.copy(alpha = 0.45f),
          )
        ),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      shape = RoundedCornerShape(28.dp),
      color = DeepBedroomNavy.copy(alpha = 0.95f),
      modifier =
        Modifier.widthIn(max = 360.dp)
          .padding(horizontal = 24.dp)
          .border(3.dp, DangerRed, RoundedCornerShape(28.dp)),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Image(
          painter = painterResource(id = R.drawable.img_messi_awake),
          contentDescription = "Messi woke up",
          modifier = Modifier.size(72.dp),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = stringResource(R.string.game_over_title),
          style = MaterialTheme.typography.displaySmall,
          color = DangerRed,
          textAlign = TextAlign.Center,
        )
        if (state.gameOverReason.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = state.gameOverReason,
            style = MaterialTheme.typography.labelLarge,
            color = ComicCream,
            textAlign = TextAlign.Center,
          )
        }
        if (state.isNewHighScore && state.score > 0) {
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "NEW HIGH SCORE!",
            style = MaterialTheme.typography.labelLarge,
            color = ComicOutline,
            modifier =
              Modifier.clip(RoundedCornerShape(10.dp))
                .background(TrophyGold)
                .padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(18.dp))
              .background(CardSlate)
              .padding(vertical = 14.dp, horizontal = 12.dp),
          horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
          StatBlock(label = "SCORE", value = state.score.toString(), color = TrophyGold)
          StatBlock(label = "BEST", value = maxOf(state.highScore, state.score).toString(), color = ArgentinaSky)
          StatBlock(label = "HITS", value = state.slapsLanded.toString(), color = TurfEmerald)
          StatBlock(label = "COMBO", value = "${state.runMaxCombo}x", color = RonaldoCrimson)
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
          onClick = onPlayAgain,
          shape = RoundedCornerShape(18.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TrophyGold, contentColor = ComicOutline),
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("play_again_button"),
        ) {
          Icon(imageVector = Icons.Rounded.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = stringResource(R.string.play_again),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Black,
          )
        }
      }
    }
  }
}

@Composable
private fun StatBlock(label: String, value: String, color: Color) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(text = label, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = SoftLavender)
    Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = color)
  }
}
