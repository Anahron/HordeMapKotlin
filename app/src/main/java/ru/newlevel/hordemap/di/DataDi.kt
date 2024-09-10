package ru.newlevel.hordemap.di

import android.content.Context
import android.hardware.SensorManager
import androidx.room.Room.databaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.newlevel.hordemap.app.MyAlarmManager
import ru.newlevel.hordemap.data.db.MyDatabase
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

    single {
        databaseBuilder(
            androidContext(),
            MyDatabase::class.java,
            "my-location-database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
}

val dataModule = module {


    single {
        MyAlarmManager(context = get())
    }

    single {
        val database = get<MyDatabase>()
        database.markersDao()
    }

    single {
        val database = get<MyDatabase>()
        database.locationDao()
    }
    single {
        val database = get<MyDatabase>()
        database.messageDao()
    }

    //Device
    single {
        androidContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    //Storages
    single<UserLocalStorage> {
        SharedPrefUserLocalStorage(context = get())
    }

    single<MessagesCountLocalStorage> {
        SharedPrefUserLocalStorage(context = get())
    }

    single<MarkersRemoteStorage> {
        MyFirebaseDatabase()
    }
    single {
        FilesLocalStorage(context = get(), FilesUtils())
    }

    single<GameMapLocalStorage> {
        FilesLocalStorage(context = get(), FilesUtils())
    }
    single<GameMapRemoteStorage> {
        MyFirebaseStorage()
    }
    single {
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

    single<ProfilePhotoStorage> {
        MyFirebaseStorage()
    }

    single<GroupsRepository>{
        GroupsRepositoryImpl(profileRemoteStorage = get())
    }

    single<ProfileRemoteStorage> {
        MyFirebaseDatabase()
    }
    // Repos
    single<SettingsRepository> {
        SettingsRepositoryImpl(userLocalStorage = get(), profilePhotoStorage = get(), profileRemoteStorage = get())
    }
    single<GeoDataRepository> {
        GeoDataRepositoryImpl(markersRemoteStorage = get(), markersLocalStorage = get())
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
        MessengerRepositoryImpl(
            messageRemoteStorage = get(),
            messageFilesStorage = get(),
            messageDao = get(),
            messagesCountLocalStorage = get()
        )
    }
}
