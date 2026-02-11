package com.example.myapplication

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

/**
 * Holds all mutable UI state for [GpsSpooferScreen] so it survives
 * configuration changes (e.g. device rotation).
 *
 * Also owns [Spoofer] and [RoutePlayerController] whose background work
 * (Handler ticks, mock-location updates) must not restart on rotation.
 */
class GpsSpooferViewModel(application: Application) : AndroidViewModel(application) {

    /* ── Domain objects ─────────────────────────────────────────────── */

    val spoofer = Spoofer(application)

    val player = RoutePlayerController(spoofer) { msg ->
        Toast.makeText(application, msg, Toast.LENGTH_LONG).show()
    }

    val pointsRepo = SavedPointsRepository(application)
    val routesRepo = RoutesRepository(application)

    /* ── Coordinate input ──────────────────────────────────────────── */

    var latText by mutableStateOf("0")
    var lonText by mutableStateOf("0")

    /* ── Camera ────────────────────────────────────────────────────── */

    var targetZoom by mutableStateOf<Float?>(null)

    /* ── Dialog visibility ─────────────────────────────────────────── */

    var showAddDialog by mutableStateOf(false)
    var addEditPoint by mutableStateOf<SavedPoint?>(null)
    var deletePoint by mutableStateOf<SavedPoint?>(null)
    var showAddRouteDialog by mutableStateOf(false)

    /* ── Route editor ──────────────────────────────────────────────── */

    var editRoute by mutableStateOf<Route?>(null)
    var editingNodes by mutableStateOf<List<RouteNode>>(emptyList())

    /* ── Tab selection ─────────────────────────────────────────────── */

    var selectedTab by mutableStateOf(0)

    /* ── Cleanup ───────────────────────────────────────────────────── */

    override fun onCleared() {
        player.cleanup()
    }
}
