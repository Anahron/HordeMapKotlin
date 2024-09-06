package ru.newlevel.hordemap.presentation.map

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.Flow
import ru.newlevel.hordemap.data.db.MarkerEntity
import ru.newlevel.hordemap.domain.usecases.LogOutUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.MapUseCases

class MapViewModel(
    private val mapUseCases: MapUseCases,
    getUserSettingsUseCase: GetUserSettingsUseCase,
    private val logOutUseCase: LogOutUseCase
) : ViewModel() {

    //поток из рума
    val userMarkersFlowLocal: Flow<List<MarkerEntity>> = mapUseCases.startMarkerUpdateInteractor.getUsersMarkers()
    val staticMarkersFlowLocal: Flow<List<MarkerEntity>> = mapUseCases.startMarkerUpdateInteractor.getStaticMarkers()

    //поток из фаербейза
    val userMarkersFlow: Flow<List<MarkerEntity>> = mapUseCases.startMarkerUpdateInteractor.startUserUpdates()
    val staticMarkersFlow: Flow<List<MarkerEntity>> = mapUseCases.startMarkerUpdateInteractor.startStaticUpdates()

    val compassAngleFlow: Flow<Float> = mapUseCases.compassInteractor.getCompassData()

    private val _mapUri = MutableLiveData<Uri?>()
    val mapOverlayUri: LiveData<Uri?> = _mapUri

    private val _isAutoLoadMap = MutableLiveData<Boolean>()
    val isAutoLoadMap: LiveData<Boolean> = _isAutoLoadMap

    init {
        _isAutoLoadMap.value = getUserSettingsUseCase.execute().autoLoad
    }

    fun startLocationUpdates() = mapUseCases.locationUpdatesInteractor.startLocationUpdates()
    fun stopLocationUpdates() = mapUseCases.locationUpdatesInteractor.stopLocationUpdates()

    fun sendMarker(latLng: LatLng, description: String, checkedItem: Int) {
        mapUseCases.sendStaticMarkerUseCase.execute(latLng, description, checkedItem)
    }
    suspend fun deleteUserFromOldGroup(userGroup: Int){
        logOutUseCase.execute(userGroup)
    }
    fun cleanUriForMap() {
        _mapUri.postValue(null)
    }

    fun setIsAutoLoadMap(boolean: Boolean) {
        _isAutoLoadMap.value = boolean
    }

    suspend fun saveGameMapToFile(uri: Uri, context: Context): Throwable? {
        val result =  mapUseCases.saveGameMapToFileUseCase.execute(uri, context)
        result.onSuccess {
            _mapUri.postValue(it)
        }
        return result.exceptionOrNull()
    }

    suspend fun loadMapFromServer(context: Context): Throwable? {
        val result = mapUseCases.loadGameMapFromServerUseCase.execute(context)
        result.onSuccess {
            _mapUri.postValue(it)
        }
        return result.exceptionOrNull()
    }

    fun loadLastGameMap(context: Context): Throwable? {
        val result = mapUseCases.loadLastGameMapUseCase.execute(context = context)
        result.onSuccess {
            _mapUri.postValue(it)
        }
        return result.exceptionOrNull()
    }

    suspend fun insertUserMarkersToLocalDB(data: List<MarkerEntity>) {
        mapUseCases.insetMarkersToDBIterator.insertUserMarkers(data)
    }

    suspend fun insertStaticMarkersToLocalDB(data: List<MarkerEntity>) {
        mapUseCases.insetMarkersToDBIterator.insertStaticMarkers(data)
    }
}