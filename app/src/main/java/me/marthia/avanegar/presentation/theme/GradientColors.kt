package com.ertebatbonyan.android.base.app.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * A class to model gradient color values for Now in Android.
 *
 * @param top The top gradient color to be rendered.
 * @param bottom The bottom gradient color to be rendered.
 * @param container The container gradient color over which the gradient will be rendered.
 */
@Immutable
data class GradientColors(
    val top: Color = Color(0x4A3EC5FF),
    val bottom: Color = Color(0x4A3EC5FF),
    val center: Color = Color.Unspecified,
    val container: Color = Color.Unspecified,
) {
    val gradient
        get() = listOf(top, center, bottom).mapNotNull { it.takeIf { it != Color.Unspecified } }
}

/**
 * A composition local for [GradientColors].
 */
val LocalGradientColors = staticCompositionLocalOf { GradientColors() }

val BackgroundContentGradient = GradientColors(top = Color(0xFF132B7E), bottom = Color(0xFF4654D4))

val BorderGradient =
    GradientColors(
        top = Color(0x4A3EC5FF),
        center = Color(0xFF1D84FE),
        bottom = Color(0xFF3EC5FF)
    )

val SecondaryBorderGradient =
    GradientColors(top = Color(0xFF8C44E8), bottom = Color(0xFF0670A6))

val ProfileHeaderGradient =
    GradientColors(top = Color(0xFF114E71), bottom = Color(0xFF6A3AAA))



val DividerGradient =
    GradientColors(
        top = Color(0x4A000000),
        center = Color(0xFF621BF9),
        bottom = Color(0x1A000000),
    )

val NeutralDividerGradient =
    GradientColors(
        top = Color(0x00FFFFFF),
        center = Color(0x5EFFFFFF),
        bottom = Color(0x00FFFFFF),
    )

