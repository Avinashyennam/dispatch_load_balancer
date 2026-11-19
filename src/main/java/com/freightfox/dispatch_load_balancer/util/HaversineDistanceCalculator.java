
package com.freightfox.dispatch_load_balancer.util;

public final class HaversineDistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    private HaversineDistanceCalculator() {
        // Utility class; prevent instantiation.
    }

    /**
     * Calculates the great-circle distance between two geographical
     * coordinates using the Haversine formula.
     *
     * @return distance in kilometres
     */
    public static double calculateDistance(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2) {

        validateCoordinates(latitude1, longitude1);
        validateCoordinates(latitude2, longitude2);

        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);

        double deltaLat = Math.toRadians(latitude2 - latitude1);
        double deltaLon = Math.toRadians(longitude2 - longitude1);

        double a = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.pow(Math.sin(deltaLon / 2), 2);

        // Protect against floating-point rounding errors.
        a = Math.max(0.0, Math.min(1.0, a));

        double centralAngle = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return EARTH_RADIUS_KM * centralAngle;
    }

    private static void validateCoordinates(double latitude, double longitude) {

        if (!Double.isFinite(latitude) || !Double.isFinite(longitude)) {
            throw new IllegalArgumentException(
                    "Coordinates must be finite numbers."
            );
        }

        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException(
                    "Latitude must be between -90 and 90."
            );
        }

        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException(
                    "Longitude must be between -180 and 180."
            );
        }
    }
}
