package ru.newlevel.hordemap.domain.repository

import androidx.lifecycle.LiveData
import ru.newlevel.hordemap.data.models.MarkerModel


interface GeoDataRepository {

    fun stopMarkerUpdates() {
    }

    fun startMarkerUpdates(): LiveData<List<MarkerModel>>
}