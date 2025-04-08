package me.marthia.avanegar.presentation.navigation

import com.ramcosta.composedestinations.annotation.NavGraph
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.generated.NavGraphs

@NavGraph<RootGraph>(start = true)
annotation class HomeGraph

val topLevelDestinations = listOf(
    NavGraphs.home.startRoute.baseRoute,
)