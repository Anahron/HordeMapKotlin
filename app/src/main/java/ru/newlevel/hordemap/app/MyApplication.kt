package ru.newlevel.hordemap.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.util.Log

import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.di.dataModule
import ru.newlevel.hordemap.di.databaseModule
import ru.newlevel.hordemap.di.domainModule
import ru.newlevel.hordemap.di.presentationModule
import java.io.File

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        UserEntityProvider.sessionId = System.currentTimeMillis()
        createBackgroundWorkNotificationChannel()
        createNewMessagesNotificationChannel()
        val dexOutputDir: File = codeCacheDir
        dexOutputDir.setReadOnly()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@MyApplication)
            modules(listOf(presentationModule, domainModule, dataModule, databaseModule))
        }
        FirebaseApp.initializeApp(applicationContext)
    }

    private fun createBackgroundWorkNotificationChannel() {
        val channel = NotificationChannel(CHANEL_GPS, "GPS", NotificationManager.IMPORTANCE_DEFAULT)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        Log.e(TAG, "Background Work notification channel created")
    }
    private fun createNewMessagesNotificationChannel() {
        val channel = NotificationChannel(CHANEL_MESSAGE, "Messages", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
        Log.e(TAG, "New messages notification channel created")
    }
}