/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.acme.maintenancescheduling.domain;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.solution.ProblemFactProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class MaintenanceSchedule {

    @ProblemFactProperty
    private WorkCalendar workCalendar;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Crew> crewList;
    @PlanningEntityCollectionProperty
    private List<Job> jobList;

    @PlanningScore
    private HardSoftLongScore score;

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    // No-arg constructor required for OptaPlanner
    public MaintenanceSchedule() {
    }

    public MaintenanceSchedule(WorkCalendar workCalendar,
            List<Crew> crewList, List<Job> jobList) {
        this.workCalendar = workCalendar;
        this.crewList = crewList;
        this.jobList = jobList;
    }

    @ValueRangeProvider
    public List<LocalDate> createStartDateList() {
        return workCalendar.getFromDate().datesUntil(workCalendar.getToDate())
                // Skip weekends. Does not work for holidays.
                // Keep in sync with EndDateUpdatingVariableListener.updateEndDate().
                // To skip holidays too, cache all working days in WorkCalendar.
                .filter(date -> date.getDayOfWeek() != DayOfWeek.SATURDAY
                        && date.getDayOfWeek() != DayOfWeek.SUNDAY)
                .collect(Collectors.toList());
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public WorkCalendar getWorkCalendar() {
        return workCalendar;
    }

    public List<Crew> getCrewList() {
        return crewList;
    }

    public List<Job> getJobList() {
        return jobList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
