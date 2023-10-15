package ru.newlevel.hordemap.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import java.util.*


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
