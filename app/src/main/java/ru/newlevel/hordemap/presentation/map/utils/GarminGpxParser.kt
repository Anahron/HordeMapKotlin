package ru.newlevel.hordemap.presentation.map.utils

import android.content.Context
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.Element
import org.w3c.dom.Node
import ru.newlevel.hordemap.domain.models.GarminGpxMarkersSet
import ru.newlevel.hordemap.domain.models.GarminMarkerModel
import javax.xml.parsers.DocumentBuilderFactory

class GarminGpxParser {
    suspend fun parseGpxFile(uri: Uri, context: Context): GarminGpxMarkersSet? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                withContext(Dispatchers.IO) {
                    val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream)
                    val nodeList = doc.getElementsByTagName("wpt")
                    val garminMarkers = mutableListOf<GarminMarkerModel>()
                    var bounds: LatLngBounds? = null

                    for (i in 0 until nodeList.length) {
                        val node = nodeList.item(i)

                        if (node.nodeType == Node.ELEMENT_NODE) {
                            val element = node as Element
                            val name = element.getElementsByTagName("name").item(0)?.textContent ?: ""
                            val lat = element.getAttribute("lat").toDouble()
                            val lon = element.getAttribute("lon").toDouble()
                            val latLng = LatLng(lat, lon)
                            val symNode = element.getElementsByTagName("sym").item(0)
                            val markerType = symNode?.textContent?.split(",")?.getOrNull(0)?.trim()
                            val markerColor = symNode?.textContent?.split(",")?.getOrNull(1)?.trim()
                            garminMarkers.add(
                                GarminMarkerModel(
                                    name,
                                    latLng,
                                    markerType ?: "",
                                    markerColor ?: ""
                                )
                            )
                        }

                        val boundsNode = doc.getElementsByTagName("bounds").item(0)
                        if (boundsNode != null) {
                            boundsNode as Element
                            val minLat = boundsNode.getAttribute("minlat").toDouble()
                            val minLon = boundsNode.getAttribute("minlon").toDouble()
                            val maxLat = boundsNode.getAttribute("maxlat").toDouble()
                            val maxLon = boundsNode.getAttribute("maxlon").toDouble()
                            bounds = LatLngBounds(LatLng(minLat, minLon), LatLng(maxLat, maxLon))
                        }
                    }
                    GarminGpxMarkersSet(garminMarkers, bounds)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}