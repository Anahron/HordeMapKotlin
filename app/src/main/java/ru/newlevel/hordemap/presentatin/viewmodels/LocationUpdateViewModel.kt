package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.usecases.GetSessionLocationsUseCase
import ru.newlevel.hordemap.domain.usecases.LocationUpdatesUseCase

class LocationUpdateViewModel(private val getSessionLocationsUseCase: GetSessionLocationsUseCase, private val locationUpdatesUseCase: LocationUpdatesUseCase) : ViewModel() {

    fun startLocationUpdates() = locationUpdatesUseCase.startLocationUpdates()

    fun stopLocationUpdates() = locationUpdatesUseCase.stopLocationUpdates()

    fun getCurrentSessionLocations(sessionId: String): TrackItemDomainModel {
       return getSessionLocationsUseCase.getCurrentSessionLocations(sessionId)
    }

    fun getAllSessionsLocations(){

    }
}
