package ru.newlevel.hordemap.presentatin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.data.repository.GeoDataRepositoryImpl
import ru.newlevel.hordemap.domain.usecases.DeleteMarkerUseCase

class MarkerViewModelFactory : ViewModelProvider.Factory{

    private val geoDataRepository by lazy(LazyThreadSafetyMode.NONE) { GeoDataRepositoryImpl() }
    private val deleteMarkerUseCase by lazy(LazyThreadSafetyMode.NONE) { DeleteMarkerUseCase(geoDataRepository) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MarkerViewModel(geoDataRepository = geoDataRepository, deleteMarkerUseCase = deleteMarkerUseCase) as T
    }
}