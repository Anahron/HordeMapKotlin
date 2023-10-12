package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import ru.newlevel.hordemap.data.MyLocationManager
import ru.newlevel.hordemap.domain.repository.LocationRepository

class LocationRepositoryImpl(
    private val context: Context,
): LocationRepository {

    @MainThread
    override fun startLocationUpdates(){
        val serviceIntent = Intent(context, MyLocationManager::class.java)
        context.startForegroundService(serviceIntent)
    }

    @MainThread
    override fun stopLocationUpdates() {
        val serviceIntent = Intent(context, MyLocationManager::class.java)
        context.stopService(serviceIntent)
    }
}
