package ru.newlevel.hordemap.domain.usecases

import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class DeleteMarkerUseCase(private val geoDataRepository: GeoDataRepository) {
    fun execute(marker: Marker){
        geoDataRepository.deleteStaticMarker(marker)
    }
}