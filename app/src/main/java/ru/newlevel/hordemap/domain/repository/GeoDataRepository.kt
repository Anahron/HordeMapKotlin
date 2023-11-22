package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface GeoDataRepository {

    fun stopMarkerUpdates()

    fun startUserMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): MutableLiveData<List<MarkerDataModel>>

    fun deleteStaticMarker(key: String)

    fun sendCoordinates(markerModel: MarkerDataModel)

    fun createStaticMarker(markerModel: MarkerDataModel)
}


