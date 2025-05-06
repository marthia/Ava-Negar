package me.marthia.avanegar.presentation.transcription

import android.content.ClipData
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import me.marthia.avanegar.R
import me.marthia.avanegar.presentation.common.VoskActivity
import me.marthia.avanegar.presentation.common.VoskViewModel
import me.marthia.avanegar.presentation.navigation.HomeGraph
import me.marthia.avanegar.presentation.theme.AvaNegarTheme
import me.marthia.avanegar.presentation.utils.ScreenTransitions

@Destination<HomeGraph>(style = ScreenTransitions::class)
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
fun TranscriptionScreen(modifier: Modifier = Modifier, output: String) {

    val scope = rememberCoroutineScope()

    val localClipboardManager = LocalClipboard.current
    var showMoreOptions by remember { mutableStateOf(false) }


    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Transcription_01",
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(Modifier.weight(1f))


            IconButton(onClick = {
                scope.launch {
                    localClipboardManager.setClipEntry(
                        ClipEntry(
                            ClipData.newPlainText(
                                "AvaNegar Transcription",
                                output
                            )
                        )
                    )
                }
            }) {
                Icon(painter = painterResource(R.drawable.copy_icon), contentDescription = "Copy")
            }

            Box {
                IconButton(onClick = {
                    showMoreOptions = true
                }) {
                    Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More")
                }

                DropdownMenu(
                    modifier = Modifier,
                    expanded = showMoreOptions,
                    onDismissRequest = { showMoreOptions = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.save_as_text_file)) },
                        onClick = {}
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.save_as_srt_file)) },
                        onClick = {}
                    )
                }
            }

        }

        Text(
            text = output,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            textAlign = TextAlign.Justify,
            style = MaterialTheme.typography.bodyLarge.copy(textDirection = TextDirection.ContentOrRtl)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTranscription() {
    AvaNegarTheme(dynamicColor = true) {
        TranscriptionScreen(output = "lorem ipsum lorem ipsum")
    }
}