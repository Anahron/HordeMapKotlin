package ru.newlevel.hordemap.domain.usecases.markersCases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolygonOptions
import com.google.maps.android.collections.MarkerManager
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.GARMIN_TAG
import ru.newlevel.hordemap.domain.models.GarminGpxMarkersSet
import ru.newlevel.hordemap.domain.models.GarminMarkerModel

class CreateGarminMarkersUseCase() {
    fun createGarminMarkers(
        garminGpxMarkersSet: GarminGpxMarkersSet,
        markerCollection: MarkerManager.Collection,
        context: Context
    ) {
        val markerSize = 60
        for (markerModel in garminGpxMarkersSet.markers) {
            if (markerModel.name.isNotEmpty())
                createTextMarker(markerModel, markerCollection)
            val icon = GarminMarkersItem.values()
                .find { it.markerType == markerModel.markerType && it.color == markerModel.markerColor }
                ?.let { createScaledBitmap(context, it.resourceId, markerSize) }
                ?: createScaledBitmap(context, R.drawable.marker_point0, markerSize)

            val marker: Marker? =
                markerCollection.addMarker(markerModelToMarkerOptions(markerModel, icon))
            marker?.tag = GARMIN_TAG
        }
    }

    fun createGarminBounds(garminGpxMarkersSet: GarminGpxMarkersSet): PolygonOptions {
        return PolygonOptions()
            .add(
                garminGpxMarkersSet.bounds?.let {
                    LatLng(
                        it.southwest.latitude,
                        it.southwest.longitude
                    )
                }
            )
            .add(
                garminGpxMarkersSet.bounds?.let {
                    LatLng(
                        it.northeast.latitude,
                        it.southwest.longitude
                    )
                }
            )
            .add(
                garminGpxMarkersSet.bounds?.let {
                    LatLng(
                        it.northeast.latitude,
                        it.northeast.longitude
                    )
                }
            )
            .add(
                garminGpxMarkersSet.bounds?.let {
                    LatLng(
                        it.southwest.latitude,
                        it.northeast.longitude
                    )
                }
            )
            .strokeColor(Color.BLACK)
            .strokeWidth(8F)
            .fillColor(Color.TRANSPARENT)
    }

    private fun createTextMarker(
        marker: GarminMarkerModel,
        markerCollection: MarkerManager.Collection
    ) {
        val text = marker.name
        val paint = createPaint()
        val textBounds = Rect()
        paint.getTextBounds(text, 0, text.length, textBounds)
        val textWidth = textBounds.width()

        val bitmap = Bitmap.createBitmap(textWidth + 15, 100, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawText(text, (bitmap.width - textWidth) / 2f, 25f, paint)

        markerCollection.addMarker(
            MarkerOptions()
                .position(LatLng(marker.latLng.latitude, marker.latLng.longitude))
                .icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        ).setAnchor(0.5f, 0f)
    }


    private enum class GarminMarkersItem(
        val resourceId: Int,
        val markerType: String,
        val color: String
    ) {
        DEFAULT(R.drawable.marker_point0, "DEFAULT", ""),
        FLAG_GREEN(R.drawable.marker_flag_green, "Flag", "Green"),
        FLAG_RED(R.drawable.marker_flag_red, "Flag", "Red"),
        FLAG_BLUE(R.drawable.marker_flag_blue, "Flag", "Blue"),
        FLAG_YELLOW(R.drawable.marker_flag_yellow, "Flag", "Yellow"),
        NAVAID_RED(R.drawable.marker_navaid_red, "Navaid", "Red"),
        NAVAID_YELLOW(R.drawable.marker_navaid_yellow, "Navaid", "Yellow"),
        NAVAID_GREEN(R.drawable.marker_navaid_green, "Navaid", "Green"),
        NAVAID_BLUE(R.drawable.marker_navaid_blue, "Navaid", "Blue"),
        NAVAID_WHITE(R.drawable.marker_navaid_white, "Navaid", "White"),
        NAVAID_ORANGE(R.drawable.marker_navaid_orange, "Navaid", "Orange"),
        NAVAID_RED_WHITE(R.drawable.marker_navaid_red_white, "Navaid", "Red/White"),
        NAVAID_RED_GREEN(R.drawable.marker_navaid_red_green, "Navaid", "Red/Green"),
        NAVAID_GREEN_RED(R.drawable.marker_navaid_green_red, "Navaid", "Green/Red"),
        NAVAID_GREEN_WHITE(R.drawable.marker_navaid_green_white, "Navaid", "Green/White"),
        NAVAID_WHITE_GREEN(R.drawable.marker_navaid_white_green, "Navaid", "White/Green"),
        NAVAID_WHITE_RED(R.drawable.marker_navaid_white_red, "Navaid", "White/Red"),
        NAVAID_AMBER(R.drawable.marker_navaid_amber, "Navaid", "Amber"),
        NAVAID_BLACK(R.drawable.marker_navaid_black, "Navaid", "Black"),
        NAVAID_VIOLET(R.drawable.marker_navaid_violet, "Navaid", "Violet"),
        BLOCK_RED(R.drawable.marker_block_red, "Block", "Red"),
        BLOCK_YELLOW(R.drawable.marker_block_yellow, "Block", "Yellow"),
        BLOCK_GREEN(R.drawable.marker_block_green, "Block", "Green"),
        BLOCK_BLUE(R.drawable.marker_block_blue, "Block", "Blue"),
        CAMPGROUND(R.drawable.marker_campground, "Campground", ""),
        HELIPORT(R.drawable.marker_heliport, "Heliport", ""),
        PARKING_AREA(R.drawable.marker_parking, "Parking Area", ""),
        MEDICAL_FACILLITY(R.drawable.marker_medical_facillity, "Medical Facility", ""),
        CEMETERY(R.drawable.marker_cemetery, "Cemetery", ""),
        RESIDENCE(R.drawable.marker_residence, "Residence", ""),
        OIL_FIELD(R.drawable.marker_oil_field, "Oil Field", ""),
    }

    private fun createPaint(): Paint {
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

    private fun markerModelToMarkerOptions(
        markerModel: GarminMarkerModel,
        icon: BitmapDescriptor,
    ): MarkerOptions {
        return MarkerOptions()
            .title(markerModel.name)
            .position(LatLng(markerModel.latLng.latitude, markerModel.latLng.longitude))
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