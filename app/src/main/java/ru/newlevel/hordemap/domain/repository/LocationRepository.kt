package ru.newlevel.hordemap.domain.repository

import ru.newlevel.hordemap.data.db.MyLocationEntity

interface LocationRepository {

    fun startLocationUpdates()

    fun stopLocationUpdates()
    suspend fun getAllLocationsGroupedBySessionId(): List<String>
    fun getLocationsSortedByUpdateTime(sessionId: String): List<MyLocationEntity>

    fun updateLocation(myLocationEntity: MyLocationEntity)

    fun addLocation(myLocationEntity: MyLocationEntity)

    fun addLocations(myLocationEntities: List<MyLocationEntity>)
}
