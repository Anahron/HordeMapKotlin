package ru.newlevel.hordemap.domain.usecases

import ru.newlevel.hordemap.domain.repository.LocationRepository

class LocationUpdatesUseCase(private val locationRepository: LocationRepository) {
    fun startLocationUpdates() = locationRepository.startLocationUpdates()
    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()
}