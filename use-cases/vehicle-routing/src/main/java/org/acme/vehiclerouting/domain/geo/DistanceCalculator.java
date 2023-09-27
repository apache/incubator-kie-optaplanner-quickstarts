/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.acme.vehiclerouting.domain.geo;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.acme.vehiclerouting.domain.Location;

public interface DistanceCalculator {

    /**
     * Calculate the distance between {@code from} and {@code to} in meters.
     *
     * @param from starting location
     * @param to   target location
     * @return distance in meters
     */
    long calculateDistance(Location from, Location to);

    /**
     * Bulk calculation of distance.
     * Typically much more scalable than {@link #calculateDistance(Location, Location)} iteratively.
     *
     * @param fromLocations never null
     * @param toLocations   never null
     * @return never null
     */
    default Map<Location, Map<Location, Long>> calculateBulkDistance(
            Collection<Location> fromLocations,
            Collection<Location> toLocations) {
        return fromLocations.stream().collect(Collectors.toMap(
                Function.identity(),
                from -> toLocations.stream().collect(Collectors.toMap(
                        Function.identity(),
                        to -> calculateDistance(from, to)
                ))
        ));
    }

    /**
     * Calculate distance matrix for the given list of locations and assign distance maps accordingly.
     *
     * @param locationList
     */
    default void initDistanceMaps(Collection<Location> locationList) {
        Map<Location, Map<Location, Long>> distanceMatrix = calculateBulkDistance(locationList, locationList);
        locationList.forEach(location -> location.setDistanceMap(distanceMatrix.get(location)));
    }
}
