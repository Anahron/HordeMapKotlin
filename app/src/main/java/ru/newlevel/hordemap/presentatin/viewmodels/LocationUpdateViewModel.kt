package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.usecases.GetSessionLocationsUseCase
import ru.newlevel.hordemap.domain.usecases.LocationUpdatesUseCase

class LocationUpdateViewModel(
    private val getSessionLocationsUseCase: GetSessionLocationsUseCase,
    private val locationUpdatesUseCase: LocationUpdatesUseCase
) : ViewModel() {

    private val _trackItemCurrent = MutableLiveData<TrackItemDomainModel>()
    val trackItemCurrent: LiveData<TrackItemDomainModel> = _trackItemCurrent

    private val _trackItemAll = MutableLiveData<List<TrackItemDomainModel>>()
    val trackItemAll: LiveData<List<TrackItemDomainModel>> = _trackItemAll
    fun startLocationUpdates() = locationUpdatesUseCase.startLocationUpdates()

    fun stopLocationUpdates() = locationUpdatesUseCase.stopLocationUpdates()

    fun getCurrentSessionLocations(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemCurrent.postValue(getSessionLocationsUseCase.getCurrentSessionLocations(sessionId))
        }
    }

    fun getAllSessionsLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemAll.postValue(getSessionLocationsUseCase.getAllSessionLocations())
        }
    }
}
