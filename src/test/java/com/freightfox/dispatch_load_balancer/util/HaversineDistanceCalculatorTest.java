package com.freightfox.dispatch_load_balancer.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HaversineDistanceCalculatorTest {

    private static final double DELTA = 0.01;

    @Test
    void shouldReturnZeroForSameCoordinates() {

        double distance =
                HaversineDistanceCalculator.calculateDistance(
                        28.6139, 77.2090,
                        28.6139, 77.2090
                );

        assertEquals(0.0, distance, DELTA);
    }

    @Test
    void shouldCalculateDistanceBetweenKnownCoordinates() {

        // Distance between (0, 0) and (0, 1) is approximately 111.195 km.
        double distance =
                HaversineDistanceCalculator.calculateDistance(
                        0.0, 0.0,
                        0.0, 1.0
                );

        assertEquals(111.195, distance, DELTA);
    }

    @Test
    void shouldCalculatePositiveDistanceAcrossHemispheres() {

        double distance =
                HaversineDistanceCalculator.calculateDistance(
                        10.0, 20.0,
                        -10.0, -160.0
                );

        assertTrue(distance > 0);
        assertEquals(20015.11, distance, 0.1);
    }

    @Test
    void shouldHandleCoordinatesAcrossAntimeridian() {

        double distance =
                HaversineDistanceCalculator.calculateDistance(
                        0.0, 179.9,
                        0.0, -179.9
                );

        assertEquals(22.239, distance, DELTA);
    }

    @Test
    void shouldRejectInvalidLatitude() {

        assertThrows(
                IllegalArgumentException.class,
                () -> HaversineDistanceCalculator.calculateDistance(
                        91.0, 77.0,
                        28.0, 77.0
                )
        );
    }

    @Test
    void shouldRejectInvalidLongitude() {

        assertThrows(
                IllegalArgumentException.class,
                () -> HaversineDistanceCalculator.calculateDistance(
                        28.0, 181.0,
                        29.0, 77.0
                )
        );
    }

    @Test
    void shouldRejectNonFiniteCoordinates() {

        assertThrows(
                IllegalArgumentException.class,
                () -> HaversineDistanceCalculator.calculateDistance(
                        Double.NaN, 77.0,
                        28.0, 77.0
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> HaversineDistanceCalculator.calculateDistance(
                        28.0, Double.POSITIVE_INFINITY,
                        29.0, 77.0
                )
        );
    }
}
