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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({ "nextCustomer" })
public class Vehicle implements Standstill {

    private long id;
    private int capacity;
    private Depot depot;

    // Shadow variable
    private Customer nextCustomer;

    public Vehicle() {
    }

    public Vehicle(long id, int capacity, Depot depot) {
        this.id = id;
        this.capacity = capacity;
        this.depot = depot;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Depot getDepot() {
        return depot;
    }

    public void setDepot(Depot depot) {
        this.depot = depot;
    }

    @Override
    public Customer getNextCustomer() {
        return nextCustomer;
    }

    @Override
    public void setNextCustomer(Customer nextCustomer) {
        this.nextCustomer = nextCustomer;
    }

    @Override
    public Location getLocation() {
        return depot.getLocation();
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    /**
     * @return route of the vehicle
     */
    public List<Location> getRoute() {
        if (getNextCustomer() == null) {
            return Collections.emptyList();
        }

        List<Location> route = new ArrayList<Location>();

        route.add(depot.getLocation());

        // add list of customer location
        Customer customer = getNextCustomer();
        while (customer != null) {
            route.add(customer.getLocation());
            customer = customer.getNextCustomer();
        }

        route.add(depot.getLocation());

        return route;
    }

    public int getTotalDemand() {
        int totalDemand = 0;
        Customer customer = getNextCustomer();
        while (customer != null) {
            totalDemand += customer.getDemand();
            customer = customer.getNextCustomer();
        }
        return totalDemand;
    }

    public long getTotalDistanceMeters() {
        long totalDistance = 0L;
        Customer customer = getNextCustomer();
        Customer lastCustomer = null;
        while (customer != null) {
            totalDistance += customer.getDistanceFromPreviousStandstill();
            lastCustomer = customer;
            customer = customer.getNextCustomer();
        }

        if (lastCustomer != null) {
            totalDistance += lastCustomer.getDistanceToDepot();
        }
        return totalDistance;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                '}';
    }
}
