/*
 * Copyright 2012 Red Hat, Inc. and/or its affiliates.
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.acme.vehiclerouting.domain.solver.DepotAngleCustomerDifficultyWeightFactory;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;

@JsonIgnoreProperties({ "previousStandstill", "nextCustomer" })
@PlanningEntity(difficultyWeightFactoryClass = DepotAngleCustomerDifficultyWeightFactory.class)
public class Customer implements Standstill {

    private long id;
    private Location location;
    private int demand;

    // Planning variable: changes during planning, between score calculations.
    @PlanningVariable(
            valueRangeProviderRefs = { "vehicleRange", "customerRange" },
            graphType = PlanningVariableGraphType.CHAINED)
    private Standstill previousStandstill;

    // Shadow variables
    private Customer nextCustomer;
    @AnchorShadowVariable(sourceVariableName = "previousStandstill")
    private Vehicle vehicle;

    public Customer() {
    }

    public Customer(long id, Location location, int demand) {
        this.id = id;
        this.location = location;
        this.demand = demand;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }

    public Standstill getPreviousStandstill() {
        return previousStandstill;
    }

    public void setPreviousStandstill(Standstill previousStandstill) {
        this.previousStandstill = previousStandstill;
    }

    @Override
    public Customer getNextCustomer() {
        return nextCustomer;
    }

    @Override
    public void setNextCustomer(Customer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @return a positive number, the distance multiplied by 1000 to avoid floating
     *         point arithmetic rounding errors
     */
    public long getDistanceFromPreviousStandstill() {
        if (previousStandstill == null) {
            // throw new IllegalStateException("This method must not be called when the
            // previousStandstill ("
            // + previousStandstill + ") is not initialized yet.");

            return Long.MAX_VALUE;
        }
        return previousStandstill.getLocation().getDistanceTo(location);
    }

    /**
     * @return a positive number, the distance multiplied by 1000 to avoid floating
     *         point arithmetic rounding errors
     */
    @JsonIgnore
    public long getDistanceToDepot() {
        return location.getDistanceTo(vehicle.getLocation());
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                '}';
    }
}
