package ru.newlevel.hordemap.presentatin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.data.repository.GeoDataRepositoryImpl

class MarkerViewModelFactory : ViewModelProvider.Factory{

    private val geoDataRepository by lazy(LazyThreadSafetyMode.NONE) { GeoDataRepositoryImpl() }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MarkerViewModel(geoDataRepository = geoDataRepository) as T
    }
}