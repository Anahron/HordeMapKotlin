package ru.newlevel.hordemap.domain.usecases.markersCases

import androidx.lifecycle.MutableLiveData
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class StartMarkerUpdateUseCase(private val geoDataRepository: GeoDataRepository) {
    fun startUserUpdates(): MutableLiveData<List<MarkerDataModel>> {
        return geoDataRepository.startUserMarkerUpdates()
    }

    fun startStaticUpdates(): MutableLiveData<List<MarkerDataModel>> {
       return geoDataRepository.startStaticMarkerUpdates()
    }
}