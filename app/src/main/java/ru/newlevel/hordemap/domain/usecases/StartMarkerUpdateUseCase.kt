package ru.newlevel.hordemap.domain.usecases

import androidx.lifecycle.LiveData
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class StartMarkerUpdateUseCase(private val geoDataRepository: GeoDataRepository) {
    fun startUserUpdates(): LiveData<List<MarkerDataModel>> {
        return   geoDataRepository.startUserMarkerUpdates()
    }

    fun startStaticUpdates():LiveData<List<MarkerDataModel>>  {
       return geoDataRepository.startStaticMarkerUpdates()
    }
}