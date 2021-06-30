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

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class Appointment {

    @JsonIdentityReference(alwaysAsId = true)
    private VaccinationCenter vaccinationCenter;
    private String boothId;
    private LocalDateTime dateTime;
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType vaccineType;

    // No-arg constructor required for Jackson
    public Appointment() {}

    public Appointment(VaccinationCenter vaccinationCenter, String boothId, LocalDateTime dateTime, VaccineType vaccineType) {
        this.vaccinationCenter = vaccinationCenter;
        this.boothId = boothId;
        this.dateTime = dateTime;
        this.vaccineType = vaccineType;
    }

    @Override
    public String toString() {
        return vaccinationCenter + "-" + boothId + "@" + dateTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public VaccinationCenter getVaccinationCenter() {
        return vaccinationCenter;
    }

    public String getBoothId() {
        return boothId;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

}
