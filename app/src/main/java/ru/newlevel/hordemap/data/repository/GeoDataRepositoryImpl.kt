package ru.newlevel.hordemap.data.repository

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.data.db.MarkersDao
import ru.newlevel.hordemap.data.db.UserMarkerEntity
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class GeoDataRepositoryImpl(
    private val markersRemoteStorage: MarkersRemoteStorage,
    private val markersLocalStorage: MarkersDao
) : GeoDataRepository {

    override suspend fun deleteStaticMarker(key: String) {
        val marker = markersLocalStorage.getSingleMarker(key.toLong())
        if (marker.local)
            markersLocalStorage.deleteMarker(key.toLong())
        else
            markersRemoteStorage.deleteStaticMarker(key)
    }

    override fun sendStaticMarkerRemote(markerModel: MarkerEntity) =
        markersRemoteStorage.sendStaticMarker(markerModel)

    override fun startUserMarkerUpdatesLocal(): Flow<List<UserMarkerEntity>> =
        markersLocalStorage.getUserMarker()

    override fun startStaticMarkerUpdatesLocal(): Flow<List<MarkerEntity>> =
        markersLocalStorage.getMarker()

    override suspend fun insertStaticMarkerLocal(marker: MarkerEntity) =
        markersLocalStorage.insertMarker(marker)

    override fun startUserMarkerUpdates(): Flow<List<MarkerEntity>> =
        markersRemoteStorage.getUserMarkerUpdates()

    override fun startStaticMarkerUpdates(): Flow<List<MarkerEntity>> =
        markersRemoteStorage.getStaticMarkerUpdates()

    override suspend fun insertUserMarkers(data: List<UserMarkerEntity>) =
        markersLocalStorage.refreshUserMarkers(data)

    override suspend fun insertStaticMarkers(data: List<MarkerEntity>) =
        markersLocalStorage.refreshStaticMarkers(data)

}