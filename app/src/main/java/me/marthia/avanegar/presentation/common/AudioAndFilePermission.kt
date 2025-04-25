@file:OptIn(ExperimentalPermissionsApi::class)

package me.marthia.avanegar.presentation.common

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import me.marthia.avanegar.presentation.utils.AndroidVersionUtils
import me.marthia.avanegar.presentation.utils.SDK

@OptIn(ExperimentalPermissionsApi::class)
@SuppressLint("InlinedApi")
@Composable
fun AudioAndFilePermission(
    showRational: () -> Unit
) {

    if (AndroidVersionUtils.atLeast(SDK.Android13)) {
        val permissionState = rememberPermissionState(
            Manifest.permission.READ_MEDIA_AUDIO
        )

        if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
            showRational()
        } else {
            // Request the permission
            LaunchedEffect(Unit) {
                permissionState.launchPermissionRequest()
            }
        }
    }
    val permissionState =
        rememberPermissionState(permission = Manifest.permission.READ_EXTERNAL_STORAGE)
    if (!permissionState.status.isGranted && permissionState.status.shouldShowRationale) {
        showRational()
    } else {
        // Request the permission
        LaunchedEffect(Unit) {
            permissionState.launchPermissionRequest()
        }
    }
}