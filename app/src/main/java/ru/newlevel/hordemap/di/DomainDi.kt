package ru.newlevel.hordemap.di

import org.koin.dsl.module
import ru.newlevel.hordemap.domain.usecases.*


val domainModule = module {

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
    factory<SaveGameMapToFileUseCase> {
        SaveGameMapToFileUseCase(gameMapRepository = get())
    }
    factory<LoadGameMapFromServerUseCase> {
        LoadGameMapFromServerUseCase(gameMapRepository = get())
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