package com.foxnet.medications.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.googlefonts.Font
import com.foxnet.medications.R

// initialize google font provider
val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.foxnet.medications.R.array.com_google_android_gms_fonts_certs
)

@Immutable
data class GoogleFonts @OptIn(ExperimentalTextApi::class) constructor(
    //val googleSansFlex: FontFamily = FontFamily(Font(googleFont = GoogleFont("Google Sans Flex"), fontProvider = googleFontProvider)),
    //val nunito: FontFamily = FontFamily(Font(googleFont = GoogleFont("Nunito"), fontProvider = googleFontProvider)),
    val googleSansFlexRounded: FontFamily = FontFamily(
        Font(resId = R.font.googlesansflex,
            variationSettings = FontVariation.Settings(
                FontVariation.Setting("ROND", 100f)
            )
        )
    )
)

val LocalFonts = staticCompositionLocalOf { GoogleFonts() }

val MaterialTheme.fonts: GoogleFonts
    @Composable
    @ReadOnlyComposable
    get() = LocalFonts.current


// Set of Material typography styles to start with
val Typography = Typography(
    /*
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    */
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)