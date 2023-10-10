package ru.newlevel.hordemap.app

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.presentatin.MainActivity

class GpsForegroundService: Service() {

    val locationLiveData = MutableLiveData<Location>()

    private val locationRequest = com.google.android.gms.location.LocationRequest().apply {
        interval = 60000 // Обновление раз в минуту
        fastestInterval = 30000
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        Log.e("AAA", "GpsForegroundService создан")
        ServiceLocator.gpsService = this
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        requestLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        // Возвращает объект, который реализует интерфейс для взаимодействия с фрагментом
        return MyBinder()
    }

    inner class MyBinder : Binder() {
        // Метод для получения экземпляра службы
        fun getService(): GpsForegroundService {
            return this@GpsForegroundService
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        removeLocationUpdates()
    }
    private fun startForeground() {
        startForeground(FOREGROUND_SERVICE_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(this, 9990, intent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel("CHANNEL_1", "GPS", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, "CHANNEL_1")
            .setSmallIcon(R.mipmap.hordecircle_round)
            .setContentTitle("Horde Map")
            .setContentText("Horde Map получает GPS данные в фоновом режиме")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setTimeoutAfter(500)

        return builder.build()
    }
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    private fun removeLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    locationLiveData.postValue(location)
                }
            }
        }
    }


    companion object {
        private const val FOREGROUND_SERVICE_ID = 1
    }
}