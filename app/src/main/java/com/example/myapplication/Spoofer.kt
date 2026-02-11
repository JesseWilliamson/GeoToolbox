package com.example.myapplication

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log

private const val TAG = "Spoofer"
private const val GPS_PROVIDER = "gps"

/** Interval between mock location updates in milliseconds. */
private const val MOCK_UPDATE_MS = 200L

/** Stop spoofing after this many consecutive mock-update failures. */
private const val MAX_CONSECUTIVE_ERRORS = 5

/**
 * Handles GPS location mocking by overriding the system "gps" provider.
 * Requires the app to be set as "Mock location app" in Developer Options.
 */
class Spoofer(context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var spoofLat = 0.0
    private var spoofLon = 0.0
    private var consecutiveErrors = 0

    /** Optional callback invoked when an error occurs during spoofing. */
    var onError: ((String) -> Unit)? = null

    private val mockUpdater = object : Runnable {
        override fun run() {
            if (!isSpoofing) return
            try {
                val loc = newLocation(spoofLat, spoofLon)
                locationManager.setTestProviderLocation(GPS_PROVIDER, loc)
                consecutiveErrors = 0
            } catch (e: Exception) {
                Log.w(TAG, "Mock location update failed", e)
                consecutiveErrors++
                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                    Log.e(TAG, "Too many consecutive mock failures, stopping")
                    onError?.invoke("Mock location updates keep failing. Is the app still set as Mock location provider?")
                    stopSpoofing()
                    return
                }
            }
            handler.postDelayed(this, MOCK_UPDATE_MS)
        }
    }

    var isSpoofing: Boolean = false
        private set

    /**
     * Starts spoofing GPS to the given coordinates. Overrides the "gps" provider.
     * @return true if started successfully, false if not
     */
    fun startSpoofing(lat: Double, lon: Double): Boolean {
        if (isSpoofing) stopSpoofing()
        return try {
            removeTestProvider()
            addTestProvider()
            locationManager.setTestProviderEnabled(GPS_PROVIDER, true)
            setProviderAvailable()
            spoofLat = lat
            spoofLon = lon
            locationManager.setTestProviderLocation(GPS_PROVIDER, newLocation(lat, lon))
            isSpoofing = true
            consecutiveErrors = 0
            handler.post(mockUpdater)
            true
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException starting spoofing — is the app set as Mock location app?", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.w(TAG, "IllegalArgumentException starting spoofing", e)
            false
        }
    }

    /**
     * Updates the spoofed location. Only has effect when [isSpoofing] is true.
     */
    fun setLocation(lat: Double, lon: Double) {
        if (isSpoofing) {
            spoofLat = lat
            spoofLon = lon
        }
    }

    /**
     * Stops spoofing and removes the test provider.
     */
    fun stopSpoofing() {
        handler.removeCallbacks(mockUpdater)
        try {
            if (locationManager.isProviderEnabled(GPS_PROVIDER)) {
                locationManager.removeTestProvider(GPS_PROVIDER)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove test provider during stop", e)
        }
        isSpoofing = false
    }

    private fun removeTestProvider() {
        try {
            if (locationManager.isProviderEnabled(GPS_PROVIDER)) {
                locationManager.removeTestProvider(GPS_PROVIDER)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to remove existing test provider", e)
        }
    }

    private fun addTestProvider() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val props = ProviderProperties.Builder()
                .setHasNetworkRequirement(false)
                .setHasSatelliteRequirement(false)
                .setHasCellRequirement(false)
                .setHasMonetaryCost(false)
                .setHasAltitudeSupport(false)
                .setHasSpeedSupport(false)
                .setHasBearingSupport(false)
                .setPowerUsage(ProviderProperties.POWER_USAGE_LOW)
                .setAccuracy(ProviderProperties.ACCURACY_FINE)
                .build()
            locationManager.addTestProvider(GPS_PROVIDER, props)
        } else {
            locationManager.addTestProvider(
                GPS_PROVIDER,
                false, false, false, false, false, false, false,
                android.location.Criteria.POWER_LOW,
                android.location.Criteria.ACCURACY_FINE
            )
        }
    }

    private fun setProviderAvailable() {
        try {
            locationManager.setTestProviderStatus(
                GPS_PROVIDER,
                android.location.LocationProvider.AVAILABLE,
                null,
                System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to set provider available", e)
        }
    }

    private fun newLocation(lat: Double, lon: Double): Location {
        return Location(GPS_PROVIDER).apply {
            latitude = lat
            longitude = lon
            altitude = 0.0
            accuracy = 1f
            time = System.currentTimeMillis()
            elapsedRealtimeNanos = System.nanoTime()
        }
    }
}
