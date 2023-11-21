package ru.newlevel.hordemap.di

import android.content.Context
import android.hardware.SensorManager
import androidx.room.Room.databaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.newlevel.hordemap.data.db.MyLocationDao
import ru.newlevel.hordemap.data.db.MyLocationDatabase
import ru.newlevel.hordemap.data.repository.*
import ru.newlevel.hordemap.data.storage.implementation.FilesLocalStorage
import ru.newlevel.hordemap.data.storage.implementation.FilesUtils
import ru.newlevel.hordemap.data.storage.implementation.MyFirebaseDatabase
import ru.newlevel.hordemap.data.storage.implementation.MyFirebaseStorage
import ru.newlevel.hordemap.data.storage.implementation.SharedPrefUserLocalStorage
import ru.newlevel.hordemap.data.storage.interfaces.*
import ru.newlevel.hordemap.device.MySensorManager
import ru.newlevel.hordemap.domain.repository.*


val databaseModule = module {

    single<MyLocationDatabase> {
        databaseBuilder(
            androidContext(),
            MyLocationDatabase::class.java,
            "my-location-database"
        )
            .build()
    }
}

val dataModule = module {

    single<MyLocationDao> {
        val database = get<MyLocationDatabase>()
        database.locationDao()
    }

    //Device
    single<SensorManager> {
        androidContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    //Storages
    single<UserLocalStorage> {
        SharedPrefUserLocalStorage(context = get())
    }

    single<MarkersRemoteStorage> {
        MyFirebaseDatabase()
    }
    single<FilesLocalStorage> {
        FilesLocalStorage(context = get(), FilesUtils())
    }

    single<GameMapLocalStorage> {
        FilesLocalStorage(context = get(), FilesUtils())
    }
    single<GameMapRemoteStorage> {
        MyFirebaseStorage()
    }
    single<MySensorManager> {
        MySensorManager(sensorManager = get())
    }
    single<MessageRemoteStorage> {
        MyFirebaseDatabase()
    }
    single<MessageFilesStorage> {
        MyFirebaseStorage()
    }

    single<MyFirebaseDatabase> {
        get()
    }

    // Repos
    single<SettingsRepository> {
        SettingsRepositoryImpl(userLocalStorage = get())
    }
    single<GeoDataRepository> {
        GeoDataRepositoryImpl(markersRemoteStorage = get())
    }

    single<LocationRepository> {
        LocationRepositoryImpl(context = get(), myLocationDao = get())
    }

    single<GameMapRepository> {
        GameMapRepositoryImpl(gameMapLocalStorage = get(), gameMapRemoteStorage = get())
    }

    single<SensorRepository> {
        SensorRepositoryImpl(mySensorManager = get())
    }

    single<MessengerRepository> {
        MessengerRepositoryImpl(messageRemoteStorage = get(), messageFilesStorage = get())
    }
}
