package ru.newlevel.hordemap.data.repository

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class GeoDataRepositoryImpl(private val markersRemoteStorage: MarkersRemoteStorage) : GeoDataRepository {

    override fun deleteStaticMarker(key: String) = markersRemoteStorage.deleteStaticMarker(key)

    override fun sendCoordinates(markerModel: MarkerEntity) = markersRemoteStorage.sendUserMarker(markerModel)

    override fun createStaticMarker(markerModel: MarkerEntity) = markersRemoteStorage.sendStaticMarker(markerModel)

    override fun startUserMarkerUpdates(): Flow<List<MarkerEntity>> = markersRemoteStorage.getUserMarkerUpdates()

    override fun startStaticMarkerUpdates(): Flow<List<MarkerEntity>> = markersRemoteStorage.getStaticMarkerUpdates()

}