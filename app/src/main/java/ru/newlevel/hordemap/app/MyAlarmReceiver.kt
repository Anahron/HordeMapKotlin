package ru.newlevel.hordemap.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.koin.core.component.KoinComponent
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase

class MyAlarmReceiver : BroadcastReceiver(), KoinComponent {

    @SuppressLint("MissingPermission")
    override fun onReceive(context: Context, intent: Intent?) {
        getKoin().get<MyAlarmManager>().startAlarmManager()
        val userEntity = try {
            getKoin().get<GetUserSettingsUseCase>().execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user settings", e)
            return
        }
        Log.e(TAG, "onReceive in MyAlarmReceiver")
        if (context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) || context.hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION))
            LocationServices.getFusedLocationProviderClient(context).getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(listener: OnTokenCanceledListener) =
                        CancellationTokenSource().token
                    override fun isCancellationRequested() = false
                }).addOnSuccessListener {
                    if (it != null) {
                        getKoin().get<MarkersRemoteStorage>().sendUserMarker(it.toMarker(userEntity))
                        Log.e(TAG, "SuccessListener in MyAlarmReceiver $it")
                    }
                }
    }
}
