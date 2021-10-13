/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.orderpicking.domain;

import java.util.HashMap;
import java.util.Map;

import org.optaplanner.core.api.solver.SolverStatus;

/**
 * Helper class for sending information to the UI.
 */
public class OrderPickingPlanning {

    private SolverStatus solverStatus;
    private OrderPickingSolution solution;
    private boolean solverWasNeverStarted;
    private Map<String, Integer> distanceToTravelByTrolley = new HashMap<>();

    public OrderPickingPlanning() {
        //marshalling constructor
    }

    public OrderPickingPlanning(SolverStatus solverStatus, OrderPickingSolution solution, boolean solverWasNeverStarted) {
        this.solverStatus = solverStatus;
        this.solution = solution;
        this.solverWasNeverStarted = solverWasNeverStarted;
        for (Trolley trolley : solution.getTrolleyList()) {
            distanceToTravelByTrolley.put(trolley.getId(), Warehouse.calculateDistanceToTravel(trolley));
        }
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public OrderPickingSolution getSolution() {
        return solution;
    }

    public boolean getSolverWasNeverStarted() {
        return solverWasNeverStarted;
    }

    public Map<String, Integer> getDistanceToTravelByTrolley() {
        return distanceToTravelByTrolley;
    }
}
