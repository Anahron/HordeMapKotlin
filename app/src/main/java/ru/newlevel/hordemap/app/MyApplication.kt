package ru.newlevel.hordemap.app

import android.app.Application

import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ru.newlevel.hordemap.di.dataModule
import ru.newlevel.hordemap.di.databaseModule
import ru.newlevel.hordemap.di.domainModule
import ru.newlevel.hordemap.di.presentationModule
import java.io.File

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApplication)
            modules(listOf(presentationModule, domainModule, dataModule, databaseModule))
        }
        FirebaseApp.initializeApp(applicationContext)
    }
}