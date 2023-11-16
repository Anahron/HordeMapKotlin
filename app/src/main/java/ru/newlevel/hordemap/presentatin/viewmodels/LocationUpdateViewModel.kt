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
import ru.newlevel.hordemap.domain.usecases.RenameTrackNameForSessionUseCase
import ru.newlevel.hordemap.domain.usecases.SetFavouriteTrackForSessionUseCase

class LocationUpdateViewModel(
    private val getSessionLocationsUseCase: GetSessionLocationsUseCase,
    private val deleteSessionLocationUseCase: DeleteSessionLocationUseCase,
    private val renameTrackNameForSessionUseCase: RenameTrackNameForSessionUseCase,
    private val locationUpdatesUseCase: LocationUpdatesUseCase,
    private val setFavouriteTrackForSessionUseCase: SetFavouriteTrackForSessionUseCase
) : ViewModel() {

    private val _trackItemCurrent = MutableLiveData<TrackItemDomainModel>()
    val trackItemCurrent: LiveData<TrackItemDomainModel> = _trackItemCurrent

    private val _trackItemAll = MutableLiveData<List<TrackItemDomainModel>?>()
    val trackItemAll: LiveData<List<TrackItemDomainModel>?> = _trackItemAll
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

    fun setFavouriteTrackForSession(sessionId: String, isFavourite: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            setFavouriteTrackForSessionUseCase.execute(sessionId, isFavourite)
        }
        val currentList = trackItemAll.value?.toMutableList()
        val trackItem = currentList?.find { it.sessionId == sessionId }
        trackItem?.let {
            val updatedItem = it.copy(isFavourite = isFavourite)
            val index = currentList.indexOf(it)

            if (index != -1) {
                currentList[index] = updatedItem
            }
        }
        _trackItemAll.value = currentList
    }

    fun renameTrackNameForSession(sessionId: String, newTrackName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            renameTrackNameForSessionUseCase.execute(
                sessionId = sessionId,
                newTrackName = newTrackName
            )
        }
        val currentList = trackItemAll.value?.toMutableList()
        val trackItem = currentList?.find { it.sessionId == sessionId }
        trackItem?.let {
            val updatedItem = it.copy(title = newTrackName)
            val index = currentList.indexOf(it)

            if (index != -1) {
                currentList[index] = updatedItem
            }
        }
        _trackItemAll.value = currentList
    }

    fun deleteSessionLocations(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deleteSessionLocationUseCase.execute(sessionId)
        }

        val currentList = trackItemAll.value?.toMutableList()
        currentList?.removeAll {
            it.sessionId == sessionId
        }
        _trackItemAll.value = currentList
    }

    fun getAllSessionsLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemAll.postValue(getSessionLocationsUseCase.getAllSessionLocations())
        }
    }

    fun sortByDate() {
        _trackItemAll.value =
            trackItemAll.value?.sortedWith(compareByDescending<TrackItemDomainModel> { it.isFavourite }
                .thenByDescending { it.timestamp })
    }

    fun sortByDistance() {
        _trackItemAll.value =
            trackItemAll.value?.sortedWith(compareByDescending<TrackItemDomainModel> { it.isFavourite }
                .thenByDescending { it.distanceMeters })
    }

    fun sortByDuration() {
        _trackItemAll.value =
            trackItemAll.value?.sortedWith(compareByDescending<TrackItemDomainModel> { it.isFavourite }
                .thenByDescending { it.durationLong })
    }
}
