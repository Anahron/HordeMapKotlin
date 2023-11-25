package ru.newlevel.hordemap.presentation.map

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.collections.MarkerManager
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.MapUseCases
import ru.newlevel.hordemap.presentation.map.utils.MarkersUtils
import kotlin.math.roundToInt

sealed class MapState {
    data object DefaultState : MapState()
    data object MarkersOffState : MapState()
    data object LoadingState : MapState()
}

class MapViewModel(
    private val mapUseCases: MapUseCases,
    private val markersUtils: MarkersUtils,
    getUserSettingsUseCase: GetUserSettingsUseCase,
) : ViewModel() {

    val state = MutableLiveData<MapState>().apply { value = MapState.LoadingState }

    private var routePolyline: Polyline? = null
    private var destination: LatLng? = null

    lateinit var userMarkersLiveData: MutableLiveData<List<MarkerDataModel>>
    lateinit var staticMarkersLiveData: MutableLiveData<List<MarkerDataModel>>
    lateinit var compassAngle: LiveData<Float>

    private val _distanceText = MutableLiveData<String>()
    val distanceText: LiveData<String> = _distanceText

    private val _mapUri = MutableLiveData<Uri?>()
    val mapUri: LiveData<Uri?> = _mapUri

    private var _isAutoLoadMap = MutableLiveData<Boolean>()
    val isAutoLoadMap: LiveData<Boolean> = _isAutoLoadMap

    val polygon: MutableLiveData<Polygon> = MutableLiveData<Polygon>()

    init {
        _isAutoLoadMap.value = getUserSettingsUseCase.execute().autoLoad
    }

    suspend fun createGpxLayer(
        uri: Uri, markerCollection: MarkerManager.Collection, context: Context, googleMap: GoogleMap
    ) {
        Log.e("AAA", "fun createGpxLayer ")
        polygon.value = markersUtils.createGpxLayer(
            uri, markerCollection, context)?.let { googleMap.addPolygon(it) }
    }

    fun startLocationUpdates() = mapUseCases.locationUpdatesInteractor.startLocationUpdates()

    fun stopLocationUpdates() = mapUseCases.locationUpdatesInteractor.stopLocationUpdates()
    fun compassActivate() {
        compassAngle = mapUseCases.compassInteractor.startSensorEventListener()
    }

    fun compassDeActivate() {
        mapUseCases.compassInteractor.stopSensorEventListener()
    }

    fun setRoutePolyline(polyline: Polyline) {
        routePolyline = polyline
    }

    fun isRoutePolylineNotNull(): Boolean {
        return routePolyline != null
    }

    fun createRoute(currentLatLng: LatLng, destination: LatLng, context: Context): PolylineOptions {
        removeRoute()
        this.destination = destination
        setDistanceText(currentLatLng, destination)
        return mapUseCases.createRouteUseCase.execute(currentLatLng, destination, context)
    }

    fun createRoute(listLatLng: List<LatLng>): PolylineOptions {
        removeRoute()
        return PolylineOptions().apply {
            color(Color.RED)
            width(15f)
        }.addAll(listLatLng)
    }

    fun removeRoute() {
        routePolyline?.remove()
        routePolyline = null
    }

    fun updateRoute(currentLatLng: LatLng) {
        if (destination != null && routePolyline != null) {
            routePolyline?.points ?: listOf(currentLatLng, destination)
            setDistanceText(currentLatLng, destination)
        }
    }

    private fun setDistanceText(currentLatLng: LatLng, destination: LatLng?) {
        val distance = SphericalUtil.computeDistanceBetween(currentLatLng, destination)
        _distanceText.postValue(
            if (distance.toInt() > 1000) ((distance / 10).roundToInt() / 100.0).toString() + " км." else distance.toInt()
                .toString() + " м."
        )
    }

    fun sendMarker(latLng: LatLng, description: String, checkedItem: Int) {
        mapUseCases.sendStaticMarkerUseCase.execute(latLng, description, checkedItem)
    }

//    fun setUriForMap(uri: Uri) {
//        _mapUri.postValue(uri)
//    }

    fun cleanUriForMap() {
        _mapUri.postValue(null)
    }

    fun reCreateMarkers() {
        userMarkersLiveData.value = userMarkersLiveData.value
        staticMarkersLiveData.value = staticMarkersLiveData.value
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

    fun turnToDefaultState() {
        state.value = MapState.DefaultState
    }

    fun showOrHideMarkers() {
        if (state.value is MapState.MarkersOffState) state.value = MapState.DefaultState
        else if (state.value is MapState.DefaultState) state.value = MapState.MarkersOffState
    }

    fun createUsersMarkers(
        it: List<MarkerDataModel>, markerCollection: MarkerManager.Collection, context: Context
    ) {
        markersUtils.createUsersMarkers(it, markerCollection = markerCollection, context = context)
    }

    fun createStaticMarkers(
        it: List<MarkerDataModel>, markerCollection: MarkerManager.Collection, context: Context
    ) {
        markersUtils.createStaticMarkers(it, markerCollection = markerCollection, context = context)
    }

    fun stopMarkerUpdates() {
        mapUseCases.stopMarkerUpdateInteractor.execute()
    }

    fun startMarkerUpdates() {
        staticMarkersLiveData = mapUseCases.startMarkerUpdateInteractor.startStaticUpdates()
        userMarkersLiveData = mapUseCases.startMarkerUpdateInteractor.startUserUpdates()
    }

    fun deleteStaticMarker(marker: Marker) {
        mapUseCases.deleteMarkerUseCase.execute(marker)
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("AAA", "MV marker stopped")
        stopMarkerUpdates()
    }
}