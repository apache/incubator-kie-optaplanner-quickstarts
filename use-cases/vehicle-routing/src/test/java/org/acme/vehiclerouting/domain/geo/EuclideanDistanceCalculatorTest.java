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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.acme.vehiclerouting.domain.Location;
import org.junit.jupiter.api.Test;

class EuclideanDistanceCalculatorTest {

    @Test
    void calculateDistance() {
        Location a = new Location(0, 0.0, 0.0);
        Location b = new Location(1, 0.0, 4.0);
        EuclideanDistanceCalculator distanceCalculator = new EuclideanDistanceCalculator();
        assertThat(distanceCalculator.calculateDistance(a, a)).isZero();
        assertThat(distanceCalculator.calculateDistance(b, b)).isZero();
        assertThat(distanceCalculator.calculateDistance(a, b)).isEqualTo(distanceCalculator.calculateDistance(b, a));
        assertThat(distanceCalculator.calculateDistance(a, b))
                .isEqualTo(4 * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }

    @Test
    void distanceMap() {
        long id = 0;
        Location a = new Location(id++, 0.0, 0.0);
        Location b = new Location(id++, 0.0, 4.0);
        List<Location> locations = Arrays.asList(a, b);
        EuclideanDistanceCalculator distanceCalculator = new EuclideanDistanceCalculator();
        Map<Location, Map<Location, Long>> distanceMatrix = distanceCalculator.calculateBulkDistance(locations, locations);
        assertThat(distanceMatrix.get(a).get(b)).isEqualTo(distanceCalculator.calculateDistance(a, b));
    }
}
