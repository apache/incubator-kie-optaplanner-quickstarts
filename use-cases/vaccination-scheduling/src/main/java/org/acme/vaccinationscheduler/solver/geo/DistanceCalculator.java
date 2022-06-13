package org.acme.vaccinationscheduler.solver.geo;

import org.acme.vaccinationscheduler.domain.Location;

public interface DistanceCalculator {

    long calculateDistance(Location from, Location to);

    /**
     * Bulk calculation of distance.
     * Typically much more scalable than {@link #calculateDistance(Location, Location)} iteratively.
     * @param fromLocations never null
     * @param toLocations never null
     * @return never null
     */
    default long[][] calculateBulkDistance(Location[] fromLocations, Location[] toLocations) {
        long[][] distanceMatrix = new long[fromLocations.length][toLocations.length];
        for (int i = 0; i < fromLocations.length; i++) {
            for (int j = 0; j < toLocations.length; j++) {
                distanceMatrix[i][j] = calculateDistance(fromLocations[i], toLocations[j]);
            }
        }
        return distanceMatrix;
    }

}
