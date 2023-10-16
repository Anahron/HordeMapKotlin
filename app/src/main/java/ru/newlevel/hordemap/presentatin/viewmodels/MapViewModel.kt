package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.app.getInputSteamFromUri
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.usecases.*
import java.io.InputStream

const val REQUEST_CODE_PICK_KMZ_FILE = 100

class MapViewModel(
    private val geoDataRepository: GeoDataRepository,
    private val deleteMarkerUseCase: DeleteMarkerUseCase,
    private val createMarkersUseCase: CreateMarkersUseCase,
    private val hideMarkersUserCase: HideMarkersUseCase,
    private val showMarkersUseCase: ShowMarkersUseCase,
    private val saveGameMapToFileUseCase: SaveGameMapToFileUseCase,
    private val loadLastGameMapUseCase: LoadLastGameMapUseCase,
    private val loadGameMapFromServerUseCase: LoadGameMapFromServerUseCase,
    private val createStaticMarkerUseCase: CreateStaticMarkerUseCase
) : ViewModel() {

    lateinit var userMarkersLiveData: LiveData<List<MarkerDataModel>>

    lateinit var staticMarkersLiveData: LiveData<List<MarkerDataModel>>

    private var _isShowMarkers = MutableLiveData<Boolean>()
    val isShowMarkers: LiveData<Boolean> = _isShowMarkers

    private var _kmzUri = MutableLiveData<Uri?>()
    val kmzUri: LiveData<Uri?> = _kmzUri

    private var _isAutoLoadMap = MutableLiveData<Boolean>()
    val isAutoLoadMap: LiveData<Boolean> = _isAutoLoadMap

    init {
        _isShowMarkers.value = true
        _isAutoLoadMap.value = UserEntityProvider.userEntity?.autoLoad
    }

    fun sendMarker(latLng: LatLng, description: String, checkedItem: Int) {
        createStaticMarkerUseCase.execute(latLng,description, checkedItem)
    }

    fun setUriForMap(uri: Uri) {
        _kmzUri.value = uri
    }

    fun setIsAutoLoadMap(boolean: Boolean){
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

    fun createUsersMarkers(it: List<MarkerDataModel>, googleMap: GoogleMap) {
        if (_isShowMarkers.value == true)
            createMarkersUseCase.createUsersMarkers(it, googleMap)
    }

    fun createStaticMarkers(it: List<MarkerDataModel>, googleMap: GoogleMap) {
        if (_isShowMarkers.value == true)
            createMarkersUseCase.createStaticMarkers(it, googleMap)
    }

    fun stopMarkerUpdates() {
        geoDataRepository.stopMarkerUpdates()
    }

    fun startMarkerUpdates() {
        userMarkersLiveData = geoDataRepository.startUserMarkerUpdates()
        staticMarkersLiveData = geoDataRepository.startStaticMarkerUpdates()
    }

    fun deleteStaticMarker(marker: Marker) {
        deleteMarkerUseCase.execute(marker)
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("AAA", "MV marker stopped")
        geoDataRepository.stopMarkerUpdates()
    }
}