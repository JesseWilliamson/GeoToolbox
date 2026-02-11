package com.example.myapplication

data class RouteWaypoint(
    val latitude: Double,
    val longitude: Double,
)

data class RouteNode(
    val id: String,
    val latitude: Double,
    val longitude: Double,
)

data class Route(
    val id: String,
    val name: String,
    val waypoints: List<RouteWaypoint>,
)
