package ru.newlevel.hordemap.data.repository

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.GeoDataStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class GeoDataRepositoryImpl(private val geoDataStorage: GeoDataStorage) : GeoDataRepository {

    override fun deleteStaticMarker(marker: Marker) {
        geoDataStorage.deleteStaticMarker(marker)
    }

    override fun sendCoordinates(markerModel: MarkerDataModel) {
       geoDataStorage.sendCoordinates(markerModel)
    }

    override fun startUserMarkerUpdates(): LiveData<List<MarkerDataModel>> {
        return  geoDataStorage.startUserMarkerUpdates()
    }

    override fun startStaticMarkerUpdates(): LiveData<List<MarkerDataModel>> {
        return geoDataStorage.startStaticMarkerUpdates()
    }

    override fun stopMarkerUpdates() {
        geoDataStorage.stopMarkerUpdates()
    }
}