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

package org.acme.vaccinationscheduler.domain.ui;

import java.time.LocalDateTime;

import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccineType;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class AppointmentVisualization {

    private Person person;
    private VaccinationCenter vaccinationCenter;
    private int lineIndex;
    private LocalDateTime dateTime;
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType vaccineType;

    public AppointmentVisualization(Person person, VaccinationCenter vaccinationCenter, int lineIndex, LocalDateTime dateTime, VaccineType vaccineType) {
        this.person = person;
        this.vaccinationCenter = vaccinationCenter;
        this.lineIndex = lineIndex;
        this.dateTime = dateTime;
        this.vaccineType = vaccineType;
    }

    public Person getPerson() {
        return person;
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
}
