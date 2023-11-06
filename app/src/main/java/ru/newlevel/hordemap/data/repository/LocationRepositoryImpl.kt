package ru.newlevel.hordemap.data.repository

import android.content.Context
import android.content.Intent
import androidx.annotation.MainThread
import ru.newlevel.hordemap.data.db.MyLocationDao
import ru.newlevel.hordemap.data.db.MyLocationEntity
import ru.newlevel.hordemap.device.MyLocationManager
import ru.newlevel.hordemap.device.MyLocationManager.Companion.ACTION_START
import ru.newlevel.hordemap.device.MyLocationManager.Companion.ACTION_STOP
import ru.newlevel.hordemap.domain.repository.LocationRepository

class LocationRepositoryImpl(private val context: Context, private val myLocationDao: MyLocationDao) :
    LocationRepository {

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

    override fun getLocationsSortedByUpdateTime(sessionId: String): List<MyLocationEntity> =
        myLocationDao.getLocationsSortedByUpdateTime(sessionId = sessionId)

    override fun updateLocation(myLocationEntity: MyLocationEntity) {
        myLocationDao.updateLocation(myLocationEntity)
    }

    override fun addLocation(myLocationEntity: MyLocationEntity) {
        myLocationDao.addLocation(myLocationEntity)
    }

    override fun addLocations(myLocationEntities: List<MyLocationEntity>) {
        myLocationDao.addLocations(myLocationEntities)
    }


}
