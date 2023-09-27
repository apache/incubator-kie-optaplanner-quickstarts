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
package org.acme.orderpicking.rest;

import java.util.concurrent.atomic.AtomicBoolean;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

import org.acme.orderpicking.domain.OrderPickingPlanning;
import org.acme.orderpicking.domain.OrderPickingSolution;
import org.acme.orderpicking.persistence.OrderPickingRepository;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;

@Path("orderPicking")
@ApplicationScoped
public class OrderPickingSolverResource {

    private static final long PROBLEM_ID = 1;
    private final AtomicBoolean solverWasNeverStarted = new AtomicBoolean(true);

    @Inject
    SolverManager<OrderPickingSolution, Long> solverManager;

    @Inject
    OrderPickingRepository orderPickingRepository;

    @GET
    public OrderPickingPlanning getBestSolution() {
        OrderPickingSolution solution = orderPickingRepository.find();
        SolverStatus solverStatus = solverManager.getSolverStatus(PROBLEM_ID);
        return new OrderPickingPlanning(solverStatus, solution, solverWasNeverStarted.get());
    }

    @POST
    @Path("solve")
    public void solve() {
        solverWasNeverStarted.set(false);
        solverManager.solveAndListen(PROBLEM_ID, (problemId) -> orderPickingRepository.find(),
                                     orderPickingRepository::save);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}