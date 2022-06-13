package org.acme.callcenter.rest;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.acme.callcenter.service.SimulationService;
import org.acme.callcenter.service.SolverService;

@Path("/call")
public class CallResource {

    @Inject
    SolverService solverService;

    @Inject
    SimulationService simulationService;

    @DELETE
    @Path("{id}")
    public void deleteCall(@PathParam("id") long id) {
        solverService.removeCall(id);
    }

    @PUT
    @Path("{id}")
    public void prolongCall(@PathParam("id") long id) {
        solverService.prolongCall(id);
        simulationService.prolongCall(id);
    }
}
