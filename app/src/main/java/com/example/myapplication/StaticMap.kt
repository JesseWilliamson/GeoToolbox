package com.example.myapplication

import com.example.myapplication.BuildConfig

fun staticMapUrl(lat: Double, lon: Double, apiKey: String = BuildConfig.MAPS_API_KEY): String {
    if (apiKey.isBlank()) return ""
    return "https://maps.googleapis.com/maps/api/staticmap?" +
        "center=$lat,$lon" +
        "&zoom=15" +
        "&size=400x400" +
        "&scale=2" +
        "&maptype=roadmap" +
        "&markers=color:red%7C$lat,$lon" +
        "&key=$apiKey"
}
