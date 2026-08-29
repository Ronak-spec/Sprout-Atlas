package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SproutColorScheme = lightColorScheme(
    primary = SproutLeaf,
    onPrimary = Color.White,
    primaryContainer = SproutLeafLight,
    onPrimaryContainer = SproutLeafDark,
    secondary = SproutCitrus,
    onSecondary = SproutInk,
    secondaryContainer = SproutPaperCard,
    onSecondaryContainer = SproutInk,
    tertiary = SproutTomato,
    onTertiary = Color.White,
    background = SproutPaper,
    onBackground = SproutInk,
    surface = SproutPaper,
    onSurface = SproutInk,
    surfaceVariant = SproutPaperCard,
    onSurfaceVariant = SproutInkSoft,
    outline = SproutLine
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SproutColorScheme,
        typography = Typography,
        content = content
    )
}

