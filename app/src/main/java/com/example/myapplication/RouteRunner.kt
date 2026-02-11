package com.example.myapplication

import android.os.Handler
import android.os.Looper

/** Interval between interpolation ticks in milliseconds. */
private const val ROUTE_TICK_MS = 150L

/**
 * Interpolates position along a path of waypoints and invokes callbacks.
 * Uses linear interpolation between consecutive waypoints.
 * @param speedProvider called each tick to get current speed in m/s (allows live slider updates)
 * @param onProgress 0f..1f, called each tick with progress along the route
 */
class RouteRunner(
    private val waypoints: List<RouteWaypoint>,
    private val speedProvider: () -> Double,
    private val onUpdate: (lat: Double, lon: Double) -> Unit,
    private val onComplete: () -> Unit,
    private val onProgress: ((Float) -> Unit)? = null,
) {
    private val handler = Handler(Looper.getMainLooper())
    private var segmentDistances: DoubleArray = doubleArrayOf()
    private var totalDistance = 0.0
    private var distanceTraveled = 0.0
    private var running = false
    private var paused = false

    private val tick = object : Runnable {
        override fun run() {
            if (!running || waypoints.size < 2) return
            if (paused) {
                handler.postDelayed(this, ROUTE_TICK_MS)
                return
            }
            distanceTraveled += speedProvider() * (ROUTE_TICK_MS / 1000.0)
            if (distanceTraveled >= totalDistance) {
                val last = waypoints.last()
                onUpdate(last.latitude, last.longitude)
                onProgress?.invoke(1f)
                running = false
                onComplete()
                return
            }
            val (lat, lon) = positionAlongPath(waypoints, segmentDistances, distanceTraveled)
            onUpdate(lat, lon)
            onProgress?.invoke((distanceTraveled / totalDistance).toFloat().coerceIn(0f, 1f))
            handler.postDelayed(this, ROUTE_TICK_MS)
        }
    }

    fun start() {
        if (waypoints.isEmpty()) {
            onComplete()
            return
        }
        if (waypoints.size == 1) {
            onUpdate(waypoints[0].latitude, waypoints[0].longitude)
            onProgress?.invoke(1f)
            onComplete()
            return
        }
        segmentDistances = computeSegmentDistances(waypoints)
        totalDistance = segmentDistances.sum()
        if (totalDistance < ZERO_DISTANCE_THRESHOLD) {
            val last = waypoints.last()
            onUpdate(last.latitude, last.longitude)
            onProgress?.invoke(1f)
            onComplete()
            return
        }
        distanceTraveled = 0.0
        running = true
        paused = false
        onUpdate(waypoints[0].latitude, waypoints[0].longitude)
        onProgress?.invoke(0f)
        handler.postDelayed(tick, ROUTE_TICK_MS)
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        if (running && waypoints.size >= 2) {
            paused = false
            handler.postDelayed(tick, ROUTE_TICK_MS)
        }
    }

    fun seekToFraction(fraction: Float) {
        val f = fraction.coerceIn(0f, 1f)
        distanceTraveled = (f * totalDistance).toDouble()
        if (totalDistance >= ZERO_DISTANCE_THRESHOLD) {
            val (lat, lon) = positionAlongPath(waypoints, segmentDistances, distanceTraveled)
            onUpdate(lat, lon)
            onProgress?.invoke(f)
        }
    }

    fun stop() {
        running = false
        paused = false
        handler.removeCallbacks(tick)
    }
}
