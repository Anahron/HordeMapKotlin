package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.data.repository.GeoDataRepositoryImpl

class MarkerViewModelFactory : ViewModelProvider.Factory{

    private val geoDataRepository by lazy(LazyThreadSafetyMode.NONE) { GeoDataRepositoryImpl() }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MarkerViewModel(geoDataRepository = geoDataRepository) as T
    }
}