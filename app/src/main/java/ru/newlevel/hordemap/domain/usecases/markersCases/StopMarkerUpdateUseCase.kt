package ru.newlevel.hordemap.domain.usecases.markersCases

import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class StopMarkerUpdateUseCase(private val geoDataRepository: GeoDataRepository) {
    fun execute() {
        geoDataRepository.stopMarkerUpdates()
    }
}