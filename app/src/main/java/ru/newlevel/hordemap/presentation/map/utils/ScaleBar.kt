package ru.newlevel.hordemap.presentation.map.utils
import android.graphics.Point
import android.location.Location
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.Projection
import ru.newlevel.hordemap.R
import kotlin.math.abs


class ScaleBar(private val rootView: View) {

    private val scaleTextView: TextView = rootView.findViewById(R.id.scaleTextView)
    private val scaleBarView: View = rootView.findViewById(R.id.scaleBarView)

    fun updateScaleBar(googleMap: GoogleMap) {
        val projection: Projection = googleMap.projection
        val width = rootView.width

        // Берём две точки на экране (левую и правую)
        val leftPoint = Point(0, rootView.height / 2)
        val rightPoint = Point((width / 2.5).toInt(), rootView.height / 2)

        // Конвертируем экранные координаты в географические
        val leftLatLng = projection.fromScreenLocation(leftPoint)
        val rightLatLng = projection.fromScreenLocation(rightPoint)

        // Вычисляем расстояние между точками в метрах
        val distanceMeters = FloatArray(1)
        Location.distanceBetween(
            leftLatLng.latitude, leftLatLng.longitude,
            rightLatLng.latitude, rightLatLng.longitude,
            distanceMeters
        )

        // Округляем расстояние до удобного значения
        val scale = getRoundedScale(distanceMeters[0])

        // Устанавливаем ширину шкалы (ширина будет зависеть от расстояния)
        val scaleWidth = (width / 2.5).toInt() // Расстояние будет пропорционально ширине экрана

        // Убедимся, что ширина не слишком маленькая или большая
        if (scaleWidth > 0) {
            scaleBarView.layoutParams.width = scaleWidth
        } else {
            scaleBarView.layoutParams.width = 100 // Минимальная ширина шкалы, если вычисления неверные
        }

        // Отображаем текст на шкале
        scaleTextView.text = formatDistance(scale)
    }

    // Получаем округленный масштаб
    private fun getRoundedScale(distance: Float): Float {
        val scales = listOf(5f, 7f, 8f,  10f, 15f, 20f, 25f, 30f, 35f, 40f, 50f, 75f, 100f, 125f, 150f, 175f, 200f, 250f, 300f, 350f, 400f, 450f, 500f, 600f, 700f, 800f, 900f, 1000f, 1100f, 1200f, 1300f, 1400f, 1500f, 1600f,1700f,1800f,1900f, 2000f, 2250f,2500f, 2750f, 3000f, 3500f,4000f,4500f, 5000f, 7500f, 10000f, 20000f, 30000f, 40000f, 50000f, 75000f, 100000f, 150000f, 200000f, 300000f,400000f,500000f,600000f,700000f,800000f,900000f,1000000f,2000000f,3000000f,4000000f,5000000f,6000000f)
        return scales.minByOrNull { abs(it - distance) } ?: 1000f
    }

    // Форматируем вывод в зависимости от значения расстояния
    private fun formatDistance(distance: Float): String {
        return if (distance < 1000) {
            "${distance.toInt()} м"
        } else {
            "${(distance / 1000).toInt()} км"
        }
    }
}