package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.LoginViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MarkerViewModel

val presentationModule = module {
    viewModel<LoginViewModel> {
        LoginViewModel(
            getUserUseCase = get(),
            saveUserUseCase = get(),
            resetUserUseCase = get()
        )
    }
    viewModel<MarkerViewModel>{
        MarkerViewModel(
            geoDataRepository = get(),
            deleteMarkerUseCase = get(),
            createMarkersUseCase = get()
        )
    }
    viewModel<LocationUpdateViewModel>{
        LocationUpdateViewModel(
            locationRepository = get()
        )
    }
}