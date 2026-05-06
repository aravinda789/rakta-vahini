package com.raktavahini.data.model

import kotlin.math.*

object LocationUtils {
    private const val EARTH_RADIUS_KM = 6371.0

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return EARTH_RADIUS_KM * c
    }

    fun isWithinRadius(
        donorLat: Double?, donorLon: Double?,
        userLat: Double, userLon: Double,
        radiusKm: Int
    ): Boolean {
        if (donorLat == null || donorLon == null) return false
        val distance = calculateDistance(userLat, userLon, donorLat, donorLon)
        return distance <= radiusKm
    }
}

data class UserLocation(
    val latitude: Double,
    val longitude: Double
)

enum class SearchRadius(val km: Int, val displayName: String) {
    TEN_KM(10, "10 km"),
    TWENTY_KM(20, "20 km"),
    FIFTY_KM(50, "50 km")
}