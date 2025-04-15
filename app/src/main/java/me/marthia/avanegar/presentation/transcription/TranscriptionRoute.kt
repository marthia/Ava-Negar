package me.marthia.avanegar.presentation.transcription

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import me.marthia.avanegar.presentation.common.VoskActivity
import me.marthia.avanegar.presentation.common.VoskViewModel
import me.marthia.avanegar.presentation.navigation.HomeGraph

@Destination<HomeGraph>()
@Composable
fun TranscriptionRoute(
    voskViewModel: VoskViewModel = hiltViewModel(LocalActivity.current as VoskActivity)
) {

    val output = voskViewModel.transcription
    Scaffold(modifier = Modifier.fillMaxSize()) { p ->
        TranscriptionScreen(modifier = Modifier.padding(p), output = output)
    }
}

@Composable
fun TranscriptionScreen(modifier: Modifier, output: String) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Text(
            text = output,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            textAlign = TextAlign.Start
        )
    }
}
