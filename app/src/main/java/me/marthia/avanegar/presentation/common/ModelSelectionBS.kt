@file:OptIn(ExperimentalMaterial3Api::class)

package me.marthia.avanegar.presentation.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch


@Composable
fun ModelSelectionBS(
    bottomSheetState: SheetState = rememberModalBottomSheetState(),
    onDismiss: () -> Unit = {},
    onSelection: (Models) -> Unit = {},
    defaultModel: String,
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
                    .padding(16.dp)
            ) {
                Models.entries.forEach { item ->
                    Row(modifier = Modifier.clickable {
                        onSelection(item)
                        scope.launch { bottomSheetState.hide() }

                    }) {
                        Text(text = item.title)
                        // is this model selected
                        if (defaultModel == item.title)
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Check"
                            )
                    }
                }
            }
        }
    }
}