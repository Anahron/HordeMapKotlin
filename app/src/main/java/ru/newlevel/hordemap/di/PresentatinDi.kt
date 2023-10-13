package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel

val presentationModule = module {
    viewModel<LoginViewModel> {
        LoginViewModel(
            getUserUseCase = get(),
            saveUserUseCase = get(),
            resetUserUseCase = get()
        )
    }
    viewModel<MapViewModel>{
        MapViewModel(
            geoDataRepository = get(),
            deleteMarkerUseCase = get(),
            createMarkersUseCase = get(),
            hideMarkersUserCase = get(),
            showMarkersUseCase = get()
        )
    }
    viewModel<LocationUpdateViewModel>{
        LocationUpdateViewModel(
            locationRepository = get()
        )
    }
}