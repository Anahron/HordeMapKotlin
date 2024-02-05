package ru.newlevel.hordemap.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.database.FirebaseDatabase
import ru.newlevel.hordemap.data.db.UserEntityProvider

class BgLocationWorker(context: Context, param: WorkerParameters) :
    CoroutineWorker(context, param) {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun doWork(): Result {
        Log.e(TAG, "doWork начала работать ")
        val userEntity = UserEntityProvider.userEntity
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return Result.failure()
        }
        locationClient.getCurrentLocation(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY, CancellationTokenSource().token,
        ).addOnSuccessListener { location ->
            location?.let {
                Log.e(TAG, "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",)
                try {
                    val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
                    userDatabaseReference.child(userEntity.deviceID)
                        .setValue(location.toMarker(userEntity))
                }catch (e: Exception){
                    Log.e(TAG, "UserEntityProvider.userEntity not initialized",)
                }
            }
        }
        return Result.success()
    }
}