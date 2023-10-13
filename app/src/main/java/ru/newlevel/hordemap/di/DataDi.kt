package ru.newlevel.hordemap.di

import org.koin.dsl.module
import ru.newlevel.hordemap.data.repository.GeoDataRepositoryImpl
import ru.newlevel.hordemap.data.repository.LocationRepositoryImpl
import ru.newlevel.hordemap.data.repository.MarkerRepositoryImpl
import ru.newlevel.hordemap.data.repository.UserRepositoryImpl
import ru.newlevel.hordemap.data.storage.FirebaseStorage
import ru.newlevel.hordemap.data.storage.GeoDataStorage
import ru.newlevel.hordemap.data.storage.SharedPrefUserStorage
import ru.newlevel.hordemap.data.storage.UserStorage
import ru.newlevel.hordemap.domain.repository.GeoDataRepository
import ru.newlevel.hordemap.domain.repository.LocationRepository
import ru.newlevel.hordemap.domain.repository.MarkerRepository
import ru.newlevel.hordemap.domain.repository.UserRepository

val dataModule = module {

    //Storeges
    single<UserStorage> {
        SharedPrefUserStorage(context = get())
    }

    single<GeoDataStorage>{
        FirebaseStorage()
    }

    // Repos
    single<UserRepository> {
        UserRepositoryImpl(userStorage = get())
    }
    single<GeoDataRepository> {
        GeoDataRepositoryImpl(geoDataStorage = get())
    }

    single<MarkerRepository> {
        MarkerRepositoryImpl()
    }

    single<LocationRepository> {
        LocationRepositoryImpl(context = get()) }
}