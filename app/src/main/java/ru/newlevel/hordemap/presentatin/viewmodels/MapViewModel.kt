package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.usecases.*
import ru.newlevel.hordemap.presentatin.fragments.MapFragment
import java.io.InputStream

const val REQUEST_CODE_PICK_KMZ_FILE = 100

class MapViewModel(
    private val geoDataRepository: GeoDataRepository,
    private val deleteMarkerUseCase: DeleteMarkerUseCase,
    private val createMarkersUseCase: CreateMarkersUseCase,
    private val hideMarkersUserCase: HideMarkersUseCase,
    private val showMarkersUseCase: ShowMarkersUseCase,
    private val loadGameMapFromUriUseCase: LoadGameMapFromUriUseCase,
    private val saveGameMapToFileUseCase: SaveGameMapToFileUseCase,
    private val loadLastGameMapUseCase: LoadLastGameMapUseCase,
    private val loadGameMapFromServerUseCase: LoadGameMapFromServerUseCase
) : ViewModel() {

    lateinit var userMarkersLiveData: LiveData<List<MarkerDataModel>>

    lateinit var staticMarkersLiveData: LiveData<List<MarkerDataModel>>

    private var isShowMarkers = MutableLiveData<Boolean>()
    val _isShowMarkers: LiveData<Boolean> = isShowMarkers

    var _kmzInputStream = MutableLiveData<InputStream>()


    init {
        isShowMarkers.value = true
    }

    suspend fun loadGameMapFromUri(uri: Uri, context: Context) {
        _kmzInputStream.value = loadGameMapFromUriUseCase.execute(uri, context)
    }

    suspend fun saveGameMapToFile(uri: Uri) {
        saveGameMapToFileUseCase.execute(uri)
    }


    suspend fun loadMapFromServer(context: Context): Boolean {
        val uri = loadGameMapFromServerUseCase.execute(context)
        return if (uri != null) {
            loadGameMapFromUri(uri, context)
            true
        } else false
    }

    fun loadGameMapFromFiles(fragment: MapFragment) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/vnd.google-earth.kmz"
        fragment.startActivityForResult(intent, REQUEST_CODE_PICK_KMZ_FILE)
    }

    suspend fun loadLastGameMap(): Boolean {
        val uri = loadLastGameMapUseCase.execute()
        return if (uri != null) {
            _kmzInputStream.value = loadLastGameMapUseCase.execute()
            true
        } else false
    }

    fun showOrHideMarkers() {
        if (_isShowMarkers.value == true)
            isShowMarkers.value = hideMarkersUserCase.execute()
        else
            isShowMarkers.value = showMarkersUseCase.execute()
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