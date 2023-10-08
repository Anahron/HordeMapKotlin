package ru.newlevel.hordemap.presentatin

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.data.models.MarkerModel

import ru.newlevel.hordemap.domain.repository.GeoDataRepository

class MarkerViewModel(private val geoDataRepository: GeoDataRepository) : ViewModel() {


    init {
        Log.e("AAA", "MV marker started")
    }
    lateinit var markersLiveData: LiveData<List<MarkerModel>>

    fun stopMarkerUpdates(){
        geoDataRepository.stopMarkerUpdates()
    }

    fun startMarkerUpdates(){
        markersLiveData = geoDataRepository.startMarkerUpdates()
    }


    override fun onCleared() {
        super.onCleared()
        Log.e("AAA", "MV marker stopped")
        geoDataRepository.stopMarkerUpdates()
    }
}