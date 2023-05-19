package org.acme.demoapp.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.acme.common.domain.TimeTable;
import org.acme.common.message.SolverRequest;
import org.acme.common.message.SolverResponse;
import org.acme.common.persistence.TimeTableRepository;
import org.acme.demoapp.generator.DemoDataGenerator;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolutionManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("demo")
public class DemoDataResource {

    private static final int MAX_LESSON_COUNT = 200;

    public static final ProblemIdSequence PROBLEM_ID_SEQUENCE = new ProblemIdSequence();

    private final DemoDataGenerator demoDataGenerator;

    private final TimeTableRepository timeTableRepository;

    private final Emitter<String> solverRequestEmitter;

    private final EventResource eventResource;

    private final SolutionManager<TimeTable, HardSoftScore> solutionManager;

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<Long, Dataset> datasets = new ConcurrentHashMap<>();

    @Inject
    public DemoDataResource(DemoDataGenerator demoDataGenerator, TimeTableRepository timeTableRepository,
                            @Channel("solver_request") Emitter<String> solverRequestEmitter,
                            EventResource eventResource, SolutionManager<TimeTable, HardSoftScore> solutionManager,
                            ObjectMapper objectMapper) {
        this.demoDataGenerator = demoDataGenerator;
        this.timeTableRepository = timeTableRepository;
        this.solverRequestEmitter = solverRequestEmitter;
        this.eventResource = eventResource;
        this.solutionManager = solutionManager;
        this.objectMapper = objectMapper;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lessons}")
    public Dataset createTimeTable(@PathParam("lessons") int lessons) {
        if (lessons < 10 || lessons > MAX_LESSON_COUNT) {
            throw new IllegalArgumentException("The number of lessons (" + lessons + ") must be between 10 and 200.");
        }
        Dataset dataset = generateAndPersistUnsolvedDataset(lessons);
        SolverRequest solverRequest = new SolverRequest(dataset.getProblemId());
        String solverRequestMessage;
        try {
            solverRequestMessage = objectMapper.writeValueAsString(solverRequest);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize the " + SolverRequest.class.getName()
                    + " (" + solverRequest + ") to JSON.", e);
        }
        datasets.put(dataset.getProblemId(), dataset);
        solverRequestEmitter.send(solverRequestMessage);

        return dataset;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("datasets")
    public List<Dataset> getDatasets() {
        return new ArrayList<>(datasets.values());
    }

    @Incoming("solver_response")
    @Acknowledgment(Acknowledgment.Strategy.MANUAL)
    public CompletionStage<Void> handleMessage(Message<String> solverResponseMessage) {
        return CompletableFuture.runAsync(() -> {
            SolverResponse solverResponse;
            try {
                solverResponse = objectMapper.readValue(solverResponseMessage.getPayload(), SolverResponse.class);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Unable to serialize the " + SolverResponse.class.getName()
                        + " (" + solverResponseMessage.getPayload() + ") from JSON.", e);
            }
            final long problemId = solverResponse.getProblemId();
            TimeTable timeTable = timeTableRepository.load(problemId);
            if (timeTable == null) {
                IllegalStateException exception = new IllegalStateException("A timetable (" + problemId
                        + ") cannot be found in the repository.");
                solverResponseMessage.nack(exception);
                throw exception;
            }
            solutionManager.update(timeTable);
            Dataset dataset = datasets.get(problemId);
            if (dataset == null) {
                throw new IllegalStateException("Impossible state: Received a notification about a solved dataset ("
                        + problemId
                        + ") that was never submitted.");
            }
            solverResponseMessage.ack();
            if (timeTable.getScore() != null) {
                dataset.setSolved(true);
                dataset.setScore(timeTable.getScore().toString());
            }
            eventResource.sendEvent(problemId);
        });
    }

    public Dataset generateAndPersistUnsolvedDataset(int lessons) {
        int sanitizedLessons = lessons > MAX_LESSON_COUNT ? MAX_LESSON_COUNT : lessons;
        final long problemId = PROBLEM_ID_SEQUENCE.next();
        int rooms = sanitizedLessons / 20;
        TimeTable timeTable = demoDataGenerator.generateUnsolvedTimeTable(problemId, sanitizedLessons, rooms);
        timeTableRepository.persist(timeTable);
        return new Dataset(problemId, sanitizedLessons, rooms, false);
    }

    private static class ProblemIdSequence {
        private final AtomicLong counter = new AtomicLong(0L);

        long next() {
            return counter.incrementAndGet();
        }
    }
}
