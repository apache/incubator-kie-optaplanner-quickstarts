/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.acme.callcenter.rest;

import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.callcenter.data.DataGenerator;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.service.SimulationService;
import org.acme.callcenter.service.SolverService;

@Path("/call-center")
public class CallCenterResource {

    private AtomicReference<CallCenter> bestSolution = new AtomicReference<>();
    private AtomicReference<Throwable> solvingError = new AtomicReference<>();

    @Inject
    SolverService solverService;

    @Inject
    SimulationService simulationService;

    @Inject
    CallCenterResource(DataGenerator dataGenerator) {
        bestSolution.set(dataGenerator.generateCallCenter());
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public CallCenter get() {
        if (solvingError.get() != null) {
            throw new IllegalStateException("Exception occurred during solving.", solvingError.get());
        }
        CallCenter callCenter = bestSolution.get();
        callCenter.setSolving(solverService.isSolving());
        return callCenter;
    }

    @POST
    @Path("solve")
    public void solve() {
        solverService.startSolving(bestSolution.get(), bestSolutionChangedEvent -> {
            bestSolution.set(bestSolutionChangedEvent.getNewBestSolution());
            simulationService.onNewBestSolution(bestSolutionChangedEvent.getNewBestSolution());
        }, throwable -> solvingError.set(throwable));
        simulationService.startSimulation();
    }

    @POST
    @Path("stop")
    public void stop() {
        solverService.stopSolving();
        simulationService.stopSimulation();
    }
}
