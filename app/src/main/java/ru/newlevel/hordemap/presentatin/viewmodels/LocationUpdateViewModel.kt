package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.app.MyLocationManager


class LocationUpdateViewModel: ViewModel() {

    fun startForegroundService(context: Context, timeToSendData: Int) {
        Log.e("AAA", "startForegroundService вызван")
        val serviceIntent = Intent(context, MyLocationManager::class.java)
        serviceIntent.putExtra("timeToSendData", timeToSendData)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopForegroundService(context: Context) {
        val serviceIntent = Intent(context, MyLocationManager::class.java)
        context.stopService(serviceIntent)
    }
}
