package ru.newlevel.hordemap.di

import com.google.android.gms.auth.api.identity.Identity
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.newlevel.hordemap.domain.usecases.LogOutUseCase
import ru.newlevel.hordemap.domain.usecases.SendUserToStorageUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.CompassInteractor
import ru.newlevel.hordemap.domain.usecases.mapCases.CreateRouteUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadGameMapFromServerUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadLastGameMapUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadProfilePhotoUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LocationUpdatesInteractor
import ru.newlevel.hordemap.domain.usecases.mapCases.MapUseCases
import ru.newlevel.hordemap.domain.usecases.mapCases.ResetUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveGameMapToFileUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveUserSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.SendStaticMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StartMarkerUpdateInteractor
import ru.newlevel.hordemap.domain.usecases.messengerCases.DeleteMessageUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.DownloadFileUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases
import ru.newlevel.hordemap.domain.usecases.messengerCases.UploadFileUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.SendMessageUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessageUpdateInteractor
import ru.newlevel.hordemap.domain.usecases.tracksCases.DeleteAllTracksUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.DeleteSessionLocationUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.GetSessionLocationsUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.RenameTrackNameForSessionUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.SaveCurrentTrackUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.SetFavouriteTrackForSessionUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.TracksUseCases
import ru.newlevel.hordemap.presentation.map.utils.GarminGpxParser
import ru.newlevel.hordemap.presentation.map.utils.MarkersUtils
import ru.newlevel.hordemap.presentation.sign_in.GoogleAuthUiClient


val domainModule = module {

    factory {
        MessageUpdateInteractor(messengerRepository = get())
    }

    factory {
        SendMessageUseCase(messengerRepository = get())
    }
    factory {
        CreateRouteUseCase()
    }

    factory {
        CompassInteractor(sensorRepository = get())
    }

    factory {
        GetUserSettingsUseCase(settingsRepository = get())
    }
    factory {
        SaveUserSettingsUseCase(settingsRepository = get())
    }
    factory {
        ResetUserSettingsUseCase(settingsRepository = get())
    }
    factory {
        DeleteMarkerUseCase(geoDataRepository = get())
    }

    factory {
        LoadLastGameMapUseCase()
    }


    factory {
        DownloadFileUseCase(messengerRepository = get())
    }
    factory {
        SaveGameMapToFileUseCase(gameMapRepository = get())
    }
    factory {
        LoadGameMapFromServerUseCase(gameMapRepository = get())
    }
    factory {
        UploadFileUseCase(messengerRepository = get())
    }
    factory {
        SaveAutoLoadUseCase(settingsRepository = get())
    }
    factory {
        SendStaticMarkerUseCase(geoDataRepository = get())
    }
    factory {
        StartMarkerUpdateInteractor(geoDataRepository = get())
    }

    factory {
        GetSessionLocationsUseCase(locationRepository = get())
    }

    factory {
        DeleteSessionLocationUseCase(locationRepository = get())
    }

    factory {
        LocationUpdatesInteractor(locationRepository = get())
    }
    factory {
        RenameTrackNameForSessionUseCase(locationRepository = get())
    }
    factory {
        SetFavouriteTrackForSessionUseCase(locationRepository = get())
    }
    factory {
        LoadProfilePhotoUseCase(settingsRepository = get())
    }
    factory {
        SaveCurrentTrackUseCase(locationRepository = get())
    }
    factory {
        DeleteAllTracksUseCase(locationRepository = get())
    }
    factory {
        MarkersUtils(garminGpxParser = get())
    }
    factory {
        GarminGpxParser()
    }
    factory {
        SendUserToStorageUseCase(settingsRepository = get())
    }
    factory {
        LogOutUseCase(settingsRepository = get())
    }
    factory {
        DeleteMessageUseCase(messengerRepository = get())
    }

    single {
        GoogleAuthUiClient(
            androidContext().applicationContext,
            Identity.getSignInClient(androidContext().applicationContext),
            logOutUseCase = get(),
            getUserSettingsUseCase = get()
        )
    }

    single {
        MapUseCases(
            deleteMarkerUseCase = get(),
            saveGameMapToFileUseCase = get(),
            loadLastGameMapUseCase = get(),
            loadGameMapFromServerUseCase = get(),
            sendStaticMarkerUseCase = get(),
            startMarkerUpdateInteractor = get(),
            compassInteractor = get(),
            createRouteUseCase = get(),
            locationUpdatesInteractor = get()
        )
    }
    single {
        TracksUseCases(
            getSessionLocationsUseCase = get(),
            deleteSessionLocationUseCase = get(),
            renameTrackNameForSessionUseCase = get(),
            setFavouriteTrackForSessionUseCase = get(),
            saveCurrentTrackUseCase = get(),
            deleteAllTracksUseCase = get(),
        )
    }
    single {
        MessengerUseCases(
            messageUpdateInteractor = get(),
            sendMessageUseCase = get(),
            uploadFileUseCase = get(),
            downloadFileUseCase = get(),
            deleteMessageUseCase = get()
        )
    }
}
