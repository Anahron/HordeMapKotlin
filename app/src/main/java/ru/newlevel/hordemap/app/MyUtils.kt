package ru.newlevel.hordemap.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.OpenableColumns
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.models.UserModel
import java.io.File
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

fun makeLongToast(text: String, context: Context) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun Context.getFileNameFromUri(uri: Uri): String? {
    var fileName: String? = null
    val cursor = contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        fileName = it.getString(nameIndex)
    }
    if (fileName == null) {
        val file = uri.path?.let { File(it) }
        fileName = file?.name
    }
    return fileName
}


fun UserDataModel.mapToDomainModel(): UserDomainModel {
    return UserDomainModel(
        name = name,
        timeToSendData = timeToSendData,
        usersMarkerSize = usersMarkerSize,
        staticMarkerSize = staticMarkerSize,
        selectedMarker = selectedMarker,
        deviceID = deviceID,
        autoLoad = autoLoad
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
        autoLoad = autoLoad
    )
}

fun Context.getMyDeviceId(): String {
    @SuppressLint("HardwareIds") val androidId = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
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

fun Context.hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this, permission
    ) == PackageManager.PERMISSION_GRANTED
}
