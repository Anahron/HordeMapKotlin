package ru.newlevel.hordemap.app

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.legacy.content.WakefulBroadcastReceiver
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.database.FirebaseDatabase
import ru.newlevel.hordemap.data.db.UserEntityProvider
import java.util.*

private const val TAG = "AAA"
private const val GEO_USER_MARKERS_PATH = "geoData0"

class MyAlarmReceiver : WakefulBroadcastReceiver() {
    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {
        val intentAlarm = Intent(context.applicationContext, MyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            intentAlarm,
            PendingIntent.FLAG_IMMUTABLE
        )
        val userEntity = UserEntityProvider.userEntity
        (context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?)?.setExactAndAllowWhileIdle(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            SystemClock.elapsedRealtime() + 600000,
            pendingIntent
        )

        Log.e(TAG, "onReceive в аларм менеджер")
        val fusedLocationClient: FusedLocationProviderClient by lazy {
            LocationServices.getFusedLocationProviderClient(context)
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                    CancellationTokenSource().token
                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener {
                if (it != null && userEntity != null) {
                    val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
                    Log.e(TAG, "SuccessListener в аларм менеджер " + it.toString())
                    userDatabaseReference.child(userEntity.deviceID)
                        .setValue(it.toMarker(userEntity))
                }
            }
    }
}
