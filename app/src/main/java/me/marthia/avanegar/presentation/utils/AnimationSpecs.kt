package me.marthia.avanegar.presentation.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

object AnimationSpecs {
    const val ANIM_DURATION = 200
    const val SCREEN_ANIM_DURATION = 200

    val enter = slideInVertically(
        animationSpec = tween(durationMillis = ANIM_DURATION),
        initialOffsetY = { it }
    )

    val enterFromRight =
        slideInHorizontally(
            animationSpec = tween(durationMillis = SCREEN_ANIM_DURATION),
            initialOffsetX = { it }
        )

    val enterFromLeft =
        slideInHorizontally(
            animationSpec = tween(durationMillis = SCREEN_ANIM_DURATION),
            initialOffsetX = { -it }
        )

    val exit = slideOutVertically(
        animationSpec = tween(durationMillis = ANIM_DURATION),
        targetOffsetY = { it }
    )

    val exitToLeft =
        slideOutHorizontally(
            animationSpec = tween(durationMillis = SCREEN_ANIM_DURATION),
            targetOffsetX = { -it }
        )

    val exitToRight =
        slideOutHorizontally(animationSpec = tween(durationMillis = SCREEN_ANIM_DURATION),
            targetOffsetX = { it })

    val reverseEnter = slideInVertically(
        animationSpec = tween(durationMillis = ANIM_DURATION),
        initialOffsetY = { -it }
    )

    val reverseExit = slideOutVertically(
        animationSpec = tween(durationMillis = ANIM_DURATION),
        targetOffsetY = { -it }
    )
}

object ScreenTransitions : DestinationStyle.Animated() {


    override val enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)
        get() = { AnimationSpecs.enterFromRight }
    override val exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)
        get() = { AnimationSpecs.exitToLeft }
    override val popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)
        get() = { AnimationSpecs.enterFromLeft }
    override val popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)
        get() = { AnimationSpecs.exitToRight }
    override val sizeTransform: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> SizeTransform?)?
        get() = super.sizeTransform

}