package com.example.myapplication

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Build
import android.os.Handler
import android.os.Looper

private const val GPS_PROVIDER = "gps"
private const val MOCK_UPDATE_MS = 200L

class Spoofer(context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val handler = Handler(Looper.getMainLooper())
    private var spoofLat = 0.0
    private var spoofLon = 0.0

    private val mockUpdater = object : Runnable {
        override fun run() {
            if (!isSpoofing) return
            try {
                val loc = newLocation(spoofLat, spoofLon)
                locationManager.setTestProviderLocation(GPS_PROVIDER, loc)
            } catch (_: Exception) {}
            handler.postDelayed(this, MOCK_UPDATE_MS)
        }
    }

    var isSpoofing: Boolean = false
        private set

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
            handler.post(mockUpdater)
            true
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalArgumentException) {
            false
        }
    }

    fun setLocation(lat: Double, lon: Double) {
        if (isSpoofing) {
            spoofLat = lat
            spoofLon = lon
        }
    }

    fun stopSpoofing() {
        handler.removeCallbacks(mockUpdater)
        try {
            if (locationManager.isProviderEnabled(GPS_PROVIDER)) {
                locationManager.removeTestProvider(GPS_PROVIDER)
            }
        } catch (_: Exception) {}
        isSpoofing = false
    }

    private fun removeTestProvider() {
        try {
            if (locationManager.isProviderEnabled(GPS_PROVIDER)) {
                locationManager.removeTestProvider(GPS_PROVIDER)
            }
        } catch (_: Exception) {}
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
        } catch (_: Exception) {}
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
