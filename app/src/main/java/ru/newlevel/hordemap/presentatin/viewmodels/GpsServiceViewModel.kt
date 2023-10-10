package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.app.GpsForegroundService


class GpsServiceViewModel: ViewModel() {

    fun startForegroundService(context: Context) {
        Log.e("AAA", "startForegroundService вызван")
        val serviceIntent = Intent(context, GpsForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopForegroundService(context: Context) {
        val serviceIntent = Intent(context, GpsForegroundService::class.java)
        context.stopService(serviceIntent)
    }
}
