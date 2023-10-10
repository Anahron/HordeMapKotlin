package ru.newlevel.hordemap.domain.usecases

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.data.storage.models.MarkerModel
import ru.newlevel.hordemap.domain.models.UserDomainModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
private val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
private var MARKER_SIZE_USERS = 40
private var MARKER_SIZE_STATIC = 40


class MarkerCreator(
    private var context: Context,
    private var googleMap: GoogleMap,
    private val userDomainModel: UserDomainModel
) {

    fun createStaticMarkers(markersModel: List<MarkerModel>) {
        MARKER_SIZE_STATIC = userDomainModel.staticMarkerSize
        for (marker in MarkerManager.getSavedStaticMarkers())
            marker.remove()
        for (marker in MarkerManager.getTextMarkers())
            marker.remove()
        MarkerManager.clearSavedStaticMarker()
        MarkerManager.clearTextMarker()

        for (markerModel in markersModel) {
            if (markerModel.item > 10)
                createTextMarker(markerModel)
            val icon = StaticMarkersItem.values()
                .find { it.ordinal == if (markerModel.item > 10) markerModel.item - 6 else markerModel.item }
                ?.let { createScaledBitmap(context, it.resourceId, MARKER_SIZE_STATIC) }
                ?: createScaledBitmap(context, R.drawable.marker_point0, MARKER_SIZE_STATIC)

            val marker: Marker? =
                googleMap.addMarker(staticMarkerModelToMarkerOptions(markerModel, icon))

            marker?.tag = markerModel.timestamp
            if (marker != null) {
                MarkerManager.addSavedStaticMarker(marker)
            }
        }
    }

    private fun createTextMarker(myMarker: MarkerModel) {
        val text = if (myMarker.title.length > 10) "${myMarker.title.substring(0, 7)}..." else myMarker.title

        val paint = createPaint()
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()

        val bitmap = Bitmap.createBitmap(textWidth + 15, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, (bitmap.width - textWidth) / 2f, 25f, paint)

        val markerText = googleMap.addMarker(
            MarkerOptions()
                .position(LatLng(myMarker.latitude, myMarker.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        )

        markerText?.setAnchor(0.5f, 0f)

        if (markerText != null) {
            MarkerManager.addTextMarker(markerText)
        }
    }

    fun createUsersMarkers(markersModels: List<MarkerModel>) {
        MARKER_SIZE_USERS = userDomainModel.usersMarkerSize
        for (marker in MarkerManager.getSavedUsersMarkers())
            marker.remove()
        MarkerManager.clearSavedUserMarker()

        for (markerModel in markersModels) {
            if (userDomainModel.deviceID == markerModel.deviceId)
                continue
            val icon = UsersMarkersItem.values().find { it.ordinal == markerModel.item }
                ?.let { createScaledBitmap(context, it.resourceId, MARKER_SIZE_USERS) }
                ?: createScaledBitmap(context, R.drawable.img_marker_red, MARKER_SIZE_USERS)

            val marker = googleMap.addMarker(userMarkerModelToMarkerOptions(markerModel, icon))
            if (marker != null) {
              MarkerManager.addSavedUserMarker(marker)
            }
        }
    }

    companion object {
        private enum class UsersMarkersItem(val resourceId: Int) {
            RED(R.drawable.img_marker_red),
            YELLOW(R.drawable.img_marker_yellow),
            GREEN(R.drawable.img_marker_green),
            BLUE(R.drawable.img_marker_blue),
            PURPLE(R.drawable.img_marker_purple)
        }

        private enum class StaticMarkersItem(val resourceId: Int) {
            DEFAULT(R.drawable.marker_point0),
            SWORDS(R.drawable.img_marker_swords),
            FLAG_RED(R.drawable.flag_red),
            FLAG_YELLOW(R.drawable.flag_yellow),
            FLAG_GREEN(R.drawable.flag_green),
            FLAG_BLUE(R.drawable.flag_blue),
            MARKER_1(R.drawable.marker_point1),
            MARKER_2(R.drawable.marker_point2),
            MARKER_3(R.drawable.marker_point3),
            MARKER_4(R.drawable.marker_point4),
            MARKER_5(R.drawable.marker_point5),
            MARKER_6(R.drawable.marker_point6),
            MARKER_7(R.drawable.marker_point7),
            MARKER_8(R.drawable.marker_point8),
            MARKER_9(R.drawable.marker_point9)
        }

        private fun createPaint(): Paint{
            return Paint().apply {
                textSize = 25f
                color = Color.BLACK
                isFakeBoldText = true
                setShadowLayer(8f, 0f, 0f, Color.WHITE)
                isAntiAlias = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    blendMode = BlendMode.SRC_OVER
                }
            }
        }

        private fun userMarkerModelToMarkerOptions(
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

        private fun staticMarkerModelToMarkerOptions(
            markerModel: MarkerModel,
            icon: BitmapDescriptor
        ): MarkerOptions {
            return MarkerOptions()
                .title(markerModel.title)
                .position(LatLng(markerModel.latitude, markerModel.longitude))
                .alpha(markerModel.alpha)
                .snippet(dateFormat.format(Date(markerModel.timestamp)))
                .icon(icon)
        }


        private fun createScaledBitmap(
            context: Context,
            resourceId: Int,
            size: Int
        ): BitmapDescriptor {
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
            return BitmapDescriptorFactory.fromBitmap(
                Bitmap.createScaledBitmap(
                    bitmap,
                    size,
                    size,
                    false
                )
            )
        }
    }
}

