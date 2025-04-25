package me.marthia.avanegar.presentation.common

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.TextUtils
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class FileUtils(var context: Context) {

    fun getPath(uri: Uri): String? {
        if (isExternalStorageDocument(uri)) {
            return getPathFromExternalStorageDocument(uri)
                ?: copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER)
        }

        if (isDownloadsDocument(uri)) {
            return getPathFromDownloadsDocument(uri)
        }

        if (isMediaDocument(uri)) {
            return getPathFromMediaDocument(uri)
        }

        if (isGoogleDriveUri(uri)) {
            return getDriveFilePath(uri)
        }

        if (isWhatsAppFile(uri)) {
            return getFilePathForWhatsApp(uri)
        }

        if ("content".equals(uri.scheme, ignoreCase = true)) {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER)
            } else {
                getDataColumn(context, uri, null, null)
            }
        }

        if ("file".equals(uri.scheme, ignoreCase = true)) {
            return uri.path
        }

        return copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER)
    }

    private fun getPathFromExternalStorageDocument(uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val type = split[0]
        var fullPath = getPathFromExtSD(split)
        if (fullPath == null || !fileExists(fullPath)) {
            Log.d(TAG, "Copy files as a fallback")
            fullPath = copyFileToInternalStorage(uri, FALLBACK_COPY_FOLDER)
        }
        return fullPath
    }

    private fun getPathFromDownloadsDocument(uri: Uri): String? {
        val id = DocumentsContract.getDocumentId(uri)
        if (!TextUtils.isEmpty(id)) {
            if (id.startsWith("raw:")) {
                return id.replaceFirst("raw:".toRegex(), "")
            }
            val contentUriPrefixesToTry = arrayOf(
                "content://downloads/public_downloads",
                "content://downloads/my_downloads"
            )
            for (contentUriPrefix in contentUriPrefixesToTry) {
                return try {
                    val contentUri =
                        ContentUris.withAppendedId(Uri.parse(contentUriPrefix), id.toLong())
                    getDataColumn(context, contentUri, null, null)
                } catch (e: NumberFormatException) {
                    uri.path!!.replaceFirst("^/document/raw:".toRegex(), "")
                        .replaceFirst("^raw:".toRegex(), "")
                }
            }
        }

        return null
    }

    @SuppressLint("NewApi")
    private fun getPathFromMediaDocument(uri: Uri): String? {
        val docId = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val contentUri = when (split[0]) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            "document" -> MediaStore.Files.getContentUri(MediaStore.getVolumeName(uri))
            else -> null
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(split[1])
        return getDataColumn(context, contentUri, selection, selectionArgs)
    }

    private fun getDriveFilePath(uri: Uri): String {
        context.contentResolver.query(uri, null, null, null, null)?.use { returnCursor ->
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            if (returnCursor.moveToFirst()) {
                val name = returnCursor.getString(nameIndex)
                val size = returnCursor.getLong(sizeIndex).toString()
                val file = File(context.cacheDir, name)
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    Log.e(TAG, "Size ${file.length()}")
                    Log.e(TAG, "Path ${file.path}")
                    Log.e(TAG, "Size ${file.length()}")
                } catch (e: Exception) {
                    Log.e(TAG, e.message ?: "")
                }
                return file.path
            }
        }
        return ""
    }

    /***
     * Used for Android Q+
     * @param uri
     * @param newDirName if you want to create a directory, you can set this variable
     * @return
     */
    private fun copyFileToInternalStorage(uri: Uri, newDirName: String): String {
        context.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE),
            null, null, null
        )?.use { returnCursor ->
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            if (returnCursor.moveToFirst()) {
                val name = returnCursor.getString(nameIndex)
                val size = returnCursor.getLong(sizeIndex).toString()
                val output: File = if (newDirName.isNotEmpty()) {
                    val randomCollisionAvoidance = UUID.randomUUID().toString()
                    val dir = File(
                        "${context.filesDir}/$newDirName/$randomCollisionAvoidance"
                    ).apply { mkdirs() }
                    File("$dir/$name")
                } else {
                    File("${context.filesDir}/$name")
                }
                try {
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(output).use { outputStream ->
                            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, e.message ?: "")
                }
                return output.path
            }
        }
        return ""
    }

    private fun getFilePathForWhatsApp(uri: Uri): String {
        return copyFileToInternalStorage(uri, "whatsapp")
    }

    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        context.contentResolver.query(
            uri ?: return null,
            arrayOf(MediaStore.MediaColumns.DATA),
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                return cursor.getString(columnIndex)
            }
        }
        return null
    }

    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    private fun isWhatsAppFile(uri: Uri): Boolean {
        return "com.whatsapp.provider.media" == uri.authority
    }

    private fun isGoogleDriveUri(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage" == uri.authority || "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    companion object {
        var FALLBACK_COPY_FOLDER = "upload_part"
        private const val TAG = "FileUtils"
        private var contentUri: Uri? = null
        private fun fileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists()
        }

        private fun getPathFromExtSD(pathData: Array<String>): String? {
            val type = pathData[0]
            val relativePath = File.separator + pathData[1]
            Log.d(TAG, "MEDIA EXTSD TYPE: $type")
            Log.d(TAG, "Relative path: $relativePath")

            val rootPaths = mutableListOf<String>()

            // Add possible root paths to the list
            rootPaths.add(Environment.getExternalStorageDirectory().toString() + relativePath)
            rootPaths.add("/storage/emulated/0/Documents$relativePath")
            rootPaths.add(System.getenv("SECONDARY_STORAGE")?.plus(relativePath) ?: "")
            rootPaths.add(System.getenv("EXTERNAL_STORAGE")?.plus(relativePath) ?: "")

            // Check if file exists in each root path
            for (path in rootPaths) {
                if (fileExists(path)) {
                    return path
                }
            }

            return null
        }

        private fun isExternalStorageDocument(uri: Uri): Boolean {
            return "com.android.externalstorage.documents" == uri.authority
        }

        private fun isDownloadsDocument(uri: Uri): Boolean {
            return "com.android.providers.downloads.documents" == uri.authority
        }
    }
}