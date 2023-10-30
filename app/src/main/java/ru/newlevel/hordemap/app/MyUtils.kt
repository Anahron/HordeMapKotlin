package ru.newlevel.hordemap.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
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
    if (permission == Manifest.permission.ACCESS_BACKGROUND_LOCATION &&
        android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q
    ) {
        return true
    }

    return ActivityCompat.checkSelfPermission(this, permission) ==
            PackageManager.PERMISSION_GRANTED
}

fun Fragment.requestPermissionWithRationale(
    permission: String,
    requestCode: Int,
    snackbar: Snackbar
) {
    val provideRationale = shouldShowRequestPermissionRationale(permission)

    if (provideRationale) {
        snackbar.show()
    } else {
        requestPermissions(arrayOf(permission), requestCode)
    }
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
