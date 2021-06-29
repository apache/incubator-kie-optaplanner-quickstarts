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

import java.util.List;

import org.acme.vaccinationscheduler.domain.solver.VaccinationSolution;
import org.optaplanner.core.api.score.buildin.bendablelong.BendableLongScore;
import org.optaplanner.core.api.solver.SolverStatus;

public class VaccinationSchedule {

    private List<VaccineType> vaccineTypeList;

    private List<VaccinationCenter> vaccinationCenterList;

    /**
     * Translated to {@link VaccinationSolution#getVaccinationCenterList()} before solving and back again after solving.
     * See {@link VaccinationSolution#VaccinationSolution(VaccinationSchedule)} and {@link VaccinationSolution#toSchedule()}.
     */
    private List<Appointment> appointmentList;

    private List<Person> personList;

    private BendableLongScore score;

    private SolverStatus solverStatus;

    // No-arg constructor required for Jackson
    public VaccinationSchedule() {
    }

    public VaccinationSchedule(List<VaccineType> vaccineTypeList, List<VaccinationCenter> vaccinationCenterList,
            List<Appointment> appointmentList, List<Person> personList) {
        this.vaccineTypeList = vaccineTypeList;
        this.vaccinationCenterList = vaccinationCenterList;
        this.appointmentList = appointmentList;
        this.personList = personList;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<VaccineType> getVaccineTypeList() {
        return vaccineTypeList;
    }

    public List<VaccinationCenter> getVaccinationCenterList() {
        return vaccinationCenterList;
    }

    public List<Appointment> getAppointmentList() {
        return appointmentList;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public BendableLongScore getScore() {
        return score;
    }

    public void setScore(BendableLongScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

}
