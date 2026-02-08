package com.example.myapplication

import com.example.myapplication.BuildConfig

fun staticMapUrl(lat: Double, lon: Double, zoom: Int = 15, apiKey: String = BuildConfig.MAPS_API_KEY): String {
    if (apiKey.isBlank()) return ""
    val z = zoom.coerceIn(1, 22)
    return "https://maps.googleapis.com/maps/api/staticmap?" +
        "center=$lat,$lon" +
        "&zoom=$z" +
        "&size=400x400" +
        "&scale=2" +
        "&maptype=roadmap" +
        "&markers=color:red%7C$lat,$lon" +
        "&key=$apiKey"
}
