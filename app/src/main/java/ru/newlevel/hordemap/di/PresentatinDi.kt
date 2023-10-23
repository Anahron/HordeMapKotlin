package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.databinding.FragmentMapsBinding
import ru.newlevel.hordemap.presentatin.viewmodels.LocationUpdateViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.PermissionViewModel

val presentationModule = module {
    viewModel<SettingsViewModel> {
        SettingsViewModel(
            getUserUseCase = get(),
            saveUserUseCase = get(),
            resetUserUseCase = get(),
            saveAutoLoadUseCase = get()
        )
    }
    viewModel<MapViewModel>{
        MapViewModel(
            deleteMarkerUseCase = get(),
            createMarkersUseCase = get(),
            saveGameMapToFileUseCase = get(),
            loadLastGameMapUseCase = get(),
            loadGameMapFromServerUseCase = get(),
            createStaticMarkerUseCase = get(),
            startMarkerUpdateUseCase = get(),
            stopMarkerUpdateUseCase = get(),
            compassUseCase = get()
        )
    }
    viewModel<LocationUpdateViewModel>{
        LocationUpdateViewModel(
            locationRepository = get()
        )
    }
    viewModel<PermissionViewModel>{
        PermissionViewModel( getUserUseCase = get(), saveUserUseCase = get(),)
    }
}