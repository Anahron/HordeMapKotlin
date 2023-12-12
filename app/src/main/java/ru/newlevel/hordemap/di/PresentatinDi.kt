package ru.newlevel.hordemap.di

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import ru.newlevel.hordemap.presentation.MainViewModel
import ru.newlevel.hordemap.presentation.map.MapViewModel
import ru.newlevel.hordemap.presentation.messenger.MessengerViewModel
import ru.newlevel.hordemap.presentation.permissions.PermissionViewModel
import ru.newlevel.hordemap.presentation.settings.SettingsViewModel
import ru.newlevel.hordemap.presentation.sign_in.SignInViewModel
import ru.newlevel.hordemap.presentation.tracks.TrackTransferViewModel
import ru.newlevel.hordemap.presentation.tracks.TracksViewModel


val presentationModule = module {
    viewModel {
        SettingsViewModel(
            getUserSettingsUseCase = get(),
            saveUserSettingsUseCase = get(),
            resetUserSettingsUseCase = get(),
            saveAutoLoadUseCase = get(),
            loadProfilePhotoUseCase = get(),
            sendUserToStorageUseCase = get()
        )
    }
    viewModel {
        MapViewModel(
            mapUseCases = get(),
            getUserSettingsUseCase = get()
        )
    }
    viewModel {
        MainViewModel(
            messageUpdateInteractor = get()
        )
    }

    viewModel {
        SignInViewModel(
            getUserSettingsUseCase = get(),
            saveUserSettingsUseCase = get(),
            googleAuthUiClient = get(),
            sendUserToStorageUseCase = get()
        )
    }
    viewModel {
        TracksViewModel(
            tracksUseCases = get()
        )
    }
    viewModel {
        PermissionViewModel()
    }

    single {
        TrackTransferViewModel()
    }

    viewModel {
        MessengerViewModel(
            messengerUseCases = get()
        )
    }
}