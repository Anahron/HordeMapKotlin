package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.default
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.usecases.DeleteAllTracksUseCase
import ru.newlevel.hordemap.domain.usecases.DeleteSessionLocationUseCase
import ru.newlevel.hordemap.domain.usecases.GetSessionLocationsUseCase
import ru.newlevel.hordemap.domain.usecases.RenameTrackNameForSessionUseCase
import ru.newlevel.hordemap.domain.usecases.SaveCurrentTrackUseCase
import ru.newlevel.hordemap.domain.usecases.SetFavouriteTrackForSessionUseCase

enum class SortState {
    DATA_SORT, DURATION_SORT, DISTANCE_SORT
}

class TracksViewModel(
    private val getSessionLocationsUseCase: GetSessionLocationsUseCase,
    private val deleteSessionLocationUseCase: DeleteSessionLocationUseCase,
    private val renameTrackNameForSessionUseCase: RenameTrackNameForSessionUseCase,
    private val setFavouriteTrackForSessionUseCase: SetFavouriteTrackForSessionUseCase,
    private val saveCurrentTrackUseCase: SaveCurrentTrackUseCase,
    private val deleteAllTracksUseCase: DeleteAllTracksUseCase
) : ViewModel() {

    private val _trackItemCurrent = MutableLiveData<TrackItemDomainModel>()
    val trackItemCurrent: LiveData<TrackItemDomainModel> = _trackItemCurrent

    private val _trackItemAll = MutableLiveData<List<TrackItemDomainModel>?>()
    val trackItemAll: LiveData<List<TrackItemDomainModel>?> = _trackItemAll

    private val _trackSortState = MutableLiveData<SortState>().default(SortState.DATA_SORT)
    val trackSortState: LiveData<SortState> = _trackSortState

    suspend fun saveCurrentTrack(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            saveCurrentTrackUseCase.execute(sessionId)
        }.join()
        deleteSessionLocations(sessionId)
        getAllSessionsLocations()
        getCurrentSessionLocations(UserEntityProvider.sessionId.toString())
    }

    suspend fun deleteAllTracks() {
        CoroutineScope(Dispatchers.IO).launch {
            deleteAllTracksUseCase.execute()
        }.join()
        getAllSessionsLocations()
    }

    fun getCurrentSessionLocations(sessionId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemCurrent.postValue(
                getSessionLocationsUseCase.getCurrentSessionLocations(
                    sessionId
                )
            )
        }
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
            setFavouriteTrackForSessionUseCase.execute(
                sessionId,
                isFavourite,
                trackItemAll.value
            )
        )
    }

    fun renameTrackNameForSession(sessionId: String, newTrackName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            _trackItemAll.postValue(
                renameTrackNameForSessionUseCase.execute(
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
                deleteSessionLocationUseCase.execute(
                    sessionId,
                    trackItemAll.value
                )
            )
        }
    }

    fun getAllSessionsLocations() {
        CoroutineScope(Dispatchers.IO).launch {
            val allSessionLocationsList = getSessionLocationsUseCase.getAllSessionLocations()
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
