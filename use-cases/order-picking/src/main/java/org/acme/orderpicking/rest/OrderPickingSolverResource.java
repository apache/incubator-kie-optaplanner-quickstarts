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

package org.acme.orderpicking.rest;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.quarkus.runtime.StartupEvent;
import org.acme.orderpicking.bootstrap.DemoDataGenerator;
import org.acme.orderpicking.domain.Order;
import org.acme.orderpicking.domain.OrderPickingPlanning;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.domain.Shelving;
import org.acme.orderpicking.domain.Trolley;
import org.acme.orderpicking.domain.TrolleyStep;
import org.acme.orderpicking.domain.WarehouseLocation;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;

import static org.acme.orderpicking.domain.Warehouse.Column.COL_A;
import static org.acme.orderpicking.domain.Warehouse.Row.ROW_1;

@Path("orderPicking")
@ApplicationScoped
public class OrderPickingSolverResource {

    private static final long PROBLEM_ID = 1;

    /**
     * Number of trolleys for the simulation.
     */
    private static final int TROLLEYS_COUNT = 5;

    /**
     * Number of buckets on each trolley.
     */
    private static final int BUCKET_COUNT = 4;

    /**
     * Buckets capacity.
     */
    private static final int BUCKET_CAPACITY = 60 * 40 * 20;

    /**
     * Number of orders for the simulation.
     */
    private static final int ORDERS_COUNT = 8;

    /**
     * Start location for the trolleys.
     */
    private static final WarehouseLocation START_LOCATION = new WarehouseLocation(Shelving.newShelvingId(COL_A, ROW_1), Shelving.Side.LEFT, 0);

    private final AtomicReference<OrderPickingSolution> bestSolution = new AtomicReference<>();
    private final AtomicBoolean solverWasNeverStarted = new AtomicBoolean(true);

    @Inject
    SolverManager<OrderPickingSolution, Long> solverManager;

    @Inject
    DemoDataGenerator demoDataGenerator;

    public void startup(@Observes StartupEvent startupEvent) {
        // Generate the random solution to work with.
        demoDataGenerator.validateBucketCapacity(BUCKET_CAPACITY);
        List<Trolley> trolleys = demoDataGenerator.buildTrolleys(TROLLEYS_COUNT, BUCKET_COUNT, BUCKET_CAPACITY, START_LOCATION);
        List<Order> orders = demoDataGenerator.buildOrders(ORDERS_COUNT);
        List<TrolleyStep> trolleySteps = demoDataGenerator.buildTrolleySteps(orders);
        bestSolution.set(new OrderPickingSolution(trolleys, trolleySteps));
    }

    @GET
    public OrderPickingPlanning getBestSolution() {
        OrderPickingSolution solution = bestSolution.get();
        SolverStatus solverStatus = solverManager.getSolverStatus(PROBLEM_ID);
        return new OrderPickingPlanning(solverStatus, solution, solverWasNeverStarted.get());
    }

    @POST
    @Path("solve")
    public void solve() {
        solverWasNeverStarted.set(false);
        solverManager.solveAndListen(PROBLEM_ID, (problemId) -> bestSolution.get(), bestSolution::set);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }

    private SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(PROBLEM_ID);
    }
}