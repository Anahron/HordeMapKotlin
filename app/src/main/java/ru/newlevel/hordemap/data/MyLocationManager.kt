package ru.newlevel.hordemap.data

import android.Manifest
import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.annotation.MainThread
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.LocationUpdatesBroadcastReceiver
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.hasPermission
import ru.newlevel.hordemap.presentatin.MainActivity
import java.util.concurrent.TimeUnit

private const val TAG = "AAA"

class MyLocationManager : Service() {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private lateinit var locationRequest: LocationRequest

    private val locationUpdatePendingIntent: PendingIntent by lazy {
        val intent = Intent(this.applicationContext, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES
        PendingIntent.getBroadcast(this.applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    @Throws(SecurityException::class)
    @MainThread
    fun startLocationUpdates() {
        Log.d(TAG, "startLocationUpdates()")
        if (!this.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationUpdatePendingIntent)
        } catch (permissionRevoked: SecurityException) {
            Log.d(TAG, "Location permission revoked; details: $permissionRevoked")
            throw permissionRevoked
        }
    }

    @MainThread
    fun stopLocationUpdates() {
        Log.d(TAG, "stopLocationUpdates()")
        fusedLocationClient.removeLocationUpdates(locationUpdatePendingIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timeToSendData = UserEntityProvider.userEntity?.timeToSendData
        locationRequest = LocationRequest().apply {
            interval = if (timeToSendData != null) timeToSendData * 1000L else 60000
            fastestInterval = if (timeToSendData != null) timeToSendData * 1000L / 2 else 30000
            maxWaitTime = TimeUnit.MINUTES.toMillis(1)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        startLocationUpdates()
        val notification = createNotification()
        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotification(): Notification {
        val intent = Intent(this.applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent =
            PendingIntent.getActivity(this.applicationContext, 9990, intent, PendingIntent.FLAG_IMMUTABLE)

        val channel = NotificationChannel("CHANNEL_1", "GPS", NotificationManager.IMPORTANCE_HIGH)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this.applicationContext, "CHANNEL_1")
            .setSmallIcon(R.mipmap.hordecircle_round)
            .setContentTitle("Horde Map")
            .setContentText("Horde Map получает GPS данные в фоновом режиме")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_LOCATION_SHARING)
            .setTimeoutAfter(500)

        return builder.build()
    }
}
