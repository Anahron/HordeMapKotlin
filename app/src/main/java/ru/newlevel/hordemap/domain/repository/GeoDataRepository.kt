package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.repository.GeoDataRepositoryImpl
import ru.newlevel.hordemap.data.storage.FirebaseStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel

interface GeoDataRepository {

    fun stopMarkerUpdates()

    fun startUserMarkerUpdates(): LiveData<List<MarkerDataModel>>

    fun startStaticMarkerUpdates(): LiveData<List<MarkerDataModel>>

    fun deleteStaticMarker(marker: Marker)

    fun sendCoordinates(markerModel: MarkerDataModel)

    companion object {
        @Volatile private var INSTANCE: GeoDataRepository? = null
        fun getInstance(): Any {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GeoDataRepositoryImpl(
                    geoDataStorage = FirebaseStorage()
                )
                    .also { INSTANCE = it }
            }
        }
    }
}


