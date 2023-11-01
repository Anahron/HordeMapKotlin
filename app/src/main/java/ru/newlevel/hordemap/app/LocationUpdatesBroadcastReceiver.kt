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
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "AAA"
private const val GEO_USER_MARKERS_PATH = "geoData0"

class LocationUpdatesBroadcastReceiver : BroadcastReceiver() {
    private val databaseReference by lazy(LazyThreadSafetyMode.NONE) { FirebaseDatabase.getInstance().reference }

    @SuppressLint("SuspiciousIndentation")
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_PROCESS_UPDATES) {
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.e(TAG, "Location services are no longer available!")
                }
            }
            Log.e(TAG, LocationResult.extractResult(intent).toString())
            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations
                if (locations.isNotEmpty()) {
                    val userEntity = UserEntityProvider.userEntity
                    val location = locationResult.lastLocation
                    if (location != null && userEntity != null) {
                        Log.e(TAG, "Отправляется на сервер" + location.toString())
                        val userDatabaseReference = databaseReference.child(GEO_USER_MARKERS_PATH)
                            userDatabaseReference.child(userEntity.deviceID).setValue(mapLocationToMarker(location, userEntity))
                    }
                }
            }
        } else   Log.e(TAG, "intent.action != ACTION_PROCESS_UPDATES")
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
