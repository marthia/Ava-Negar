package me.marthia.avanegar.presentation.common

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipFile


fun File.getFolderSize() = this.walkTopDown().filter { it.isFile }.map { it.length() }.sum()

fun String.replaceFileExtensionWith(newExtension: String) =
    this.dropLastWhile { it != '.' } + newExtension

fun String.getFileNameFromPath() = this.takeLastWhile { it != '/' }
fun String.getFileExtensionFromPath() = substringAfterLast(delimiter = ".", missingDelimiterValue = "")

/**
 * @param destDirectory
 * @throws IOException
 */
@Throws(IOException::class)
fun File.unzip(destDirectory: String) {

    File(destDirectory).run { if (!exists()) mkdirs() }

    ZipFile(this).use { zip ->
        zip.entries().asSequence().forEach { entry ->
            zip.getInputStream(entry).use { input ->
                val filePath = destDirectory + File.separator + entry.name

                if (!entry.isDirectory) {
                    // if the entry is a file, extracts it
                    extractFile(input, filePath)
                } else {
                    // if the entry is a directory, make the directory
                    val dir = File(filePath)
                    dir.mkdir()
                }
            }
        }
    }
}

/**
 * Unzip a file that contains a single file with same name and return that file
 * Ex:
 *  input:
 *      new.zip
 *          - new.json
 *
 *  output:
 *      new.json
 *
 *  here the 'contentFileExtension' is json
 */
fun File.unzipSingleContent(contentFileExtension: String): File {
    val fileName = this.path.getFileNameFromPath()
    val zipFileDirectory = this.path.removeSuffix("/$fileName")

    this.unzip(destDirectory = zipFileDirectory)

    return File(zipFileDirectory + "/" + fileName.replaceFileExtensionWith(contentFileExtension))
}
//
//fun File.unzipFile(extractedFileName: String = ""): File? {
//    return try {
//        val fileName = this.path.getFileNameFromPath()
//        val zipFileDirectory = this.path.removeSuffix("/$fileName")
//
//        val out = unzip(extractedFileName = extractedFileName, destDirectory = zipFileDirectory)
//        out?.let { File(it) }
//    } catch (e: Exception) {
//        Timber.e("Could not unzip file $this")
//        null
//    }
//}


/**
 * Extracts a zip entry (file entry)
 * @param inputStream
 * @param destFilePath
 * @throws IOException
 */
@Throws(IOException::class)
private fun extractFile(inputStream: InputStream, destFilePath: String) {
    val bos = BufferedOutputStream(FileOutputStream(destFilePath))
    val bytesIn = ByteArray(DEFAULT_BUFFER_SIZE)
    var read: Int
    while (inputStream.read(bytesIn).also { read = it } != -1) {
        bos.write(bytesIn, 0, read)
    }
    bos.close()
}