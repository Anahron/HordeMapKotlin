package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.repository.LocationRepository

class LocationUpdateViewModel(
    private val locationRepository: LocationRepository
    ) : ViewModel() {

    fun startLocationUpdates() = locationRepository.startLocationUpdates()

    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()
}
