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
import java.time.LocalDateTime;

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.vaccinationscheduler.domain.Injection;
import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

@QuarkusTest
class VaccinationScheduleConstraintProviderTest {

    private static final VaccineType PFIZER = new VaccineType("Pfizer", 19, 21);
    private static final VaccineType MODERNA = new VaccineType("Moderna", 26, 28);
    private static final VaccineType ASTRAZENECA = new VaccineType("AstraZeneca", 4 * 7, 6 * 7, 55);

    private static final VaccinationCenter VACCINATION_CENTER_1 = new VaccinationCenter("Alpha", new Location(0, 0), 3);
    private static final LocalDate MONDAY = LocalDate.of(2021, 2, 1);
    private static final LocalDate TUESDAY = LocalDate.of(2021, 2, 2);
    private static final LocalDate WEDNESDAY = LocalDate.of(2021, 2, 3);
    private static final LocalDate THURSDAY = LocalDate.of(2021, 2, 4);
    private static final LocalDate FRIDAY = LocalDate.of(2021, 2, 4);
    private static final LocalDateTime MONDAY_0900 = LocalDateTime.of(2021, 2, 1, 9, 0);
    private static final LocalDateTime MONDAY_1000 = LocalDateTime.of(2021, 2, 1, 10, 0);
    private static final LocalDateTime MONDAY_1100 = LocalDateTime.of(2021, 2, 1, 11, 0);
    private static final LocalDateTime TUESDAY_0900 = LocalDateTime.of(2021, 2, 2, 9, 0);
    private static final LocalDateTime WEDNESDAY_0900 = LocalDateTime.of(2021, 2, 3, 9, 0);
    private static final LocalDateTime THURSDAY_0900 = LocalDateTime.of(2021, 2, 4, 9, 0);
    private static final LocalDateTime FRIDAY_0900 = LocalDateTime.of(2021, 2, 5, 9, 0);
    private static final Person ANN = new Person(1, "Ann", new Location(1, 0), LocalDate.of(1950, 1, 1), 71);
    private static final Person BETH = new Person(2, "Beth", new Location(2, 0), LocalDate.of(1980, 1, 1), 41);
    private static final Person CARL = new Person(3, "Carl", new Location(3, 0), LocalDate.of(1970, 1, 1), 51,
            true, MODERNA, THURSDAY.minusDays(28));

    @Inject
    ConstraintVerifier<VaccinationScheduleConstraintProvider, VaccinationSchedule> constraintVerifier;

    @Test
    void personConflict() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::personConflict)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, PFIZER, BETH),
                        new Injection(3, VACCINATION_CENTER_1, 0, MONDAY_1100, PFIZER, ANN)
                        )
                .penalizesBy(1);
    }

    @Test
    void vaccinationTypeMaximumAge() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationTypeMaximumAge)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN)
                )
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationTypeMaximumAge)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, ASTRAZENECA, ANN)
                )
                .penalizesBy(71 - 55);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::vaccinationTypeMaximumAge)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, ASTRAZENECA, BETH)
                )
                .penalizesBy(0);
    }

    @Test
    void secondDoseInvalidVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseInvalidVaccineType)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, PFIZER, CARL)
                )
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseInvalidVaccineType)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, MODERNA, CARL)
                )
                .penalizesBy(0);
    }

    @Test
    void secondDoseReadyDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, MODERNA, ANN))
                .penalizesBy(0);

        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, MODERNA, CARL))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, TUESDAY_0900, MODERNA, CARL))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseReadyDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, WEDNESDAY_0900, MODERNA, CARL))
                .penalizesBy(0);
    }

    @Test
    void secondDoseIdealDate() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, MODERNA, ANN))
                .penalizesBy(0);

        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, MODERNA, CARL))
                .penalizesBy(3);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, TUESDAY_0900, MODERNA, CARL))
                .penalizesBy(2);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, WEDNESDAY_0900, MODERNA, CARL))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, THURSDAY_0900, MODERNA, CARL))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseIdealDate)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, FRIDAY_0900, MODERNA, CARL))
                .penalizesBy(1);
    }

    @Test
    void secondDoseMustBeAssigned() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseMustBeAssigned)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, CARL)
                        )
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondDoseMustBeAssigned)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN)
                        )
                .penalizesBy(1);
    }

    @Test
    void assignAllOlderPeople() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::assignAllOlderPeople)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN)
                        )
                .penalizesBy(51 + 41);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::assignAllOlderPeople)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, PFIZER, CARL)
                        )
                .penalizesBy(41);
    }

    @Test
    void distanceCost() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::distanceCost)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN))
                .penalizesBy((long) Location.METERS_PER_DEGREE);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::distanceCost)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, PFIZER, BETH))
                .penalizesBy(3L * (long) Location.METERS_PER_DEGREE);
    }

}
