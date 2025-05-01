package me.marthia.avanegar.presentation.utils

import java.io.InputStream
import java.io.OutputStream

fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, onProgress : (Long) -> Unit): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        onProgress(bytesCopied)
        bytes = read(buffer)
    }
    return bytesCopied
}