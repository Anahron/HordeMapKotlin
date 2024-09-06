package ru.newlevel.hordemap.domain.usecases.markersCases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.newlevel.hordemap.app.mapToMarkerEntity
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class StartMarkerUpdateInteractor(private val geoDataRepository: GeoDataRepository) {
    fun startUserUpdates(): Flow<List<MarkerEntity>> = geoDataRepository.startUserMarkerUpdates()
    
    fun getUsersMarkers(): Flow<List<MarkerEntity>> = geoDataRepository.startUserMarkerUpdatesLocal().map { data ->
        data.map { it.mapToMarkerEntity() }
    }
    fun startStaticUpdates(): Flow<List<MarkerEntity>> = geoDataRepository.startStaticMarkerUpdates()

    fun getStaticMarkers(): Flow<List<MarkerEntity>> = geoDataRepository.startStaticMarkerUpdatesLocal()
}