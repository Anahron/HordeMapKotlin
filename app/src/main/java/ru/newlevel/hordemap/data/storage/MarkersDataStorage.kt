package ru.newlevel.hordemap.data.storage

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface MarkersDataStorage {
    fun deleteStaticMarker(marker: Marker)

    fun sendCoordinates(markerModel: MarkerDataModel)

    fun startUserMarkerUpdates(): LiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): LiveData<List<MarkerDataModel>>

    fun stopMarkerUpdates()

    fun createStaticMarker(markerModel: MarkerDataModel)
}