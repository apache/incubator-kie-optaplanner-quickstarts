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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.acme.vehiclerouting.domain.location.AirLocation;
import org.acme.vehiclerouting.domain.location.Location;
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
                .setDepotCount(depotCount).setSouthWestCorner(new AirLocation(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new AirLocation(0L, 43.809291, 11.290195)).build();

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
        DemoDataBuilder builder = correctBuilder().setVehicleCapacity(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setVehicleCapacity(-1);
        assertThrows(IllegalStateException.class, builder::build);
    }

    @Test
    void demand_test() {
        DemoDataBuilder builder = correctBuilder().setMinDemand(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setMinDemand(-1);
        assertThrows(IllegalStateException.class, builder::build);

        builder = correctBuilder().setMaxDemand(0);
        assertThrows(IllegalStateException.class, builder::build);
        builder.setMaxDemand(-1);
        assertThrows(IllegalStateException.class, builder::build);

        builder = correctBuilder().setMaxDemand(2);
        builder = correctBuilder().setMaxDemand(1);
        assertThrows(IllegalStateException.class, builder::build);

    }

    @Test
    void map_corner_test() {
        DemoDataBuilder builder = correctBuilder().setSouthWestCorner(new AirLocation(0L, 2, 1));
        builder.setNorthEastCorner(new AirLocation(0L, 1, 2));
        assertThrows(IllegalStateException.class, builder::build);

        builder = correctBuilder().setSouthWestCorner(new AirLocation(0L, 1, 1));
        builder.setNorthEastCorner(new AirLocation(0L, 1, 2));
        assertThrows(IllegalStateException.class, builder::build);

        builder = correctBuilder().setSouthWestCorner(new AirLocation(0L, 1, 1));
        builder.setNorthEastCorner(new AirLocation(0L, 2, 1));
        assertThrows(IllegalStateException.class, builder::build);

        builder = correctBuilder().setSouthWestCorner(new AirLocation(0L, 1, 2));
        builder.setNorthEastCorner(new AirLocation(0L, 2, 1));
        assertThrows(IllegalStateException.class, builder::build);
    }

    static DemoDataBuilder correctBuilder() {
        return DemoDataBuilder.builder().setMinDemand(1).setMaxDemand(2).setVehicleCapacity(15).setCustomerCount(77)
                .setVehicleCount(6).setDepotCount(2).setSouthWestCorner(new AirLocation(0L, 43.751466, 11.177210))
                .setNorthEastCorner(new AirLocation(0L, 43.809291, 11.290195));

    }
}
