package ru.newlevel.hordemap

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.util.*


object Utils {

    fun getDeviceId(context: Context): String {
        @SuppressLint("HardwareIds") val androidId =
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        return androidId ?: UUID.randomUUID().toString()
    }
}