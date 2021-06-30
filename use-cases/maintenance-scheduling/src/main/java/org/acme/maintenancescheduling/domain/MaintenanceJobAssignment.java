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

package org.acme.maintenancescheduling.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@Entity
@PlanningEntity
public class MaintenanceJobAssignment {

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    @OneToOne
    private MaintenanceJob maintenanceJob;

    // TODO: Add configuration option for how long each TimeGrain is
    @PlanningVariable(valueRangeProviderRefs = "timeGrainRange")
    @ManyToOne
    private TimeGrain startingTimeGrain;

    @PlanningVariable(valueRangeProviderRefs = "assignedCrewRange")
    @ManyToOne
    private MaintenanceCrew assignedCrew;

    public MaintenanceJobAssignment() {
    }

    public MaintenanceJobAssignment(MaintenanceJob maintenanceJob) {
        this.maintenanceJob = maintenanceJob;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public int calculateOverlap(MaintenanceJobAssignment other) {
        if (startingTimeGrain == null || other.getStartingTimeGrain() == null) {
            return 0;
        }
        int start = startingTimeGrain.getGrainIndex();
        int end = start + maintenanceJob.getDurationInGrains();
        int otherStart = other.getStartingTimeGrain().getGrainIndex();
        int otherEnd = otherStart + other.getMaintenanceJob().getDurationInGrains();

        if (end < otherStart) {
            return 0;
        } else if (otherEnd < start) {
            return 0;
        }
        return Math.min(end, otherEnd) - Math.max(start, otherStart);
    }

    public int calculateSafetyMarginPenalty() {
        if (startingTimeGrain == null) {
            return 0;
        }
        int start = startingTimeGrain.getGrainIndex();
        int end = start + maintenanceJob.getDurationInGrains();
        int safetyMarginStart = maintenanceJob.getDueTimeGrainIndex() - maintenanceJob.getSafetyMarginDurationInGrains();

        if (end < safetyMarginStart) {
            return 0;
        }
        return (end - safetyMarginStart) * (end - safetyMarginStart);
    }

    @Override
    public String toString() {
        return "MaintenanceJobAssignment{" +
                "id=" + id +
                ", maintenanceJob=" + maintenanceJob +
                '}';
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MaintenanceJob getMaintenanceJob() {
        return maintenanceJob;
    }

    public void setMaintenanceJob(MaintenanceJob maintenanceJob) {
        this.maintenanceJob = maintenanceJob;
    }

    public TimeGrain getStartingTimeGrain() {
        return startingTimeGrain;
    }

    public void setStartingTimeGrain(TimeGrain startingTimeGrain) {
        this.startingTimeGrain = startingTimeGrain;
    }

    public MaintenanceCrew getAssignedCrew() {
        return assignedCrew;
    }

    public void setAssignedCrew(MaintenanceCrew assignedCrew) {
        this.assignedCrew = assignedCrew;
    }
}
