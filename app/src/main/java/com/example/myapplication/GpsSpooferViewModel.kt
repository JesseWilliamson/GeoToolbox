package com.example.myapplication

import android.app.Application
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class GpsSpooferViewModel(application: Application) : AndroidViewModel(application) {

    val spoofer = Spoofer(application)

    val player = RoutePlayerController(spoofer) { msg ->
        Toast.makeText(application, msg, Toast.LENGTH_LONG).show()
    }

    val pointsRepo = SavedPointsRepository(application)
    val routesRepo = RoutesRepository(application)

    var latText by mutableStateOf("0")
    var lonText by mutableStateOf("0")

    var targetZoom by mutableStateOf<Float?>(null)

    var showAddDialog by mutableStateOf(false)
    var addEditPoint by mutableStateOf<SavedPoint?>(null)
    var deletePoint by mutableStateOf<SavedPoint?>(null)
    var showAddRouteDialog by mutableStateOf(false)

    var editRoute by mutableStateOf<Route?>(null)
    var editingNodes by mutableStateOf<List<RouteNode>>(emptyList())

    var selectedTab by mutableStateOf(0)

    override fun onCleared() {
        player.cleanup()
    }
}
