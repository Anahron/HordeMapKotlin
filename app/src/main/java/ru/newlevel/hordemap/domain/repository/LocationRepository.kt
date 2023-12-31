package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.LiveData
import ru.newlevel.hordemap.data.db.MyLocationEntity

interface LocationRepository {

    fun  renameTrackNameForSession(sessionId: String, newTrackName: String)
    fun startLocationUpdates()

    fun stopLocationUpdates()
    suspend fun getAllLocationsGroupedBySessionId(): List<String>
    fun getCurrentLocationsLiveData(sessionId: String): LiveData<List<MyLocationEntity>>

    fun getLocationsBySessionId(sessionId: String): List<MyLocationEntity>
    fun updateLocation(myLocationEntity: MyLocationEntity)

    fun addLocation(myLocationEntity: MyLocationEntity)

    fun addLocations(myLocationEntities: List<MyLocationEntity>)

    fun deleteLocationsBySessionId(sessionId: String)

    fun setFavouriteTrackForSession(sessionId: String, isFavourite: Boolean)

    fun saveCurrentTrackSession(sessionId: String, newSessionId: String)
}
