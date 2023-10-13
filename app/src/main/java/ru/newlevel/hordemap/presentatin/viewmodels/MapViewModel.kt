package ru.newlevel.hordemap.presentatin.viewmodels

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.usecases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.CreateMarkersUseCase
import ru.newlevel.hordemap.domain.usecases.HideMarkersUseCase
import ru.newlevel.hordemap.domain.usecases.ShowMarkersUseCase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MapViewModel(
    private val geoDataRepository: GeoDataRepository,
    private val deleteMarkerUseCase: DeleteMarkerUseCase,
    private val createMarkersUseCase: CreateMarkersUseCase,
    private val hideMarkersUserCase: HideMarkersUseCase,
    private val showMarkersUseCase: ShowMarkersUseCase
) : ViewModel() {

    lateinit var userMarkersLiveData: LiveData<List<MarkerDataModel>>

    lateinit var staticMarkersLiveData: LiveData<List<MarkerDataModel>>

    private var isShowMarkers = MutableLiveData<Boolean>()
    val _isShowMarkers: LiveData<Boolean> = isShowMarkers

    init {
        Log.e("AAA", "MV marker started")
        isShowMarkers.value = true
    }

    fun sendCoordinates(location: Location, userDomainModel: UserDomainModel) {
        mapLocationToMarker(location, userDomainModel)
        geoDataRepository.sendCoordinates(mapLocationToMarker(location, userDomainModel))
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

    @SuppressLint("SimpleDateFormat")
    private fun mapLocationToMarker(
        location: Location,
        userDomainModel: UserDomainModel
    ): MarkerDataModel {
        val marker = MarkerDataModel()
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
        val date = dateFormat.format(Date(System.currentTimeMillis()))
        marker.latitude = location.latitude
        marker.longitude = location.longitude
        marker.userName = userDomainModel.name
        marker.deviceId = userDomainModel.deviceID
        marker.timestamp = System.currentTimeMillis()
        marker.item = userDomainModel.selectedMarker
        marker.title = date
        return marker
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