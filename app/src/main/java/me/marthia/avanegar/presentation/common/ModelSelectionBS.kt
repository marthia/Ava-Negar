@file:OptIn(ExperimentalMaterial3Api::class)

package me.marthia.avanegar.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.marthia.avanegar.presentation.theme.AvaNegarTheme


@Composable
fun ModelSelectionBS(
    bottomSheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit = {},
    onSelection: (LanguageModel) -> Unit = {},
    defaultModel: LanguageModel,
) {
    val scope = rememberCoroutineScope()
    if (bottomSheetState.isVisible) {
        ModalBottomSheet(
            sheetState = bottomSheetState,
            onDismissRequest = { onDismiss() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LanguageModel.entries.forEach { item ->
                    Row(
                        modifier = Modifier.clickable {
                            onSelection(item)
                            scope.launch { bottomSheetState.hide() }
                        },
                    ) {
                        Text(text = item.title, style = MaterialTheme.typography.bodyLarge)

                        Spacer(Modifier.weight(1f))
                        // is this model selected
                        if (defaultModel == item)
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Check",
                                tint = MaterialTheme.colorScheme.primary
                            )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewModelSelection() {
    val state = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    AvaNegarTheme {
        ModelSelectionBS(bottomSheetState = state, onDismiss = {}, onSelection = { model ->

        }, defaultModel = LanguageModel.FA)
    }

    SideEffect {
        scope.launch {
            state.show()
        }
    }
}