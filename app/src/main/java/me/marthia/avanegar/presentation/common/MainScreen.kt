package me.marthia.avanegar.presentation.common

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.navigation.dependency
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import me.marthia.avanegar.presentation.navigation.AppNavigationProvider


@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreenContent(
    activity: Activity,
) {
    val navController = rememberNavController()
    val destinationsNavigator = navController.rememberDestinationsNavigator()
    val navigator = AppNavigationProvider(destinationsNavigator, navController)

    val snackbarHostState = remember { SnackbarHostState() }
    val windowClassSize: WindowSizeClass = calculateWindowSizeClass(activity)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = {

            val modifier = Modifier.padding(bottom = it.calculateBottomPadding())
            DestinationsNavHost(
                modifier = modifier,
                navController = navigator.nav,
                navGraph = NavGraphs.root,
                dependenciesContainerBuilder = {
                    dependency(navigator)
                    dependency(windowClassSize)
                }
            )
        }
    )
}