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
import ru.newlevel.hordemap.domain.usecases.markersCases.StopMarkerUpdateInteractor
import ru.newlevel.hordemap.domain.usecases.messengerCases.DownloadFileUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.MessengerUseCases
import ru.newlevel.hordemap.domain.usecases.messengerCases.UploadFileUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.SendMessageUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.StartMessageUpdateInteractor
import ru.newlevel.hordemap.domain.usecases.messengerCases.StopMessageUpdateInteractor
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

    factory<StartMessageUpdateInteractor> {
        StartMessageUpdateInteractor(messengerRepository = get())
    }

    factory<StopMessageUpdateInteractor> {
        StopMessageUpdateInteractor(messengerRepository = get())
    }
    factory<SendMessageUseCase> {
        SendMessageUseCase(messengerRepository = get())
    }
    factory<CreateRouteUseCase> {
        CreateRouteUseCase()
    }

    factory<CompassInteractor> {
        CompassInteractor(sensorRepository = get())
    }

    factory<GetUserSettingsUseCase> {
        GetUserSettingsUseCase(settingsRepository = get())
    }
    factory<SaveUserSettingsUseCase> {
        SaveUserSettingsUseCase(settingsRepository = get())
    }
    factory<ResetUserSettingsUseCase> {
        ResetUserSettingsUseCase(settingsRepository = get())
    }
    factory<DeleteMarkerUseCase> {
        DeleteMarkerUseCase(geoDataRepository = get())
    }

    factory<LoadLastGameMapUseCase> {
        LoadLastGameMapUseCase()
    }

    factory<DownloadFileUseCase> {
        DownloadFileUseCase(messengerRepository = get())
    }
    factory<SaveGameMapToFileUseCase> {
        SaveGameMapToFileUseCase(gameMapRepository = get())
    }
    factory<LoadGameMapFromServerUseCase> {
        LoadGameMapFromServerUseCase(gameMapRepository = get())
    }
    factory<UploadFileUseCase> {
        UploadFileUseCase(messengerRepository = get())
    }
    factory<SaveAutoLoadUseCase> {
        SaveAutoLoadUseCase(settingsRepository = get())
    }
    factory<SendStaticMarkerUseCase> {
        SendStaticMarkerUseCase(geoDataRepository = get())
    }
    factory<StartMarkerUpdateInteractor> {
        StartMarkerUpdateInteractor(geoDataRepository = get())
    }
    factory<StopMarkerUpdateInteractor> {
        StopMarkerUpdateInteractor(geoDataRepository = get())
    }

    factory<GetSessionLocationsUseCase> {
        GetSessionLocationsUseCase(locationRepository = get())
    }

    factory<DeleteSessionLocationUseCase> {
        DeleteSessionLocationUseCase(locationRepository = get())
    }

    factory<LocationUpdatesInteractor> {
        LocationUpdatesInteractor(locationRepository = get())
    }
    factory<RenameTrackNameForSessionUseCase> {
        RenameTrackNameForSessionUseCase(locationRepository = get())
    }
    factory<SetFavouriteTrackForSessionUseCase> {
        SetFavouriteTrackForSessionUseCase(locationRepository = get())
    }
    factory<LoadProfilePhotoUseCase> {
        LoadProfilePhotoUseCase(settingsRepository = get())
    }
    factory<SaveCurrentTrackUseCase> {
        SaveCurrentTrackUseCase(locationRepository = get())
    }
    factory<DeleteAllTracksUseCase> {
        DeleteAllTracksUseCase(locationRepository = get())
    }
    factory<MarkersUtils> {
        MarkersUtils(garminGpxParser = get())
    }
    factory<GarminGpxParser> {
        GarminGpxParser()
    }
    factory<SendUserToStorageUseCase> {
        SendUserToStorageUseCase(settingsRepository = get())
    }
    factory<LogOutUseCase> {
        LogOutUseCase(settingsRepository = get())
    }

    single<GoogleAuthUiClient> {
        GoogleAuthUiClient(
            androidContext().applicationContext,
            Identity.getSignInClient(androidContext().applicationContext), logOutUseCase = get(), getUserSettingsUseCase = get()
        )
    }

    single<MapUseCases> {
        MapUseCases(
            deleteMarkerUseCase = get(),
            saveGameMapToFileUseCase = get(),
            loadLastGameMapUseCase = get(),
            loadGameMapFromServerUseCase = get(),
            sendStaticMarkerUseCase = get(),
            startMarkerUpdateInteractor = get(),
            stopMarkerUpdateInteractor = get(),
            compassInteractor = get(),
            createRouteUseCase = get(),
            locationUpdatesInteractor = get()
        )
    }
    single<TracksUseCases> {
        TracksUseCases(
            getSessionLocationsUseCase = get(),
            deleteSessionLocationUseCase = get(),
            renameTrackNameForSessionUseCase = get(),
            setFavouriteTrackForSessionUseCase = get(),
            saveCurrentTrackUseCase = get(),
            deleteAllTracksUseCase = get(),
        )
    }
    single<MessengerUseCases> {
        MessengerUseCases(
            startMessageUpdateInteractor = get(),
            stopMessageUpdateInteractor = get(),
            sendMessageUseCase = get(),
            uploadFileUseCase = get(),
            downloadFileUseCase = get()
        )
    }
}
