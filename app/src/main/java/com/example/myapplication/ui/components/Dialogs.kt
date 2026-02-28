package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
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
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(point) {
        name = point?.name ?: ""
        latText = (point?.latitude ?: currentLat).toString()
        lonText = (point?.longitude ?: currentLon).toString()
        zoomText = (point?.zoom ?: currentZoom).toString()
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val isNew = point == null

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text(if (isNew) "Save location" else "Edit location") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    name,
                    { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g. Home, Office...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
                if (!isNew) {
                    OutlinedTextField(
                        latText, { latText = it },
                        label = { Text("Latitude") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        lonText, { lonText = it },
                        label = { Text("Longitude") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        zoomText, { zoomText = it },
                        label = { Text("Zoom level") },
                        supportingText = { Text("Between 2 and 22") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                    )
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
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
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
        title = { Text("Delete \"${point.name}\"?") },
        text = { Text("This location will be permanently removed.") },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            ) { Text("Delete") }
        },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
fun AddRouteDialog(
    onDismiss: () -> Unit,
    onSave: (name: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("New route") },
        text = {
            OutlinedTextField(
                name,
                { name = it },
                label = { Text("Route name") },
                placeholder = { Text("e.g. Morning commute...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
        },
        confirmButton = { Button(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { Text("Create") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
