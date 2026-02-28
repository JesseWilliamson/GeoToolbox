package com.example.myapplication.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.myapplication.Route
import com.example.myapplication.RoutePlayerController

@Composable
fun RoutesSection(
    routes: List<Route>,
    player: RoutePlayerController,
    onAddRoute: () -> Unit,
    onEditRoute: (Route) -> Unit,
    onDeleteRoute: (Route) -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Routes", style = MaterialTheme.typography.titleMedium)
            FilledTonalButton(onClick = onAddRoute) { Text("+ New route") }
        }

        if (routes.isEmpty()) {
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
                Text(
                    "No routes yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "Create a route, draw waypoints on the map, then tap Follow to simulate movement",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                )
            }
        } else {
            routes.forEach { route ->
                RouteCard(
                    route = route,
                    isFollowing = player.isFollowing,
                    onPreview = { player.preview(route) },
                    onFollow = { player.follow(route) },
                    onEdit = { onEditRoute(route) },
                    onDelete = { onDeleteRoute(route) },
                )
            }
        }
    }
}

@Composable
fun FloatingPlayerOverlay(player: RoutePlayerController, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header: speed + stop
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Column {
                    Text("Now following", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Text(
                        "%.0f km/h".format(player.speedMps * 3.6),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
                FilledTonalIconButton(onClick = player::stop) {
                    Icon(Icons.Default.Close, "Stop")
                }
            }

            // Speed slider with labels
            Column {
                Text("Speed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Slider(
                    value = player.speedMps.toFloat(),
                    onValueChange = { player.speedMps = it.toDouble() },
                    valueRange = 2f..80f,
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("7 km/h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("288 km/h", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Progress
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                    Text("Progress", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("%.0f%%".format(player.progress * 100), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                }
                Slider(
                    player.progress,
                    onValueChange = player::seek,
                    valueRange = 0f..1f,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Pause / Resume button
            Button(
                onClick = if (player.isPaused) player::resume else player::pause,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    if (player.isPaused) Icons.Default.PlayArrow else Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(if (player.isPaused) "Resume" else "Pause")
            }
        }
    }
}

@Composable
private fun RouteCard(
    route: Route,
    isFollowing: Boolean,
    onPreview: () -> Unit,
    onFollow: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .6f)),
        onClick = onPreview,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(route.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${route.waypoints.size} waypoint${if (route.waypoints.size != 1) "s" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
                if (route.waypoints.size >= 2) {
                    Button(
                        onClick = onFollow,
                        enabled = !isFollowing,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Follow")
                    }
                }
                OutlinedButton(
                    onClick = onEdit,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = MaterialTheme.colorScheme.error.copy(alpha = .7f)),
                ) {
                    Icon(Icons.Default.Delete, "Delete", Modifier.size(20.dp))
                }
            }
        }
    }
}
