package ru.newlevel.hordemap.data.repository

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class GeoDataRepositoryImpl(private val markersRemoteStorage: MarkersRemoteStorage) : GeoDataRepository {

    override fun deleteStaticMarker(key: String) {
        markersRemoteStorage.deleteStaticMarker(key)
    }

    override fun sendCoordinates(markerModel: MarkerDataModel) {
       markersRemoteStorage.sendUserMarker(markerModel)
    }

    override fun createStaticMarker(markerModel: MarkerDataModel) {
        markersRemoteStorage.sendStaticMarker(markerModel)
    }

    override fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        return  markersRemoteStorage.startUserMarkerUpdates()
    }

    override fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        return markersRemoteStorage.startStaticMarkerUpdates()
    }

    override fun stopMarkerUpdates() {
        markersRemoteStorage.stopMarkerUpdates()
    }


}