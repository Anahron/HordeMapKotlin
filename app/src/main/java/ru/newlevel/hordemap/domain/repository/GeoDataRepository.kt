package ru.newlevel.hordemap.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface GeoDataRepository {

    fun startUserMarkerUpdates(): Flow<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): Flow<List<MarkerDataModel>>

    fun deleteStaticMarker(key: String)

    fun sendCoordinates(markerModel: MarkerDataModel)

    fun createStaticMarker(markerModel: MarkerDataModel)
}


