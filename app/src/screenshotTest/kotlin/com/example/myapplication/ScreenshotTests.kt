package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.myapplication.ui.components.AddEditPointDialog
import com.example.myapplication.ui.components.AddRouteDialog
import com.example.myapplication.ui.components.DeleteConfirmDialog
import com.example.myapplication.ui.components.LocationsSection
import com.example.myapplication.ui.theme.MyApplicationTheme

// ── Locations tab ────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun LocationsTabEmptyPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = true, onClick = {}, label = { Text("Locations") })
                FilterChip(selected = false, onClick = {}, label = { Text("Routes") })
            }
            LocationsSection(
                items = emptyList(),
                onPointClick = {},
                onEdit = {},
                onDelete = {},
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun LocationsTabWithItemsPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = true, onClick = {}, label = { Text("Locations") })
                FilterChip(selected = false, onClick = {}, label = { Text("Routes") })
            }
            LocationsSection(
                items = listOf(
                    SavedPoint("1", "Home", 37.7749, -122.4194, 15f),
                    SavedPoint("2", "Office", 37.3861, -122.0839, 14f),
                    SavedPoint("3", "Park", 37.7694, -122.4862, 16f),
                ),
                onPointClick = {},
                onEdit = {},
                onDelete = {},
            )
        }
    }
}

// ── Routes tab ───────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RoutesTabEmptyPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = {}, label = { Text("Locations") })
                FilterChip(selected = true, onClick = {}, label = { Text("Routes") })
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Routes", style = MaterialTheme.typography.titleMedium)
                Button(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Add route")
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Speed: 25 m/s (90 km/h)", style = MaterialTheme.typography.labelMedium)
                Slider(value = 25f, onValueChange = {}, valueRange = 2f..80f, modifier = Modifier.fillMaxWidth())
            }
            Text(
                "Add a route, then edit it on the map. Tap Follow to simulate movement.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        }
    }
}

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RoutesTabWithRoutesPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = false, onClick = {}, label = { Text("Locations") })
                FilterChip(selected = true, onClick = {}, label = { Text("Routes") })
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Routes", style = MaterialTheme.typography.titleMedium)
                Button(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text("Add route")
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Speed: 25 m/s (90 km/h)", style = MaterialTheme.typography.labelMedium)
                Slider(value = 25f, onValueChange = {}, valueRange = 2f..80f, modifier = Modifier.fillMaxWidth())
            }
            // Sample route cards
            RouteCardPreview("Morning Commute", 5)
            RouteCardPreview("Park Loop", 8)
            RouteCardPreview("Downtown Tour", 12)
        }
    }
}

@Composable
private fun RouteCardPreview(name: String, waypointCount: Int) {
    Row(
        Modifier.fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f))
            .padding(12.dp),
        Arrangement.SpaceBetween,
        Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall)
            Text("$waypointCount waypoints", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = {}, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            Icon(Icons.Default.PlayArrow, null, Modifier.padding(end = 2.dp))
            Text("Follow")
        }
    }
}

// ── Route player controls ────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RoutePlayerControlsPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(
                Modifier.fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Route player", style = MaterialTheme.typography.titleSmall)
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Button(onClick = {}, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                        Text("Pause")
                    }
                    Column(Modifier.weight(1f)) {
                        Slider(value = 0.42f, onValueChange = {}, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
                        Text("42%", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("Stop") }
            }
        }
    }
}

// ── Route edit overlay ───────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RouteEditOverlayPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Box(Modifier.fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Morning Commute", style = MaterialTheme.typography.titleMedium)
                Text("Tap map to add points, drag nodes to move. 5 point(s).", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                    Button(onClick = {}) { Text("Cancel") }
                    Box(Modifier.padding(8.dp))
                    Button(onClick = {}) { Text("Save") }
                }
            }
        }
    }
}

// ── Dialogs ──────────────────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true)
@Composable
fun AddLocationDialogPreview() {
    MyApplicationTheme(dynamicColor = false) {
        AddEditPointDialog(
            point = null,
            currentLat = 37.7749,
            currentLon = -122.4194,
            currentZoom = 15f,
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun EditLocationDialogPreview() {
    MyApplicationTheme(dynamicColor = false) {
        AddEditPointDialog(
            point = SavedPoint("1", "Home", 37.7749, -122.4194, 15f),
            currentLat = 37.7749,
            currentLon = -122.4194,
            currentZoom = 15f,
            onDismiss = {},
            onSave = { _, _, _, _, _ -> },
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun DeleteLocationDialogPreview() {
    MyApplicationTheme(dynamicColor = false) {
        DeleteConfirmDialog(
            point = SavedPoint("1", "Home", 37.7749, -122.4194, 15f),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@PreviewTest
@Preview(showBackground = true)
@Composable
fun NewRouteDialogPreview() {
    MyApplicationTheme(dynamicColor = false) {
        AddRouteDialog(
            onDismiss = {},
            onSave = {},
        )
    }
}
