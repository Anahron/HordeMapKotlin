package ru.newlevel.hordemap.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import java.io.InputStream
import java.util.*

fun makeLongToast(text: String, context: Context) {
    Toast.makeText(
        context,
        text,
        Toast.LENGTH_LONG
    ).show()
}
fun makeShortToast(text: String, context: Context) {
    Toast.makeText(
        context,
        text,
        Toast.LENGTH_LONG
    ).show()
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
