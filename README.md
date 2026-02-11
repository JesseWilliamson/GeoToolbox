# GeoToolbox (GPS Toolbox)

An Android app for GPS location mocking and route simulation. Useful for developers and testers who need to simulate GPS movement along predefined routes.

## Features

- **Location bookmarks** — Save and quickly navigate to GPS coordinates with map previews
- **Route creation** — Tap the map to define waypoints, drag nodes to adjust
- **Route playback** — Simulate movement along a route with adjustable speed (2-80 m/s)
- **Playback controls** — Pause, resume, seek, and stop route simulation

## Prerequisites

- Android device or emulator running **API 29+** (Android 10)
- **Developer Options** enabled on the device
- This app set as **Mock location app** in Developer Options

## Setup

1. Clone the repository
2. Add your Google Maps API key to `local.properties`:
   ```
   MAPS_API_KEY=your_api_key_here
   ```
3. Open in Android Studio and sync Gradle
4. Build and run on your device or emulator
5. When the app launches it will prompt you to enable mock locations in Developer Options

## Architecture

Single-activity Jetpack Compose app:

- **MainViewModel** — owns business logic, repositories, and playback state
- **Spoofer** — manages the GPS mock provider via `LocationManager`
- **RouteRunner** — interpolates position along waypoints using equirectangular projection
- **RouteGeometry** — pure math functions for distance and interpolation (unit-tested)
- **DataStore + GSON** — persists routes and saved locations as JSON

## Running Tests

```
./gradlew test
```
