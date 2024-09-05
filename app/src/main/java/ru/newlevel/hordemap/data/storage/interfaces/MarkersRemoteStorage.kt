package ru.newlevel.hordemap.data.storage.interfaces

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity

interface MarkersRemoteStorage {
    fun deleteStaticMarker(key: String)

    fun sendUserMarker(markerModel: MarkerEntity)

    fun getUserMarkerUpdates(): Flow<List<MarkerEntity>>

    fun getStaticMarkerUpdates(): Flow<List<MarkerEntity>>

    fun sendStaticMarker(markerModel: MarkerEntity)
}