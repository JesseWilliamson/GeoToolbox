package com.example.myapplication

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RouteGeometryTest {

    // ── distanceMeters ──────────────────────────────────────────────────

    @Test
    fun distanceMeters_identicalPoints_returnsZero() {
        val p = RouteWaypoint(40.0, -74.0)
        assertEquals(0.0, distanceMeters(p, p), 1e-9)
    }

    @Test
    fun distanceMeters_oneDegreeLatitude_approximatelyCorrect() {
        // 1 degree latitude ≈ 111,320 m
        val a = RouteWaypoint(0.0, 0.0)
        val b = RouteWaypoint(1.0, 0.0)
        assertEquals(111_320.0, distanceMeters(a, b), 100.0)
    }

    @Test
    fun distanceMeters_oneDegreeLongitudeAtEquator_approximatelyCorrect() {
        // At equator, 1 degree longitude ≈ 111,320 m
        val a = RouteWaypoint(0.0, 0.0)
        val b = RouteWaypoint(0.0, 1.0)
        assertEquals(111_320.0, distanceMeters(a, b), 100.0)
    }

    @Test
    fun distanceMeters_longitudeShrinksAtHighLatitude() {
        // At 60° N, 1° longitude ≈ 111,320 * cos(60°) ≈ 55,660 m
        val a = RouteWaypoint(60.0, 0.0)
        val b = RouteWaypoint(60.0, 1.0)
        assertEquals(55_660.0, distanceMeters(a, b), 200.0)
    }

    @Test
    fun distanceMeters_isSymmetric() {
        val a = RouteWaypoint(40.7128, -74.0060)
        val b = RouteWaypoint(40.7580, -73.9855)
        assertEquals(distanceMeters(a, b), distanceMeters(b, a), 1e-9)
    }

    @Test
    fun distanceMeters_diagonalMovement() {
        val a = RouteWaypoint(0.0, 0.0)
        val b = RouteWaypoint(1.0, 1.0)
        val dist = distanceMeters(a, b)
        // Should be roughly sqrt(2) * 111,320 ≈ 157,400 m
        assertTrue("diagonal distance $dist should be > 150,000", dist > 150_000)
        assertTrue("diagonal distance $dist should be < 165,000", dist < 165_000)
    }

    // ── computeSegmentDistances ─────────────────────────────────────────

    @Test
    fun computeSegmentDistances_emptyList_returnsEmpty() {
        assertEquals(0, computeSegmentDistances(emptyList()).size)
    }

    @Test
    fun computeSegmentDistances_singleWaypoint_returnsEmpty() {
        val result = computeSegmentDistances(listOf(RouteWaypoint(0.0, 0.0)))
        assertEquals(0, result.size)
    }

    @Test
    fun computeSegmentDistances_correctCount() {
        val waypoints = listOf(
            RouteWaypoint(0.0, 0.0),
            RouteWaypoint(1.0, 0.0),
            RouteWaypoint(2.0, 0.0),
        )
        assertEquals(2, computeSegmentDistances(waypoints).size)
    }

    @Test
    fun computeSegmentDistances_matchesDirectCalculation() {
        val a = RouteWaypoint(0.0, 0.0)
        val b = RouteWaypoint(1.0, 0.0)
        val c = RouteWaypoint(1.0, 1.0)
        val distances = computeSegmentDistances(listOf(a, b, c))
        assertEquals(distanceMeters(a, b), distances[0], 1e-9)
        assertEquals(distanceMeters(b, c), distances[1], 1e-9)
    }

    @Test
    fun computeSegmentDistances_duplicatePoints_returnsZeroDistance() {
        val p = RouteWaypoint(10.0, 20.0)
        val distances = computeSegmentDistances(listOf(p, p))
        assertEquals(1, distances.size)
        assertEquals(0.0, distances[0], 1e-9)
    }

    // ── positionAlongPath ───────────────────────────────────────────────

    @Test
    fun positionAlongPath_atStart() {
        val waypoints = listOf(
            RouteWaypoint(10.0, 20.0),
            RouteWaypoint(11.0, 20.0),
        )
        val distances = computeSegmentDistances(waypoints)
        val (lat, lon) = positionAlongPath(waypoints, distances, 0.0)
        assertEquals(10.0, lat, 1e-9)
        assertEquals(20.0, lon, 1e-9)
    }

    @Test
    fun positionAlongPath_atEnd() {
        val waypoints = listOf(
            RouteWaypoint(10.0, 20.0),
            RouteWaypoint(11.0, 20.0),
        )
        val distances = computeSegmentDistances(waypoints)
        val (lat, lon) = positionAlongPath(waypoints, distances, distances.sum())
        assertEquals(11.0, lat, 1e-6)
        assertEquals(20.0, lon, 1e-6)
    }

    @Test
    fun positionAlongPath_pastEnd_clampsToLastPoint() {
        val waypoints = listOf(
            RouteWaypoint(10.0, 20.0),
            RouteWaypoint(11.0, 20.0),
        )
        val distances = computeSegmentDistances(waypoints)
        val (lat, lon) = positionAlongPath(waypoints, distances, distances.sum() + 999_999.0)
        assertEquals(11.0, lat, 1e-9)
        assertEquals(20.0, lon, 1e-9)
    }

    @Test
    fun positionAlongPath_midpoint() {
        val waypoints = listOf(
            RouteWaypoint(0.0, 0.0),
            RouteWaypoint(2.0, 0.0),
        )
        val distances = computeSegmentDistances(waypoints)
        val (lat, lon) = positionAlongPath(waypoints, distances, distances.sum() / 2.0)
        assertEquals(1.0, lat, 0.01)
        assertEquals(0.0, lon, 0.01)
    }

    @Test
    fun positionAlongPath_multipleSegments_secondSegment() {
        val waypoints = listOf(
            RouteWaypoint(0.0, 0.0),
            RouteWaypoint(1.0, 0.0),
            RouteWaypoint(1.0, 1.0),
        )
        val distances = computeSegmentDistances(waypoints)
        // Travel past segment 0, halfway through segment 1
        val dist = distances[0] + distances[1] / 2.0
        val (lat, lon) = positionAlongPath(waypoints, distances, dist)
        assertEquals(1.0, lat, 0.01)
        assertTrue("lon should be ~0.5, was $lon", lon in 0.3..0.7)
    }

    @Test
    fun positionAlongPath_skipsZeroLengthSegments() {
        val waypoints = listOf(
            RouteWaypoint(0.0, 0.0),
            RouteWaypoint(0.0, 0.0), // duplicate
            RouteWaypoint(1.0, 0.0),
        )
        val distances = computeSegmentDistances(waypoints)
        val half = distances.sum() / 2.0
        val (lat, _) = positionAlongPath(waypoints, distances, half)
        assertEquals(0.5, lat, 0.01)
    }

    @Test
    fun positionAlongPath_quarterAndThreeQuarter() {
        val waypoints = listOf(
            RouteWaypoint(0.0, 0.0),
            RouteWaypoint(4.0, 0.0),
        )
        val distances = computeSegmentDistances(waypoints)
        val total = distances.sum()

        val (lat25, _) = positionAlongPath(waypoints, distances, total * 0.25)
        assertEquals(1.0, lat25, 0.02)

        val (lat75, _) = positionAlongPath(waypoints, distances, total * 0.75)
        assertEquals(3.0, lat75, 0.02)
    }
}
