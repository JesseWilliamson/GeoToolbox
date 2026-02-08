package com.example.myapplication

import SavedPoint
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

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "saved_points")

class SavedPointsRepository(private val context: Context) {

    private val gson = Gson()
    private val type = object : TypeToken<List<SavedPoint>>() {}.type
    private val key = stringPreferencesKey("points")

    val savedPoints: Flow<List<SavedPoint>> = context.dataStore.data.map { prefs ->
        val json = prefs[key] ?: "[]"
        runCatching { gson.fromJson<List<SavedPoint>>(json, type) }.getOrElse { emptyList() }
    }

    suspend fun add(point: SavedPoint) {
        context.dataStore.edit { prefs ->
            val list = runCatching {
                gson.fromJson<List<SavedPoint>>(prefs[key] ?: "[]", type)
            }.getOrElse { emptyList() }
            prefs[key] = gson.toJson(list + point)
        }
    }

    suspend fun add(name: String, latitude: Double, longitude: Double) {
        add(SavedPoint(id = UUID.randomUUID().toString(), name = name, latitude = latitude, longitude = longitude))
    }

    suspend fun update(point: SavedPoint) {
        context.dataStore.edit { prefs ->
            val list = runCatching {
                gson.fromJson<List<SavedPoint>>(prefs[key] ?: "[]", type)
            }.getOrElse { emptyList() }
            val updated = list.map { if (it.id == point.id) point else it }
            prefs[key] = gson.toJson(updated)
        }
    }

    suspend fun delete(id: String) {
        context.dataStore.edit { prefs ->
            val list = runCatching {
                gson.fromJson<List<SavedPoint>>(prefs[key] ?: "[]", type)
            }.getOrElse { emptyList() }
            prefs[key] = gson.toJson(list.filter { it.id != id })
        }
    }
}
