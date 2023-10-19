package ru.newlevel.hordemap.di

import org.koin.dsl.module
import ru.newlevel.hordemap.domain.usecases.*


val domainModule = module {

    factory<GetUserUseCase> {
        GetUserUseCase(userRepository = get())
    }
    factory<SaveUserUseCase> {
        SaveUserUseCase(userRepository = get())
    }
    factory<ResetUserUseCase> {
         ResetUserUseCase(userRepository = get())
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
        SaveAutoLoadUseCase(userRepository = get())
    }
    factory<CreateStaticMarkerUseCase> {
        CreateStaticMarkerUseCase(geoDataRepository = get())
    }
    factory<StartMarkerUpdateUseCase> {
        StartMarkerUpdateUseCase(geoDataRepository = get())
    }
    factory<StopMarkerUpdateUseCase> {
        StopMarkerUpdateUseCase(geoDataRepository = get())
    }
}