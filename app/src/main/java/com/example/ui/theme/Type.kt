package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.R

val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val nunitoGoogleFont = GoogleFont("Nunito")

val NunitoFontFamily = FontFamily(
    Font(googleFont = nunitoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = nunitoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = nunitoGoogleFont, fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = nunitoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Bold),
    Font(googleFont = nunitoGoogleFont, fontProvider = fontProvider, weight = FontWeight.ExtraBold),
    Font(googleFont = nunitoGoogleFont, fontProvider = fontProvider, weight = FontWeight.Black)
)

// Set of Material typography styles to start with
val Typography =
  Typography(
    bodyLarge =
      TextStyle(
        fontFamily = NunitoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
      )
  )
