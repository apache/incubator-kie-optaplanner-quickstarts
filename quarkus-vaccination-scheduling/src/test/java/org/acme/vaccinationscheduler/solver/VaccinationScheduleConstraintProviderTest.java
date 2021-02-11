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

package org.acme.vaccinationscheduler.solver;

import java.time.LocalDate;
import java.time.LocalTime;

import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.Timeslot;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccinationSlot;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.stream.ConstraintStreamImplType;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

class VaccinationScheduleConstraintProviderTest {

    private static final VaccineType PFIZER = new VaccineType("Pfizer", 19, 21);
    private static final VaccineType MODERNA = new VaccineType("Moderna", 26, 28);
    private static final VaccineType ASTRAZENECA = new VaccineType("AstraZeneca", 4 * 7, 6 * 7, 55);

    private static final VaccinationCenter VACCINATION_CENTER_1 = new VaccinationCenter(1L, "Downtown", new Location(0, 0), 3, 6);
    private static final Timeslot MONDAY_0900 = new Timeslot(LocalDate.of(2021, 2, 1), LocalTime.of(9, 0), LocalTime.of(10, 0));
    private static final Timeslot TUESDAY_0900 =
            new Timeslot(LocalDate.of(2021, 2, 2), LocalTime.of(9, 0), LocalTime.of(10, 0));
    private static final Timeslot WEDNESDAY_0900 =
            new Timeslot(LocalDate.of(2021, 2, 3), LocalTime.of(9, 0), LocalTime.of(10, 0));
    private static final Timeslot THURSDAY_0900 =
            new Timeslot(LocalDate.of(2021, 2, 4), LocalTime.of(9, 0), LocalTime.of(10, 0));
    private static final Timeslot FRIDAY_0900 =
            new Timeslot(LocalDate.of(2021, 2, 5), LocalTime.of(9, 0), LocalTime.of(10, 0));
    private static final VaccinationSlot PFIZER_MONDAY_SLOT =
            new VaccinationSlot(1L, VACCINATION_CENTER_1, MONDAY_0900, PFIZER, 0, 2, 12);
    private static final VaccinationSlot PFIZER_TUESDAY_SLOT =
            new VaccinationSlot(1L, VACCINATION_CENTER_1, TUESDAY_0900, PFIZER, 0, 2, 12);
    private static final VaccinationSlot PFIZER_WEDNESDAY_SLOT =
            new VaccinationSlot(1L, VACCINATION_CENTER_1, WEDNESDAY_0900, PFIZER, 0, 2, 12);
    private static final VaccinationSlot PFIZER_THURSDAY_SLOT =
            new VaccinationSlot(1L, VACCINATION_CENTER_1, THURSDAY_0900, PFIZER, 0, 2, 12);
    private static final VaccinationSlot PFIZER_FRIDAY_SLOT =
            new VaccinationSlot(1L, VACCINATION_CENTER_1, FRIDAY_0900, PFIZER, 0, 2, 12);
    private static final VaccinationSlot MODERNA_SLOT = new VaccinationSlot(1L, VACCINATION_CENTER_1, MONDAY_0900, MODERNA, 0, 2, 12);
    private static final VaccinationSlot AZ_SLOT = new VaccinationSlot(1L, VACCINATION_CENTER_1, MONDAY_0900, ASTRAZENECA, 0, 2, 12);

    // TODO Quarkus should inject ConstraintVerifier so its 100% in sync with the real solver config
    private final ConstraintVerifier<VaccinationScheduleConstraintProvider, VaccinationSchedule> constraintVerifier =
            ConstraintVerifier.build(new VaccinationScheduleConstraintProvider(), VaccinationSchedule.class, Person.class)
            // To scale up to 200 000 persons and more
            .withConstraintStreamImplType(ConstraintStreamImplType.BAVET);

