package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface GeoDataRepository {

    fun stopMarkerUpdates()

    fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun deleteStaticMarker(marker: Marker)

    fun sendCoordinates(markerModel: MarkerDataModel)

    fun createStaticMarker(markerModel: MarkerDataModel)
}


