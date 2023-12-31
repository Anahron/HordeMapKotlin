package ru.newlevel.hordemap.app

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.models.UserModel
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


fun Location.getLatLng(): LatLng{
    return LatLng(this.latitude, this.longitude)
}


fun Double.toDistanceText(): String{
   return if (this.toInt() > 1000) ((this / 10).roundToInt() / 100.0).toString() + " км." else this.toInt()
        .toString() + " м."
}

fun Context.getFileNameFromUri(uri: Uri): String {
    var fileName: String? = null
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        fileName = it.getString(nameIndex)
    }
    if (fileName == null) {
        val file = uri.path?.let { File(it) }
        fileName = file?.name ?: ""
    }
    return fileName ?: ""
}

fun Context.getFileSizeFromUri(uri: Uri): Long {
    val cursor = contentResolver.query(uri, null, null, null, null)
    return cursor?.use { c ->
        val sizeIndex = c.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex != -1) {
            c.moveToFirst()
            c.getLong(sizeIndex)
        } else {
            0
        }
    } ?: 0
}

fun Context.copyTextInSystem(text: String) {
    val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("Text", text)
    clipboardManager.setPrimaryClip(clipData)
}

fun UserDataModel.mapToDomainModel(): UserDomainModel {
    return UserDomainModel(
        name = name,
        timeToSendData = timeToSendData,
        usersMarkerSize = usersMarkerSize,
        staticMarkerSize = staticMarkerSize,
        selectedMarker = selectedMarker,
        deviceID = deviceID,
        autoLoad = autoLoad,
        profileImageUrl = profileImageUrl,
        authName = authName,
    )
}

fun UserDomainModel.mapToDataModel(): UserDataModel {
    return UserDataModel(
        name = name,
        timeToSendData = timeToSendData,
        usersMarkerSize = usersMarkerSize,
        staticMarkerSize = staticMarkerSize,
        selectedMarker = selectedMarker,
        deviceID = deviceID,
        autoLoad = autoLoad,
        profileImageUrl = profileImageUrl,
        authName = authName,
    )
}

@SuppressLint("HardwareIds")
fun Context.getMyDeviceId(): String {
    val androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    Log.e(TAG, "androidId = $androidId")
    return androidId ?: UUID.randomUUID().toString()
}

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = apply { setValue(initialValue) }

fun Location.toMarker(userModel: UserModel): MarkerDataModel {
    val marker = MarkerDataModel()
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    val currentTime = LocalTime.now()
    val formattedTime = currentTime.format(timeFormatter)
    marker.latitude = this.latitude
    marker.longitude = this.longitude
    marker.userName = userModel.name
    marker.deviceId = userModel.deviceID
    marker.timestamp = System.currentTimeMillis()
    marker.item = userModel.selectedMarker
    marker.title = formattedTime
    return marker
}

fun Context.convertDpToPx(dp: Int): Int {
    val density: Float = resources.displayMetrics.density
    return (dp.toFloat() * density).roundToInt()
}

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}


fun Context.createTempImageFile(): File? {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFileName = "JPEG_" + timeStamp + "_"
    val storageDir = filesDir
    try {
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

