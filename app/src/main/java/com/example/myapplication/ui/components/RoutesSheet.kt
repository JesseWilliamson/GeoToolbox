package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Routes", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onAddRoute, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) { Text("Add route") }
        }

        SpeedSlider(player)

        if (player.isFollowing) {
            PlayerControls(player)
        }

        if (routes.isEmpty()) {
            Text("Add a route, then edit it on the map. Tap Follow to simulate movement.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(vertical = 8.dp))
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
private fun SpeedSlider(player: RoutePlayerController) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Speed: %.0f m/s (%.0f km/h)".format(player.speedMps, player.speedMps * 3.6), style = MaterialTheme.typography.labelMedium)
        Slider(
            value = player.speedMps.toFloat(),
            onValueChange = { player.speedMps = it.toDouble() },
            valueRange = RoutePlayerController.MIN_SPEED_MPS.toFloat()..RoutePlayerController.MAX_SPEED_MPS.toFloat(),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun PlayerControls(player: RoutePlayerController) {
    Column(
        Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant).padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Route player", style = MaterialTheme.typography.titleSmall)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = if (player.isPaused) player::resume else player::pause,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            ) { Text(if (player.isPaused) "Resume" else "Pause") }
            Column(Modifier.weight(1f)) {
                Slider(player.progress, onValueChange = player::seek, valueRange = 0f..1f, modifier = Modifier.fillMaxWidth())
                Text("%.0f%%".format(player.progress * 100), style = MaterialTheme.typography.labelSmall)
            }
        }
        Button(
            onClick = player::stop,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth(),
        ) { Text("Stop") }
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
    var showMenu by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Row(
            Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .5f)).padding(12.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).clickable(onClick = onPreview)) {
                Text(route.name, style = MaterialTheme.typography.titleSmall)
                Text("${route.waypoints.size} waypoints", style = MaterialTheme.typography.bodySmall)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (route.waypoints.size >= 2) {
                    Button(onClick = onFollow, enabled = !isFollowing, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                        Icon(Icons.Default.PlayArrow, null, Modifier.size(16.dp))
                        Text("Follow", Modifier.padding(start = 2.dp))
                    }
                }
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Options") }
            }
        }
        DropdownMenu(showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null) })
        }
    }
}
