package com.example.myapplication

import SavedPoint
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Slider
import androidx.compose.ui.draw.clip
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import java.util.UUID
import kotlinx.coroutines.launch
import kotlin.collections.emptyList
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    private lateinit var spoofer: Spoofer
    private var isSpoofing by mutableStateOf(false)
    private val routeFollowingLocationState = mutableStateOf<Pair<Double, Double>?>(null)
    private val isFollowingRouteState = mutableStateOf(false)
    private val followedRouteWaypointsState = mutableStateOf<List<RouteWaypoint>?>(null)
    private val followSpeedMpsState = mutableStateOf(25.0)
    private var routeRunner: RouteRunner? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openDeveloperOptions()
        else Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        spoofer = Spoofer(this)
        setContent {
            MyApplicationTheme {
                Scaffold(Modifier.fillMaxSize()) { padding ->
                    GpsSpooferScreen(
                        modifier = Modifier.padding(padding),
                        routeFollowingLocationState = routeFollowingLocationState,
                        isFollowingRouteState = isFollowingRouteState,
                        followedRouteWaypoints = followedRouteWaypointsState.value,
                        followSpeedMps = followSpeedMpsState.value,
                        onFollowSpeedChange = { followSpeedMpsState.value = it },
                        onFollowRoute = { startFollowingRoute(it) },
                        onStopRoute = { stopFollowingRoute() },
                    )
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
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (_: Exception) {}
        Toast.makeText(this, "Developer Options → set this app as \"Mock location app\"", Toast.LENGTH_LONG).show()
    }

    private fun stopSpoofing() {
        spoofer.stopSpoofing()
        isSpoofing = false
    }

    private fun startFollowingRoute(route: Route) {
        val points = route.waypoints
        if (points.isEmpty()) return
        if (!spoofer.startSpoofing(points[0].latitude, points[0].longitude)) {
            Toast.makeText(this, "Could not start mock location. Set this app as Mock location app in Developer options.", Toast.LENGTH_LONG).show()
            return
        }
        isSpoofing = true
        followedRouteWaypointsState.value = points
        routeFollowingLocationState.value = Pair(points[0].latitude, points[0].longitude)
        isFollowingRouteState.value = true
        routeRunner = RouteRunner(
            waypoints = points,
            speedProvider = { followSpeedMpsState.value },
            onUpdate = { lat, lon ->
                spoofer.setLocation(lat, lon)
                routeFollowingLocationState.value = Pair(lat, lon)
            },
            onComplete = { stopFollowingRoute() }
        )
        routeRunner?.start()
    }

    private fun stopFollowingRoute() {
        routeRunner?.stop()
        routeRunner = null
        stopSpoofing()
        followedRouteWaypointsState.value = null
        routeFollowingLocationState.value = null
        isFollowingRouteState.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFollowingRouteState.value) stopFollowingRoute()
        else if (isSpoofing) stopSpoofing()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawerContents(
    items: List<SavedPoint>,
    onPointClick: (SavedPoint) -> Unit,
    onEdit: (SavedPoint) -> Unit,
    onDelete: (SavedPoint) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            "Saved locations",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        if (items.isEmpty()) {
            Text(
                "Tap the map to save a location.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    SavedPointCard(
                        point = item,
                        onPointClick = onPointClick,
                        onEdit = onEdit,
                        onDelete = onDelete
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPointCard(
    point: SavedPoint,
    onPointClick: (SavedPoint) -> Unit,
    onEdit: (SavedPoint) -> Unit,
    onDelete: (SavedPoint) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Column(
            modifier = Modifier
                .width(186.dp)
                .height(205.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable { onPointClick(point) }
        ) {
            val url = staticMapUrl(point.latitude, point.longitude, point.zoom.toInt().coerceIn(1, 22))
            AsyncImage(
                model = url,
                contentDescription = point.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = point.name,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
        IconButton(
            onClick = { showMenu = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        androidx.compose.material3.DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("Use location") },
                onClick = {
                    showMenu = false
                    onPointClick(point)
                }
            )
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    showMenu = false
                    onEdit(point)
                },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    showMenu = false
                    onDelete(point)
                },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}

@Composable
fun AddEditPointDialog(
    point: SavedPoint?,
    currentLat: Double,
    currentLon: Double,
    currentZoom: Float,
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, lat: Double, lon: Double, zoom: Float) -> Unit,
) {
    var name by remember { mutableStateOf(point?.name ?: "") }
    var latText by remember { mutableStateOf((point?.latitude ?: currentLat).toString()) }
    var lonText by remember { mutableStateOf((point?.longitude ?: currentLon).toString()) }
    var zoomText by remember { mutableStateOf((point?.zoom ?: currentZoom).toString()) }
    LaunchedEffect(point) {
        name = point?.name ?: ""
        latText = (point?.latitude ?: currentLat).toString()
        lonText = (point?.longitude ?: currentLon).toString()
        zoomText = (point?.zoom ?: currentZoom).toString()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (point == null) "Save location" else "Edit location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = latText,
                    onValueChange = { latText = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lonText,
                    onValueChange = { lonText = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = zoomText,
                    onValueChange = { zoomText = it },
                    label = { Text("Zoom (2–22)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lon = lonText.toDoubleOrNull()
                    val zoom = zoomText.toFloatOrNull()?.coerceIn(2f, 22f) ?: 15f
                    if (name.isNotBlank() && lat != null && lon != null) {
                        onSave(point?.id, name.trim(), lat, lon, zoom)
                        onDismiss()
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DeleteConfirmDialog(
    point: SavedPoint,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete location?") },
        text = { Text("Remove \"${point.name}\" from saved locations?") },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) { Text("Delete") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun UserLocationPuck(effectiveLat: Double, effectiveLon: Double) {
    val puckState = rememberMarkerState(position = LatLng(effectiveLat, effectiveLon))
    LaunchedEffect(effectiveLat, effectiveLon) {
        puckState.position = LatLng(effectiveLat, effectiveLon)
    }
    Marker(
        state = puckState,
        title = "You",
        draggable = false
    )
}

@Composable
fun RouteNodeMarker(
    node: RouteNode,
    onPositionChange: (lat: Double, lon: Double) -> Unit,
) {
    val markerState = remember(node.id) {
        MarkerState(position = LatLng(node.latitude, node.longitude))
    }
    LaunchedEffect(markerState.position) {
        onPositionChange(markerState.position.latitude, markerState.position.longitude)
    }
    Marker(
        state = markerState,
        draggable = true,
    )
}

@Composable
fun RouteEditOverlay(
    modifier: Modifier = Modifier,
    routeName: String,
    nodeCount: Int,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(routeName, style = MaterialTheme.typography.titleMedium)
        Text(
            "Tap map to add points, drag nodes to move. $nodeCount point(s).",
            style = MaterialTheme.typography.bodySmall
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCancel) { Text("Cancel") }
            Spacer(modifier = Modifier.padding(8.dp))
            Button(onClick = onSave) { Text("Save") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsSpooferScreen(
    modifier: Modifier = Modifier,
    routeFollowingLocationState: State<Pair<Double, Double>?>? = null,
    isFollowingRouteState: State<Boolean>? = null,
    followedRouteWaypoints: List<RouteWaypoint>? = null,
    followSpeedMps: Double = 25.0,
    onFollowSpeedChange: (Double) -> Unit = {},
    onFollowRoute: (Route) -> Unit = {},
    onStopRoute: () -> Unit = {},
) {
    var latText by remember { mutableStateOf("0") }
    var lonText by remember { mutableStateOf("0") }
    val context = LocalContext.current
    val repository = remember { SavedPointsRepository(context) }
    val routesRepository = remember { RoutesRepository(context) }
    val savedPoints by repository.savedPoints.collectAsState(initial = emptyList())
    val routes by routesRepository.routes.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val routeFollowingLocation = routeFollowingLocationState?.value
    val isFollowingRoute = isFollowingRouteState?.value ?: false

    var addEditPoint: SavedPoint? by remember { mutableStateOf(null) }
    var deletePoint: SavedPoint? by remember { mutableStateOf(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var targetZoom by remember { mutableStateOf<Float?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Locations, 1 = Routes
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var editRoute: Route? by remember { mutableStateOf(null) }
    var editingNodes by remember { mutableStateOf<List<RouteNode>>(emptyList()) }

    LaunchedEffect(editRoute) {
        editingNodes = when (val r = editRoute) {
            null -> emptyList()
            else -> r.waypoints.map { RouteWaypoint(it.latitude, it.longitude) }.mapIndexed { i, w ->
                RouteNode("n_$i", w.latitude, w.longitude)
            }
        }
    }

    val currentLat = latText.toDoubleOrNull() ?: 0.0
    val currentLon = lonText.toDoubleOrNull() ?: 0.0
    val effectiveLat = routeFollowingLocation?.first ?: currentLat
    val effectiveLon = routeFollowingLocation?.second ?: currentLon
    val currentLocation = LatLng(effectiveLat, effectiveLon)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    LaunchedEffect(effectiveLat, effectiveLon) {
        val zoom = targetZoom ?: cameraPositionState.position.zoom
        if (targetZoom != null) targetZoom = null
        val update = CameraUpdateFactory.newLatLngZoom(LatLng(effectiveLat, effectiveLon), zoom)
        if (isFollowingRoute) {
            cameraPositionState.move(update)
        } else {
            cameraPositionState.animate(update)
        }
    }

    if (showAddDialog) {
        AddEditPointDialog(
            point = null,
            currentLat = currentLat,
            currentLon = currentLon,
            currentZoom = cameraPositionState.position.zoom,
            onDismiss = { showAddDialog = false },
            onSave = { _, name, lat, lon, zoom ->
                scope.launch { repository.add(name, lat, lon, zoom) }
            }
        )
    }
    addEditPoint?.let { point ->
        AddEditPointDialog(
            point = point,
            currentLat = currentLat,
            currentLon = currentLon,
            currentZoom = cameraPositionState.position.zoom,
            onDismiss = { addEditPoint = null },
            onSave = { id, name, lat, lon, zoom ->
                if (id != null) {
                    scope.launch {
                        repository.update(point.copy(name = name, latitude = lat, longitude = lon, zoom = zoom))
                    }
                }
                addEditPoint = null
            }
        )
    }
    deletePoint?.let { point ->
        DeleteConfirmDialog(
            point = point,
            onDismiss = { deletePoint = null },
            onConfirm = { scope.launch { repository.delete(point.id) } }
        )
    }
    if (showAddRouteDialog) {
        AddRouteDialog(
            onDismiss = { showAddRouteDialog = false },
            onSave = { name ->
                val route = Route(UUID.randomUUID().toString(), name.trim(), emptyList())
                scope.launch {
                    routesRepository.add(route)
                    editRoute = route
                    showAddRouteDialog = false
                }
            }
        )
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded,
            confirmValueChange = { it != SheetValue.Hidden }
        )
    )

    BottomSheetScaffold(
        modifier = modifier,
        scaffoldState = scaffoldState,
        sheetPeekHeight = 128.dp,
        sheetContent = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        label = { Text("Locations") }
                    )
                    FilterChip(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        label = { Text("Routes") }
                    )
                }
                if (selectedTab == 0) {
                    DrawerContents(
                        items = savedPoints,
                        onPointClick = { point ->
                            latText = point.latitude.toString()
                            lonText = point.longitude.toString()
                            targetZoom = point.zoom.coerceIn(2f, 22f)
                        },
                        onEdit = { addEditPoint = it },
                        onDelete = { deletePoint = it }
                    )
                } else {
                    RoutesContent(
                        routes = routes,
                        isFollowingRoute = isFollowingRoute,
                        followSpeedMps = followSpeedMps,
                        onFollowSpeedChange = onFollowSpeedChange,
                        onAddRoute = { showAddRouteDialog = true },
                        onEditRoute = { editRoute = it },
                        onFollowRoute = onFollowRoute,
                        onStopRoute = onStopRoute,
                        onUpdateRoute = { scope.launch { routesRepository.update(it) } },
                        onDeleteRoute = { scope.launch { routesRepository.delete(it.id) } }
                    )
                }
            }
        }
    ) {
        Box(Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.ui.graphics.Color(0xFFE8F5E9)),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    if (editRoute != null) {
                        editingNodes = editingNodes + RouteNode(
                            UUID.randomUUID().toString(),
                            latLng.latitude,
                            latLng.longitude
                        )
                    } else {
                        latText = latLng.latitude.toString()
                        lonText = latLng.longitude.toString()
                        showAddDialog = true
                    }
                }
            ) {
                if (editRoute != null && editingNodes.isNotEmpty()) {
                    Polyline(
                        points = editingNodes.map { LatLng(it.latitude, it.longitude) },
                        color = Color(0xFF1976D2),
                        width = 14f
                    )
                    editingNodes.forEach { node ->
                        RouteNodeMarker(
                            node = node,
                            onPositionChange = { lat, lon ->
                                editingNodes = editingNodes.map {
                                    if (it.id == node.id) it.copy(latitude = lat, longitude = lon) else it
                                }
                            }
                        )
                    }
                }
                if (followedRouteWaypoints != null && followedRouteWaypoints.size >= 2) {
                    Polyline(
                        points = followedRouteWaypoints.map { LatLng(it.latitude, it.longitude) },
                        color = Color(0xFF388E3C),
                        width = 12f
                    )
                }
                UserLocationPuck(effectiveLat = effectiveLat, effectiveLon = effectiveLon)
            }
            if (editRoute != null) {
                RouteEditOverlay(
                    modifier = Modifier.align(Alignment.TopCenter),
                    routeName = editRoute!!.name,
                    nodeCount = editingNodes.size,
                    onSave = {
                        val route = editRoute!!.copy(
                            waypoints = editingNodes.map { RouteWaypoint(it.latitude, it.longitude) }
                        )
                        scope.launch {
                            routesRepository.update(route)
                            editRoute = null
                        }
                    },
                    onCancel = { editRoute = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutesContent(
    routes: List<Route>,
    isFollowingRoute: Boolean,
    followSpeedMps: Double,
    onFollowSpeedChange: (Double) -> Unit,
    onAddRoute: () -> Unit,
    onEditRoute: (Route) -> Unit,
    onFollowRoute: (Route) -> Unit,
    onStopRoute: () -> Unit,
    onUpdateRoute: (Route) -> Unit,
    onDeleteRoute: (Route) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Routes", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onAddRoute, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                Text("Add route")
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                "Follow speed: %.0f m/s (%.0f km/h)".format(followSpeedMps, followSpeedMps * 3.6),
                style = MaterialTheme.typography.labelMedium
            )
            Slider(
                value = followSpeedMps.toFloat(),
                onValueChange = { onFollowSpeedChange(it.toDouble()) },
                valueRange = 2f..80f,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (isFollowingRoute) {
            Button(
                onClick = onStopRoute,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Stop following")
            }
        }
        if (routes.isEmpty()) {
            Text(
                "Add a route, then add waypoints by editing it. Tap Follow to simulate moving along the path.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            routes.forEach { route ->
                RouteCard(
                    route = route,
                    onEdit = { onEditRoute(route) },
                    onFollow = { onFollowRoute(route) },
                    onDelete = { onDeleteRoute(route) },
                    isFollowingRoute = isFollowingRoute
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteCard(
    route: Route,
    onEdit: () -> Unit,
    onFollow: () -> Unit,
    onDelete: () -> Unit,
    isFollowingRoute: Boolean,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(route.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${route.waypoints.size} waypoints",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (route.waypoints.size >= 2) {
                    Button(
                        onClick = onFollow,
                        enabled = !isFollowingRoute,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, Modifier.size(16.dp))
                        Text("Follow", modifier = Modifier.padding(start = 2.dp))
                    }
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }
            }
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { showMenu = false; onDelete() },
                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
            )
        }
    }
}

@Composable
fun AddRouteDialog(
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New route") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Route name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { Text("Create") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EditRouteDialog(
    route: Route,
    currentLat: Double,
    currentLon: Double,
    savedPoints: List<SavedPoint>,
    onDismiss: () -> Unit,
    onSave: (Route) -> Unit,
) {
    var name by remember(route.id) { mutableStateOf(route.name) }
    var waypoints by remember(route.id) { mutableStateOf(route.waypoints.toList()) }
    LaunchedEffect(route.id) {
        name = route.name
        waypoints = route.waypoints.toList()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit route") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Route name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Waypoints (${waypoints.size})", style = MaterialTheme.typography.labelMedium)
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    waypoints.forEachIndexed { index, wp ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${index + 1}. %.5f, %.5f".format(wp.latitude, wp.longitude),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { waypoints = waypoints.filterIndexed { i, _ -> i != index } }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove")
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { waypoints = waypoints + RouteWaypoint(currentLat, currentLon) },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Add current location")
                    }
                    if (savedPoints.isNotEmpty()) {
                        var showPicker by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { showPicker = true },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("Add saved…")
                            }
                            DropdownMenu(
                                expanded = showPicker,
                                onDismissRequest = { showPicker = false }
                            ) {
                                savedPoints.forEach { pt ->
                                    DropdownMenuItem(
                                        text = { Text("${pt.name} (%.4f, %.4f)".format(pt.latitude, pt.longitude)) },
                                        onClick = {
                                            waypoints = waypoints + RouteWaypoint(pt.latitude, pt.longitude)
                                            showPicker = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(route.copy(name = name.trim(), waypoints = waypoints))
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun DrawerContentsPreview() {
    val items = remember {
        listOf(
            SavedPoint("1", "Sydney", -33.865143, 151.209900),
            SavedPoint("2", "Melbourne", -37.813627, 144.96305),
            SavedPoint("3", "Hong Kong", 22.3193, 114.16)
        )
    }
    DrawerContents(
        items = items,
        onPointClick = {},
        onEdit = {},
        onDelete = {}
    )
}
