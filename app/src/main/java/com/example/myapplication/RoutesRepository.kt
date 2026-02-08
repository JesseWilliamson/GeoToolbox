package com.example.myapplication

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.routesDataStore: DataStore<Preferences> by preferencesDataStore(name = "routes")

class RoutesRepository(private val context: Context) {

    private val gson = Gson()
    private val type = object : TypeToken<List<Route>>() {}.type
    private val key = stringPreferencesKey("routes")

    val routes: Flow<List<Route>> = context.routesDataStore.data.map { prefs ->
        val json = prefs[key] ?: "[]"
        runCatching { gson.fromJson<List<Route>>(json, type) }.getOrElse { emptyList() }
    }

    suspend fun add(route: Route) {
        context.routesDataStore.edit { prefs ->
            val list = runCatching {
                gson.fromJson<List<Route>>(prefs[key] ?: "[]", type)
            }.getOrElse { emptyList() }
            prefs[key] = gson.toJson(list + route)
        }
    }

    suspend fun add(name: String, waypoints: List<RouteWaypoint>) {
        add(Route(id = UUID.randomUUID().toString(), name = name, waypoints = waypoints))
    }

    suspend fun update(route: Route) {
        context.routesDataStore.edit { prefs ->
            val list = runCatching {
                gson.fromJson<List<Route>>(prefs[key] ?: "[]", type)
            }.getOrElse { emptyList() }
            val updated = list.map { if (it.id == route.id) route else it }
            prefs[key] = gson.toJson(updated)
        }
    }

    suspend fun delete(id: String) {
        context.routesDataStore.edit { prefs ->
            val list = runCatching {
                gson.fromJson<List<Route>>(prefs[key] ?: "[]", type)
            }.getOrElse { emptyList() }
            prefs[key] = gson.toJson(list.filter { it.id != id })
        }
    }
}
