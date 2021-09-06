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

package org.acme.vehiclerouting.domain;

import java.util.Arrays;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.vehiclerouting.domain.geo.EuclideanDistanceCalculator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

@QuarkusTest
class VehicleRoutingConstraintProviderTest {

    @Inject
    ConstraintVerifier<VehicleRoutingConstraintProvider, VehicleRoutingSolution> constraintVerifier;
    private static final Location location1 = new Location(1L, 0.0, 0.0);
    private static final Location location2 = new Location(2L, 0.0, 4.0);
    private static final Location location3 = new Location(3L, 3.0, 0.0);

    @BeforeAll
    static void initDistanceMaps() {
        new EuclideanDistanceCalculator().initDistanceMaps(Arrays.asList(location1, location2, location3));
    }

    @Test
    void vehicleCapacityUnpenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1)
                .penalizesBy(0);
    }

    @Test
    void vehicleCapacityPenalized() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::vehicleCapacity)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(20);
    }

    @Test
    void distanceToPreviousStandstill() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::distanceToPreviousStandstill)
                .given(vehicleA, customer1, customer2)
                .penalizesBy((4 + 5) * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }

    @Test
    void distanceFromLastCustomerToDepot() {
        Vehicle vehicleA = new Vehicle(1L, 100, new Depot(1L, location1));
        Customer customer1 = new Customer(2L, location2, 80);
        customer1.setPreviousStandstill(vehicleA);
        customer1.setVehicle(vehicleA);
        vehicleA.setNextCustomer(customer1);
        Customer customer2 = new Customer(3L, location3, 40);
        customer2.setPreviousStandstill(customer1);
        customer2.setVehicle(vehicleA);
        customer1.setNextCustomer(customer2);

        constraintVerifier.verifyThat(VehicleRoutingConstraintProvider::distanceFromLastCustomerToDepot)
                .given(vehicleA, customer1, customer2)
                .penalizesBy(3 * EuclideanDistanceCalculator.METERS_PER_DEGREE);
    }
}
