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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties({ "nextCustomer" })
public class Vehicle implements Standstill {

    protected Long id;
    protected int capacity;
    protected Depot depot;

    // Shadow variables
    protected Customer nextCustomer;

    public Vehicle() {
    }

    public Vehicle(long id, int capacity, Depot depot) {
        this.id = id;
        this.capacity = capacity;
        this.depot = depot;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @Override
    @JsonBackReference
    public Vehicle getVehicle() {
        return this;
    }

    @Override
    public Location getLocation() {
        return depot.getLocation();
    }

    /**
     * @param standstill never null
     * @return a positive number, the distance multiplied by 1000 to avoid floating
     *         point arithmetic rounding errors
     */
    public long getDistanceTo(Standstill standstill) {
        return depot.getDistanceTo(standstill);
    }

    /**
     * @return route of the vehicle
     */
    public List<Location> getRoute() {

        List<Location> route = new ArrayList<Location>();

        // add list of customer location
        Customer customer = getNextCustomer();
        while (customer != null) {
            route.add(customer.getLocation());
            customer = customer.getNextCustomer();
        }

        return route;
    }

    public Long getTotalDistance() {

        Long totalDistance = getDistanceTo(this);
        // add list of ride location
        Customer ride = getNextCustomer();
        Customer lastRide = getNextCustomer();
        while (ride != null) {
            totalDistance += ride.getDistanceFromPreviousStandstill();
            lastRide = ride;
            ride = ride.getNextCustomer();
        }

        if (lastRide != null) {
            totalDistance += lastRide.getDistanceTo(this);
        }
        return totalDistance;
    }

    public String getTotalDistanceKm() {

        long totalDistance = getTotalDistance();
        long km = totalDistance / 10L;
        long meter = totalDistance % 10L;
        return km + "km " + meter + "m";
    }

    @Override
    public String toString() {
        Location location = getLocation();
        if (location.getName() == null) {
            return super.toString();
        }
        return location.getName() + "/" + super.toString();
    }

}
