package ru.newlevel.hordemap.data.storage.interfaces

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface MarkersRemoteStorage {
    fun deleteStaticMarker(key: String)

    fun sendUserMarker(markerModel: MarkerDataModel)

    fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun stopMarkerUpdates()

    fun sendStaticMarker(markerModel: MarkerDataModel)
}