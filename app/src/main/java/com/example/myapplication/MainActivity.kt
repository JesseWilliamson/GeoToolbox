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
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.components.AddEditPointDialog
import com.example.myapplication.ui.components.AddRouteDialog
import com.example.myapplication.ui.components.DeleteConfirmDialog
import com.example.myapplication.ui.components.EditingPolyline
import com.example.myapplication.ui.components.FloatingPlayerOverlay
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
import kotlinx.coroutines.launch

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsSpooferScreen(modifier: Modifier = Modifier, vm: GpsSpooferViewModel = viewModel()) {
    val player = vm.player
    val savedPoints by vm.pointsRepo.savedPoints.collectAsState(initial = emptyList())
    val routes by vm.routesRepo.routes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val currentLat = vm.latText.toDoubleOrNull() ?: 0.0
    val currentLon = vm.lonText.toDoubleOrNull() ?: 0.0
    val followLoc = player.followingLocation
    val effectiveLat = followLoc?.first ?: currentLat
    val effectiveLon = followLoc?.second ?: currentLon

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(effectiveLat, effectiveLon), 15f)
    }

    LaunchedEffect(effectiveLat, effectiveLon) {
        val zoom = vm.targetZoom ?: cameraState.position.zoom
        if (vm.targetZoom != null) vm.targetZoom = null
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

    LaunchedEffect(vm.editRoute) {
        vm.editingNodes = vm.editRoute?.waypoints?.mapIndexed { i, w ->
            RouteNode("n_$i", w.latitude, w.longitude)
        } ?: emptyList()
    }

    // Dialogs
    if (vm.showAddDialog) {
        AddEditPointDialog(null, currentLat, currentLon, cameraState.position.zoom, onDismiss = { vm.showAddDialog = false }) { _, name, lat, lon, zoom ->
            scope.launch { vm.pointsRepo.add(name, lat, lon, zoom) }
        }
    }
    vm.addEditPoint?.let { point ->
        AddEditPointDialog(point, currentLat, currentLon, cameraState.position.zoom, onDismiss = { vm.addEditPoint = null }) { id, name, lat, lon, zoom ->
            if (id != null) scope.launch { vm.pointsRepo.update(point.copy(name = name, latitude = lat, longitude = lon, zoom = zoom)) }
            vm.addEditPoint = null
        }
    }
    vm.deletePoint?.let { point ->
        DeleteConfirmDialog(point, onDismiss = { vm.deletePoint = null }) { scope.launch { vm.pointsRepo.delete(point.id) } }
    }
    if (vm.showAddRouteDialog) {
        AddRouteDialog(onDismiss = { vm.showAddRouteDialog = false }) { name ->
            val route = Route(UUID.randomUUID().toString(), name, emptyList())
            scope.launch { vm.routesRepo.add(route); vm.editRoute = route; vm.showAddRouteDialog = false }
        }
    }

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
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContent = {
            Column(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Segmented button row for tab switching
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = vm.selectedTab == 0,
                        onClick = { vm.selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    ) { Text("Locations") }
                    SegmentedButton(
                        selected = vm.selectedTab == 1,
                        onClick = { vm.selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    ) { Text("Routes") }
                }

                if (vm.selectedTab == 0) {
                    LocationsSection(savedPoints, onPointClick = { pt ->
                        vm.latText = pt.latitude.toString()
                        vm.lonText = pt.longitude.toString()
                        vm.targetZoom = pt.zoom.coerceIn(2f, 22f)
                    }, onEdit = { vm.addEditPoint = it }, onDelete = { vm.deletePoint = it })
                } else {
                    RoutesSection(routes, player, onAddRoute = { vm.showAddRouteDialog = true }, onEditRoute = { vm.editRoute = it }, onDeleteRoute = { scope.launch { vm.routesRepo.delete(it.id) } })
                }
            }
        },
    ) {
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize().background(Color(0xFFE8F5E9)),
                cameraPositionState = cameraState,
                onMapClick = { latLng ->
                    if (vm.editRoute != null) {
                        vm.editingNodes = vm.editingNodes + RouteNode(UUID.randomUUID().toString(), latLng.latitude, latLng.longitude)
                    }
                },
                onMapLongClick = { latLng ->
                    if (vm.editRoute == null) {
                        vm.latText = latLng.latitude.toString()
                        vm.lonText = latLng.longitude.toString()
                        vm.showAddDialog = true
                    }
                },
            ) {
                if (vm.editRoute != null) {
                    EditingPolyline(vm.editingNodes)
                    vm.editingNodes.forEach { node ->
                        RouteNodeMarker(node) { lat, lon ->
                            vm.editingNodes = vm.editingNodes.map { if (it.id == node.id) it.copy(latitude = lat, longitude = lon) else it }
                        }
                    }
                }
                PreviewPolyline(player.previewRoute)
                FollowingPolyline(player.followedWaypoints)
                UserLocationPuck(effectiveLat, effectiveLon)
            }

            // Route editing toolbar
            if (vm.editRoute != null) {
                RouteEditOverlay(
                    modifier = Modifier.align(Alignment.TopCenter).padding(horizontal = 12.dp, vertical = 8.dp),
                    routeName = vm.editRoute!!.name,
                    nodeCount = vm.editingNodes.size,
                    onSave = {
                        val updated = vm.editRoute!!.copy(waypoints = vm.editingNodes.map { RouteWaypoint(it.latitude, it.longitude) })
                        scope.launch { vm.routesRepo.update(updated); vm.editRoute = null }
                    },
                    onCancel = { vm.editRoute = null },
                )
            }

            // Floating player controls
            if (player.isFollowing) {
                FloatingPlayerOverlay(
                    player = player,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(start = 12.dp, end = 12.dp, bottom = 140.dp),
                )
            }
        }
    }
}

@Composable
private fun RouteEditOverlay(
    modifier: Modifier = Modifier,
    routeName: String,
    nodeCount: Int,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.EditLocationAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text("Editing: $routeName", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        "$nodeCount point${if (nodeCount != 1) "s" else ""} — tap map to add, drag to move",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically, ) {
                OutlinedButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Discard")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = onSave) {
                    Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Save")
                }
            }
        }
    }
}
