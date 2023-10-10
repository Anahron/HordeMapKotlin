package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.app.GpsForegroundService
import ru.newlevel.hordemap.app.ServiceLocator


class GpsServiceViewModel: ViewModel() {

    private lateinit var locationLiveData: LiveData<Location>

    fun startForegroundService(context: Context) {
        Log.e("AAA", "startForegroundService вызван")
        val serviceIntent = Intent(context, GpsForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    fun stopForegroundService(context: Context) {
        val serviceIntent = Intent(context, GpsForegroundService::class.java)
        context.stopService(serviceIntent)
    }


    fun getForegroundServiceLiveData(): LiveData<Location> {
        Log.e("AAA", "getForegroundServiceLiveData вызван")
        locationLiveData = ServiceLocator.gpsService?.locationLiveData!!
        return locationLiveData
    }

}
