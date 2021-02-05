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

import java.time.LocalDateTime;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class Injection {

    @PlanningId
    private Long id;

    private VaccinationCenter vaccinationCenter;
    private int lineIndex;
    private LocalDateTime dateTime;
    private VaccineType vaccineType;

    @PlanningPin
    private boolean pinned;

    @PlanningVariable(valueRangeProviderRefs = {"personRange"})
    private Person person;

    // No-arg constructor required for OptaPlanner
    public Injection() {
    }

    public Injection(Long id, VaccinationCenter vaccinationCenter, int lineIndex,
            LocalDateTime dateTime, VaccineType vaccineType) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.lineIndex = lineIndex;
        this.dateTime = dateTime;
        this.vaccineType = vaccineType;
    }

    public Injection(long id, VaccinationCenter vaccinationCenter, int lineIndex, LocalDateTime dateTime, VaccineType vaccineType, Person person) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.lineIndex = lineIndex;
        this.dateTime = dateTime;
        this.vaccineType = vaccineType;
        this.person = person;
    }

    @Override
    public String toString() {
        return dateTime + "@" + vaccinationCenter.getName() + "(" + id + ")";
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

    public int getLineIndex() {
        return lineIndex;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

}
