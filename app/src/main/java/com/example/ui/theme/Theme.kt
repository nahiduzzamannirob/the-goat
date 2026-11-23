package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ArcadeDarkColorScheme =
  darkColorScheme(
    primary = ArgentinaSky,
    onPrimary = MidnightIndigo,
    primaryContainer = ArgentinaBlue,
    onPrimaryContainer = ComicCream,
    secondary = TrophyGold,
    onSecondary = MidnightIndigo,
    secondaryContainer = CardSlate,
    onSecondaryContainer = TrophyGold,
    tertiary = RonaldoCrimson,
    onTertiary = Color.White,
    background = MidnightIndigo,
    onBackground = ComicCream,
    surface = DeepBedroomNavy,
    onSurface = ComicCream,
    surfaceVariant = CardSlate,
    onSurfaceVariant = SoftLavender,
    error = DangerRed,
    onError = Color.White,
  )

@Composable
fun MessiSlapTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = ArcadeDarkColorScheme,
    typography = Typography,
    content = content,
  )
}
