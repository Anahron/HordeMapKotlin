package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.presentation.map.MapViewModel
import ru.newlevel.hordemap.presentation.messenger.MessengerViewModel
import ru.newlevel.hordemap.presentation.permissions.PermissionViewModel
import ru.newlevel.hordemap.presentation.settings.SettingsViewModel
import ru.newlevel.hordemap.presentation.sign_in.SignInViewModel
import ru.newlevel.hordemap.presentation.tracks.TrackTransferViewModel
import ru.newlevel.hordemap.presentation.tracks.TracksViewModel


val presentationModule = module {
    viewModel<SettingsViewModel> {
        SettingsViewModel(
            getUserSettingsUseCase = get(),
            saveUserSettingsUseCase = get(),
            resetUserSettingsUseCase = get(),
            saveAutoLoadUseCase = get(),
            loadProfilePhotoUseCase = get(),
            sendUserToStorageUseCase = get()
        )
    }
    viewModel<MapViewModel> {
        MapViewModel(
            mapUseCases = get(),
            markersUtils = get(),
           getUserSettingsUseCase = get()
        )
    }

    viewModel<SignInViewModel>() {
        SignInViewModel(
            getUserSettingsUseCase = get(),
            saveUserSettingsUseCase = get(),
            googleAuthUiClient = get(),
            sendUserToStorageUseCase = get()
        )
    }
    viewModel<TracksViewModel> {
        TracksViewModel(
            tracksUseCases = get()
        )
    }
    viewModel<PermissionViewModel> {
        PermissionViewModel()
    }

    single<TrackTransferViewModel> {
        TrackTransferViewModel()
    }

    viewModel<MessengerViewModel> {
        MessengerViewModel(
            messengerUseCases = get()
        )
    }
}