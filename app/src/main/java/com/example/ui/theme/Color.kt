package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Warm Craft Paper Canvas & Card Tokens
val SproutPaper = Color(0xFFFBF8EE)
val SproutPaperCard = Color(0xFFFFFDF5)
val SproutPaperCardHover = Color(0xFFF5EFE0)
val SproutPaperDark = Color(0xFFEDE5D0)
val SproutInk = Color(0xFF23251E)
val SproutInkSoft = Color(0xFF4A4E42)
val SproutInkMuted = Color(0xFF7D8370)
val SproutLine = Color(0xFF4A4E42)

// Tactile Botanical Primary & Accents
val SproutLeaf = Color(0xFF2E6B47)
val SproutLeafDark = Color(0xFF1E482F)
val SproutLeafLight = Color(0xFFE4F0E5)
val SproutForest = Color(0xFF1B4332)
val SproutTomato = Color(0xFFC7432B)
val SproutTomatoLight = Color(0xFFFBECE8)
val SproutCitrus = Color(0xFFD97724)
val SproutCitrusLight = Color(0xFFFDF0E2)
val SproutBerry = Color(0xFF6B3E75)
val SproutBerryLight = Color(0xFFF2E6F5)
val SproutBlue = Color(0xFF2B5B84)
val SproutBlueLight = Color(0xFFE8F1F8)
val SproutCautionBg = Color(0xFFFDF4DC)
val SproutCautionBorder = Color(0xFFE0A02E)
val SproutCautionText = Color(0xFF8B5E00)
val SproutTape = Color(0x33E0A02E)

// Soft Gradient Theme Colors for Expressive Craft Surfaces
val GenZNeonMintLight = Color(0xFFF0FDF4)
val GenZElectricLimeLight = Color(0xFFDCFCE7)
val GenZNeonMatcha = Color(0xFF86EFAC)
val GenZElectricEmerald = Color(0xFF22C55E)
val GenZCyberGreen = Color(0xFF10B981)
val GenZDarkForest = Color(0xFF065F46)
val GenZAcidLime = Color(0xFFA3E635)

// Cool Gen Z Green Glassmorphism & Ambient Gradients
val PulseSlideGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFF5FEF7),
        Color(0xFFDCFCE7),
        Color(0xFFA7F3D0),
        Color(0xFF34D399).copy(alpha = 0.65f)
    )
)

val PulseBorderGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF22C55E).copy(alpha = 0.65f),
        Color(0xFF10B981).copy(alpha = 0.45f),
        Color(0xFF84CC16).copy(alpha = 0.55f)
    )
)

val PulsePillGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF047857))
)

val GradientEmeraldStart = Color(0xFF34D399)
val GradientEmeraldEnd = Color(0xFF059669)
val GradientForestStart = Color(0xFF10B981)
val GradientForestMid = Color(0xFF0D9488)
val GradientForestEnd = Color(0xFF0F766E)

val GradientSunsetStart = Color(0xFFFF8A80)
val GradientSunsetMid = Color(0xFFFFAB91)
val GradientSunsetEnd = Color(0xFFFFCC80)

val GradientAuroraStart = Color(0xFF818CF8)
val GradientAuroraMid = Color(0xFFA78BFA)
val GradientAuroraEnd = Color(0xFFF472B6)

val GradientCitrusStart = Color(0xFFFBBF24)
val GradientCitrusEnd = Color(0xFFF59E0B)

val GradientOceanStart = Color(0xFF5BA0C2)
val GradientOceanEnd = Color(0xFF3F82A3)

val GradientBgTop = Color(0xFFFBF8EE)
val GradientBgMid = Color(0xFFFFFDF5)
val GradientBgBottom = Color(0xFFF5EFE0)

// Pre-built Brushes for rapid high-performance Compose drawing
object SproutGradients {
    val BotanicalLush = Brush.linearGradient(
        colors = listOf(Color(0xFFE4F0E5), Color(0xFFF2F8F3), Color(0xFFFFFDF5))
    )
    val ForestDeep = Brush.verticalGradient(
        colors = listOf(Color(0xFF2E6B47), Color(0xFF1B4332))
    )
    val SunsetGlow = Brush.linearGradient(
        colors = listOf(Color(0xFFFDF0E2), Color(0xFFFBECE8))
    )
    val CitrusFresh = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97724))
    )
    val BerryMystic = Brush.linearGradient(
        colors = listOf(Color(0xFFF2E6F5), Color(0xFFEDE9FE))
    )
    val GlassCard = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFDF5).copy(alpha = 0.98f), Color(0xFFFBF8EE).copy(alpha = 0.95f))
    )
    val ScreenBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBF8EE), Color(0xFFFFFDF5), Color(0xFFFBF8EE))
    )
    val HeroCard = Brush.linearGradient(
        colors = listOf(Color(0xFFE8F5E9), Color(0xFFE4F0E5), Color(0xFFFFFDF5))
    )
    val QuizGlow = Brush.linearGradient(
        colors = listOf(Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFFFFFDF5))
    )
    val TopBarGlow = Brush.verticalGradient(
        colors = listOf(Color(0xFFFFFDF5).copy(alpha = 0.98f), Color(0xFFFBF8EE).copy(alpha = 0.95f))
    )
    val AccentPill = Brush.horizontalGradient(
        colors = listOf(Color(0xFF2E6B47), Color(0xFF1B4332))
    )

    // Botanical Atlas Gradients
    val BotanicalSlideLight = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEFF7FA),
            Color(0xFFD4ECF5),
            Color(0xFFFFFDF5)
        )
    )
    val BotanicalBorderLight = Brush.linearGradient(
        colors = listOf(
            Color(0xFF5BA0C2).copy(alpha = 0.5f),
            Color(0xFFD4ECF5),
            Color(0xFF5BA0C2).copy(alpha = 0.5f)
        )
    )

    // Daily Quiz Gradients
    val QuizSlideLight = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFAF5FF),
            Color(0xFFF3E8FF),
            Color(0xFFFFFDF5)
        )
    )
    val QuizBorderLight = Brush.linearGradient(
        colors = listOf(
            Color(0xFFDDD6FE),
            Color(0xFFE9D5FF),
            Color(0xFFDDD6FE)
        )
    )
    val QuizBadgeGlow = Brush.horizontalGradient(
        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97724))
    )
}


