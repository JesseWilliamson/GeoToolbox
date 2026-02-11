package com.example.myapplication

import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class RoutePlayerController(
    private val spoofer: Spoofer,
    private val toastProvider: (String) -> Unit,
) {
    var followingLocation by mutableStateOf<Pair<Double, Double>?>(null)
        private set

    var isFollowing by mutableStateOf(false)
        private set

    var followedWaypoints by mutableStateOf<List<RouteWaypoint>?>(null)
        private set

    var previewRoute by mutableStateOf<Route?>(null)
        private set

    var speedMps by mutableStateOf(25.0)

    var progress by mutableStateOf(0f)
        private set

    var isPaused by mutableStateOf(false)
        private set

    private var routeRunner: RouteRunner? = null

    fun preview(route: Route) {
        previewRoute = route
    }

    fun follow(route: Route) {
        val pts = route.waypoints
        if (pts.isEmpty()) return
        if (!spoofer.startSpoofing(pts[0].latitude, pts[0].longitude)) {
            toastProvider("Could not start mock location. Set this app as Mock location app in Developer options.")
            return
        }
        previewRoute = route
        followedWaypoints = pts
        followingLocation = Pair(pts[0].latitude, pts[0].longitude)
        isFollowing = true
        isPaused = false
        progress = 0f
        routeRunner = RouteRunner(
            waypoints = pts,
            speedProvider = { speedMps },
            onUpdate = { lat, lon ->
                spoofer.setLocation(lat, lon)
                followingLocation = Pair(lat, lon)
            },
            onComplete = { stop() },
            onProgress = { progress = it },
        )
        routeRunner?.start()
    }

    fun pause() {
        routeRunner?.pause()
        isPaused = true
    }

    fun resume() {
        routeRunner?.resume()
        isPaused = false
    }

    fun seek(fraction: Float) {
        routeRunner?.seekToFraction(fraction)
        progress = fraction
    }

    fun stop() {
        routeRunner?.stop()
        routeRunner = null
        spoofer.stopSpoofing()
        followedWaypoints = null
        followingLocation = null
        isFollowing = false
        progress = 0f
    }

    fun cleanup() {
        if (isFollowing) stop()
    }
}
