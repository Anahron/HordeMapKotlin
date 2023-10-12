package ru.newlevel.hordemap.presentatin.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.newlevel.hordemap.data.repository.MarkerRepositoryImpl
import ru.newlevel.hordemap.data.repository.UserRepositoryImpl
import ru.newlevel.hordemap.data.storage.SharedPrefUserStorage
import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.usecases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.MarkerCreator

class MarkerViewModelFactory(context: Context) : ViewModelProvider.Factory{

    private val geoDataRepository by lazy(LazyThreadSafetyMode.NONE) { GeoDataRepository.getInstance() as GeoDataRepository }
    private val deleteMarkerUseCase by lazy(LazyThreadSafetyMode.NONE) { DeleteMarkerUseCase(geoDataRepository) }
    private val userRepository by lazy(LazyThreadSafetyMode.NONE) { UserRepositoryImpl(userStorage = SharedPrefUserStorage(context)) }
    private val markerRepository by lazy(LazyThreadSafetyMode.NONE) { MarkerRepositoryImpl() }
    private val markerCreator by lazy(LazyThreadSafetyMode.NONE) {  MarkerCreator(context, userRepository.getUser(),markerRepository) }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MarkerViewModel(geoDataRepository = geoDataRepository, deleteMarkerUseCase = deleteMarkerUseCase, markerCreator = markerCreator) as T
    }
}