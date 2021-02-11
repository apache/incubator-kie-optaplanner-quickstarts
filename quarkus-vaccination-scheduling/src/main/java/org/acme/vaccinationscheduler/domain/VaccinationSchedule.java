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

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

@PlanningSolution
public class VaccinationSchedule {

    @ProblemFactCollectionProperty
    private List<VaccineType> vaccineTypeList;

    @ProblemFactCollectionProperty
    private List<VaccinationCenter> vaccinationCenterList;
    @ProblemFactCollectionProperty
    private List<Timeslot> timeslotList;

    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "vaccinationSlotRange")
    private List<VaccinationSlot> vaccinationSlotList;

    @PlanningEntityCollectionProperty
    private List<Person> personList;

    @PlanningScore
    private HardMediumSoftLongScore score;

    // No-arg constructor required for OptaPlanner
    public VaccinationSchedule() {
    }

    public VaccinationSchedule(List<VaccineType> vaccineTypeList, List<VaccinationCenter> vaccinationCenterList, List<Timeslot> timeslotList, List<VaccinationSlot> vaccinationSlotList, List<Person> personList) {
        this.vaccineTypeList = vaccineTypeList;
        this.vaccinationCenterList = vaccinationCenterList;
        this.timeslotList = timeslotList;
        this.vaccinationSlotList = vaccinationSlotList;
        this.personList = personList;
    }

    public List<VaccineType> getVaccineTypeList() {
        return vaccineTypeList;
    }

    public List<VaccinationCenter> getVaccinationCenterList() {
        return vaccinationCenterList;
    }

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public List<VaccinationSlot> getVaccinationSlotList() {
        return vaccinationSlotList;
    }

    public List<Person> getPersonList() {
        return personList;
    }

    public HardMediumSoftLongScore getScore() {
        return score;
    }

}
