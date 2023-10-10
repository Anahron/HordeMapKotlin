package ru.newlevel.hordemap.app

import android.Manifest
import android.app.*
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.presentatin.MainActivity


class GpsForegroundService : Service() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    companion object {
        const val ACTION_LOCATION_UPDATE = "ru.newlevel.hordemap.ACTION_LOCATION_UPDATE"
        const val EXTRA_LOCATION = "extra_location"
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("AAA", "onCreate в LocationUpdateService вызван")
        startForeground(1, createNotification())
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("AAA", "onStartCommand в LocationUpdateService вызван")
        requestLocationUpdates()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this, 9990, intent, PendingIntent.FLAG_IMMUTABLE)

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

     //   val locationRequest = LocationRequest.Builder(30000)

        val locationRequest = LocationRequest.create().apply {
            interval = 60000 // Интервал обновления местоположения в миллисекундах (10 секунд)
            fastestInterval = 30000 // Самый быстрый интервал обновления в миллисекундах (5 секунд)
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY // Приоритет обновления
        }

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                p0.lastLocation?.let { location ->
                    // Ваш код обработки нового местоположения
                    Log.e("AAA", "Новое местоположение: $location")
                    sendLocationUpdate(location)
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }


    private fun sendLocationUpdate(location: Location) {
        Log.e("AAA", "Местоположение отправлено в бродкаст")
        val intent = Intent(ACTION_LOCATION_UPDATE)
        intent.putExtra(EXTRA_LOCATION, location)
        sendBroadcast(intent)
    }
}
