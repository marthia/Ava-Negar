package me.marthia.avanegar.presentation.navigation

import androidx.navigation.NavHostController
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

interface NavigationProvider {

    val nav: NavHostController

    val destNav: DestinationsNavigator

    fun navigateUp()

    fun openTranscriptionResult()
}