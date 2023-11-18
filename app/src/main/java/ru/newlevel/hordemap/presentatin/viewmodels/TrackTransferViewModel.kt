package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class TrackTransferViewModel: ViewModel() {

    private val _trackToShowOnMap = MutableLiveData<List<LatLng>>()
    val trackToShowOnMap: LiveData<List<LatLng>> = _trackToShowOnMap

    fun setTrack(list: List<LatLng>){
        _trackToShowOnMap.value = list
    }

    fun clearTrack(){
        _trackToShowOnMap.value = listOf()
    }
}