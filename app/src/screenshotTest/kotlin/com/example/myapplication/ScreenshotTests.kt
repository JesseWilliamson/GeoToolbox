package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.EditLocationAlt
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.myapplication.ui.components.AddEditPointDialog
import com.example.myapplication.ui.components.AddRouteDialog
import com.example.myapplication.ui.components.DeleteConfirmDialog
import com.example.myapplication.ui.components.LocationsSection
import com.example.myapplication.ui.theme.MyApplicationTheme

// ── Locations tab ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun LocationsTabEmptyPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = true, onClick = {}, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Locations") }
                SegmentedButton(selected = false, onClick = {}, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Routes") }
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

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun LocationsTabWithItemsPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = true, onClick = {}, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Locations") }
                SegmentedButton(selected = false, onClick = {}, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Routes") }
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

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RoutesTabEmptyPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = false, onClick = {}, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Locations") }
                SegmentedButton(selected = true, onClick = {}, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Routes") }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Routes", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = {}) { Text("+ New route") }
            }
            Column(
                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Route,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .5f),
                )
                Text("No routes yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    "Create a route, draw waypoints on the map, then tap Follow to simulate movement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RoutesTabWithRoutesPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                SegmentedButton(selected = false, onClick = {}, shape = SegmentedButtonDefaults.itemShape(0, 2)) { Text("Locations") }
                SegmentedButton(selected = true, onClick = {}, shape = SegmentedButtonDefaults.itemShape(1, 2)) { Text("Routes") }
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Routes", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = {}) { Text("+ New route") }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .6f)),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Column {
                Text(name, style = MaterialTheme.typography.titleSmall)
                Text("$waypointCount waypoints", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                Button(onClick = {}, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Follow")
                }
                OutlinedButton(onClick = {}) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
                IconButton(
                    onClick = {},
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = .7f)),
                ) { Icon(Icons.Default.Delete, "Delete", Modifier.size(20.dp)) }
            }
        }
    }
}

// ── Route player controls ────────────────────────────────────────────

@PreviewTest
@Preview(showBackground = true, widthDp = 400)
@Composable
fun RoutePlayerControlsPreview() {
    MyApplicationTheme(dynamicColor = false) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Column {
                        Text("Now following", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        Text("90 km/h", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    }
                    FilledTonalIconButton(onClick = {}) { Icon(Icons.Default.Close, "Stop") }
                }
                Column {
                    Text("Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Slider(value = 25f, onValueChange = {}, valueRange = 2f..80f, modifier = Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                        Text("7 km/h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("288 km/h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Text("Progress", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("42%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                    }
                    Slider(value = 0.42f, onValueChange = {}, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
                }
                Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Pause")
                }
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
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.EditLocationAlt, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Editing: Morning Commute", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text("5 points — tap map to add, drag to move", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Row(Modifier.fillMaxWidth(), Arrangement.End, Alignment.CenterVertically) {
                    OutlinedButton(onClick = {}) {
                        Icon(Icons.Default.Close, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Discard")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {}) {
                        Icon(Icons.Default.Check, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save")
                    }
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
