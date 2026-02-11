package com.example.myapplication

import kotlin.math.cos
import kotlin.math.sqrt

/** Approximate meters per degree of latitude (equirectangular projection). */
internal const val METERS_PER_DEGREE_LAT = 111_320.0

/** Distance in meters below which a segment is treated as zero-length. */
internal const val ZERO_DISTANCE_THRESHOLD = 1e-6

/**
 * Estimates the distance in meters between two waypoints using an
 * equirectangular approximation. Accurate for short distances (< ~100 km).
 */
internal fun distanceMeters(a: RouteWaypoint, b: RouteWaypoint): Double {
    val dlat = (b.latitude - a.latitude) * METERS_PER_DEGREE_LAT
    val dlon = (b.longitude - a.longitude) * METERS_PER_DEGREE_LAT * cos(Math.toRadians(a.latitude))
    return sqrt(dlat * dlat + dlon * dlon)
}

/**
 * Pre-computes the distance of each segment between consecutive waypoints.
 */
internal fun computeSegmentDistances(waypoints: List<RouteWaypoint>): DoubleArray {
    if (waypoints.size < 2) return doubleArrayOf()
    return DoubleArray(waypoints.size - 1) { i ->
        distanceMeters(waypoints[i], waypoints[i + 1])
    }
}

/**
 * Finds the (lat, lon) position at a given [distance] along a path defined
 * by [waypoints] with pre-computed [segmentDistances].
 * Uses linear interpolation within each segment.
 */
internal fun positionAlongPath(
    waypoints: List<RouteWaypoint>,
    segmentDistances: DoubleArray,
    distance: Double,
): Pair<Double, Double> {
    var d = distance
    for (i in segmentDistances.indices) {
        val segLen = segmentDistances[i]
        if (segLen < ZERO_DISTANCE_THRESHOLD) continue
        if (d <= segLen) {
            val t = d / segLen
            val a = waypoints[i]
            val b = waypoints[i + 1]
            return Pair(
                a.latitude + t * (b.latitude - a.latitude),
                a.longitude + t * (b.longitude - a.longitude),
            )
        }
        d -= segLen
    }
    val last = waypoints.last()
    return Pair(last.latitude, last.longitude)
}
