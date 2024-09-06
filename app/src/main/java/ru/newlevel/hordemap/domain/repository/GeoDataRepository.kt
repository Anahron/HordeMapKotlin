package ru.newlevel.hordemap.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.data.db.UserMarkerEntity

interface GeoDataRepository {

    fun startUserMarkerUpdates(): Flow<List<MarkerEntity>>

    fun startStaticMarkerUpdates(): Flow<List<MarkerEntity>>
    suspend fun insertUserMarkers(data: List<UserMarkerEntity>)
    suspend fun insertStaticMarkers(data: List<MarkerEntity>)
    fun deleteStaticMarker(key: String)

    fun sendCoordinates(markerModel: MarkerEntity)

    fun createStaticMarker(markerModel: MarkerEntity)
    fun startUserMarkerUpdatesLocal(): Flow<List<UserMarkerEntity>>
    fun startStaticMarkerUpdatesLocal(): Flow<List<MarkerEntity>>
}


