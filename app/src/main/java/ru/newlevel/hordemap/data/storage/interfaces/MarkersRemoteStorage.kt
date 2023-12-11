package ru.newlevel.hordemap.data.storage.interfaces

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface MarkersRemoteStorage {
    fun deleteStaticMarker(key: String)

    fun sendUserMarker(markerModel: MarkerDataModel)

    fun getUserMarkerUpdates(): Flow<List<MarkerDataModel>>

    fun getStaticMarkerUpdates(): Flow<List<MarkerDataModel>>

    fun sendStaticMarker(markerModel: MarkerDataModel)
}