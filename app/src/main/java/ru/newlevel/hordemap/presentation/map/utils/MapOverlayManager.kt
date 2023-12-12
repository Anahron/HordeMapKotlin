package ru.newlevel.hordemap.presentation.map.utils

import android.content.Context
import android.graphics.Color
import android.net.Uri
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.SphericalUtil
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
import ru.newlevel.hordemap.data.storage.models.MarkerDataModel
import ru.newlevel.hordemap.domain.usecases.mapCases.CreateRouteUseCase
import ru.newlevel.hordemap.domain.usecases.markersCases.DeleteMarkerUseCase
import kotlin.math.roundToInt

class MapOverlayManager(googleMap: GoogleMap) : KoinComponent {

    var isMarkersVisible: Boolean = true
    private val markerManager: MarkerManager = MarkerManager(googleMap)
    private val polygonManager: PolygonManager = PolygonManager(googleMap)
    private val polylineManager: PolylineManager = PolylineManager(googleMap)
    private val markersUtils by inject<MarkersUtils>()
    private val deleteMarkerUseCase by inject<DeleteMarkerUseCase>()
    private val createRouteUseCase by inject<CreateRouteUseCase>()
    private val polylineCollection: PolylineManager.Collection = polylineManager.newCollection()
    private val userMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    private val staticMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    private val garminMarkerCollection: MarkerManager.Collection = markerManager.newCollection()
    private val polygonCollection: PolygonManager.Collection = polygonManager.newCollection()

    private var destination: LatLng? = null
    private var kmlLayer: KmlLayer? = null

    private val _distanceText = MutableStateFlow("")
    val distanceText: StateFlow<String> = _distanceText
    init {
        staticMarkerCollection.setOnInfoWindowClickListener {
            it.hideInfoWindow()
        }
        staticMarkerCollection.setOnMarkerClickListener { marker: Marker ->
            marker.showInfoWindow()
            true
        }
        staticMarkerCollection.setOnInfoWindowLongClickListener {
            deleteMarkerUseCase.execute(it)
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
                        null,
                        null
                    )
                } catch (e: XmlPullParserException) {
                    e.printStackTrace()
                    return Result.failure(Throwable(e.message))
                }
            }
            kmlLayer?.addLayerToMap()
            kmlLayer?.let { layer ->
                layer.groundOverlays?.let { it ->
                    it.any { overlay ->
                        val center = overlay.latLngBox.center
                        return Result.success(center)
                    }
                }
            }
        }
        return Result.failure(Throwable("Overlays not found"))
    }


    fun updateRoute(currentLatLng: LatLng) {
        if (destination != null) {
            polylineCollection.polylines.first {
                it.points = listOf(currentLatLng, destination)
                true
            }
            setDistanceText(currentLatLng, destination)
        }
    }

    private fun setDistanceText(currentLatLng: LatLng, destination: LatLng?) {
        val distance = SphericalUtil.computeDistanceBetween(currentLatLng, destination)
        _distanceText.value =
            if (distance.toInt() > 1000) ((distance / 10).roundToInt() / 100.0).toString() + " км." else distance.toInt()
                .toString() + " м."

    }

    fun addPolyline(latLngList: List<LatLng>) {
        removeRoute()
        polylineCollection.addPolyline(PolylineOptions().apply {
            color(Color.RED)
            zIndex(1f)
            width(15f)
        }.addAll(latLngList))
    }

    fun isRoutePolylineNotNull(): Boolean {
        return polylineCollection.polylines.isNotEmpty()
    }

    fun createRoute(currentLatLng: LatLng, destination: LatLng, context: Context) {
        removeRoute()
        this.destination = destination
        setDistanceText(currentLatLng, destination)
        polylineCollection.addPolyline(createRouteUseCase.execute(currentLatLng, destination, context))
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
        markersUtils.createGpxLayer(uri, garminMarkerCollection, context)?.let { polygonCollection.addPolygon(it) }
        garminMarkerCollection.markers.any {
            return Result.success(it.position)
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
        _distanceText.value = ""
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
                return  createGpxLayer(context, uri)
            }
        }
        return Result.failure(Throwable(context.getString(R.string.file_wrong)))
    }
}