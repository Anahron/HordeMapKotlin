package ru.newlevel.hordemap.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.database.FirebaseDatabase
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "AAA"
private const val GEO_USER_MARKERS_PATH = "geoData0"

class BgLocationWorker(context: Context, param: WorkerParameters) :
    CoroutineWorker(context, param) {

    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }

    companion object {
        const val workName = "BgLocationWorker"
    }

    private val locationClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun doWork(): Result {
        Log.e(
            TAG, "doWork начала работать ",
        )
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
                Log.e(
                    TAG,
                    "Current Location = [lat : ${location.latitude}, lng : ${location.longitude}]",
                )
                val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
                if (userEntity != null) {
                    userDatabaseReference.child(userEntity.deviceID)
                        .setValue(mapLocationToMarker(location, userEntity))
                }
            }
        }
        return Result.success()
    }

    private fun mapLocationToMarker(
        location: Location,
        userDomainModel: UserDataModel
    ): MarkerDataModel {
        val marker = MarkerDataModel()
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")
        val date = dateFormat.format(Date(System.currentTimeMillis()))
        marker.latitude = location.latitude
        marker.longitude = location.longitude
        marker.userName = userDomainModel.name
        marker.deviceId = userDomainModel.deviceID
        marker.timestamp = System.currentTimeMillis()
        marker.item = userDomainModel.selectedMarker
        marker.title = date
        return marker
    }
}