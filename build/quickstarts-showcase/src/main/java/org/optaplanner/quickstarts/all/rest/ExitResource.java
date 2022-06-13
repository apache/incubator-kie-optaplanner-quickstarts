package org.optaplanner.quickstarts.all.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

import io.quarkus.runtime.Quarkus;

@Path("exit")
public class ExitResource {

    @POST
    public void exit() {
        Quarkus.asyncExit();
    }

}
