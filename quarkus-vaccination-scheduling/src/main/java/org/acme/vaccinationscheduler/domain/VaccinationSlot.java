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

package org.acme.vaccinationscheduler.domain;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class VaccinationSlot {

    @PlanningId
    private Long id;

    private VaccinationCenter vaccinationCenter;
    private Timeslot timeslot;
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType vaccineType;

    private final int lineIndexOffset;
    private int lineCount;
    private int capacity;

    public VaccinationSlot(Long id, VaccinationCenter vaccinationCenter, Timeslot timeslot, VaccineType vaccineType,
            int lineIndexOffset, int lineCount, int capacity) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.timeslot = timeslot;
        this.vaccineType = vaccineType;
        this.lineIndexOffset = lineIndexOffset;
        this.lineCount = lineCount;
        if (lineCount > vaccinationCenter.getLineCount()) {
            throw new IllegalStateException("A vaccination slot's lineCount (" + lineCount
                    + ") must be less or equal to the vaccination center's lineCount ("
                    + vaccinationCenter.getLineCount() + ").");
        }
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return vaccinationCenter + "@" + timeslot + "/" + vaccineType;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public VaccinationCenter getVaccinationCenter() {
        return vaccinationCenter;
    }

    public Timeslot getTimeslot() {
        return timeslot;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

    public int getLineIndexOffset() {
        return lineIndexOffset;
    }

    public int getLineCount() {
        return lineCount;
    }

    public int getCapacity() {
        return capacity;
    }

}
