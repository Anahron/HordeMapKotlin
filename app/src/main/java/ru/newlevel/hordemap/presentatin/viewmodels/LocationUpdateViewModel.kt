package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.usecases.DeleteSessionLocationUseCase
import ru.newlevel.hordemap.domain.usecases.GetSessionLocationsUseCase
import ru.newlevel.hordemap.domain.usecases.LocationUpdatesUseCase

class LocationUpdateViewModel(
    private val getSessionLocationsUseCase: GetSessionLocationsUseCase,
    private val deleteSessionLocationUseCase: DeleteSessionLocationUseCase,
    private val locationUpdatesUseCase: LocationUpdatesUseCase
) : ViewModel() {

    private val _trackItemCurrent = MutableLiveData<TrackItemDomainModel>()
    val trackItemCurrent: LiveData<TrackItemDomainModel> = _trackItemCurrent

    private val _trackItemAll = MutableLiveData<List<TrackItemDomainModel>?>()
    val trackItemAll: MutableLiveData<List<TrackItemDomainModel>?> = _trackItemAll
    fun startLocationUpdates() = locationUpdatesUseCase.startLocationUpdates()

    fun stopLocationUpdates() = locationUpdatesUseCase.stopLocationUpdates()

    fun getCurrentSessionLocations(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemCurrent.postValue(
                getSessionLocationsUseCase.getCurrentSessionLocations(
                    sessionId
                )
            )
        }
    }

    fun deleteSessionLocations(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteSessionLocationUseCase.execute(sessionId)
        }

        val currentList = trackItemAll.value?.toMutableList()
        currentList?.removeAll { trackItem -> trackItem.title == sessionId }
        _trackItemAll.value = currentList
    }

    fun getAllSessionsLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemAll.postValue(getSessionLocationsUseCase.getAllSessionLocations())
        }
    }

    fun sortByDate() {
        _trackItemAll.value = trackItemAll.value?.sortedByDescending { it.timestamp }
    }

    fun sortByDistance() {
        _trackItemAll.value = trackItemAll.value?.sortedByDescending { it.distanceMeters }
    }

    fun sortByDuration() {
        _trackItemAll.value = trackItemAll.value?.sortedByDescending { it.durationLong }
    }
}
