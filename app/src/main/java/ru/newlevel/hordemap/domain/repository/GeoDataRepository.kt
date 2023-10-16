package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface GeoDataRepository {

    fun stopMarkerUpdates()

    fun startUserMarkerUpdates(): LiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): LiveData<List<MarkerDataModel>>

    fun deleteStaticMarker(marker: Marker)

    fun sendCoordinates(markerModel: MarkerDataModel)

    fun createStaticMarker(markerModel: MarkerDataModel)
}


