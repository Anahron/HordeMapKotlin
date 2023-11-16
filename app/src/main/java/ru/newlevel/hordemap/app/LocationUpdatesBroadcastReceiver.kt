package ru.newlevel.hordemap.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import ru.newlevel.hordemap.data.db.MyLocationDatabase
import ru.newlevel.hordemap.data.db.MyLocationEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.data.storage.models.UserDataModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*

private const val TAG = "AAA"

class LocationUpdatesBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent) {
        Log.e(TAG, "onReceive in LocationUpdatesBroadcastReceiver")
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
                    saveLocation(locations)
                }
            }
        } else Log.e(TAG, "intent.action != ACTION_PROCESS_UPDATES")
    }

    private fun saveLocation(locations: List<Location>) {
        val userEntity = UserEntityProvider.userEntity
        val location = locations[locations.lastIndex]
        Log.e(TAG, "Location result = $location")
        CoroutineScope(Dispatchers.IO).launch {
            val locationDao = getKoin().get<MyLocationDatabase>().locationDao()
            locations.forEach { location ->
                locationDao.addLocation(
                    MyLocationEntity(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        date = Date(location.time),
                        sessionId = UserEntityProvider.sessionId.toString()
                    )
                )
            }
            userEntity?.let {
                getKoin().get<MarkersRemoteStorage>()
                    .sendUserMarker(mapLocationToMarker(location, it))
            }
        }
    }

    private fun mapLocationToMarker(
        location: Location,
        userDomainModel: UserDataModel
    ): MarkerDataModel {
        val marker = MarkerDataModel()
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
        val currentTime = LocalTime.now()
        val formattedTime = currentTime.format(timeFormatter)
        marker.latitude = location.latitude
        marker.longitude = location.longitude
        marker.userName = userDomainModel.name
        marker.deviceId = userDomainModel.deviceID
        marker.timestamp = System.currentTimeMillis()
        marker.item = userDomainModel.selectedMarker
        marker.title = formattedTime
        return marker
    }

    companion object {
        const val ACTION_PROCESS_UPDATES =
            "ru.newlevel.hordemap.app.action." +
                    "PROCESS_UPDATES"
    }
}
