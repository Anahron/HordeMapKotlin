package ru.newlevel.hordemap.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.MarkersDataStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class GeoDataRepositoryImpl(private val markersDataStorage: MarkersDataStorage) : GeoDataRepository {

    override fun deleteStaticMarker(marker: Marker) {
        markersDataStorage.deleteStaticMarker(marker)
    }

    override fun sendCoordinates(markerModel: MarkerDataModel) {
       markersDataStorage.sendCoordinates(markerModel)
    }

    override fun createStaticMarker(markerModel: MarkerDataModel) {
        markersDataStorage.createStaticMarker(markerModel)
    }

    override fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        return  markersDataStorage.startUserMarkerUpdates()
    }

    override fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>> {
        return markersDataStorage.startStaticMarkerUpdates()
    }

    override fun stopMarkerUpdates() {
        markersDataStorage.stopMarkerUpdates()
    }


}