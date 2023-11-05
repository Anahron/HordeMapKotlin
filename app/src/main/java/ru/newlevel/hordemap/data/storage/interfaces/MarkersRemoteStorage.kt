package ru.newlevel.hordemap.data.storage.interfaces

import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface MarkersRemoteStorage {
    fun deleteStaticMarker(marker: Marker)

    fun sendUserMarker(markerModel: MarkerDataModel)

    fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun stopMarkerUpdates()

    fun sendStaticMarker(markerModel: MarkerDataModel)
}