package org.acme.vehiclerouting.rest;

import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.core.api.solver.SolverStatus;

class Status {

    public final VehicleRoutingSolution solution;
    public final String scoreExplanation;
    public final boolean isSolving;

    Status(VehicleRoutingSolution solution, String scoreExplanation, SolverStatus solverStatus) {
        this.solution = solution;
        this.scoreExplanation = scoreExplanation;
        this.isSolving = solverStatus != SolverStatus.NOT_SOLVING;
    }
}
