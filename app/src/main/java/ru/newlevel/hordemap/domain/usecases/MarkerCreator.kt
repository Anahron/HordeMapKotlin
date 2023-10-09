package ru.newlevel.hordemap.domain.usecases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.models.MarkerModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
private val timeZone = TimeZone.getDefault()
const val MARKER_SIZE_USERS = 40


class MarkerCreator(private var context: Context, private var googleMap: GoogleMap, private val userDomainModel: UserDomainModel) {

    private enum class UsersMarkersItem(val resourceId: Int) {
        RED(R.drawable.img_marker_red),
        YELLOW(R.drawable.img_marker_yellow),
        GREEN(R.drawable.img_marker_green),
        BLUE(R.drawable.img_marker_blue),
        PURPLE(R.drawable.img_marker_purple)
    }

    private val savedMarkers: ArrayList<Marker> = ArrayList()

    fun createUsersMarkers(markersModels: List<MarkerModel>) {
        for (marker in savedMarkers)
            marker.remove()
        for (markerModel in markersModels) {
            if (userDomainModel.deviceID == markerModel.deviceId)
                continue
            val icon = UsersMarkersItem.values().find { it.ordinal == markerModel.item }
                ?.let { createScaledBitmap(context, it.resourceId) }
                ?: createScaledBitmap(context, R.drawable.img_marker_red)

            val marker = googleMap.addMarker(markerModelToMarkerOptions(markerModel, icon))
            if (marker != null) {
                savedMarkers.add(marker)
            }
        }
    }

    companion object {
        private fun markerModelToMarkerOptions(
            markerModel: MarkerModel,
            icon: BitmapDescriptor
        ): MarkerOptions {
            return MarkerOptions()
                .title(markerModel.userName)
                .position(LatLng(markerModel.latitude, markerModel.longitude))
                .alpha(markerModel.alpha)
                .snippet(dateFormat.format(Date(markerModel.timestamp)))
                .icon(icon)
        }

        private fun createScaledBitmap(context: Context, resourceId: Int): BitmapDescriptor {
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            return BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap,
                    MARKER_SIZE_USERS,
                    MARKER_SIZE_USERS,
                    false
                )
            )
        }
    }
}

