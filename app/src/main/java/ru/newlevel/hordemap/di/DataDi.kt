package ru.newlevel.hordemap.di

import android.content.Context
import android.hardware.SensorManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.newlevel.hordemap.data.repository.*
import ru.newlevel.hordemap.data.storage.*
import ru.newlevel.hordemap.device.MySensorManager
import ru.newlevel.hordemap.domain.repository.*

val dataModule = module {

    //Device
    single<SensorManager>  {
        androidContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager }

    //Storages
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
    single<MapStorage> {
        FirebaseStorageImpl()
    }
    single<MySensorManager> {
        MySensorManager(sensorManager = get())
    }
    single<MessageStorage> {
        FirebaseStorageImpl()
    }
    single<FirebaseStorageImpl> {
       get()
    }

    // Repos
    single<SettingsRepository> {
        SettingsRepositoryImpl(userStorage = get())
    }
    single<GeoDataRepository> {
        GeoDataRepositoryImpl(markersDataStorage = get())
    }

    single<LocationRepository> {
        LocationRepositoryImpl(context = get())
    }

    single<GameMapRepository> {
        GameMapRepositoryImpl(gameMapLocalStorage = get(), mapStorage = get())
    }

    single<SensorRepository> {
        SensorRepositoryImpl(mySensorManager = get())
    }

    single<MessengerRepository> {
        MessengerRepositoryImpl(messageStorage = get())
    }
}