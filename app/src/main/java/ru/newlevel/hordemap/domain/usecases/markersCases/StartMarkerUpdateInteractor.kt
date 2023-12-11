package ru.newlevel.hordemap.domain.usecases.markersCases

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class StartMarkerUpdateInteractor(private val geoDataRepository: GeoDataRepository) {
    fun startUserUpdates(): Flow<List<MarkerDataModel>> = geoDataRepository.startUserMarkerUpdates()

    fun startStaticUpdates(): Flow<List<MarkerDataModel>> = geoDataRepository.startStaticMarkerUpdates()
}