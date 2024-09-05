package ru.newlevel.hordemap.domain.usecases.markersCases

import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class StartMarkerUpdateInteractor(private val geoDataRepository: GeoDataRepository) {
    fun startUserUpdates(): Flow<List<MarkerEntity>> = geoDataRepository.startUserMarkerUpdates()
    
    fun getUsersMarkers(){
        
    }
    fun startStaticUpdates(): Flow<List<MarkerEntity>> = geoDataRepository.startStaticMarkerUpdates()

    fun getStaticMarkers(){

    }
}