package com.example.myapplication

data class RouteWaypoint(
    val latitude: Double,
    val longitude: Double,
)

/** Node with stable id for drag-and-drop in the route line editor. */
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
