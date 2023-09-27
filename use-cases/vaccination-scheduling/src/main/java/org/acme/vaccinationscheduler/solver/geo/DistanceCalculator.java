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
