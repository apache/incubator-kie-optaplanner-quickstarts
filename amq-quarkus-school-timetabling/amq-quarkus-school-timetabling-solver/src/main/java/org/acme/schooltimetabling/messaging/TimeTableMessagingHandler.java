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

package org.acme.schooltimetabling.messaging;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.acme.schooltimetabling.domain.TimeTable;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

@ApplicationScoped
public class TimeTableMessagingHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimeTableMessagingHandler.class);
    public static final String SOLVER_REQUEST_CHANNEL = "solver_request";
    public static final String SOLVER_RESPONSE_CHANNEL = "solver_response";

    Solver<TimeTable> solver;

    @Inject
    @Channel(SOLVER_RESPONSE_CHANNEL)
    Emitter<String> solverResponseEmitter;

    @Inject
    TimeTableMessagingHandler(SolverFactory<TimeTable> solverFactory) {
        solver = solverFactory.buildSolver();
    }

    @Incoming(SOLVER_REQUEST_CHANNEL)
    public CompletionStage<Void> solve(Message<String> solverRequestMessage) {
        return CompletableFuture.runAsync(() -> {
            SolverRequest solverRequest = null;
            try {
                solverRequest = SolverJsonMapper.get().readValue(solverRequestMessage.getPayload(), SolverRequest.class);
            } catch (JsonProcessingException ex) {
                LOGGER.warn("Unable to deserialize solver request from JSON.", ex);
                // Bad request should go directly to a DLQ.
                solverRequestMessage.nack(ex);
                return;
            }

            TimeTable solution = null;
            try {
                solution = solver.solve(solverRequest.getTimeTable());
                replySuccess(solverRequestMessage, solverRequest.getProblemId(), solution);
            } catch (Exception ex) {
                LOGGER.warn("Error during processing a solver request ({}).", solverRequest.getProblemId(), ex);
                replyFailure(solverRequestMessage, solverRequest.getProblemId(), ex);
            }
        });
    }

    private void replySuccess(Message<String> solverRequestMessage, Long problemId, TimeTable solution) {
        SolverResponse solverResponse = new SolverResponse(problemId, solution);
        reply(solverRequestMessage, solverResponse, exception -> replyFailure(solverRequestMessage, problemId, exception));
    }

    private void replyFailure(Message<String> solverRequestMessage, Long problemId, Exception exception) {
        SolverResponse solverResponse =
                new SolverResponse(problemId, new ErrorInfo(exception.getClass().getName(), exception.getMessage()));
        reply(solverRequestMessage, solverResponse, serializationException -> {
            throw new IllegalStateException("Unable to serialize error response.", serializationException);
        });
    }

    private void reply(Message<String> solverRequestMessage, SolverResponse solverResponse,
            Consumer<? super Exception> onFailure) {
        try {
            String jsonResponse = SolverJsonMapper.get().writeValueAsString(solverResponse);
            solverResponseEmitter.send(jsonResponse).thenAccept(x -> solverRequestMessage.ack());
        } catch (JsonProcessingException ex) {
            onFailure.accept(ex);
        }
    }
}
