package me.marthia.avanegar.presentation.common

import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.marthia.avanegar.presentation.common.ForegroundUtils.NOTIFICATION_ID
import me.marthia.avanegar.presentation.utils.fromJson
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

@HiltWorker
class DownloadModelWorker @AssistedInject constructor(
    @Assisted val context: Context,
    @Assisted workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var totalSize = 0L
    private var downloadedSize = 0L

    override suspend fun doWork(): Result {

        try {

            subscribeNotif()

            withContext(Dispatchers.IO) {
                val url = inputData.getString(DATA_INFO_KEY)!!.fromJson<String>()!!
                val fileName = Uri.parse(url).lastPathSegment ?: ""
                val target = File("${context.getExternalFilesDir(null)}/model/$fileName")

                ForegroundUtils.downloadFile(
                    target = target,
                    url = url,
                    onProgress = { bytesDownloaded ->
                        setProgressAsync(
                            workDataOf(
                                KEY_FILE_TOTAL_SIZE to totalSize,
                                KEY_FILE_DOWNLOADED to bytesDownloaded
                            )
                        )
                    },
                    onComplete = {
                        // todo get this from unified provider
                        target.unzip("${context.getExternalFilesDir(null)}/model")
//                        target.delete()
                    }
                )
            }


            notificationManager.cancel(NOTIFICATION_ID)

            return Result.success()
        } catch (ex: Exception) {
            Log.e("worker", "Error in file download worker")
            return Result.failure()
        }
    }

    private suspend fun DownloadModelWorker.subscribeNotif() {
        try {
            setForeground(
                ForegroundUtils.createForegroundInfo(
                    applicationContext = applicationContext,
                    notificationManager = notificationManager,
                    totalSize = totalSize,
                    downloadedSize = downloadedSize
                )
            )
        } catch (ex: Exception) {
            Log.e("Foreground", "Error in file download worker notification")
        }
    }

    companion object {
        const val KEY_FILE_TOTAL_SIZE = "file_size"
        const val KEY_FILE_DOWNLOADED = "file_progress"
        const val DATA_INFO_KEY = "data"

        fun enqueue(context: Context, inputData: Data): UUID {
            val uploadWorkRequest = OneTimeWorkRequestBuilder<DownloadModelWorker>()
                .keepResultsForAtLeast(duration = 5, timeUnit = TimeUnit.MINUTES)
                .setInputData(inputData)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueue(uploadWorkRequest)

            return uploadWorkRequest.id
        }
    }
}