package me.marthia.avanegar.presentation.home

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import me.marthia.avanegar.presentation.common.VoskActivity
import me.marthia.avanegar.presentation.common.VoskViewModel
import me.marthia.avanegar.presentation.navigation.HomeGraph
import me.marthia.avanegar.presentation.navigation.NavigationProvider
import me.marthia.avanegar.presentation.theme.AvaNegarTheme
import me.marthia.avanegar.presentation.utils.ScreenTransitions


@Destination<HomeGraph>(style = ScreenTransitions::class)
@Composable
fun ImportRoute(
    modifier: Modifier = Modifier,
    audioUri: String,
    navigator: NavigationProvider,
    viewModel: VoskViewModel = hiltViewModel(LocalActivity.current as VoskActivity),

    ) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val progress = viewModel.progress

    LaunchedEffect(progress) {
        when (progress) {
            is ImportProgressState.Idle -> {
                viewModel.toggleFileRecognition(audioUri)
            }

            is ImportProgressState.Done -> {
                navigator.navigateUp()
                navigator.openTranscriptionResult()
            }

            is ImportProgressState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        "Could not import the file"
                    )
                }

            }

            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { p ->
        ImportScreen(modifier = Modifier.padding(p))
    }
}

@Composable
fun ImportScreen(modifier: Modifier = Modifier) {

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Card(modifier = Modifier.padding(16.dp), shape = MaterialTheme.shapes.extraLarge) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text("Importing the audio file", style = MaterialTheme.typography.titleLarge)

                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            }


        }

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PreviewImport() {
    AvaNegarTheme {
        ImportScreen()
    }
}
