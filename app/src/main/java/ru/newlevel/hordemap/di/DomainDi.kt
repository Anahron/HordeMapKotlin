package ru.newlevel.hordemap.di

import org.koin.dsl.module
import ru.newlevel.hordemap.domain.usecases.mapCases.CompassUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.CreateRouteUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.GetSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadGameMapFromServerUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LoadLastGameMapUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.LocationUpdatesUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.ResetSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveAutoLoadUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveGameMapToFileUseCase
import ru.newlevel.hordemap.domain.usecases.mapCases.SaveSettingsUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.CreateMarkersUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.SendStaticMarkerUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StartMarkerUpdateUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.StopMarkerUpdateUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.DownloadFileUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.SendFileUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.SendMessageUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.StartMessageUpdateUseCase
import ru.newlevel.hordemap.domain.usecases.messengerCases.StopMessageUpdateUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.DeleteAllTracksUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.DeleteSessionLocationUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.GetSessionLocationsUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.RenameTrackNameForSessionUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.SaveCurrentTrackUseCase
import ru.newlevel.hordemap.domain.usecases.tracksCases.SetFavouriteTrackForSessionUseCase


val domainModule = module {

    factory<StartMessageUpdateUseCase> {
        StartMessageUpdateUseCase(messengerRepository = get())
    }

    factory<StopMessageUpdateUseCase> {
        StopMessageUpdateUseCase(messengerRepository = get())
    }
    factory<SendMessageUseCase> {
        SendMessageUseCase(messengerRepository = get())
    }
    factory<CreateRouteUseCase> {
        CreateRouteUseCase()
    }

    factory<CompassUseCase> {
        CompassUseCase(sensorRepository = get())
    }

    factory<GetSettingsUseCase> {
        GetSettingsUseCase(settingsRepository = get())
    }
    factory<SaveSettingsUseCase> {
        SaveSettingsUseCase(settingsRepository = get())
    }
    factory<ResetSettingsUseCase> {
        ResetSettingsUseCase(settingsRepository = get())
    }
    factory<DeleteMarkerUseCase> {
        DeleteMarkerUseCase(geoDataRepository = get())
    }
    factory<CreateMarkersUseCase> {
        CreateMarkersUseCase(context = get())
    }

    factory<LoadLastGameMapUseCase> {
        LoadLastGameMapUseCase(gameMapRepository = get())
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
    factory<SendFileUseCase> {
        SendFileUseCase(messengerRepository = get())
    }
    factory<SaveAutoLoadUseCase> {
        SaveAutoLoadUseCase(settingsRepository = get())
    }
    factory<SendStaticMarkerUseCase> {
        SendStaticMarkerUseCase(geoDataRepository = get())
    }
    factory<StartMarkerUpdateUseCase> {
        StartMarkerUpdateUseCase(geoDataRepository = get())
    }
    factory<StopMarkerUpdateUseCase> {
        StopMarkerUpdateUseCase(geoDataRepository = get())
    }

    factory<GetSessionLocationsUseCase> {
        GetSessionLocationsUseCase(locationRepository = get())
    }

    factory<DeleteSessionLocationUseCase> {
        DeleteSessionLocationUseCase(locationRepository = get())
    }

    factory<LocationUpdatesUseCase> {
        LocationUpdatesUseCase(locationRepository = get())
    }
    factory<RenameTrackNameForSessionUseCase> {
        RenameTrackNameForSessionUseCase(locationRepository = get())
    }
    factory<SetFavouriteTrackForSessionUseCase> {
        SetFavouriteTrackForSessionUseCase(locationRepository = get())
    }
    factory<SaveCurrentTrackUseCase> {
        SaveCurrentTrackUseCase(locationRepository = get())
    }
    factory<DeleteAllTracksUseCase> {
        DeleteAllTracksUseCase(locationRepository = get())
    }
}