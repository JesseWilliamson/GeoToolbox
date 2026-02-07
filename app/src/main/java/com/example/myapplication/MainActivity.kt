package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState

class MainActivity : ComponentActivity() {

    private lateinit var spoofer: Spoofer
    private var isSpoofing by mutableStateOf(false)

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openDeveloperOptions()
        else Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        spoofer = Spoofer(this)
        setContent {
            MyApplicationTheme {
                Scaffold(Modifier.fillMaxSize()) { padding ->
                    GpsSpooferScreen(
                        modifier = Modifier.padding(padding),
                        onStart = { lat, lon -> startSpoofing(lat, lon) },
                        onStop = { stopSpoofing() },
                        isSpoofing = isSpoofing
                    )
                }
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            openDeveloperOptions()
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun openDeveloperOptions() {
        try {
            startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
        } catch (_: Exception) {}
        Toast.makeText(this, "Developer Options → set this app as \"Mock location app\"", Toast.LENGTH_LONG).show()
    }

    private fun startSpoofing(lat: Double, lon: Double) {
        if (spoofer.startSpoofing(lat, lon)) {
            isSpoofing = true
            Toast.makeText(this, "Mocking location: $lat, $lon", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Set this app as Mock location app in Developer Options", Toast.LENGTH_LONG).show()
            openDeveloperOptions()
        }
    }

    private fun stopSpoofing() {
        spoofer.stopSpoofing()
        isSpoofing = false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isSpoofing) stopSpoofing()
    }
}

@Preview
@Composable
fun GpsSpooferScreen(
    modifier: Modifier = Modifier,
    onStart: (Double, Double) -> Unit = { _, _ -> },
    onStop: () -> Unit = {},
    isSpoofing: Boolean = false
) {
    var latText by remember { mutableStateOf("0") }
    var lonText by remember { mutableStateOf("0") }
    val context = LocalContext.current

    val currentLat = latText.toDoubleOrNull() ?: 0.0
    val currentLon = lonText.toDoubleOrNull() ?: 0.0
    val currentLocation = LatLng(currentLat, currentLon)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentLocation, 15f)
    }

    LaunchedEffect(latText, lonText) {
        val lat = latText.toDoubleOrNull()
        val lon = lonText.toDoubleOrNull()
        if (lat != null && lon != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), cameraPositionState.position.zoom)
            )
        }
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = latText,
                onValueChange = { latText = it },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = lonText,
                onValueChange = { lonText = it },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val lat = latText.toDoubleOrNull()
                        val lon = lonText.toDoubleOrNull()
                        when {
                            lat == null || lon == null ->
                                Toast.makeText(context, "Enter valid numbers", Toast.LENGTH_SHORT).show()
                            lat !in -90.0..90.0 || lon !in -180.0..180.0 ->
                                Toast.makeText(context, "Invalid coordinates", Toast.LENGTH_SHORT).show()
                            else -> onStart(lat, lon)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isSpoofing
                ) { Text("Start") }
                Button(
                    onClick = onStop,
                    modifier = Modifier.weight(1f),
                    enabled = isSpoofing,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Stop") }
            }
        }

        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            cameraPositionState = cameraPositionState
        )
    }
}
