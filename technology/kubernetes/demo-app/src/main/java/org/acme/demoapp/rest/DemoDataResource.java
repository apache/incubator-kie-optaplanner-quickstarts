/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.demoapp.rest;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.common.domain.TimeTable;
import org.acme.common.event.SolverEvent;
import org.acme.common.event.SolverEventType;
import org.acme.common.persistence.TimeTableRepository;
import org.acme.demoapp.generator.DemoDataGenerator;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@Path("demo")
public class DemoDataResource {

    private static final int MAX_LESSON_COUNT = 200;

    public static final ProblemIdSequence PROBLEM_ID_SEQUENCE = new ProblemIdSequence();

    private final DemoDataGenerator demoDataGenerator;

    private final TimeTableRepository timeTableRepository;

    private final Emitter<SolverEvent> solverEventEmitter;

    private final EventResource eventResource;

    private final ScoreManager<TimeTable, HardSoftScore> scoreManager;

    private final ConcurrentHashMap<Long, Dataset> datasets = new ConcurrentHashMap<>();

    @Inject
    public DemoDataResource(DemoDataGenerator demoDataGenerator, TimeTableRepository timeTableRepository,
                            @Channel("solver_request") Emitter<SolverEvent> solverEventEmitter,
                            EventResource eventResource, ScoreManager<TimeTable, HardSoftScore> scoreManager) {
        this.demoDataGenerator = demoDataGenerator;
        this.timeTableRepository = timeTableRepository;
        this.solverEventEmitter = solverEventEmitter;
        this.eventResource = eventResource;
        this.scoreManager = scoreManager;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{lessons}")
    public Dataset createTimeTable(@PathParam("lessons") int lessons) {
        if (lessons < 10 || lessons > MAX_LESSON_COUNT) {
            throw new IllegalArgumentException("The number of lessons (" + lessons + ") must be between 10 and 200.");
        }
        Dataset dataset = generateAndPersistUnsolvedDataset(lessons);
        SolverEvent solverEvent = new SolverEvent(dataset.getProblemId(), SolverEventType.SOLVER_REQUEST);
        datasets.put(dataset.getProblemId(), dataset);
        solverEventEmitter.send(solverEvent);

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
    public CompletionStage<Void> handleMessage(Message<SolverEvent> solverEventMessage) {
        return CompletableFuture.runAsync(() -> {
            final long problemId = solverEventMessage.getPayload().getProblemId();
            TimeTable timeTable = timeTableRepository.load(problemId);
            if (timeTable == null) {
                IllegalStateException exception = new IllegalStateException("A timetable (" + problemId
                        + ") cannot be found in the repository.");
                solverEventMessage.nack(exception);
                throw exception;
            }
            scoreManager.updateScore(timeTable);
            Dataset dataset = datasets.get(problemId);
            if (dataset == null) {
                throw new IllegalStateException("Impossible state: Received a notification about a solved dataset ("
                        + problemId
                        + ") that was never submitted.");
            }
            solverEventMessage.ack();
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
