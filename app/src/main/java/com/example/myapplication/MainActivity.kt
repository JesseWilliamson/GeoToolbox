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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlin.collections.emptyList

class MainActivity : ComponentActivity() {

    private lateinit var spoofer: Spoofer
    private var isSpoofing by mutableStateOf(false)

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

    override fun onDestroy() {
        super.onDestroy()
        if (isSpoofing) stopSpoofing()
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
            val url = staticMapUrl(point.latitude, point.longitude)
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
    onDismiss: () -> Unit,
    onSave: (id: String?, name: String, lat: Double, lon: Double) -> Unit,
) {
    var name by remember { mutableStateOf(point?.name ?: "") }
    var latText by remember { mutableStateOf((point?.latitude ?: currentLat).toString()) }
    var lonText by remember { mutableStateOf((point?.longitude ?: currentLon).toString()) }
    LaunchedEffect(point) {
        name = point?.name ?: ""
        latText = (point?.latitude ?: currentLat).toString()
        lonText = (point?.longitude ?: currentLon).toString()
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val lat = latText.toDoubleOrNull()
                    val lon = lonText.toDoubleOrNull()
                    if (name.isNotBlank() && lat != null && lon != null) {
                        onSave(point?.id, name.trim(), lat, lon)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GpsSpooferScreen(
    modifier: Modifier = Modifier,
) {
    var latText by remember { mutableStateOf("0") }
    var lonText by remember { mutableStateOf("0") }
    val context = LocalContext.current
    val repository = remember { SavedPointsRepository(context) }
    val savedPoints by repository.savedPoints.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var addEditPoint: SavedPoint? by remember { mutableStateOf(null) }
    var deletePoint: SavedPoint? by remember { mutableStateOf(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val currentLat = latText.toDoubleOrNull() ?: 0.0
    val currentLon = lonText.toDoubleOrNull() ?: 0.0
    val currentLocation = LatLng(currentLat, currentLon)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    LaunchedEffect(latText, lonText) {
        val lat = latText.toDoubleOrNull()
        val lon = lonText.toDoubleOrNull()
        if (lat != null && lon != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), cameraPositionState.position.zoom)
            )
        }
    }

    if (showAddDialog) {
        AddEditPointDialog(
            point = null,
            currentLat = currentLat,
            currentLon = currentLon,
            onDismiss = { showAddDialog = false },
            onSave = { _, name, lat, lon ->
                scope.launch { repository.add(name, lat, lon) }
            }
        )
    }
    addEditPoint?.let { point ->
        AddEditPointDialog(
            point = point,
            currentLat = currentLat,
            currentLon = currentLon,
            onDismiss = { addEditPoint = null },
            onSave = { id, name, lat, lon ->
                if (id != null) {
                    scope.launch {
                        repository.update(point.copy(name = name, latitude = lat, longitude = lon))
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
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DrawerContents(
                    items = savedPoints,
                    onPointClick = { point ->
                        latText = point.latitude.toString()
                        lonText = point.longitude.toString()
                    },
                    onEdit = { addEditPoint = it },
                    onDelete = { deletePoint = it }
                )
            }
        }
    ) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.ui.graphics.Color(0xFFE8F5E9)),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                latText = latLng.latitude.toString()
                lonText = latLng.longitude.toString()
                showAddDialog = true
            }
        )
    }
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
