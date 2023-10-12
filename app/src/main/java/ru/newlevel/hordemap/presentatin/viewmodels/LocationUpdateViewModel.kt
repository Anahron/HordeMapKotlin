package ru.newlevel.hordemap.presentatin.viewmodels


import android.content.Context
import androidx.lifecycle.ViewModel
import ru.newlevel.hordemap.data.repository.LocationRepositoryImpl

class LocationUpdateViewModel(context: Context) : ViewModel() {

    private val locationRepository = LocationRepositoryImpl(context)

    fun startLocationUpdates() = locationRepository.startLocationUpdates()

    fun stopLocationUpdates() = locationRepository.stopLocationUpdates()
}
