package ru.newlevel.hordemap.presentatin.viewmodels

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.models.UserDomainModel

import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.usecases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.MarkerCreator
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MarkerViewModel(
    private val geoDataRepository: GeoDataRepository,
    private val deleteMarkerUseCase: DeleteMarkerUseCase,
    private val markerCreator: MarkerCreator
) : ViewModel() {


    init {
        Log.e("AAA", "MV marker started")
    }

    lateinit var userMarkersLiveData: LiveData<List<MarkerDataModel>>
    lateinit var staticMarkersLiveData: LiveData<List<MarkerDataModel>>

    fun sendCoordinates(location: Location, userDomainModel: UserDomainModel) {
        mapLocationToMarker(location, userDomainModel)
        geoDataRepository.sendCoordinates(mapLocationToMarker(location, userDomainModel))
    }


    fun createUsersMarkers(it: List<MarkerDataModel>, googleMap: GoogleMap) {
        markerCreator.createUsersMarkers(it,googleMap)
    }
    fun createStaticMarkers(it: List<MarkerDataModel>, googleMap: GoogleMap) {
        markerCreator.createStaticMarkers(it,googleMap)
    }
    private var userMarkersObserver: Observer<List<MarkerDataModel>>? = null


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