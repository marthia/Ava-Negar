package me.marthia.avanegar.presentation.navigation

import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.generated.destinations.ImportRouteDestination
import com.ramcosta.composedestinations.generated.destinations.TranscriptionRouteDestination
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


    override fun openTranscriptionResult() {
        destinationsNavigator.navigate(TranscriptionRouteDestination())
    }

    override fun openImportScreen(audioUri: String) {
        destinationsNavigator.navigate(ImportRouteDestination(audioUri = audioUri))
    }
}

