package me.marthia.avanegar.presentation.navigation

import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

class AppNavigationProvider(
    private val destinationsNavigator: DestinationsNavigator,
    private val navController: NavHostController,
) : NavigationProvider {

    override val nav: NavHostController
        get() = navController

    override val destNav: DestinationsNavigator
        get() = destinationsNavigator

    override fun navigateUp() {
        navController.navigateUp()
    }


}

