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

package org.acme.schooltimetabling.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.messaging.SolverRequest;
import org.acme.schooltimetabling.messaging.SolverResponse;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.optaplanner.core.api.solver.SolverStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("timeTable")
public class TimeTableResource {

    public static final Long SINGLETON_TIME_TABLE_ID = 1L;

    @Inject
    TimeTableRepository timeTableRepository;

    @Inject
    @Channel("solver_request")
    Emitter<String> solverRequestEmitter;

    @Inject
    ObjectMapper objectMapper;

    @Incoming("solver_response")
    @Acknowledgment(Acknowledgment.Strategy.POST_PROCESSING)
    public void process(String solverResponseMessage) {
        SolverResponse solverResponse;
        try {
            solverResponse = objectMapper.readValue(solverResponseMessage, SolverResponse.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize the solver response.", ex);
        }

        if (solverResponse.isSuccess()) {
            TimeTable timeTable = solverResponse.getTimeTable();
            timeTable.setSolverStatus(SolverStatus.NOT_SOLVING);
            timeTableRepository.update(timeTable);
        } else {
            timeTableRepository.get().setSolverStatus(SolverStatus.NOT_SOLVING);
            throw new IllegalStateException("Error during solving. "
                    + solverResponse.getErrorInfo().getExceptionClassName()
                    + " : "
                    + solverResponse.getErrorInfo().getExceptionMessage());
        }
    }

    // To try, open http://localhost:8080/timeTable
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TimeTable getTimeTable() {
        TimeTable timeTable = timeTableRepository.get();
        return timeTable;
    }

    @POST
    @Path("solve")
    public void solve() throws JsonProcessingException {
        TimeTable timeTable = timeTableRepository.get();
        timeTable.setSolverStatus(SolverStatus.SOLVING_SCHEDULED);

        SolverRequest solverRequest = new SolverRequest(SINGLETON_TIME_TABLE_ID, timeTable);
        String solverRequestJson = objectMapper.writeValueAsString(solverRequest);
        solverRequestEmitter.send(solverRequestJson);
    }
}
