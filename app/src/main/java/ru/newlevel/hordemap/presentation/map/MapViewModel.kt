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
import ru.newlevel.hordemap.app.getInputSteamFromUri
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.usecases.mapCases.CompassUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.CreateRouteUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadGameMapFromServerUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadLastGameMapUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LocationUpdatesUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveGameMapToFileUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.CreateGarminMarkersUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.CreateMarkersUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.GarminGPXParser
import ru.newlevel.hordemap.domain.usecases.markersCases.SendStaticMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StartMarkerUpdateUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StopMarkerUpdateUseCase
import java.io.InputStream
import kotlin.math.roundToInt

sealed class MapState {
    data object DefaultState : MapState()
    data object MarkersOffState : MapState()
    data object LoadingState : MapState()
}

class MapViewModel(
    private val deleteMarkerUseCase: DeleteMarkerUseCase,
    private val createMarkersUseCase: CreateMarkersUseCase,
    private val saveGameMapToFileUseCase: SaveGameMapToFileUseCase,
    private val loadLastGameMapUseCase: LoadLastGameMapUseCase,
    private val loadGameMapFromServerUseCase: LoadGameMapFromServerUseCase,
    private val sendStaticMarkerUseCase: SendStaticMarkerUseCase,
    private val stopMarkerUpdateUseCase: StopMarkerUpdateUseCase,
    private val startMarkerUpdateUseCase: StartMarkerUpdateUseCase,
    private val compassUseCase: CompassUseCase,
    private val createRouteUseCase: CreateRouteUseCase,
    private val locationUpdatesUseCase: LocationUpdatesUseCase,
    private val createGarminMarkersUseCase: CreateGarminMarkersUseCase,
    private val garminGPXParser: GarminGPXParser
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
        _isAutoLoadMap.value = UserEntityProvider.userEntity?.autoLoad
    }

    suspend fun parseGpx(
        inputStream: InputStream,
        markerCollection: MarkerManager.Collection,
        context: Context,
        googleMap: GoogleMap
    ) {
        val parse = garminGPXParser.parseGPX(inputStream)
        parse?.let {
            createGarminMarkersUseCase.createGarminMarkers(parse, markerCollection, context)
            parse.bounds?.let {
                polygon.value = googleMap.addPolygon(createGarminMarkersUseCase.createGarminBounds(parse))
            }
        }
    }

    fun startLocationUpdates() = locationUpdatesUseCase.startLocationUpdates()

    fun stopLocationUpdates() = locationUpdatesUseCase.stopLocationUpdates()
    fun compassActivate() {
        compassAngle = compassUseCase.startSensorEventListener()
    }

    fun compassDeActivate() {
        compassUseCase.stopSensorEventListener()
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
        return createRouteUseCase.execute(currentLatLng, destination, context)
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
        sendStaticMarkerUseCase.execute(latLng, description, checkedItem)
    }

    fun setUriForMap(uri: Uri) {
        _mapUri.postValue(uri)
    }

    fun cleanUriForMap() {
        _mapUri.postValue(null)
        polygon.value?.remove()
    }

    fun reCreateMarkers() {
        userMarkersLiveData.value = userMarkersLiveData.value
        staticMarkersLiveData.value = staticMarkersLiveData.value
    }

    fun setIsAutoLoadMap(boolean: Boolean) {
        _isAutoLoadMap.value = boolean
    }

    suspend fun saveGameMapToFile(uri: Uri, suffix: String): Uri = saveGameMapToFileUseCase.execute(uri, suffix)


    suspend fun getInputSteam(uri: Uri, context: Context): InputStream? {
        return getInputSteamFromUri(uri, context)
    }

    suspend fun loadMapFromServer(context: Context): Uri? {
        return loadGameMapFromServerUseCase.execute(context)
    }

    suspend fun loadLastGameMap(): Boolean {
        val uri = loadLastGameMapUseCase.execute()
        return if (uri != null) {
            _mapUri.postValue(uri)
            true
        } else false
    }

    fun turnToDefaultState() {
        state.value = MapState.DefaultState
    }

    fun showOrHideMarkers() {
        if (state.value is MapState.MarkersOffState)
            state.value = MapState.DefaultState
        else if (state.value is MapState.DefaultState)
            state.value = MapState.MarkersOffState
    }

    fun createUsersMarkers(
        it: List<MarkerDataModel>,
        markerCollection: MarkerManager.Collection
    ) {
        createMarkersUseCase.createUsersMarkers(it, markerCollection)
    }

    fun createStaticMarkers(
        it: List<MarkerDataModel>,
        markerCollection: MarkerManager.Collection
    ) {
        createMarkersUseCase.createStaticMarkers(it, markerCollection = markerCollection)
    }

    fun stopMarkerUpdates() {
        stopMarkerUpdateUseCase.execute()
    }

    fun startMarkerUpdates() {
        staticMarkersLiveData = startMarkerUpdateUseCase.startStaticUpdates()
        userMarkersLiveData = startMarkerUpdateUseCase.startUserUpdates()
    }

    fun deleteStaticMarker(marker: Marker) {
        deleteMarkerUseCase.execute(marker)
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("AAA", "MV marker stopped")
        stopMarkerUpdates()
    }
}