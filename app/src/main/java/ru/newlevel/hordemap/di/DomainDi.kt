package ru.newlevel.hordemap.di

import org.koin.dsl.module
import ru.newlevel.hordemap.domain.usecases.*


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
}