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
        solverService.startSolving(bestSolution.get(), newBestSolution -> {
            bestSolution.set(newBestSolution);
            simulationService.onNewBestSolution(newBestSolution);
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
