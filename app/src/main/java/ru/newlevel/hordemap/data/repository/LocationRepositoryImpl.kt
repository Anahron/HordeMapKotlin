package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import ru.newlevel.hordemap.device.MyLocationManager
import ru.newlevel.hordemap.device.MyLocationManager.Companion.ACTION_START
import ru.newlevel.hordemap.device.MyLocationManager.Companion.ACTION_STOP
import ru.newlevel.hordemap.domain.repository.LocationRepository

class LocationRepositoryImpl(private val context: Context) : LocationRepository {

    @MainThread
    override fun startLocationUpdates() {
        context.applicationContext.startForegroundService(
            Intent(
                context.applicationContext,
                MyLocationManager::class.java
            ).apply {
                action = ACTION_START
            })
    }

    @MainThread
    override fun stopLocationUpdates() {
        context.applicationContext.startService(
            Intent(
                context.applicationContext,
                MyLocationManager::class.java
            ).apply {
                action = ACTION_STOP
            })
    }
}
