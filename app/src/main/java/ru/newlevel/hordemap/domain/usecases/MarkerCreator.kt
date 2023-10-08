package ru.newlevel.hordemap.domain.usecases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.models.MarkerModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
private val timeZone = TimeZone.getDefault()
const val MARKER_SIZE_USERS = 40


class MarkerCreator(private var context: Context, private var googleMap: GoogleMap) {
    private val bitmap0: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.img_marker_red)
    private val bitmap1: Bitmap =
        BitmapFactory.decodeResource(context.resources, R.drawable.img_marker_yellow)
    private val bitmap2 =
        BitmapFactory.decodeResource(context.resources, R.drawable.img_marker_green)
    private val bitmap3 =
        BitmapFactory.decodeResource(context.resources, R.drawable.img_marker_blue)
    private val bitmap4 =
        BitmapFactory.decodeResource(context.resources, R.drawable.img_marker_purple)

    private val savedMarkers: ArrayList<Marker> = ArrayList()

    fun createMarkers(markersModels: List<MarkerModel>) {
        for (marker in savedMarkers)
            marker.remove()
        for (markerModel in markersModels) {
            val marker = googleMap.addMarker(markerModelToMarkerOptions(markerModel))
            if (marker != null) {
                savedMarkers.add(marker)
            }
        }
    }

    private fun markerModelToMarkerOptions(markerModel: MarkerModel): MarkerOptions {

        //  if (User.getInstance().getDeviceId().equals(myMarker.getDeviceId())) continue
        val icon = when (markerModel.item) {
            1 -> BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap1,
                    MARKER_SIZE_USERS,
                    MARKER_SIZE_USERS,
                    false
                )
            )
            2 -> BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap2,
                    MARKER_SIZE_USERS,
                    MARKER_SIZE_USERS,
                    false
                )
            )
            3 -> BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap3,
                    MARKER_SIZE_USERS,
                    MARKER_SIZE_USERS,
                    false
                )
            )
            4 -> BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap4,
                    MARKER_SIZE_USERS,
                    MARKER_SIZE_USERS,
                    false
                )
            )
            else -> BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap0,
                    MARKER_SIZE_USERS,
                    MARKER_SIZE_USERS,
                    false
                )
            )
        }

        return MarkerOptions().title(markerModel.userName)
            .position(LatLng(markerModel.latitude, markerModel.longitude)).alpha(markerModel.alpha)
            .snippet(dateFormat.format(Date(markerModel.timestamp))).icon(icon)
    }
}

