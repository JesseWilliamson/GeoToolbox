package com.example.myapplication

import android.os.Handler
import android.os.Looper
import kotlin.math.cos
import kotlin.math.sqrt

private const val UPDATE_MS = 150L
private const val METERS_PER_DEGREE_LAT = 111_320.0

/**
 * Interpolates position along a path of waypoints and invokes callbacks.
 * Uses linear interpolation between consecutive waypoints.
 * @param speedProvider called each tick to get current speed in m/s (allows live slider updates)
 */
class RouteRunner(
    private val waypoints: List<RouteWaypoint>,
    private val speedProvider: () -> Double,
    private val onUpdate: (lat: Double, lon: Double) -> Unit,
    private val onComplete: () -> Unit,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var segmentDistances: DoubleArray = doubleArrayOf()
    private var totalDistance = 0.0
    private var distanceTraveled = 0.0
    private var running = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running || waypoints.size < 2) return
            distanceTraveled += speedProvider() * (UPDATE_MS / 1000.0)
            if (distanceTraveled >= totalDistance) {
                val last = waypoints.last()
                onUpdate(last.latitude, last.longitude)
                running = false
                onComplete()
                return
            }
            val (lat, lon) = positionAlongPath(distanceTraveled)
            onUpdate(lat, lon)
            handler.postDelayed(this, UPDATE_MS)
        }
    }

    fun start() {
        if (waypoints.isEmpty()) {
            onComplete()
            return
        }
        if (waypoints.size == 1) {
            onUpdate(waypoints[0].latitude, waypoints[0].longitude)
            onComplete()
            return
        }
        segmentDistances = DoubleArray(waypoints.size - 1) { i ->
            distanceMeters(waypoints[i], waypoints[i + 1])
        }
        totalDistance = segmentDistances.sum()
        if (totalDistance < 1e-6) {
            val last = waypoints.last()
            onUpdate(last.latitude, last.longitude)
            onComplete()
            return
        }
        distanceTraveled = 0.0
        running = true
        onUpdate(waypoints[0].latitude, waypoints[0].longitude)
        handler.postDelayed(tick, UPDATE_MS)
    }

    fun stop() {
        running = false
        handler.removeCallbacks(tick)
    }

    private fun positionAlongPath(distance: Double): Pair<Double, Double> {
        var d = distance
        for (i in segmentDistances.indices) {
            val segLen = segmentDistances[i]
            if (segLen < 1e-6) continue // skip zero-length segments
            if (d <= segLen) {
                val t = d / segLen
                val a = waypoints[i]
                val b = waypoints[i + 1]
                return Pair(
                    a.latitude + t * (b.latitude - a.latitude),
                    a.longitude + t * (b.longitude - a.longitude)
                )
            }
            d -= segLen
        }
        val last = waypoints.last()
        return Pair(last.latitude, last.longitude)
    }

    private fun distanceMeters(a: RouteWaypoint, b: RouteWaypoint): Double {
        val dlat = (b.latitude - a.latitude) * METERS_PER_DEGREE_LAT
        val dlon = (b.longitude - a.longitude) * METERS_PER_DEGREE_LAT * cos(Math.toRadians(a.latitude))
        return sqrt(dlat * dlat + dlon * dlon)
    }
}
