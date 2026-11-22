package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VibrantLightColorScheme = lightColorScheme(
  primary = SapphirePrimary,
  onPrimary = Color.White,
  primaryContainer = SkyPrimaryContainer,
  onPrimaryContainer = OnSkyPrimaryContainer,
  secondary = TealSecondary,
  onSecondary = Color.White,
  secondaryContainer = MintSecondaryContainer,
  onSecondaryContainer = OnMintSecondaryContainer,
  tertiary = CoralTertiary,
  onTertiary = Color.White,
  tertiaryContainer = PeachTertiaryContainer,
  onTertiaryContainer = OnPeachTertiaryContainer,
  background = CanvasBackground,
  onBackground = TextPrimary,
  surface = PureCardSurface,
  onSurface = TextPrimary,
  surfaceVariant = SoftSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = SoftOutline,
  error = Color(0xFFC62828),
  onError = Color.White,
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = VibrantLightColorScheme,
    typography = AppTypography,
    content = content,
  )
}
