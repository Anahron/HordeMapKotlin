package ru.newlevel.hordemap.presentation.map.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.collections.MarkerManager
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.GARMIN_TAG
import ru.newlevel.hordemap.data.db.UserEntityProvider
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.models.GarminGpxMarkersSet
import ru.newlevel.hordemap.domain.models.GarminMarkerModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

class MarkersUtils(private val garminGpxParser: GarminGpxParser) {


    suspend fun createGpxLayer(
        uri: Uri,
        markerCollection: MarkerManager.Collection,
        context: Context,
        googleMap: GoogleMap
    ): Polygon? {
        val parse = garminGpxParser.parseGpxFile(uri, context)
        parse?.let {
            createGarminMarkers(parse, markerCollection, context)
            parse.bounds?.let {
               googleMap.addPolygon(createGarminBounds(parse))
            }
        }
        return parse?.bounds?.let {googleMap.addPolygon(createGarminBounds(parse))}
    }

    private fun createGarminMarkers(
        garminGpxMarkersSet: GarminGpxMarkersSet, markerCollection: MarkerManager.Collection, context: Context
    ) {
        val markerSize = 70
        for (markerModel in garminGpxMarkersSet.markers) {
            val icon = MarkersItems.GpxMarkersItem.values()
                .find { it.markerType == markerModel.markerType && it.color == markerModel.markerColor }
                ?.let { createScaledBitmap(context, it.resourceId, markerSize) } ?: createScaledBitmap(
                context,
                R.drawable.marker_point0,
                markerSize
            )

            val marker: Marker? = markerCollection.addMarker(gpxModelToMarkerOptions(markerModel, icon))
            if (markerModel.name.isNotEmpty()) createTextMarker(markerModel, markerCollection)
            marker?.tag = GARMIN_TAG
        }
    }

    fun createGarminBounds(garminGpxMarkersSet: GarminGpxMarkersSet): PolygonOptions {
        return PolygonOptions().add(garminGpxMarkersSet.bounds?.let {
            LatLng(
                it.southwest.latitude, it.southwest.longitude
            )
        }).add(garminGpxMarkersSet.bounds?.let {
            LatLng(
                it.northeast.latitude, it.southwest.longitude
            )
        }).add(garminGpxMarkersSet.bounds?.let {
            LatLng(
                it.northeast.latitude, it.northeast.longitude
            )
        }).add(garminGpxMarkersSet.bounds?.let {
            LatLng(
                it.southwest.latitude, it.northeast.longitude
            )
        }).strokeColor(Color.BLACK).strokeWidth(8F).fillColor(Color.TRANSPARENT)
    }

    private fun createTextMarker(
        marker: GarminMarkerModel, markerCollection: MarkerManager.Collection
    ) {
        val text = marker.name
        val paint = createPaint()
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()
        val bitmap = Bitmap.createBitmap(textWidth + 15, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, (bitmap.width - textWidth) / 2f, 25f, paint)
        if (marker.markerType == "Navaid") markerCollection.addMarker(
            MarkerOptions().position(LatLng(marker.latLng.latitude, marker.latLng.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        ).setAnchor(0.55f, 0.5f)
        else markerCollection.addMarker(
            MarkerOptions().position(LatLng(marker.latLng.latitude, marker.latLng.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        ).setAnchor(0.5f, 0f)
    }


    fun createStaticMarkers(
        markersModel: List<MarkerDataModel>,
        markerCollection: MarkerManager.Collection, context: Context
    ) {
        MARKER_SIZE_STATIC = UserEntityProvider.userEntity?.staticMarkerSize!!
        for (markerModel in markersModel) {
            if (markerModel.title != "Маркер" && markerModel.title.isNotEmpty())
                createStaticTextMarker(markerModel, markerCollection)
            val icon = MarkersItems.StaticMarkersItem.values()
                .find { it.id == markerModel.item }
                ?.let {
                    createScaledBitmap(
                        context,
                        it.resourceId,
                        MARKER_SIZE_STATIC
                    )
                }
                ?: createScaledBitmap(
                    context,
                    R.drawable.marker_point0,
                    MARKER_SIZE_STATIC
                )
            val marker: Marker? =
                markerCollection.addMarker(markerModelToMarkerOptions(markerModel, icon, 1))
            marker?.tag = markerModel.timestamp
        }
    }

    private fun createStaticTextMarker(
        marker: MarkerDataModel,
        markerCollection: MarkerManager.Collection
    ) {
        val text =
            if (marker.title.length > 10) "${marker.title.substring(0, 7)}..." else marker.title
        val paint = createPaint()
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()
        val bitmap = Bitmap.createBitmap(textWidth + 15, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, (bitmap.width - textWidth) / 2f, 25f, paint)
        markerCollection.addMarker(
            MarkerOptions()
                .position(LatLng(marker.latitude, marker.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        ).setAnchor(0.5f, 0f)
    }

    fun createUsersMarkers(
        markersModels: List<MarkerDataModel>,
        markerCollection: MarkerManager.Collection,
        context: Context
    ) {
        MARKER_SIZE_USERS = UserEntityProvider.userEntity?.usersMarkerSize!!
        for (markerModel in markersModels) {
            if (UserEntityProvider.userEntity?.deviceID == markerModel.deviceId)
                continue
            val icon = MarkersItems.UsersMarkersItem.values().find { it.id == markerModel.item }
                ?.let {
                    createScaledBitmap(
                        context,
                        it.resourceId,
                        MARKER_SIZE_USERS
                    )
                }
                ?: createScaledBitmap(
                    context,
                    R.drawable.img_marker_red,
                    MARKER_SIZE_USERS
                )
            markerCollection.addMarker(markerModelToMarkerOptions(markerModel, icon, 0))
        }
    }

    private fun markerModelToMarkerOptions(
        markerModel: MarkerDataModel,
        icon: BitmapDescriptor,
        flagToChooseTitle: Int
    ): MarkerOptions {
        return MarkerOptions()
            .title(if (flagToChooseTitle == 0) markerModel.userName else markerModel.title)
            .position(LatLng(markerModel.latitude, markerModel.longitude))
            .alpha(markerModel.alpha)
            .snippet(dateFormat.format(Date(markerModel.timestamp)))
            .icon(icon)
    }

    private fun createPaint(): Paint {
        return Paint().apply {
            textSize = 30f
            color = Color.WHITE
            isFakeBoldText = true
            setShadowLayer(10f, 0f, 0f, Color.BLACK)
            isAntiAlias = true
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                blendMode = BlendMode.SRC_OVER
            }
        }
    }

    private fun gpxModelToMarkerOptions(
        markerModel: GarminMarkerModel,
        icon: BitmapDescriptor,
    ): MarkerOptions {
        return MarkerOptions().position(LatLng(markerModel.latLng.latitude, markerModel.latLng.longitude)).icon(icon)
    }

    private fun createScaledBitmap(
        context: Context, resourceId: Int, size: Int
    ): BitmapDescriptor {
        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        return BitmapDescriptorFactory.fromBitmap(
            Bitmap.createScaledBitmap(
                bitmap, size, size, false
            )
        )
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        val dateFormat: DateFormat = SimpleDateFormat("HH:mm")
        var MARKER_SIZE_USERS = 40
        var MARKER_SIZE_STATIC = 40
    }
}