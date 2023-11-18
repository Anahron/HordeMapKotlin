package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.presentatin.viewmodels.MapViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.MessengerViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.PermissionViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.SettingsViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.TrackTransferViewModel
import ru.newlevel.hordemap.presentatin.viewmodels.TracksViewModel


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
            createRouteUseCase = get(),
            locationUpdatesUseCase = get()
        )
    }
    viewModel<TracksViewModel> {
        TracksViewModel(
            getSessionLocationsUseCase = get(),
            deleteSessionLocationUseCase = get(),
            renameTrackNameForSessionUseCase = get(),
            setFavouriteTrackForSessionUseCase = get()
        )
    }
    viewModel<PermissionViewModel> {
        PermissionViewModel(getSettingsUseCase = get(), saveSettingsUseCase = get())
    }

    single<TrackTransferViewModel>{
        TrackTransferViewModel()
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