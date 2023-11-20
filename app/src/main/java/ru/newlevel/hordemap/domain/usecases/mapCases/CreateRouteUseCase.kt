package ru.newlevel.hordemap.domain.usecases.mapCases

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CustomCap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import ru.newlevel.hordemap.R

class CreateRouteUseCase {
    fun execute(currentLatLng: LatLng, destination: LatLng, context: Context): PolylineOptions {
        val bitmapCustomCap =
            BitmapFactory.decodeResource(context.resources, R.drawable.star)
        val bitmapCustomCapIcon = BitmapDescriptorFactory.fromBitmap(
            Bitmap.createScaledBitmap(
                bitmapCustomCap,
                60,
                60,
                false
            )
        )
        val customCap = CustomCap(bitmapCustomCapIcon)
        return PolylineOptions().addAll(listOf(currentLatLng, destination)).endCap(customCap)
            .geodesic(true).color(Color.BLUE).width(6f)
    }
}