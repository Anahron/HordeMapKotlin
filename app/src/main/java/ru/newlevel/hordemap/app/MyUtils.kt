package ru.newlevel.hordemap.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.provider.Settings
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.models.UserModel
import java.io.InputStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

fun makeLongToast(text: String, context: Context) {
    Toast.makeText(
        context,
        text,
        Toast.LENGTH_LONG
    ).show()
}

fun Context.getMimeType(uri: Uri): String? {
    return if (uri.scheme == "content") {
        contentResolver.getType(uri)
    } else {
        // Для файлов на устройстве, используем MimeTypeMap
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.lowercase(Locale.ROOT))
    }
}

fun mapUserDataToDomain(user: UserDataModel): UserDomainModel {
    return UserDomainModel(
        user.name,
        user.timeToSendData,
        user.usersMarkerSize,
        user.staticMarkerSize,
        user.selectedMarker,
        user.deviceID,
        user.autoLoad
    )
}

fun mapUserDomainToData(userDomainModel: UserDomainModel): UserDataModel {
    return UserDataModel(
        userDomainModel.name,
        userDomainModel.timeToSendData,
        userDomainModel.usersMarkerSize,
        userDomainModel.staticMarkerSize,
        userDomainModel.selectedMarker,
        userDomainModel.deviceID,
        userDomainModel.autoLoad
    )
}

fun getDeviceId(context: Context): String {
    @SuppressLint("HardwareIds") val androidId =
        Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
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
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

suspend fun getInputSteamFromUri(uri: Uri, context: Context): InputStream? {
    return withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)
        } catch (e: Exception) {
            null
        }
    }
}