    @Test
    void vaccinationSlotCapacity() {
        VaccinationCenter vaccinationCenter = new VaccinationCenter(1L, "Uptown", new Location(0, 0), 1, 3);
        VaccinationSlot vaccinationSlot = new VaccinationSlot(null, vaccinationCenter, null, null, 0, 1, 3);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationSlotCapacity)
                .given(
                        vaccinationSlot, PFIZER_MONDAY_SLOT,
                        new Person(null, "Ann", null, null, 0, vaccinationSlot),
                        new Person(null, "Beth", null, null, 0, vaccinationSlot),
                        new Person(null, "Carl", null, null, 0, vaccinationSlot),
                        new Person(null, "Dan", null, null, 0, vaccinationSlot),
                        new Person(null, "Ed", null, null, 0, vaccinationSlot),
                        new Person(null, "Flo", null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(2);
    }

    @Test
    void vaccinationTypeMaximumAge() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationTypeMaximumAge)
                .given(
                        new Person(null, "Ann", null, LocalDate.of(1950, 1, 1), 71, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationTypeMaximumAge)
                .given(
                        new Person(null, "Ann", null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT))
                .penalizesBy(71 - 55);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationTypeMaximumAge)
                .given(
                        new Person(null, "Beth", null, LocalDate.of(1980, 1, 1), 41, AZ_SLOT))
                .penalizesBy(0);
    }

    @Test
    void secondDoseInvalidVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseInvalidVaccineType)
                .given(
                        new Person(null, "Ann", null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseInvalidVaccineType)
                .given(
                        new Person(null, "Carl", null, null, 0,
                                true, PFIZER, LocalDate.of(2021, 2, 4).minusDays(21), MODERNA_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseInvalidVaccineType)
                .given(
                        new Person(null, "Carl", null, null, 0,
                                true, PFIZER, LocalDate.of(2021, 2, 4).minusDays(21), PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void secondDoseReadyDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(
                        new Person(null, "Ann", null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);

        LocalDate firstDoseDate = LocalDate.of(2021, 2, 4).minusDays(21);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_MONDAY_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_TUESDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_WEDNESDAY_SLOT))
                .penalizesBy(0);
    }

    @Test
    void secondDoseIdealDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(
                        new Person(null, "Ann", null, null, 0, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);

        LocalDate firstDoseDate = LocalDate.of(2021, 2, 4).minusDays(21);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_MONDAY_SLOT))
                .penalizesBy(3);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_TUESDAY_SLOT))
                .penalizesBy(2);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_WEDNESDAY_SLOT))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_THURSDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_FRIDAY_SLOT))
                .penalizesBy(1);
    }

    @Test
    void secondDoseMustBeAssigned() {
        LocalDate firstDoseDate = LocalDate.of(2021, 2, 4).minusDays(21);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseMustBeAssigned)
                .given(
                        new Person(null, "Ann", null, null, 0, null),
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, PFIZER_MONDAY_SLOT))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseMustBeAssigned)
                .given(
                        new Person(null, "Ann", null, null, 0, PFIZER_MONDAY_SLOT),
                        new Person(null, "Carl", null, null, 0, true, PFIZER, firstDoseDate, null))
                .penalizesBy(1);
    }

    @Test
    void assignAllOlderPeople() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::assignAllOlderPeople)
                .given(
                        new Person(null, "Ann", null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT),
                        new Person(null, "Beth", null, LocalDate.of(1980, 1, 1), 41, null),
                        new Person(null, "carl", null, LocalDate.of(1970, 1, 1), 51, null))
                .penalizesBy(51 + 41);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::assignAllOlderPeople)
                .given(
                        new Person(null, "Ann", null, LocalDate.of(1950, 1, 1), 71, AZ_SLOT),
                        new Person(null, "Beth", null, LocalDate.of(1980, 1, 1), 41, null),
                        new Person(null, "carl", null, LocalDate.of(1970, 1, 1), 51, AZ_SLOT))
                .penalizesBy(41);
    }

    @Test
    void distanceCost() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::distanceCost)
                .given(
                        new Person(null, "Ann", new Location(1, 0), null, 0, AZ_SLOT))
                .penalizesBy((long) Location.METERS_PER_DEGREE);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::distanceCost)
                .given(
                        new Person(null, "Ann", new Location(1, 0), null, 0, AZ_SLOT),
                        new Person(null, "Beth", new Location(2, 0), null, 0, AZ_SLOT))
                .penalizesBy(3L * (long) Location.METERS_PER_DEGREE);
    }

}
