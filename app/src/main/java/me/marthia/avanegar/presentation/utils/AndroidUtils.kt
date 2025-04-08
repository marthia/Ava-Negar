package me.marthia.avanegar.presentation.utils

import android.os.Build


sealed class SDK(val version: Int) {

    data object Android7 : SDK(version = 24)
    data object Android71 : SDK(version = 25)
    data object Android8 : SDK(version = 26)
    data object Android81 : SDK(version = 27)
    data object Android9 : SDK(version = 28)
    data object Android10 : SDK(version = 29)
    data object Android11 : SDK(version = 30)
    data object Android12 : SDK(version = 31)
    data object Android12L : SDK(version = 32)
    data object Android13 : SDK(version = 33)
    data object Android14 : SDK(version = 34)
    data object Android5 : SDK(version = 35)
}

object AndroidVersionUtils {

    fun exact(v: SDK) = Build.VERSION.SDK_INT == v.version

    fun atLeast(v: SDK) = Build.VERSION.SDK_INT >= v.version

    fun atMost(v: SDK) = Build.VERSION.SDK_INT < v.version
}