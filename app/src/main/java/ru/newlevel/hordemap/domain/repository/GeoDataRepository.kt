package ru.newlevel.hordemap.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity

interface GeoDataRepository {

    fun startUserMarkerUpdates(): Flow<List<MarkerEntity>>

    fun startStaticMarkerUpdates(): Flow<List<MarkerEntity>>

    fun deleteStaticMarker(key: String)

    fun sendCoordinates(markerModel: MarkerEntity)

    fun createStaticMarker(markerModel: MarkerEntity)
}


