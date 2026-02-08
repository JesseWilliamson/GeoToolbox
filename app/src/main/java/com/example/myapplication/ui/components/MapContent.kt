package com.example.myapplication.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.example.myapplication.Route
import com.example.myapplication.RouteNode
import com.example.myapplication.RouteWaypoint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun UserLocationPuck(lat: Double, lon: Double) {
    val state = rememberMarkerState(position = LatLng(lat, lon))
    LaunchedEffect(lat, lon) { state.position = LatLng(lat, lon) }
    Marker(state = state, title = "You", draggable = false)
}

@Composable
fun RouteNodeMarker(
    node: RouteNode,
    onPositionChange: (lat: Double, lon: Double) -> Unit,
) {
    val state = remember(node.id) { MarkerState(position = LatLng(node.latitude, node.longitude)) }
    LaunchedEffect(state.position) { onPositionChange(state.position.latitude, state.position.longitude) }
    Marker(state = state, draggable = true)
}

private val EDIT_COLOR = Color(0xFF1976D2)
private val PREVIEW_COLOR = Color(0xFF757575)
private val FOLLOW_COLOR = Color(0xFF388E3C)

@Composable
fun EditingPolyline(nodes: List<RouteNode>) {
    if (nodes.size < 2) return
    Polyline(points = nodes.map { LatLng(it.latitude, it.longitude) }, color = EDIT_COLOR, width = 14f)
}

@Composable
fun PreviewPolyline(route: Route?) {
    val pts = route?.waypoints ?: return
    if (pts.size < 2) return
    Polyline(points = pts.map { LatLng(it.latitude, it.longitude) }, color = PREVIEW_COLOR, width = 10f)
}

@Composable
fun FollowingPolyline(waypoints: List<RouteWaypoint>?) {
    val pts = waypoints ?: return
    if (pts.size < 2) return
    Polyline(points = pts.map { LatLng(it.latitude, it.longitude) }, color = FOLLOW_COLOR, width = 12f)
}
