package org.acme.schooltimetabling.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.common.domain.TimeTable;
import org.acme.common.event.SolverEvent;
import org.acme.common.event.SolverEventType;
import org.acme.common.persistence.TimeTableRepository;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MessageHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageHandler.class);

    @Channel("solver_out")
    Emitter<SolverEvent> solverEventEmitter;

    @Inject
    TimeTableRepository repository;

    private Solver<TimeTable> solver;

    @Inject
    public MessageHandler(SolverFactory<TimeTable> solverFactory) {
        solver = solverFactory.buildSolver();
    }

    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    @Incoming("solver_in")
    public CompletionStage<Void> solve(Message<SolverEvent> solverEventMessage) {
        SolverEvent solverEvent = solverEventMessage.getPayload();
        return CompletableFuture.runAsync(() -> {
            final Long problemId = solverEvent.getProblemId();
            TimeTable problem = repository.load(problemId);
            TimeTable solution;
            try {
                solution = solver.solve(problem);
            } catch (Throwable throwable) {
                LOGGER.error("Solving an input problem (" + solverEvent.getProblemId() + ") has failed.", throwable);
                solverEventMessage.nack(throwable);
                return;
            }

            try {
                repository.save(problemId, solution);
                solverEventEmitter.send(new SolverEvent(problemId, SolverEventType.SOLVER_FINISHED)).thenRun(() -> {
                    solverEventMessage.ack();
                    LOGGER.debug("Solution saved for an input problem (" + solverEvent.getProblemId() + ")");
                });
            } catch (Throwable throwable) {
                LOGGER.error("Saving a solution for an input problem (" + solverEvent.getProblemId() + ") has failed.",
                        throwable);
                solverEventMessage.nack(throwable);
            }
        });
    }
}