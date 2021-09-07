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
import java.util.List;

import org.acme.vehiclerouting.bootstrap.DemoDataBuilder;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class VehicleRoutingSolution {

    private String name;

    @ProblemFactCollectionProperty
    private List<Location> locationList;

    @ProblemFactCollectionProperty
    private List<Depot> depotList;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "vehicleRange")
    private List<Vehicle> vehicleList;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider(id = "customerRange")
    private List<Customer> customerList;

    @PlanningScore
    private HardSoftLongScore score;

    private Location southWestCorner;
    private Location northEastCorner;

    public VehicleRoutingSolution() {
    }

    public VehicleRoutingSolution(String name,
            List<Location> locationList, List<Depot> depotList, List<Vehicle> vehicleList, List<Customer> customerList,
            Location southWestCorner, Location northEastCorner) {
        this.name = name;
        this.locationList = locationList;
        this.depotList = depotList;
        this.vehicleList = vehicleList;
        this.customerList = customerList;
        this.southWestCorner = southWestCorner;
        this.northEastCorner = northEastCorner;
    }

    public static VehicleRoutingSolution empty() {
        VehicleRoutingSolution problem = DemoDataBuilder.builder().setMinDemand(1).setMaxDemand(2)
                .setVehicleCapacity(77).setCustomerCount(77).setVehicleCount(7).setDepotCount(1)
                .setSouthWestCorner(new Location(0L, 51.44, -0.16))
                .setNorthEastCorner(new Location(0L, 51.56, -0.01)).build();

        problem.setScore(HardSoftLongScore.ZERO);

        return problem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Location> getLocationList() {
        return locationList;
    }

    public void setLocationList(List<Location> locationList) {
        this.locationList = locationList;
    }

    public List<Depot> getDepotList() {
        return depotList;
    }

    public void setDepotList(List<Depot> depotList) {
        this.depotList = depotList;
    }

    public List<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    public List<Customer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<Customer> customerList) {
        this.customerList = customerList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public List<Location> getBounds() {
        return Arrays.asList(southWestCorner, northEastCorner);
    }

    public long getDistanceMeters() {
        return score == null ? 0 : -score.getSoftScore();
    }
}
