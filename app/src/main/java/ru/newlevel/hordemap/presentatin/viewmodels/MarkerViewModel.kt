package ru.newlevel.hordemap.presentatin.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.data.storage.models.MarkerModel

import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class MarkerViewModel(private val geoDataRepository: GeoDataRepository) : ViewModel() {


    init {
        Log.e("AAA", "MV marker started")
    }

    lateinit var userMarkersLiveData: LiveData<List<MarkerModel>>
    lateinit var staticMarkersLiveData: LiveData<List<MarkerModel>>


    fun stopMarkerUpdates(){
        geoDataRepository.stopMarkerUpdates()
    }

    fun startMarkerUpdates(){
        userMarkersLiveData = geoDataRepository.startUserMarkerUpdates()
        staticMarkersLiveData = geoDataRepository.startStaticMarkerUpdates()
    }


    override fun onCleared() {
        super.onCleared()
        Log.e("AAA", "MV marker stopped")
        geoDataRepository.stopMarkerUpdates()
    }
}