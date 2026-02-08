# GeoToolbox Codebase Analysis

## Overview

**Project**: Android GPS location spoofing and route simulation app.
**Stack**: Kotlin 2.0.21, Jetpack Compose, Google Maps, DataStore, Gradle 9.0.
**Size**: ~18 Kotlin files, ~2,500 lines of code.

---

## Key Findings & Suggestions

### 1. Testing — No Real Coverage (HIGH)

The two test files are scaffolding placeholders. Zero tests for actual business logic.

- Unit test `RouteRunner` — interpolation math, distance calculations, edge cases (zero-length routes, single-waypoint, seek bounds).
- Unit test repositories — serialization round-trips, CRUD, corrupted/empty data handling.
- Test `RoutePlayerController` state transitions — play/pause/stop/seek/speed.
- Add Compose UI tests — dependencies present but unused.

### 2. Architecture — Tight Coupling in MainActivity (HIGH)

`MainActivity.kt` directly instantiates `Spoofer`, `RoutePlayerController`, and repositories.

- Introduce an `AndroidViewModel` for business logic and state management.
- Adopt Hilt for dependency injection.
- Extract dialog/navigation state into a sealed UI state class.

### 3. Error Handling — Silent Failures (MEDIUM)

`Spoofer.kt` catches exceptions silently with `catch (_: Exception)`.

- Return `Result<Unit>` or emit error states instead of swallowing.
- Validate mock provider status before starting.
- Log errors at minimum.

### 4. Package Naming (MEDIUM)

Still uses `com.example.myapplication` (Android Studio template default). Rename to something meaningful.

### 5. Magic Numbers (MEDIUM)

Hardcoded values without documentation: `UPDATE_MS = 150L`, `MOCK_UPDATE_MS = 200L`, `METERS_PER_DEGREE_LAT = 111_320.0`, speed range `2f..80f`.

- Extract to named constants with documentation.

### 6. Missing README (HIGH)

No README or setup guide. Add documentation covering: app purpose, API key setup, mock location configuration, build/run instructions.

### 7. No CI/CD (MEDIUM)

No GitHub Actions, no lint configuration (Detekt/ktlint).

- Add a build/test workflow for PRs.
- Add static analysis tooling.

### 8. Persistence — GSON + DataStore is Fragile (LOW)

No schema versioning or migration support.

- Consider Room for structured data, or add a version field to serialized format.
- Replace GSON with kotlinx.serialization (Kotlin-native, no reflection).

### 9. Performance (LOW)

- List recomposition could be optimized.
- No explicit Coil disk cache configuration.
- Handler-based timers risk leaks — migrate to coroutines with `viewModelScope`.

### 10. Missing Features (LOW)

- GPX/KML import/export.
- Location accuracy simulation.
- Multiple provider support (TODO in Spoofer.kt).
- Elevation/altitude data.

---

## Priority Summary

| Priority | Item | Effort |
|----------|------|--------|
| High | Add unit tests for RouteRunner and repositories | Medium |
| High | Extract logic into a ViewModel | Medium |
| High | Add README with setup instructions | Low |
| Medium | Surface errors from Spoofer to UI | Low |
| Medium | Replace Handler with coroutines | Medium |
| Medium | Add CI/CD with GitHub Actions | Low |
| Medium | Rename package from com.example.myapplication | Low |
| Low | Migrate from GSON to kotlinx.serialization | Medium |
| Low | Consider Room for persistence | High |
| Low | Add GPX/KML import/export | High |
