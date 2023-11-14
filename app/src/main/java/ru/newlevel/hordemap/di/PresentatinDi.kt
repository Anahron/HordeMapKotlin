package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.presentatin.viewmodels.*

val presentationModule = module {
    viewModel<SettingsViewModel> {
        SettingsViewModel(
            getSettingsUseCase = get(),
            saveSettingsUseCase = get(),
            resetSettingsUseCase = get(),
            saveAutoLoadUseCase = get()
        )
    }
    viewModel<MapViewModel> {
        MapViewModel(
            deleteMarkerUseCase = get(),
            createMarkersUseCase = get(),
            saveGameMapToFileUseCase = get(),
            loadLastGameMapUseCase = get(),
            loadGameMapFromServerUseCase = get(),
            sendStaticMarkerUseCase = get(),
            startMarkerUpdateUseCase = get(),
            stopMarkerUpdateUseCase = get(),
            compassUseCase = get(),
            createRouteUseCase = get()
        )
    }
    viewModel<LocationUpdateViewModel> {
        LocationUpdateViewModel(
            getSessionLocationsUseCase = get(),
            locationUpdatesUseCase = get()
        )
    }
    viewModel<PermissionViewModel> {
        PermissionViewModel(getSettingsUseCase = get(), saveSettingsUseCase = get())
    }
    viewModel<MessengerViewModel> {
        MessengerViewModel(
            startMessageUpdateUseCase = get(),
            stopMessageUpdateUseCase = get(),
            sendMessageUseCase = get(),
            sendFileUseCase = get(),
            downloadFileUseCase = get()
        )
    }
}