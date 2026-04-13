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
import ru.newlevel.hordemap.data.db.MyDatabase
import ru.newlevel.hordemap.data.db.MyLocationEntity
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.interfaces.MarkersRemoteStorage
import ru.newlevel.hordemap.domain.usecases.mapCases.GetUserSettingsUseCase
import java.util.Date


class LocationUpdatesBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.e(TAG, "onReceive in LocationUpdatesBroadcastReceiver")
        if (intent?.action == ACTION_PROCESS_UPDATES) {
            LocationAvailability.extractLocationAvailability(intent)?.let { locationAvailability ->
                if (!locationAvailability.isLocationAvailable) {
                    Log.e(TAG, "Location services are no longer available!")
                }
            }
            LocationResult.extractResult(intent)?.let { locationResult ->
                val locations = locationResult.locations
                if (locations.isNotEmpty()) {
                    saveLocation(locations)
                }
            }
        } else Log.e(TAG, "intent.action != ACTION_PROCESS_UPDATES")
    }

    private fun saveLocation(locations: List<Location>) {
        val userEntity = try {
            getKoin().get<GetUserSettingsUseCase>().execute()
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user settings", e)
            return
        }

        val location = locations.last()
        CoroutineScope(Dispatchers.IO).launch {
            val locationDao = getKoin().get<MyDatabase>().locationDao()
            locations.forEach { loc ->
                if (loc.accuracy < 25) {
                    locationDao.addLocation(
                        MyLocationEntity(
                            latitude = loc.latitude,
                            longitude = loc.longitude,
                            date = Date(loc.time),
                            sessionId = UserEntityProvider.sessionId.toString()
                        )
                    )
                }
            }
            getKoin().get<MarkersRemoteStorage>().sendUserMarker(location.toMarker(userEntity))
            Log.e(TAG, "sendUserMarker in BroadcastReceiver")
        }
    }
}
