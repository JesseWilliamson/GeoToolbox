package com.example.myapplication

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val pointsRepo = SavedPointsRepository(application)
    private val routesRepo = RoutesRepository(application)

    val spoofer = Spoofer(application)
    val player: RoutePlayerController

    val savedPoints = pointsRepo.savedPoints
    val routes = routesRepo.routes

    private val _toastMessages = MutableSharedFlow<String>()
    val toastMessages: SharedFlow<String> = _toastMessages

    init {
        player = RoutePlayerController(spoofer) { msg ->
            viewModelScope.launch { _toastMessages.emit(msg) }
        }
        spoofer.onError = { msg ->
            viewModelScope.launch { _toastMessages.emit(msg) }
        }
    }

    fun addPoint(name: String, lat: Double, lon: Double, zoom: Float) {
        viewModelScope.launch { pointsRepo.add(name, lat, lon, zoom) }
    }

    fun updatePoint(point: SavedPoint) {
        viewModelScope.launch { pointsRepo.update(point) }
    }

    fun deletePoint(id: String) {
        viewModelScope.launch { pointsRepo.delete(id) }
    }

    fun addRoute(route: Route) {
        viewModelScope.launch { routesRepo.add(route) }
    }

    fun updateRoute(route: Route) {
        viewModelScope.launch { routesRepo.update(route) }
    }

    fun deleteRoute(id: String) {
        viewModelScope.launch { routesRepo.delete(id) }
    }

    override fun onCleared() {
        super.onCleared()
        player.cleanup()
    }
}
