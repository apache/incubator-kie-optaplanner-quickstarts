/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.vehiclerouting.bootstrap;

import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.vehiclerouting.domain.Location;
import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.junit.jupiter.api.Test;

class DemoDataBuilderTest {

    @Test
    void should_build_data() {
        Integer customerCount = 77;
        Integer vehicleCount = 6;
        Integer depotCount = 2;
        Integer minDemand = 1;
        Integer maxDemand = 2;

        VehicleRoutingSolution problem = DemoDataBuilder.builder().setMinDemand(minDemand).setMaxDemand(maxDemand)
                .setVehicleCapacity(15).setCustomerCount(customerCount).setVehicleCount(vehicleCount)
                .setDepotCount(depotCount).setSouthWestCorner(new Location(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new Location(0L, 43.809291, 11.290195)).build();

        problem.getCustomerList().forEach(
                customer -> assertTrue((minDemand <= customer.getDemand()) && (maxDemand >= customer.getDemand())));

        assertEquals(customerCount, problem.getCustomerList().size());
        assertEquals(vehicleCount, problem.getVehicleList().size());
        assertEquals(depotCount, problem.getDepotList().size());
    }

    @Test
    void correct_builder_builds_ok() {
        assertNotNull(correctBuilder().build());
    }

    @Test
    void capacity_greater_than_zero() {
        assertThatIllegalStateException().isThrownBy(correctBuilder().setVehicleCapacity(0)::build);
        assertThatIllegalStateException().isThrownBy(correctBuilder().setVehicleCapacity(-1)::build);
    }

    @Test
    void demand_test() {
        assertThatIllegalStateException().isThrownBy(correctBuilder().setMinDemand(0)::build);
        assertThatIllegalStateException().isThrownBy(correctBuilder().setMinDemand(-1)::build);

        assertThatIllegalStateException().isThrownBy(correctBuilder().setMaxDemand(0)::build);
        assertThatIllegalStateException().isThrownBy(correctBuilder().setMaxDemand(-1)::build);

        assertThatIllegalStateException().isThrownBy(correctBuilder().setMinDemand(2).setMaxDemand(1)::build);
    }

    @Test
    void map_corner_test() {
        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(0L, 2, 1))
                        .setNorthEastCorner(new Location(0L, 1, 2))::build)
                .withMessageMatching(".*northEast.*Latitude.*must be greater than southWest.*Latitude.*");

        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(0L, 1, 1))
                        .setNorthEastCorner(new Location(0L, 1, 2))::build)
                .withMessageMatching(".*northEast.*Latitude.*must be greater than southWest.*Latitude.*");

        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(0L, 1, 1))
                        .setNorthEastCorner(new Location(0L, 2, 1))::build)
                .withMessageMatching(".*northEast.*Longitude.*must be greater than southWest.*Longitude.*");

        assertThatIllegalStateException().isThrownBy(correctBuilder()
                        .setSouthWestCorner(new Location(0L, 1, 2))
                        .setNorthEastCorner(new Location(0L, 2, 1))::build)
                .withMessageMatching(".*northEast.*Longitude.*must be greater than southWest.*Longitude.*");
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder().setMinDemand(1).setMaxDemand(2).setVehicleCapacity(15).setCustomerCount(77)
                .setVehicleCount(6).setDepotCount(2).setSouthWestCorner(new Location(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new Location(0L, 43.809291, 11.290195));
    }
}
