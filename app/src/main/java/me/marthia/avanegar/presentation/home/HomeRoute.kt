@file:OptIn(ExperimentalMaterial3Api::class)

package me.marthia.avanegar.presentation.home

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch
import me.marthia.avanegar.R
import me.marthia.avanegar.presentation.common.AudioAndFilePermission
import me.marthia.avanegar.presentation.common.LanguageModel
import me.marthia.avanegar.presentation.common.ModelSelectionBS
import me.marthia.avanegar.presentation.common.Pref
import me.marthia.avanegar.presentation.common.VoskActivity
import me.marthia.avanegar.presentation.common.VoskViewModel
import me.marthia.avanegar.presentation.navigation.HomeGraph
import me.marthia.avanegar.presentation.navigation.NavigationProvider
import me.marthia.avanegar.presentation.theme.AvaNegarTheme

@Destination<HomeGraph>(start = true)
@Composable
fun VoskApp(
    navigator: NavigationProvider,
    viewModel: VoskViewModel = hiltViewModel(LocalActivity.current as VoskActivity)
) {

    val mediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                viewModel.toggleFileRecognition(uri.toString())
                navigator.openTranscriptionResult()
            }
        }
    )

    val currentSelectedModel = Pref.currentModel


    Scaffold { p ->
        VoskScreen(
            modifier = Modifier.padding(p),
            onRecognizeFileClick = {
                mediaPicker.launch("audio/*")
            },
            selectedModel = currentSelectedModel,
            onModelSelected = { model ->
                viewModel.initModel(model)
            }
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.initModel(currentSelectedModel)
    }

    AudioAndFilePermission { }

    if (viewModel.downloadState)
        DownloadProgress()
}

@Composable
fun VoskScreen(
    modifier: Modifier = Modifier,
    selectedModel: LanguageModel?,
    onRecognizeFileClick: () -> Unit,
    onModelSelected: (LanguageModel) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
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

        Spacer(Modifier.height(48.dp))

        PreviousTranscriptions()

        Spacer(Modifier.weight(1f))

        CurrentModel(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            selectedModel = selectedModel,
            onModelSelected = onModelSelected
        )
    }
}


@Composable
fun CurrentModel(
    modifier: Modifier = Modifier,
    selectedModel: LanguageModel? = null,
    onModelSelected: (LanguageModel) -> Unit
) {
    val state = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModelSelectionBS(
        bottomSheetState = state,
        defaultModel = LanguageModel.FA,
        onSelection = onModelSelected
    )
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(selectedModel?.title ?: "no model selected")
        IconButton(onClick = {
            scope.launch { state.show() }
        }) {
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Arrow")
        }
    }
}

@Composable
fun PreviousTranscriptions(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {

        Row(
            modifier = Modifier.padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.latest_text_files))
            Icon(imageVector = Icons.Default.ArrowDropDown, "Arrow Drop Down")
        }

        Spacer(Modifier.height(16.dp))


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

            Icon(
                painter = painterResource(R.drawable.outline_article_24),
                tint = MaterialTheme.colorScheme.primary,
                contentDescription = "icon"
            )

            Column {
                Text("Transcription_01", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Lorem Ipsum Lorem Ipsum Lorem Ipsum",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "23 Sep 2024", style = MaterialTheme.typography.labelSmall.copy(
                        LocalContentColor.current.copy(alpha = 0.6f)
                    )
                )
            }

            Spacer(Modifier.weight(1f))
            IconButton({}) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "More Icon")
            }

        }
    }
}

@Composable
fun DownloadProgress(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.White.copy(alpha = 0.37f)),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewTranscriptionList() {
    AvaNegarTheme {
        PreviousTranscriptions()
    }
}
