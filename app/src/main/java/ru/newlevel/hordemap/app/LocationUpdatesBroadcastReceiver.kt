package ru.newlevel.hordemap.app

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import com.google.firebase.database.FirebaseDatabase
import ru.newlevel.hordemap.data.db.MyLocationEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "AAA"

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }
    private val GEO_USER_MARKERS_PATH = "geoData0"

    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "onReceive() context:$context, intent:$intent")
        if (intent.action == ACTION_PROCESS_UPDATES) {
            // Checks for location availability changes.
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.e(TAG, "Location services are no longer available!")
                }
            }

            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations.map { location ->
                    MyLocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        date = Date(location.time)
                    )
                }
                if (locations.isNotEmpty()) {
                    val userEntity = UserEntityProvider.userEntity
                    val location = locationResult.lastLocation
                    if (location != null && userEntity != null) {
                        val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
                            userDatabaseReference.child(userEntity.deviceID).setValue(mapLocationToMarker(location, userEntity))
                    }
                }
            }
        }
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

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "ru.newlevel.hordemap.app.action." +
                    "PROCESS_UPDATES"
    }
}
