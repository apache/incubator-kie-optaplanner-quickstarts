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
package org.acme.vehiclerouting.rest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.acme.vehiclerouting.persistence.VehicleRoutingSolutionRepository;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.SolutionManager;
import org.optaplanner.core.api.solver.SolverManager;

@Path("/vrp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolverResource {

    private static final long PROBLEM_ID = 0L;

    private final AtomicReference<Throwable> solverError = new AtomicReference<>();

    private final VehicleRoutingSolutionRepository repository;
    private final SolverManager<VehicleRoutingSolution, Long> solverManager;
    private final SolutionManager<VehicleRoutingSolution, HardSoftLongScore> solutionManager;

    public SolverResource(VehicleRoutingSolutionRepository repository,
            SolverManager<VehicleRoutingSolution, Long> solverManager,
            SolutionManager<VehicleRoutingSolution, HardSoftLongScore> solutionManager) {
        this.repository = repository;
        this.solverManager = solverManager;
        this.solutionManager = solutionManager;
    }

    private Status statusFromSolution(VehicleRoutingSolution solution) {
        return new Status(solution,
                solutionManager.explain(solution).getSummary(),
                solverManager.getSolverStatus(PROBLEM_ID));
    }

    @GET
    @Path("status")
    public Status status() {
        Optional.ofNullable(solverError.getAndSet(null)).ifPresent(throwable -> {
            throw new RuntimeException("Solver failed", throwable);
        });

        Optional<VehicleRoutingSolution> s1 = repository.solution();

        VehicleRoutingSolution s = s1.orElse(VehicleRoutingSolution.empty());
        return statusFromSolution(s);
    }

    @POST
    @Path("solve")
    public void solve() {
        Optional<VehicleRoutingSolution> maybeSolution = repository.solution();
        maybeSolution.ifPresent(
                vehicleRoutingSolution -> solverManager.solveAndListen(PROBLEM_ID, id -> vehicleRoutingSolution,
                        repository::update, (problemId, throwable) -> solverError.set(throwable)));
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(PROBLEM_ID);
    }
}
