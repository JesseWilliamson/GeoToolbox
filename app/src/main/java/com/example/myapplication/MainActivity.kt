package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AddEditPointDialog
import com.example.myapplication.ui.components.AddRouteDialog
import com.example.myapplication.ui.components.DeleteConfirmDialog
import com.example.myapplication.ui.components.EditingPolyline
import com.example.myapplication.ui.components.FollowingPolyline
import com.example.myapplication.ui.components.LocationsSection
import com.example.myapplication.ui.components.PreviewPolyline
import com.example.myapplication.ui.components.RouteNodeMarker
import com.example.myapplication.ui.components.RoutesSection
import com.example.myapplication.ui.components.UserLocationPuck
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import java.util.UUID

/* ══════════════════════════════════════════════════════════════════════════
   Activity
   ══════════════════════════════════════════════════════════════════════════ */

class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openDeveloperOptions()
        else Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Scaffold(Modifier.fillMaxSize()) { padding ->
                    GpsSpooferScreen(Modifier.padding(padding))
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            openDeveloperOptions()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun openDeveloperOptions() {
        try { startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) } catch (_: Exception) {}
        Toast.makeText(this, "Developer Options → set this app as \"Mock location app\"", Toast.LENGTH_LONG).show()
    }
}

/* ══════════════════════════════════════════════════════════════════════════
   Main screen
   ══════════════════════════════════════════════════════════════════════════ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsSpooferScreen(modifier: Modifier = Modifier, vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val player = vm.player
    val savedPoints by vm.savedPoints.collectAsState(initial = emptyList())
    val routes by vm.routes.collectAsState(initial = emptyList())

    // Observe toast messages from ViewModel
    LaunchedEffect(Unit) {
        vm.toastMessages.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
        }
    }

    // Coordinate state
    var latText by remember { mutableStateOf("0") }
    var lonText by remember { mutableStateOf("0") }
    val currentLat = latText.toDoubleOrNull() ?: 0.0
    val currentLon = lonText.toDoubleOrNull() ?: 0.0
    val followLoc = player.followingLocation
    val effectiveLat = followLoc?.first ?: currentLat
    val effectiveLon = followLoc?.second ?: currentLon

    // Camera
    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(effectiveLat, effectiveLon), 15f)
    }
    var targetZoom by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(effectiveLat, effectiveLon) {
        val zoom = targetZoom ?: cameraState.position.zoom
        if (targetZoom != null) targetZoom = null
        val update = CameraUpdateFactory.newLatLngZoom(LatLng(effectiveLat, effectiveLon), zoom)
        if (player.isFollowing) cameraState.move(update) else cameraState.animate(update)
    }

    LaunchedEffect(player.previewRoute) {
        val route = player.previewRoute ?: return@LaunchedEffect
        if (player.isFollowing) return@LaunchedEffect
        val pts = route.waypoints
        if (pts.size >= 2) {
            val bounds = LatLngBounds.builder().apply { pts.forEach { include(LatLng(it.latitude, it.longitude)) } }.build()
            cameraState.move(CameraUpdateFactory.newLatLngBounds(bounds, 80))
        }
    }

    // Dialog state
    var showAddDialog by remember { mutableStateOf(false) }
    var addEditPoint by remember { mutableStateOf<SavedPoint?>(null) }
    var deletePoint by remember { mutableStateOf<SavedPoint?>(null) }
    var showAddRouteDialog by remember { mutableStateOf(false) }

    // Route editor state
    var editRoute by remember { mutableStateOf<Route?>(null) }
    var editingNodes by remember { mutableStateOf<List<RouteNode>>(emptyList()) }
    LaunchedEffect(editRoute) {
        editingNodes = editRoute?.waypoints?.mapIndexed { i, w ->
            RouteNode("n_$i", w.latitude, w.longitude)
        } ?: emptyList()
    }

    // Tab state
    var selectedTab by remember { mutableStateOf(0) }

    /* ── Dialogs ─────────────────────────────────────────────────────── */

    if (showAddDialog) {
        AddEditPointDialog(null, currentLat, currentLon, cameraState.position.zoom, onDismiss = { showAddDialog = false }) { _, name, lat, lon, zoom ->
            vm.addPoint(name, lat, lon, zoom)
        }
    }
    addEditPoint?.let { point ->
        AddEditPointDialog(point, currentLat, currentLon, cameraState.position.zoom, onDismiss = { addEditPoint = null }) { id, name, lat, lon, zoom ->
            if (id != null) vm.updatePoint(point.copy(name = name, latitude = lat, longitude = lon, zoom = zoom))
            addEditPoint = null
        }
    }
    deletePoint?.let { point ->
        DeleteConfirmDialog(point, onDismiss = { deletePoint = null }) { vm.deletePoint(point.id) }
    }
    if (showAddRouteDialog) {
        AddRouteDialog(onDismiss = { showAddRouteDialog = false }) { name ->
            val route = Route(UUID.randomUUID().toString(), name, emptyList())
            vm.addRoute(route)
            editRoute = route
            showAddRouteDialog = false
        }
    }

    /* ── Layout ──────────────────────────────────────────────────────── */

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            confirmValueChange = { it != SheetValue.Hidden },
        )
    )

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selectedTab == 0, onClick = { selectedTab = 0 }, label = { Text("Locations") })
                    FilterChip(selectedTab == 1, onClick = { selectedTab = 1 }, label = { Text("Routes") })
                }
                if (selectedTab == 0) {
                    LocationsSection(savedPoints, onPointClick = { pt ->
                        latText = pt.latitude.toString()
                        lonText = pt.longitude.toString()
                        targetZoom = pt.zoom.coerceIn(2f, 22f)
                    }, onEdit = { addEditPoint = it }, onDelete = { deletePoint = it })
                } else {
                    RoutesSection(routes, player, onAddRoute = { showAddRouteDialog = true }, onEditRoute = { editRoute = it }, onDeleteRoute = { vm.deleteRoute(it.id) })
                }
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)),
                cameraPositionState = cameraState,
                onMapClick = { latLng ->
                    if (editRoute != null) {
                        editingNodes = editingNodes + RouteNode(UUID.randomUUID().toString(), latLng.latitude, latLng.longitude)
                    } else {
                        latText = latLng.latitude.toString()
                        lonText = latLng.longitude.toString()
                        showAddDialog = true
                    }
                },
            ) {
                // Route editor polyline + draggable markers
                if (editRoute != null) {
                    EditingPolyline(editingNodes)
                    editingNodes.forEach { node ->
                        RouteNodeMarker(node) { lat, lon ->
                            editingNodes = editingNodes.map { if (it.id == node.id) it.copy(latitude = lat, longitude = lon) else it }
                        }
                    }
                }
                // Route preview / follow polylines
                PreviewPolyline(player.previewRoute)
                FollowingPolyline(player.followedWaypoints)
                // User location puck
                UserLocationPuck(effectiveLat, effectiveLon)
            }

            // Route editor top bar
            if (editRoute != null) {
                RouteEditOverlay(
                    modifier = Modifier.align(Alignment.TopCenter),
                    routeName = editRoute!!.name,
                    nodeCount = editingNodes.size,
                    onSave = {
                        val updated = editRoute!!.copy(waypoints = editingNodes.map { RouteWaypoint(it.latitude, it.longitude) })
                        vm.updateRoute(updated)
                        editRoute = null
                    },
                    onCancel = { editRoute = null },
                )
            }
        }
    }
}

/* ── Route edit overlay ───────────────────────────────────────────────── */

@Composable
private fun RouteEditOverlay(
    modifier: Modifier = Modifier,
    routeName: String,
    nodeCount: Int,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(routeName, style = MaterialTheme.typography.titleMedium)
        Text("Tap map to add points, drag nodes to move. $nodeCount point(s).", style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
            Button(onClick = onCancel) { Text("Cancel") }
            Spacer(Modifier.padding(8.dp))
            Button(onClick = onSave) { Text("Save") }
        }
    }
}
