package ru.newlevel.hordemap.domain.usecases.mapCases

import ru.newlevel.hordemap.domain.repository.LocationRepository

class LocationUpdatesInteractor(private val locationRepository: LocationRepository) {
    fun startLocationUpdates() = locationRepository.startLocationUpdates()
    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()
}