package org.acme.vehiclerouting.rest;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.vehiclerouting.domain.VehicleRoutingSolution;
import org.acme.vehiclerouting.persistence.VehicleRoutingSolutionRepository;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.SolverManager;

@Path("/vrp")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SolverResource {

    private static final long PROBLEM_ID = 0L;

    private final AtomicReference<Throwable> solverError = new AtomicReference<>();

    private final VehicleRoutingSolutionRepository repository;
    private final SolverManager<VehicleRoutingSolution, Long> solverManager;
    private final ScoreManager<VehicleRoutingSolution, HardSoftLongScore> scoreManager;

    public SolverResource(VehicleRoutingSolutionRepository repository,
            SolverManager<VehicleRoutingSolution, Long> solverManager,
            ScoreManager<VehicleRoutingSolution, HardSoftLongScore> scoreManager) {
        this.repository = repository;
        this.solverManager = solverManager;
        this.scoreManager = scoreManager;
    }

    private Status statusFromSolution(VehicleRoutingSolution solution) {
        return new Status(solution, scoreManager.explainScore(solution).getSummary(),
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
