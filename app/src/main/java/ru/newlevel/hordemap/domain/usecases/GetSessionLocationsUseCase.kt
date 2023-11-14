package ru.newlevel.hordemap.domain.usecases

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.domain.models.TrackItemDomainModel
import ru.newlevel.hordemap.domain.repository.LocationRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class GetSessionLocationsUseCase(private val locationRepository: LocationRepository) {

    fun getCurrentSessionLocations(sessionId: String): TrackItemDomainModel {
        val locationEntity = locationRepository.getLocationsSortedByUpdateTime(sessionId)
        val locationsList = locationEntity.map {
            LatLng(it.latitude, it.longitude)
        }
        return TrackItemDomainModel(
            "My track",
            convertTimestampToDate(locationEntity[0].date.time),
            calculateDurations(locationEntity[0].date.time - locationEntity[locationEntity.lastIndex].date.time),
            calculateDistance(locationsList),
            locationsList
        )
    }

    suspend fun getAllSessionLocations(): List<TrackItemDomainModel> {
        val uniqueSessionIds = locationRepository.getAllLocationsGroupedBySessionId()
        val allLocations = mutableListOf<TrackItemDomainModel>()

        for (sessionId in uniqueSessionIds) {
            val locationEntity = locationRepository.getLocationsSortedByUpdateTime(sessionId)
            val locationsList = locationEntity.map {
                LatLng(it.latitude, it.longitude)
            }
            if (locationEntity[0].sessionId != UserEntityProvider.sessionId.toString()) {
                allLocations.add(
                    TrackItemDomainModel(
                        "My track",
                        convertTimestampToDate(locationEntity[0].date.time),
                        calculateDurations(locationEntity[0].date.time - locationEntity[locationEntity.lastIndex].date.time),
                        calculateDistance(locationsList),
                        locationsList
                    )
                )
            }
        }
        return allLocations
    }

    private fun calculateDistance(latLngList: List<LatLng>): String {
        val formattedString = StringBuilder()
        var result = ""
        var resultDistance = SphericalUtil.computeLength(latLngList)
        if (resultDistance < 1000) {
            result = resultDistance.roundToInt().toString()
            formattedString.append(result + "m ")
        } else {
            resultDistance /= 1000
            result = String.format("%.2f", resultDistance).toDouble().toString()
            formattedString.append(result + "km ")
        }
        return formattedString.toString()

    }

    private fun calculateDurations(milliseconds: Long): String {
        val days = (milliseconds / (1000 * 60 * 60 * 24)).toInt()
        val hours = ((milliseconds % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)).toInt()
        val minutes = ((milliseconds % (1000 * 60 * 60)) / (1000 * 60)).toInt()

        val formattedString = StringBuilder()

        if (days > 0) {
            formattedString.append("$days" + "d ")
        }
        if (hours > 0) {
            formattedString.append("$hours" + "h ")
        }
        if (minutes > 0) {
            formattedString.append("$minutes" + "m")
        } else
            formattedString.append("0m")
        return formattedString.toString()
    }

    private fun convertTimestampToDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        val date = Date(timestamp)
        return dateFormat.format(date)
    }
}