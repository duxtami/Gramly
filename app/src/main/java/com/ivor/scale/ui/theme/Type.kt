@file:OptIn(ExperimentalTextApi::class)

package com.ivor.scale.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ivor.scale.R

/** A variable-font [Font] for Roboto Flex at a given weight/width on the variation axes. */
private fun robotoFlex(weightAxis: Int, fontWeight: FontWeight, width: Float = 100f) = Font(
    resId = R.font.roboto_flex,
    weight = fontWeight,
    variationSettings = FontVariation.Settings(
        FontVariation.weight(weightAxis),
        FontVariation.width(width),
    ),
)

/** Roboto Flex — the expressive variable font family, registered across the weight axis. */
val RobotoFlex = FontFamily(
    robotoFlex(300, FontWeight.Light),
    robotoFlex(400, FontWeight.Normal),
    robotoFlex(500, FontWeight.Medium),
    robotoFlex(600, FontWeight.SemiBold),
    robotoFlex(700, FontWeight.Bold),
    robotoFlex(800, FontWeight.ExtraBold),
    robotoFlex(900, FontWeight.Black),
)

/**
 * A one-off Roboto Flex family pinned to an exact weight/width on the variation
 * axes. Used by the hero numeral so its weight can be *animated* smoothly
 * (an expressive variable-font morph) rather than snapping between named weights.
 */
@Composable
fun rememberRobotoFlexAxis(weightAxis: Int, width: Float = 100f): FontFamily =
    remember(weightAxis, width) {
        FontFamily(
            Font(
                resId = R.font.roboto_flex,
                variationSettings = FontVariation.Settings(
                    FontVariation.weight(weightAxis.coerceIn(100, 1000)),
                    FontVariation.width(width),
                ),
            )
        )
    }

/**
 * Expressive type scale on Roboto Flex: heavier weights and tighter tracking on
 * the large styles so the result number reads as the hero of the screen.
 */
val ExpressiveTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Black,
        fontSize = 57.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 45.sp,
        lineHeight = 50.sp,
        letterSpacing = (-0.25).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        lineHeight = 42.sp,
        letterSpacing = 0.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFlex,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)
