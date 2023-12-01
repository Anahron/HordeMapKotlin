package ru.newlevel.hordemap.device

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.ACTION_PROCESS_UPDATES
import ru.newlevel.hordemap.app.LocationUpdatesBroadcastReceiver
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.presentation.MainActivity

class MyLocationManager : Service() {

    private var timeToSendData = 60000L

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private lateinit var locationRequest: LocationRequest

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE)
    }


    private fun startLocationUpdates() {
        Log.e("AAA", "startLocationUpdates()")
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            Log.e(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                9990,
                intent,
                PendingIntent.FLAG_IMMUTABLE
            )
        val channel = NotificationChannel("CHANNEL_1", "GPS", NotificationManager.IMPORTANCE_LOW)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this.applicationContext, "CHANNEL_1")
            .setSmallIcon(R.mipmap.hordecircle_round)
            .setContentTitle("Horde Map")
            .setContentText("Horde Map получает GPS данные в фоновом режиме")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setTimeoutAfter(500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setCategory(Notification.CATEGORY_LOCATION_SHARING)
        }
        return builder.build()
    }

    private fun startService() {
        Log.e(TAG, "startService()")
        val notification = createNotification()
        startForeground(1, notification)
        handler.postDelayed({
            timeToSendData = try {
                UserEntityProvider.userEntity.timeToSendData.times(1000L)
            } catch (e: Exception){
                30000
            }
            Log.e(TAG, "locationRequest set with $timeToSendData")
            locationRequest =
                LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, timeToSendData)
                    //TODO 0 для теста установить 10
                    .setMinUpdateDistanceMeters(12F)
                    .setMinUpdateIntervalMillis(6000L)
                    .setMaxUpdateAgeMillis(Long.MAX_VALUE)
                    .setMaxUpdateDelayMillis(timeToSendData)
                    .build()
            startLocationUpdates()
        }, 10000)
    }

    private fun stopService() {
        Log.d(TAG, "stopLocationUpdates()")
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private val handler = Handler(Looper.getMainLooper())
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e("AAA", "onStartCommand c " + intent?.action.toString())
        when (intent?.action) {
            ACTION_START -> startService()
            ACTION_STOP -> stopService()
        }
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}

