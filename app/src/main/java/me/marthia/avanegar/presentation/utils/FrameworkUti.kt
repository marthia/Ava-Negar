package me.marthia.avanegar.presentation.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Context.VIBRATOR_SERVICE
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Picture
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.LocaleList
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.annotation.IntegerRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.Locale

@SuppressLint("NewApi")
@Suppress("DEPRECATION")
fun Context.vibrator() =
    if (AndroidVersionUtils.atLeast(SDK.Android12)) {
        val vibratorManager =
            getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

val Context.inflater: LayoutInflater
    get() = LayoutInflater.from(this)

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
fun KeepScreenOn(enabled: Boolean = true) {
    AndroidView({ View(it).apply { keepScreenOn = enabled } })
    LaunchedEffect(enabled) {
        Log.d("Keep-Screen-On", "Requested state = $enabled")
    }
}

@Composable
fun DoNotDim() {
    val activity = LocalActivity.current

    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
}

/**
 * @param integerRes An integer resource ID.
 * @return The integer resource requested.
 * @brief Get an integer resource.
 */
fun Context.getInteger(@IntegerRes integerRes: Int) = resources.getInteger(integerRes)

@Stable
fun Modifier.autoMirrorOnRtl(rtl: Boolean = true): Modifier = composed {
    when (rtl) {
        false -> this
        true -> {
            if (LocalLayoutDirection.current == LayoutDirection.Rtl)
                this.scale(scaleX = -1f, scaleY = 1f)
            else
                this
        }
    }
}

fun String.shareExternal(): Intent {
    val dataToShare = this
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, dataToShare)
        type = "text/plain"
    }
    return Intent.createChooser(sendIntent, null)
}

fun File.shareExternal(): Intent {
    val dataToShare = this
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "video/mp4"
        putExtra(Intent.EXTRA_STREAM, Uri.fromFile(dataToShare))
    }
    return Intent.createChooser(sendIntent, null)
}

fun Uri.shareExternal(mimeType: String): Intent {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, this@shareExternal)
    }
    return Intent.createChooser(sendIntent, null)
}

fun Picture.toBitmap(): Bitmap {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        Bitmap.createBitmap(this)
    } else {
        val bitmap = Bitmap.createBitmap(
            this.width,
            this.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)
        canvas.drawPicture(this)
        bitmap
    }
    return bitmap
}

fun Bitmap.shareExternal(context: Context): Intent {
    val sendIntent: Intent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "image/*"
        putExtra(Intent.EXTRA_STREAM, getImageUri(context))
    }
    return Intent.createChooser(sendIntent, null)
}

fun Bitmap.getImageUri(context: Context, title: String? = null): Uri {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(
        context.contentResolver,
        this,
        title ?: "Temp image",
        null
    )
    return Uri.parse(path.toString())
}

fun Context.showMessage(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.getBitmapFromAsset(filePath: String): Bitmap? {

    try {
        return assets
            .open(filePath)
            .use(BitmapFactory::decodeStream)
    } catch (e: IOException) {
        Log.e("getBitmapFromAsset", "Could not get bitmap from assets: ${e.message}")
    }
    return null
}

fun Context.getBitmapFromUri(uri: Uri?) =
    uri?.let { BitmapFactory.decodeStream(contentResolver.openInputStream(uri)) }

fun Context.resolveResource(name: String, type: String): Int {
    return resources.getIdentifier(name, type, packageName)
}

fun Context.setLocale(language: String): Context {
    val locale = Locale(language)
    Locale.setDefault(locale)

    val current = resources.configuration.locales.get(0)

    if (current == locale) return this

    val config = Configuration(resources.configuration)
    // bring the target locale to the front of the list
    val set = linkedSetOf(locale)

    val defaultLocales = LocaleList.getDefault()
    val all = List<Locale>(defaultLocales.size()) { defaultLocales[it] }
    // append other locales supported by the user
    set.addAll(all)

    config.setLocales(LocaleList(*set.toTypedArray()))
    config.setLayoutDirection(locale)
    // TODO it won't work without this line why?
    resources.updateConfiguration(config, resources.displayMetrics)
    return createConfigurationContext(config)
}

fun Context.getFileName(uri: Uri): String {
    var result: String? = null
    try {
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = cut?.plus(1)?.let { result?.substring(it) }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return result.orEmpty()
}

fun Context.getFileFromUrl(uri: Uri): File {
    var result: String? = null
    try {
        if (uri.scheme == "content") {
            contentResolver.query(
                uri,
                arrayOf(MediaStore.Video.Media.DATA),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    result =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return File(result.orEmpty())
}


/**
 * Bring the activity to the full screen.
 */
internal fun Activity.setFullScreen(fullscreen: Boolean) {
    window.setFullScreen(fullscreen)
}

/**
 * Bring the window to full screen. (Remove the status bar and navigation bar.)
 */
@Suppress("Deprecation")
internal fun Window.setFullScreen(fullscreen: Boolean) {
    if (fullscreen) {
        decorView.systemUiVisibility = (
                SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
    } else {
        decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
    }
}

fun Bitmap.saveIntoGallery(context: Context): Boolean {
    return try {
        if (Build.VERSION.SDK_INT >= 29) {
            val values = contentValues()
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/")
            values.put(MediaStore.Images.Media.IS_PENDING, true)

            val uri: Uri? =
                context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                saveImageToStream(this, context.contentResolver.openOutputStream(uri))
                values.put(MediaStore.Images.Media.IS_PENDING, false)
                context.contentResolver.update(uri, values, null, null)
            }
        } else {
            val directory = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            )

            val fileName = System.currentTimeMillis().toString() + ".jpg"
            val file = File(directory, fileName)
            saveImageToStream(this, FileOutputStream(file))
            val values = contentValues()
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun contentValues(): ContentValues {
    val values = ContentValues()
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
    return values
}

private fun saveImageToStream(bitmap: Bitmap, outputStream: OutputStream?) {
    if (outputStream != null) {
        try {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun Color.toHexCodeWithAlpha(): String {
    val alpha = this.alpha * 255
    val red = this.red * 255
    val green = this.green * 255
    val blue = this.blue * 255
    return String.format(
        "0x%02x%02x%02x%02x",
        alpha.toInt(),
        red.toInt(),
        green.toInt(),
        blue.toInt()
    )
}

@Composable
fun Float.toDp(): Dp {
    val sizeInPixels = this
    // Get the current Density
    val density = LocalDensity.current
    // Convert 16 pixels to dp
    return with(density) { sizeInPixels.toDp() }
}

@Composable
fun Dp.toPx(): Float {
    val sizeInDp = this
    // Get the current Density
    val density = LocalDensity.current
    // Convert 16 pixels to dp
    return with(density) { sizeInDp.toPx() }
}


@Composable
fun Int.toDp(): Dp {
    val sizeInPixels = this
    // Get the current Density
    val density = LocalDensity.current
    // Convert 16 pixels to dp
    return with(density) { sizeInPixels.toDp() }
}


fun Context.openAppNotificationSettings(channelId: String?) {
    val context = this
    val intent = Intent().apply {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                channelId?.let { putExtra(Settings.EXTRA_CHANNEL_ID, it) }
            }

            else -> {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                addCategory(Intent.CATEGORY_DEFAULT)
                data = Uri.parse("package:" + context.packageName)
            }
        }
    }

    context.startActivity(intent)
}