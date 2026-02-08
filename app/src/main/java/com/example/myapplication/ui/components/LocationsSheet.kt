package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.SavedPoint
import com.example.myapplication.staticMapUrl

@Composable
fun LocationsSection(
    items: List<SavedPoint>,
    onPointClick: (SavedPoint) -> Unit,
    onEdit: (SavedPoint) -> Unit,
    onDelete: (SavedPoint) -> Unit,
) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Saved locations", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(horizontal = 16.dp))
        if (items.isEmpty()) {
            Text("Tap the map to save a location.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(16.dp))
        } else {
            LazyRow(
                Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(items, key = { it.id }) { item ->
                    SavedPointCard(item, onPointClick, onEdit, onDelete)
                }
            }
        }
    }
}

@Composable
private fun SavedPointCard(
    point: SavedPoint,
    onPointClick: (SavedPoint) -> Unit,
    onEdit: (SavedPoint) -> Unit,
    onDelete: (SavedPoint) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Column(
            Modifier.width(186.dp).height(205.dp)
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable { onPointClick(point) }
        ) {
            AsyncImage(
                model = staticMapUrl(point.latitude, point.longitude, point.zoom.toInt().coerceIn(1, 22)),
                contentDescription = point.name,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_launcher_foreground),
                error = painterResource(R.drawable.ic_launcher_foreground),
            )
            Box(
                Modifier.fillMaxWidth().height(45.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                Text(point.name, style = MaterialTheme.typography.labelMedium, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp))
            }
        }
        IconButton(onClick = { showMenu = true }, Modifier.align(Alignment.TopEnd).padding(4.dp)) {
            Icon(Icons.Default.MoreVert, "Options")
        }
        DropdownMenu(showMenu, onDismissRequest = { showMenu = false }) {
            DropdownMenuItem(text = { Text("Use location") }, onClick = { showMenu = false; onPointClick(point) })
            DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit(point) }, leadingIcon = { Icon(Icons.Default.Edit, null) })
            DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete(point) }, leadingIcon = { Icon(Icons.Default.Delete, null) })
        }
    }
}
