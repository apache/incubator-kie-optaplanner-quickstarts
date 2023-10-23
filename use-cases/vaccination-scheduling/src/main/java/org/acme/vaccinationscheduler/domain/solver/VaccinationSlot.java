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
package org.acme.vaccinationscheduler.domain.solver;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.acme.vaccinationscheduler.domain.Appointment;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.optaplanner.core.api.domain.lookup.PlanningId;

/**
 * Only used by OptaPlanner, not part of the input or output model.
 * Follows the bucket design pattern, this is a bucket of {@link Appointment} instances.
 */
public class VaccinationSlot {

    @PlanningId
    private Long id;

    private VaccinationCenter vaccinationCenter;
    private LocalDate date;
    private LocalTime startTime;
    private VaccineType vaccineType;

    private List<Appointment> unscheduledAppointmentList;
    private int capacity;

    public VaccinationSlot(Long id, VaccinationCenter vaccinationCenter,
            LocalDateTime startDateTime, VaccineType vaccineType, List<Appointment> unscheduledAppointmentList, int capacity) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.date = startDateTime.toLocalDate();
        this.startTime = startDateTime.toLocalTime();
        this.vaccineType = vaccineType;
        this.unscheduledAppointmentList = unscheduledAppointmentList;
        this.capacity = capacity;
    }

    /** For testing purposes only */
    public VaccinationSlot(Long id, VaccinationCenter vaccinationCenter,
            LocalDateTime startDateTime, VaccineType vaccineType, int capacity) {
        this.id = id;
        this.vaccinationCenter = vaccinationCenter;
        this.date = startDateTime == null ? null : startDateTime.toLocalDate();
        this.startTime = startDateTime == null ? null : startDateTime.toLocalTime();
        this.vaccineType = vaccineType;
        unscheduledAppointmentList = null;
        this.capacity = capacity;
    }


    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(date, startTime);
    }

    @Override
    public String toString() {
        return vaccinationCenter + "@" + date + "_" + startTime + "/" + vaccineType;
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

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public VaccineType getVaccineType() {
        return vaccineType;
    }

    public List<Appointment> getUnscheduledAppointmentList() {
        return unscheduledAppointmentList;
    }

    public int getCapacity() {
        return capacity;
    }

}
