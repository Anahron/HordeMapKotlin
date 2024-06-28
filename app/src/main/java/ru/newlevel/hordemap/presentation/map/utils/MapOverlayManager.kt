package ru.newlevel.hordemap.presentation.map.utils

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.net.Uri
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
import com.google.maps.android.collections.GroundOverlayManager
import com.google.maps.android.collections.MarkerManager
import com.google.maps.android.collections.PolygonManager
import com.google.maps.android.collections.PolylineManager
import com.google.maps.android.data.kml.KmlLayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.xmlpull.v1.XmlPullParserException
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.GPX_EXTENSION
import ru.newlevel.hordemap.app.KMZ_EXTENSION
import ru.newlevel.hordemap.app.getFileNameFromUri
import ru.newlevel.hordemap.app.getLatLng
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.usecases.mapCases.CreateRouteUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase

class MapOverlayManager(googleMap: GoogleMap) : KoinComponent {

    var isMarkersVisible: Boolean = true
    private val markerManager: MarkerManager = MarkerManager(googleMap)
    private val groundOverlayManager: GroundOverlayManager = GroundOverlayManager(googleMap)
    private val polygonManager: PolygonManager = PolygonManager(googleMap)
    private val polylineManager: PolylineManager = PolylineManager(googleMap)
    private val garminGpxParser by inject<GarminGpxParser>()
    private val markersUtils by inject<MarkersUtils>()
    private val deleteMarkerUseCase by inject<DeleteMarkerUseCase>()
    private val createRouteUseCase by inject<CreateRouteUseCase>()
    private val polylineCollection: PolylineManager.Collection = polylineManager.newCollection()
    private val userMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    private val staticMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    private val garminMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    private val polygonCollection: PolygonManager.Collection = polygonManager.newCollection()

    private var destination: Location? = null
    private var kmlLayer: KmlLayer? = null

    private val _distanceText = MutableStateFlow(Pair(0.0, ""))
    val distanceText: StateFlow<Pair<Double, String>> = _distanceText

    private var onInfoWindowLongClickListener: ((Marker) -> Unit)? = null

    fun setOnInfoWindowLongClickListener(listener: (Marker) -> Unit) {
        onInfoWindowLongClickListener = listener
    }

    init {
        staticMarkerCollection.setOnInfoWindowClickListener {
            it.hideInfoWindow()
        }
        staticMarkerCollection.setOnMarkerClickListener {
            it.showInfoWindow()
            true
        }

        staticMarkerCollection.setOnInfoWindowLongClickListener {
            onInfoWindowLongClickListener?.invoke(it)
            it.hideInfoWindow()
        }
    }

    private fun createKmzLayer(context: Context, uri: Uri, googleMap: GoogleMap): Result<LatLng> {
        context.contentResolver.openInputStream(uri).use { stream ->
            stream?.use {
                try {
                    kmlLayer = KmlLayer(
                        googleMap,
                        it,
                        context,
                        markerManager,
                        polygonManager,
                        polylineManager,
                        groundOverlayManager,
                        null
                    )
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                    return Result.failure(Throwable(e.message))
                }
            }
            kmlLayer?.addLayerToMap()
            kmlLayer?.let { layer ->
                layer.groundOverlays?.let {
                    it.any { overlay ->
                        val center = overlay.latLngBox.center
                        return Result.success(center)
                    }
                }
            }
        }
        return Result.failure(Throwable("Overlays not found"))
    }


    fun updateRoute(currentLatLng: Location) {
        if (destination != null) {
            polylineCollection.polylines.first {
                it.points = listOf(currentLatLng.getLatLng(), destination?.getLatLng())
                true
            }
            setDistanceText(currentLatLng, destination)
        }
    }

    private fun setDistanceText(currentLatLng: Location, destination: Location?) {
        val distance = SphericalUtil.computeDistanceBetween(currentLatLng.getLatLng(), destination?.getLatLng())
        destination?.let {
            var finalBearing = currentLatLng.bearingTo(it).toInt()
            if (finalBearing < 0)
                finalBearing += 360
            _distanceText.value = Pair(distance, "$finalBearing\u00B0 ")
        }
    }

    fun addPolyline(latLngList: List<LatLng>) {
        removeRoute()
        polylineCollection.addPolyline(PolylineOptions().apply {
            color(Color.RED)
            zIndex(1f)
            width(15f)
        }.addAll(latLngList))
        polylineCollection.polylines.forEach { it.zIndex = 1f }
    }

    fun isRoutePolylineNotNull(): Boolean {
        return polylineCollection.polylines.isNotEmpty()
    }

    fun createRoute(currentLatLng: Location, destination: LatLng, context: Context) {
        removeRoute()
        this.destination = Location("Dest").apply {
            latitude = destination.latitude
            longitude = destination.longitude
        }
        setDistanceText(currentLatLng, this.destination)
        polylineCollection.addPolyline(createRouteUseCase.execute(currentLatLng.getLatLng(), destination, context))
    }

    fun createUsersMarkers(
        data: List<MarkerDataModel>, context: Context
    ) {
        markersUtils.createUsersMarkers(
            data,
            markerCollection = userMarkerCollection,
            context = context,
            visibility = isMarkersVisible
        )
    }

    fun createStaticMarkers(
        data: List<MarkerDataModel>, context: Context
    ) {
        staticMarkerCollection.markers.forEach { marker -> marker.remove() }
        markersUtils.createStaticMarkers(
            data,
            markerCollection = staticMarkerCollection,
            context = context,
            visibility = isMarkersVisible
        )
    }

    private suspend fun createGpxLayer(context: Context, uri: Uri): Result<LatLng> {
        garminGpxParser.parseGpxFile(uri, context)?.let {
            markersUtils.createGarminMarkers(it, garminMarkerCollection, context)
            markersUtils.createGarminBounds(it, polygonCollection)
        }
        garminMarkerCollection.markers.any {
            if (it != null)
                return Result.success(it.position)
            else
                return Result.failure(Throwable("GPX Marker not found"))
        }
        return Result.failure(Throwable("GPX Marker not found"))
    }

    fun showAllMarkers() {
        staticMarkerCollection.showAll()
        userMarkerCollection.showAll()
    }

    fun hideAllMarkers() {
        staticMarkerCollection.hideAll()
        userMarkerCollection.hideAll()
    }

    fun removeMarkers() {
        userMarkerCollection.markers.forEach { marker -> marker.remove() }
        staticMarkerCollection.markers.forEach { marker -> marker.remove() }
    }

    fun removeOverlays() {
        kmlLayer?.removeLayerFromMap()
        garminMarkerCollection.markers.forEach { marker -> marker.remove() }
        polygonCollection.polygons.forEach { polygon -> polygon.remove() }
    }

    fun removeRoute() {
        destination = null
        _distanceText.value = Pair(0.0, "")
        polylineCollection.polylines.forEach { polyline -> polyline.remove() }
        polylineCollection.clear()
    }

    suspend fun createOverlay(uri: Uri, context: Context, googleMap: GoogleMap): Result<LatLng> {
        val mimeType = context.getFileNameFromUri(uri)
        when {
            mimeType.endsWith(KMZ_EXTENSION) -> {
                return createKmzLayer(context, uri, googleMap)
            }

            mimeType.endsWith(GPX_EXTENSION) -> {
                return createGpxLayer(context, uri)
            }
        }
        return Result.failure(Throwable(context.getString(R.string.file_wrong)))
    }

    fun deleteMarker(marker: Marker) {
        deleteMarkerUseCase.execute(marker)
    }
}