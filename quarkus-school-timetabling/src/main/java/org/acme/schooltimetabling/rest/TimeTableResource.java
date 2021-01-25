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

package org.acme.schooltimetabling.rest;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.persistence.TimeTableRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.optaplanner.ErrorResponse;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;

@Path("time-table")
@Produces(MediaType.APPLICATION_JSON)
public class TimeTableResource {

    private static final String OPEN_API_PROBLEM_ID_DESC = "Never null, an ID for each planning problem. This must be unique.";

    private static final String PARAM_PROBLEM_ID = "problemId";

    // TODO: If the client calls only solve(), there is a memory leak.
    private Map<Long, Throwable> errors = new ConcurrentHashMap<>();

    @Inject
    TimeTableRepository timeTableRepository;

    @Inject
    SolverManager<TimeTable, Long> solverManager;
    @Inject
    ScoreManager<TimeTable, HardSoftScore> scoreManager;

    @Operation(
            summary = "Submit a TimeTable planning problem. Only the best solution that meets Termination conditions will be saved using the TimeTableRepository bean.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200"),
            @APIResponse(responseCode = "409",
                    description = "In case a TimeTable under the same problem ID has been already submitted and hasn't finished solving yet."),
            @APIResponse(responseCode = "500", description = "In case an exception was thrown during solving.")
    })
    @POST
    @Path("{problemId}/solve")
    public Response solve(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        Optional<Response> alreadySolvingResponse = checkSolverStatus(problemId);
        if (alreadySolvingResponse.isPresent()) {
            return alreadySolvingResponse.get();
        }
        try {
            solverManager.solve(problemId, this::findById, timeTable -> save(problemId, timeTable), this::solvingFailed);
            return Response.ok().build();
        } catch (RuntimeException runtimeException) {
            return createServerErrorResponse(runtimeException);
        }
    }

    @Operation(
            summary = "Submit a TimeTable planning problem. Any new best solution will be saved using the TimeTableRepository bean.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200"),
            @APIResponse(responseCode = "409",
                    description = "In case a TimeTable under the same problem ID has been already submitted and hasn't finished solving yet."),
            @APIResponse(responseCode = "500", description = "In case an exception was thrown during solving.")
    })
    @POST
    @Path("{problemId}/solve-and-listen")
    public Response
    solveAndListen(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        Optional<Response> alreadySolvingResponse = checkSolverStatus(problemId);
        if (alreadySolvingResponse.isPresent()) {
            return alreadySolvingResponse.get();
        }
        try {
            solverManager.solveAndListen(problemId,
                    this::findById,
                    timeTable -> save(problemId, timeTable),
                    this::solvingFailed);
            return Response.ok().build();
        } catch (RuntimeException runtimeException) {
            return createServerErrorResponse(runtimeException);
        }
    }

    @Operation(summary = "Get a solver status by a problem ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "An instance of SolverStatus.", // TODO: maybe a link to javadoc?
                    content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "500", description = "In case an exception was thrown during solving.")
    })
    @GET
    @Path("{problemId}/status")
    public Response getStatus(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        Optional<Response> errorResponse = checkExceptionDuringSolving(problemId);
        if (errorResponse.isPresent()) {
            return errorResponse.get();
        } else {
            SolverStatus solverStatus = solverManager.getSolverStatus(problemId);
            return Response.ok().entity(solverStatus).build();
        }
    }

    @Operation(summary = "Stop solving a TimeTable by identified by the problem ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200"),
            @APIResponse(responseCode = "500",
                    description = "In case an exception was thrown during solving. Clears the exception, se that any subsequent call.")
    })
    @DELETE
    @Path("{problemId}")
    public Response
    stopSolving(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        Optional<Response> errorResponse = checkExceptionDuringSolving(problemId);
        if (errorResponse.isPresent()) {
            return errorResponse.get();
        } else {
            solverManager.terminateEarly(problemId);
            return Response.ok().build();
        }
    }

    @Operation(summary = "Get a solved TimeTable by a problem ID. The solution is retrieved from the TimeTableRepository bean.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", description = "A solved instance of TimeTable.",
                    content = @Content(mediaType = "application/json")),
            @APIResponse(responseCode = "404", description = "In case no TimeTable was found by the TimeTableRepository bean."),
            @APIResponse(responseCode = "500", description = "In case an exception was thrown during solving.")
    })
    @GET
    @Path("{problemId}")
    public Response
            getTimeTable(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        Optional<Response> errorResponse = checkExceptionDuringSolving(problemId);
        if (errorResponse.isPresent()) {
            return errorResponse.get();
        } else {
            TimeTable timeTable = findById(problemId);
            if (timeTable == null) {
                return Response
                        .status(Response.Status.NOT_FOUND.getStatusCode(), "No solution found by problemId (" + problemId + ")")
                        .build();
            }
            scoreManager.updateScore(timeTable);
            return Response.ok().entity(timeTable).build();
        }
    }

    @Operation(summary = "Get an execution error by a problem ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_JSON)),
            @APIResponse(responseCode = "404",
                    description = "In case there is no execution error associated with the problem ID.")
    })
    @GET
    @Path("{problemId}/error")
    public Response getError(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        Throwable exception = errors.get(problemId);
        if (exception == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            // TODO: send the entire stacktrace?
            return Response.ok().entity(new ErrorResponse(exception.getMessage())).build();
        }
    }

    @Operation(summary = "Delete an execution error by a problem ID.")
    @APIResponses(value = {
            @APIResponse(responseCode = "200"),
            @APIResponse(responseCode = "404",
                    description = "In case there is no execution error associated with the problem ID.")
    })
    @DELETE
    @Path("{problemId}/error")
    public Response deleteError(@Parameter(description = OPEN_API_PROBLEM_ID_DESC) @PathParam(PARAM_PROBLEM_ID) Long problemId) {
        return errors.remove(problemId) == null ? Response.status(Response.Status.NOT_FOUND).build() : Response.ok().build();
    }

    private Optional<Response> checkSolverStatus(Long problemId) {
        SolverStatus solverStatus = solverManager.getSolverStatus(problemId);
        if (SolverStatus.NOT_SOLVING != solverStatus) {
            return Optional.of(Response.status(Response.Status.CONFLICT.getStatusCode(),
                    "There is already running or scheduled solving for the problemId (" + problemId + ")").build());
        } else {
            return Optional.empty();
        }
    }

    protected void solvingFailed(Long problemId, Throwable throwable) {
        errors.put(problemId, throwable);
    }

    private Optional<Response> checkExceptionDuringSolving(Long problemId) {
        Throwable error = errors.get(problemId);
        return (error == null) ? Optional.empty()
                : Optional.of(createServerErrorResponse(error));
    }

    private Response createServerErrorResponse(Throwable exception) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), exception.getMessage()).build();
    }

    protected TimeTable findById(Long id) {
        return timeTableRepository.get(id);
    }

    protected void save(Long problemId, TimeTable timeTable) {
        timeTableRepository.put(problemId, timeTable);
    }
}
