package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.*
import com.google.maps.android.SphericalUtil
import com.google.maps.android.collections.MarkerManager
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.getInputSteamFromUri
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.usecases.*
import java.io.InputStream
import kotlin.math.roundToInt

const val REQUEST_CODE_PICK_KMZ_FILE = 100

class MapViewModel(
    private val deleteMarkerUseCase: DeleteMarkerUseCase,
    private val createMarkersUseCase: CreateMarkersUseCase,
    private val hideMarkersUserCase: HideMarkersUseCase,
    private val showMarkersUseCase: ShowMarkersUseCase,
    private val saveGameMapToFileUseCase: SaveGameMapToFileUseCase,
    private val loadLastGameMapUseCase: LoadLastGameMapUseCase,
    private val loadGameMapFromServerUseCase: LoadGameMapFromServerUseCase,
    private val createStaticMarkerUseCase: CreateStaticMarkerUseCase,
    private val stopMarkerUpdateUseCase: StopMarkerUpdateUseCase,
    private val startMarkerUpdateUseCase: StartMarkerUpdateUseCase
) : ViewModel() {

    private var routePolyline: Polyline? = null
    private var destination: LatLng? = null

    lateinit var userMarkersLiveData: LiveData<List<MarkerDataModel>>

    lateinit var staticMarkersLiveData: LiveData<List<MarkerDataModel>>

    private var _isShowMarkers = MutableLiveData<Boolean>()
    val isShowMarkers: LiveData<Boolean> = _isShowMarkers

    private val _distanceText = MutableLiveData<String>()
    val distanceText: LiveData<String> = _distanceText

    private val _kmzUri = MutableLiveData<Uri?>()
    val kmzUri: LiveData<Uri?> = _kmzUri

    private var _isAutoLoadMap = MutableLiveData<Boolean>()
    val isAutoLoadMap: LiveData<Boolean> = _isAutoLoadMap

    init {
        _isShowMarkers.value = true
        _isAutoLoadMap.value = UserEntityProvider.userEntity?.autoLoad
    }

    fun getDestination(): LatLng? {
        return destination
    }

    fun setDestination(destination: LatLng) {
        this.destination = destination
    }

    fun getRoutePolyline(): Polyline? {
        return routePolyline
    }

    fun setRoutePolyline(polyline: Polyline) {
        routePolyline = polyline
    }


    fun createRoute(currentLatLng: LatLng, destination: LatLng, context: Context): PolylineOptions {
        val bitmapcustomcap =
            BitmapFactory.decodeResource(context.resources, R.drawable.star)
        val bitmapcustomcapicon = BitmapDescriptorFactory.fromBitmap(
            Bitmap.createScaledBitmap(
                bitmapcustomcap,
                60,
                60,
                false
            )
        )
        val customCap = CustomCap(bitmapcustomcapicon)
        return PolylineOptions().addAll(listOf(currentLatLng, destination)).endCap(customCap)
            .geodesic(true).color(Color.BLUE).width(6f)
    }

    fun removeRoute() {
        routePolyline = null
    }

    fun updateRoute(currentLatLng: LatLng, destination: LatLng) {
        val distance = SphericalUtil.computeDistanceBetween(currentLatLng, destination)
        routePolyline?.points ?: listOf(currentLatLng, destination)
        _distanceText.postValue(
            if (distance.toInt() > 1000) ((distance / 10).roundToInt() / 100.0).toString() + " км." else distance.toInt()
                .toString() + " м."
        )
    }


    fun sendMarker(latLng: LatLng, description: String, checkedItem: Int) {
        createStaticMarkerUseCase.execute(latLng, description, checkedItem)
    }

    fun setUriForMap(uri: Uri) {
        _kmzUri.value = uri
    }

    fun setIsAutoLoadMap(boolean: Boolean) {
        _isAutoLoadMap.value = boolean
    }

    suspend fun saveGameMapToFile(uri: Uri) {
        saveGameMapToFileUseCase.execute(uri)
    }

    suspend fun getInputSteam(uri: Uri, context: Context): InputStream? {
        return getInputSteamFromUri(uri, context)
    }

    suspend fun loadMapFromServer(context: Context): Boolean {
        val uri = loadGameMapFromServerUseCase.execute(context)
        return if (uri != null) {
            setUriForMap(uri)
            true
        } else false
    }

    fun loadGameMapFromFiles(fragment: Fragment) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/vnd.google-earth.kmz"
        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_KMZ_FILE)
    }

    suspend fun loadLastGameMap(): Boolean {
        val uri = loadLastGameMapUseCase.execute()
        return if (uri != null) {
            _kmzUri.value = uri
            true
        } else false
    }

    fun showOrHideMarkers() {
        if (_isShowMarkers.value == true)
            _isShowMarkers.value = hideMarkersUserCase.execute()
        else
            _isShowMarkers.value = showMarkersUseCase.execute()
    }

    fun createUsersMarkers(it: List<MarkerDataModel>, markerCollection: MarkerManager.Collection) {
        if (_isShowMarkers.value == true)
            createMarkersUseCase.createUsersMarkers(it, markerCollection)
    }

    fun createStaticMarkers(it: List<MarkerDataModel>, markerCollection: MarkerManager.Collection) {
        if (_isShowMarkers.value == true)
            createMarkersUseCase.createStaticMarkers(it, markerCollection)
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