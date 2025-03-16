package ru.newlevel.hordemap.presentation.map.utils

import kotlin.math.*

class GaussKrugerConverter() {
    fun wgs84ToSK42(latitude: Double, longitude: Double): Pair<Int, Int> {
        val a = 6378245.0  // Большая полуось эллипсоида Красовского
        val e2 = 0.006693421622  // Квадрат эксцентриситета
        val zone = ((longitude + 6) / 6).toInt()  // Определяем номер зоны
        val λ0 = (zone * 6 - 3).toDouble()  // Центральный меридиан зоны
        val λ = Math.toRadians(longitude)
        val φ = Math.toRadians(latitude)
        val λ0Rad = Math.toRadians(λ0)

        val N = a / sqrt(1 - e2 * sin(φ) * sin(φ))
        val t = tan(φ)
        val η2 = e2 / (1 - e2) * cos(φ) * cos(φ)

        val l = λ - λ0Rad

        val M = a * (
                (1 - e2 / 4 - 3 * e2 * e2 / 64 - 5 * e2 * e2 * e2 / 256) * φ -
                        (3 * e2 / 8 + 3 * e2 * e2 / 32 + 45 * e2 * e2 * e2 / 1024) * sin(2 * φ) +
                        (15 * e2 * e2 / 256 + 45 * e2 * e2 * e2 / 1024) * sin(4 * φ) -
                        (35 * e2 * e2 * e2 / 3072) * sin(6 * φ)
                )

        val x = M + N * t * (
                (l * l / 2) +
                        (5 - t * t + 9 * η2 + 4 * η2 * η2) * (l * l * l * l / 24) +
                        (61 - 58 * t * t + t * t * t * t) * (l * l * l * l * l * l / 720)
                )

        val y = (N * cos(φ) * (
                l +
                        (1 - t * t + η2) * (l * l * l / 6) +
                        (5 - 18 * t * t + t * t * t * t + 14 * η2 - 58 * t * t * η2) * (l * l * l * l * l / 120)
                )+ zone * 1_000_000 + 500_000 )

        return Pair(x.toInt(), y.toInt())
    }

}