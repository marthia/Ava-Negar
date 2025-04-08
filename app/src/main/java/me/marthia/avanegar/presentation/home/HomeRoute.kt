package me.marthia.avanegar.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import me.marthia.avanegar.R
import me.marthia.avanegar.presentation.common.VoskViewModel
import me.marthia.avanegar.presentation.navigation.HomeGraph
import me.marthia.avanegar.presentation.theme.AvaNegarTheme

@Destination<HomeGraph>(start = true)
@Composable
fun VoskApp(
    modifier: Modifier = Modifier,
    viewModel: VoskViewModel = hiltViewModel()
) {
    val state = viewModel.transcription

    Scaffold { p ->
        VoskScreen(
            modifier = modifier.padding(p),
            uiState = state,
            onRecognizeFileClick = {
                viewModel.toggleFileRecognition()
            },
            onRecognizeMicClick = {
                viewModel.toggleMicrophoneRecognition()
            },
            onPauseClick = { isPaused ->
                viewModel.setPause(isPaused)
            }
        )
    }
}

@Composable
fun VoskScreen(
    modifier: Modifier = Modifier,
    uiState: String,
    onRecognizeFileClick: () -> Unit,
    onRecognizeMicClick: () -> Unit,
    onPauseClick: (Boolean) -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clickable(onClick = onRecognizeFileClick),
            shape = MaterialTheme.shapes.extraLarge.copy(CornerSize(48.dp))
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                Image(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(R.drawable.recognize_icon),
                    contentDescription = "icon"
                )

                Column {
                    Text(
                        "Import Audio File",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    Text(
                        "Transcribe audio to text",
                        style = MaterialTheme.typography.bodySmall,
                    )

                }
            }


        }

        PreviousTranscriptions()
    }
}

@Composable
fun PreviousTranscriptions(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Latest Text Files")
            Icon(imageVector = Icons.Default.ArrowDropDown, "Arrow Drop Down")
        }

        Spacer(Modifier.height(8.dp))
        repeat(3) {

            TranscriptionItemList(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(16.dp))
        }

    }
}

@Composable
fun TranscriptionItemList(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.copy(CornerSize(48.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Icon(painter = painterResource(R.drawable.article_icon), contentDescription = "icon")

            Column {
                Text("Transcription_01", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Lorem Ipsum Lorem Ipsum Lorem Ipsum",
                    style = MaterialTheme.typography.bodySmall
                )
                Text("23 Sep 2024", style = MaterialTheme.typography.labelSmall)
            }

            Spacer(Modifier.weight(1f))
            IconButton({}) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Icon")
            }

        }
    }
}



@Composable
fun MoreOptions(
    modifier: Modifier = Modifier,
    defaultState: Boolean,
    onSaveTxt: () -> Unit,
    onSaveSrt: () -> Unit
) {
    var expanded by remember { mutableStateOf(defaultState) }

    DropdownMenu(
        modifier = modifier,
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.save_as_text_file)) },
            onClick = onSaveTxt
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.save_as_srt_file)) },
            onClick = onSaveSrt
        )
    }
}






@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTranscriptionList() {
    AvaNegarTheme {
        PreviousTranscriptions()
    }
}
