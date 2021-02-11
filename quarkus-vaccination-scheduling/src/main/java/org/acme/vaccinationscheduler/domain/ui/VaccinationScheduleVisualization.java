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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.Timeslot;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccinationSlot;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;
import org.optaplanner.core.api.solver.SolverStatus;

public class VaccinationScheduleVisualization {

    private List<VaccineType> vaccineTypeList;

    private List<VaccinationCenter> vaccinationCenterList;

    private List<LocalDateTime> dateTimeList;

    private List<Person> personList;

    private List<AppointmentVisualization> appointmentList;

    private HardMediumSoftLongScore score;

    private SolverStatus solverStatus;

    public VaccinationScheduleVisualization(VaccinationSchedule schedule, SolverStatus solverStatus) {
        vaccineTypeList = schedule.getVaccineTypeList();
        vaccinationCenterList = schedule.getVaccinationCenterList();

        // TODO the UI doesn't support multiple VaccinationCenters with different injectionsPerLinePerTimeslot
        int injectionsPerLinePerTimeslot = vaccinationCenterList.isEmpty() ? 6
                : vaccinationCenterList.get(0).getInjectionsPerLinePerTimeslot();
        dateTimeList = new ArrayList<>(schedule.getTimeslotList().size() * injectionsPerLinePerTimeslot);
        for (Timeslot timeslot : schedule.getTimeslotList()) {
            for (int timeIndex = 0; timeIndex < injectionsPerLinePerTimeslot; timeIndex++) {
                LocalDateTime dateTime = LocalDateTime.of(timeslot.getDate(),
                        timeslot.getStartTime()).plus(
                        timeslot.getDuration().multipliedBy(timeIndex).dividedBy(
                                injectionsPerLinePerTimeslot));
                dateTimeList.add(dateTime);
            }
        }

        appointmentList = new ArrayList<>(schedule.getPersonList().size());
        personList = schedule.getPersonList();
        Map<VaccinationSlot, Integer> nextPersonIndexMap = new HashMap<>(
                schedule.getVaccinationSlotList().size());
        for (Person person : schedule.getPersonList()) {
            VaccinationSlot vaccinationSlot = person.getVaccinationSlot();
            if (vaccinationSlot != null) {
                VaccinationCenter vaccinationCenter = vaccinationSlot.getVaccinationCenter();
                int personIndex = nextPersonIndexMap.computeIfAbsent(vaccinationSlot, key -> 0);
                nextPersonIndexMap.put(vaccinationSlot, personIndex + 1);
                int lineIndex = vaccinationSlot.getLineIndexOffset() + (personIndex % vaccinationSlot.getLineCount());
                int timeIndex = personIndex / vaccinationSlot.getLineCount();
                Timeslot timeslot = vaccinationSlot.getTimeslot();
                LocalDateTime dateTime = LocalDateTime.of(timeslot.getDate(),
                        timeslot.getStartTime()).plus(
                                timeslot.getDuration().multipliedBy(timeIndex).dividedBy(
                                        vaccinationCenter.getInjectionsPerLinePerTimeslot()));
                appointmentList.add(new AppointmentVisualization(
                        person, vaccinationCenter, lineIndex, dateTime, vaccinationSlot.getVaccineType()));
            }
        }
        score = schedule.getScore();
        this.solverStatus = solverStatus;
    }

    public List<VaccineType> getVaccineTypeList() {
        return vaccineTypeList;
    }

    public List<VaccinationCenter> getVaccinationCenterList() {
        return vaccinationCenterList;
    }

    public List<LocalDateTime> getDateTimeList() {
        return dateTimeList;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public List<AppointmentVisualization> getAppointmentList() {
        return appointmentList;
    }

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }
}
