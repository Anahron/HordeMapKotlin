package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.LiveData
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerModel


interface GeoDataRepository {

    fun stopMarkerUpdates()

    fun startUserMarkerUpdates(): LiveData<List<MarkerModel>>

    fun startStaticMarkerUpdates(): LiveData<List<MarkerModel>>

    fun deleteStaticMarker(marker: Marker)
}


