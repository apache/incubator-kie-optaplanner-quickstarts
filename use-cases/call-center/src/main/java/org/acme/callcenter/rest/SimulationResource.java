package org.acme.callcenter.rest;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.acme.callcenter.service.SimulationService;

@Path("/simulation")
public class SimulationResource {

    @Inject
    SimulationService simulationService;

    @PUT
    public void updateSimulationParameters(SimulationParameters simulationParameters) {
        simulationService.restartSimulation(simulationParameters.getFrequency(), simulationParameters.getDuration());
    }
}
