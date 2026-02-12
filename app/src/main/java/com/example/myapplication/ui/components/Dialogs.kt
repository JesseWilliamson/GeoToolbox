package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.SavedPoint

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

    val isNew = point == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "Save location" else "Edit location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (!isNew) {
                    OutlinedTextField(latText, { latText = it }, label = { Text("Latitude") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(lonText, { lonText = it }, label = { Text("Longitude") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(zoomText, { zoomText = it }, label = { Text("Zoom (2–22)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val lat = latText.toDoubleOrNull()
                val lon = lonText.toDoubleOrNull()
                val zoom = zoomText.toFloatOrNull()?.coerceIn(2f, 22f) ?: 15f
                if (name.isNotBlank() && lat != null && lon != null) {
                    onSave(point?.id, name.trim(), lat, lon, zoom)
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
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
        confirmButton = { Button(onClick = { onConfirm(); onDismiss() }) { Text("Delete") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
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
            OutlinedTextField(name, { name = it }, label = { Text("Route name") }, modifier = Modifier.fillMaxWidth())
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { Text("Create") } },
        dismissButton = { Button(onClick = onDismiss) { Text("Cancel") } }
    )
}
