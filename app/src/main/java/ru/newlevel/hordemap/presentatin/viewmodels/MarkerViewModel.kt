package ru.newlevel.hordemap.presentatin.viewmodels

import android.annotation.SuppressLint
import android.location.Location
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.Marker
import ru.newlevel.hordemap.data.storage.models.MarkerModel
import ru.newlevel.hordemap.domain.models.UserDomainModel

import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.usecases.DeleteMarkerUseCase
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class MarkerViewModel(private val geoDataRepository: GeoDataRepository, private val deleteMarkerUseCase: DeleteMarkerUseCase) : ViewModel() {


    init {
        Log.e("AAA", "MV marker started")
    }

    lateinit var userMarkersLiveData: LiveData<List<MarkerModel>>
    lateinit var staticMarkersLiveData: LiveData<List<MarkerModel>>

    fun sendCoordinates(location: Location, userDomainModel: UserDomainModel){
        mapLocationToMarker(location,userDomainModel)
        geoDataRepository.sendCoordinates(mapLocationToMarker(location,userDomainModel))
    }

    @SuppressLint("SimpleDateFormat")
    private fun mapLocationToMarker(location: Location, userDomainModel: UserDomainModel): MarkerModel{
        val marker = MarkerModel()
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
    fun stopMarkerUpdates(){
        geoDataRepository.stopMarkerUpdates()
    }

    fun startMarkerUpdates(){
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