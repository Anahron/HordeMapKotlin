package ru.newlevel.hordemap.di

import org.koin.dsl.module
import ru.newlevel.hordemap.data.repository.*
import ru.newlevel.hordemap.data.storage.*
import ru.newlevel.hordemap.domain.repository.*

val dataModule = module {

    //Storeges
    single<UserStorage> {
        SharedPrefUserStorage(context = get())
    }

    single<MarkersDataStorage> {
        FirebaseStorageImpl()
    }
    single<FilesLocalStorage> {
        FilesLocalStorage(context = get())
    }

    single<GameMapLocalStorage> {
        FilesLocalStorage(context = get())
    }
    single<FirebaseMapStorage> {
        FirebaseStorageImpl()
    }



    // Repos
    single<UserRepository> {
        UserRepositoryImpl(userStorage = get())
    }
    single<GeoDataRepository> {
        GeoDataRepositoryImpl(markersDataStorage = get())
    }

    single<LocationRepository> {
        LocationRepositoryImpl(context = get())
    }

    single<GameMapRepository> {
        GameMapRepositoryImpl(gameMapLocalStorage = get(), firebaseMapStorage = get())
    }
}