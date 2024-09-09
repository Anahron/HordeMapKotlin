package ru.newlevel.hordemap.domain.usecases.markersCases

import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class DeleteMarkerUseCase(private val geoDataRepository: GeoDataRepository) {
    suspend fun execute(marker: Marker){
        geoDataRepository.deleteStaticMarker(marker.tag.toString())
    }
}