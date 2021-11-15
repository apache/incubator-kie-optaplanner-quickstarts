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

package org.acme.employeescheduling.rest;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.acme.employeescheduling.persistence.AvailabilityRepository;
import org.acme.employeescheduling.persistence.EmployeeRepository;
import org.acme.employeescheduling.persistence.ShiftRepository;
import org.optaplanner.core.api.score.ScoreManager;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverStatus;

import io.quarkus.panache.common.Sort;

@Path("/schedule")
public class EmployeeScheduleResource {

    public static final Long SINGLETON_SCHEDULE_ID = 1L;

    @Inject
    AvailabilityRepository availabilityRepository;
    @Inject
    EmployeeRepository employeeRepository;
    @Inject
    ShiftRepository shiftRepository;

    @Inject
    SolverManager<EmployeeSchedule, Long> solverManager;
    @Inject
    ScoreManager<EmployeeSchedule, HardSoftScore> scoreManager;

    // To try, open http://localhost:8080/schedule
    @GET
    public EmployeeSchedule getSchedule() {
        // Get the solver status before loading the solution
        // to avoid the race condition that the solver terminates between them
        SolverStatus solverStatus = getSolverStatus();
        EmployeeSchedule solution = findById(SINGLETON_SCHEDULE_ID);
        scoreManager.updateScore(solution); // Sets the score
        solution.setSolverStatus(solverStatus);
        solution.computeFromAndToDates();
        return solution;
    }

    public SolverStatus getSolverStatus() {
        return solverManager.getSolverStatus(SINGLETON_SCHEDULE_ID);
    }

    @POST
    @Path("solve")
    public void solve() {
        solverManager.solveAndListen(SINGLETON_SCHEDULE_ID,
                this::findById,
                this::save);
    }

    @POST
    @Path("stopSolving")
    public void stopSolving() {
        solverManager.terminateEarly(SINGLETON_SCHEDULE_ID);
    }

    @Transactional
    protected EmployeeSchedule findById(Long id) {
        if (!SINGLETON_SCHEDULE_ID.equals(id)) {
            throw new IllegalStateException("There is no schedule with id (" + id + ").");
        }
        return new EmployeeSchedule(
                availabilityRepository.listAll(Sort.by("date").and("id")),
                employeeRepository.listAll(Sort.by("name")),
                shiftRepository.listAll(Sort.by("location").and("start").and("id")));
    }

    @Transactional
    protected void save(EmployeeSchedule schedule) {
        for (Shift shift : schedule.getShiftList()) {
            // TODO this is awfully naive: optimistic locking causes issues if called by the SolverManager
            Shift attachedShift = shiftRepository.findById(shift.getId());
            attachedShift.setEmployee(shift.getEmployee());
        }
    }
}
