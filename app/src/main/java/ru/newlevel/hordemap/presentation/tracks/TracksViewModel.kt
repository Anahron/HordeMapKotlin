package ru.newlevel.hordemap.presentation.tracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.default
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.usecases.tracksCases.TracksUseCases

enum class SortState {
    DATA_SORT, DURATION_SORT, DISTANCE_SORT
}

class TracksViewModel(
    private val tracksUseCases: TracksUseCases
) : ViewModel() {

   val currentTrack: LiveData<TrackItemDomainModel> = tracksUseCases.getSessionLocationsUseCase.getCurrentSessionLocationsLiveData(UserEntityProvider.sessionId.toString())

    private val _trackItemAll = MutableLiveData<List<TrackItemDomainModel>?>()
    val trackItemAll: LiveData<List<TrackItemDomainModel>?> = _trackItemAll

    private val _trackSortState = MutableLiveData<SortState>().default(SortState.DATA_SORT)
    val trackSortState: LiveData<SortState> = _trackSortState

    suspend fun saveCurrentTrack(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            tracksUseCases.saveCurrentTrackUseCase.execute(sessionId)
        }.join()
        deleteSessionLocations(sessionId)
        getAllSessionsLocations()
    }

    suspend fun deleteAllTracks() {
        CoroutineScope(Dispatchers.IO).launch {
            tracksUseCases.deleteAllTracksUseCase.execute()
        }.join()
        getAllSessionsLocations()
    }

    fun setCheckedSortButton(checkedId: Int) {
        when (checkedId) {
            R.id.btnDistance -> _trackSortState.value = SortState.DISTANCE_SORT
            R.id.btnDuration -> _trackSortState.value = SortState.DURATION_SORT
            else -> _trackSortState.value = SortState.DATA_SORT
        }
    }

    fun setFavouriteTrackForSession(sessionId: String, isFavourite: Boolean) {
        _trackItemAll.postValue(
            tracksUseCases.setFavouriteTrackForSessionUseCase.execute(
                sessionId,
                isFavourite,
                trackItemAll.value
            )
        )
    }

    fun renameTrackNameForSession(sessionId: String, newTrackName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemAll.postValue(
                tracksUseCases.renameTrackNameForSessionUseCase.execute(
                    sessionId = sessionId,
                    newTrackName = newTrackName,
                    trackItemAll.value
                )
            )
        }
    }

    fun deleteSessionLocations(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemAll.postValue(
                tracksUseCases.deleteSessionLocationUseCase.execute(
                    sessionId,
                    trackItemAll.value
                )
            )
        }
    }

    fun getAllSessionsLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            val allSessionLocationsList = tracksUseCases.getSessionLocationsUseCase.getAllSessionLocations()
            withContext(Dispatchers.Main) {
                _trackItemAll.value = allSessionLocationsList
            }
        }
    }

    fun sortByDate() {
        CoroutineScope(Dispatchers.Main).launch {
            _trackItemAll.value =
                trackItemAll.value?.sortedWith(compareByDescending<TrackItemDomainModel> { it.isFavourite }
                    .thenByDescending { it.timestamp })
        }
    }

    fun sortByDistance() {
        CoroutineScope(Dispatchers.Main).launch {
            _trackItemAll.value =
                trackItemAll.value?.sortedWith(compareByDescending<TrackItemDomainModel> { it.isFavourite }
                    .thenByDescending { it.distanceMeters })
        }
    }

    fun sortByDuration() {
        CoroutineScope(Dispatchers.Main).launch {
            _trackItemAll.value =
                trackItemAll.value?.sortedWith(compareByDescending<TrackItemDomainModel> { it.isFavourite }
                    .thenByDescending { it.durationLong })
        }
    }
}
