package me.marthia.avanegar.presentation.common


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import androidx.work.ForegroundInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.marthia.avanegar.R
import me.marthia.avanegar.presentation.utils.AndroidVersionUtils
import me.marthia.avanegar.presentation.utils.LocaleProvider
import me.marthia.avanegar.presentation.utils.SDK
import java.io.BufferedInputStream
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Timer
import kotlin.concurrent.schedule
import kotlin.random.Random


object ForegroundUtils {
    private const val CHANNEL_NAME = "Download"
    private const val timeout = 60 * 1000
    const val CHANNEL_ID = "FILE_DOWNLOAD_WORKER"
    const val CHANNEL_DESC = "Downloading"
    const val KEY_FILE_TOTAL_SIZE = "file_size"
    const val KEY_FILE_DOWNLOADED = "file_progress"
    private const val KEY_GROUP_ID = "group_id"
    const val NOTIFICATION_ID = 101

    @SuppressLint("NewApi")
    fun createNotificationGroups(context: Context, notificationManager: NotificationManager) {
        if (AndroidVersionUtils.atLeast(SDK.Android8)) {

            val notificationChannelGroup = NotificationChannelGroup(
                KEY_GROUP_ID,
                context.applicationContext.getString(R.string.group_name)
            )
            notificationManager.createNotificationChannelGroup(notificationChannelGroup)
        }
    }

    @SuppressLint("NewApi")
    fun createForegroundInfo(
        applicationContext: Context,
        notificationManager: NotificationManager,
        titleRes: Int = R.string.downloading_data,
        onGoing: Boolean = true,
        icon: Int = R.drawable.round_download_24,
        totalSize: Long = 0,
        downloadedSize: Long = 0,
    ): ForegroundInfo {

        if (AndroidVersionUtils.atLeast(SDK.Android8)) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
//            channel.description = CHANNEL_DESC
//            channel.group = KEY_GROUP_ID
            channel.enableVibration(false)
            notificationManager.createNotificationChannel(channel)
        }
        val localeContext = LocaleProvider.onAttach(applicationContext)
        val title = localeContext.getString(titleRes)
        val notification =
            NotificationCompat.Builder(localeContext, CHANNEL_ID)
                .setContentTitle(title)
                .setTicker(title)
                .setSmallIcon(icon)
                .run {
                    if (onGoing) {
                        setProgress(totalSize.toInt(), downloadedSize.toInt(), totalSize == 0L || downloadedSize == 0L)
                        setOngoing(true)
                    } else setOngoing(false)
                }
                .build()

        val foregroundInfo =
            if (AndroidVersionUtils.atLeast(SDK.Android10))
//                TODO Android 15 will remove data sync type
                ForegroundInfo(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            else
                ForegroundInfo(NOTIFICATION_ID, notification)

        return foregroundInfo
    }

    @SuppressLint("NewApi")
    fun notify(
        applicationContext: Context,
        notificationManager: NotificationManager,
        channelId: String,
        channelName: String,
        content: String,
        titleRes: Int,
        icon: Int,
    ): Int {

        if (AndroidVersionUtils.atLeast(SDK.Android8)) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val title = applicationContext.getString(titleRes)
        val notification =
            NotificationCompat.Builder(applicationContext, channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setTicker(title)
                .setSmallIcon(icon)
                .build()

        val id = Random.nextInt()
        notificationManager.notify(id, notification)

        return id
    }

    /**@return the file size of the given file url , or -1L if there was any kind of error while doing so*/
    @WorkerThread
    internal fun getUrlFileLength(url: String): Long {
        return try {
            val connection = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "HEAD"
                connectTimeout = timeout
            }
            val timer = Timer()
            timer.schedule(timeout.toLong()) {
                connection.disconnect()
            }

            val contentLength = connection.contentLengthLong.coerceAtLeast(-1L)

            timer.cancel()
            contentLength
        } catch (ignored: Exception) {
            Log.e("Foreground", "Could not resolve file length from url ${ignored.message}")
            -1L
        }
    }

    suspend fun downloadFile(
        target: File,
        url: String,
        onProgress: (Long) -> Unit,
        onComplete: suspend (Boolean) -> Unit,
    ) {
        if (!isInternetAvailableWithSocket()) {
            onComplete(false)
            return
        }

        try {
            withContext(Dispatchers.IO) {
                BufferedInputStream(URL(url).openStream())
            }.use { input ->
                target.outputStream().use { outputStream ->
//                    input.copyTo(outputStream, DEFAULT_BUFFER_SIZE) { bytesRead ->
//                        onProgress(bytesRead)
//                    }
                }
            }

            onComplete(true)
            Log.d("Foreground","Successfully downloaded the file $target")
        } catch (e: Exception) {
            Log.e("Foreground","Couldn't Download the file $target: ${e.message}")
            onComplete(false)
        }
    }
}

