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

import org.acme.vaccinationscheduler.domain.Injection;
import org.acme.vaccinationscheduler.domain.Location;
import org.acme.vaccinationscheduler.domain.Person;
import org.acme.vaccinationscheduler.domain.VaccinationCenter;
import org.acme.vaccinationscheduler.domain.VaccinationSchedule;
import org.acme.vaccinationscheduler.domain.VaccineType;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

class VaccinationScheduleConstraintProviderTest {

    private static final VaccinationCenter VACCINATION_CENTER_1 = new VaccinationCenter("Alpha", new Location(0, 0), 3);
    private static final LocalDate MONDAY = LocalDate.of(2021, 2, 1);
    private static final LocalDate TUESDAY = LocalDate.of(2021, 2, 2);
    private static final LocalDate WEDNESDAY = LocalDate.of(2021, 2, 3);
    private static final LocalDateTime MONDAY_0900 = LocalDateTime.of(2021, 2, 1, 9, 0);
    private static final LocalDateTime MONDAY_1000 = LocalDateTime.of(2021, 2, 1, 10, 0);
    private static final LocalDateTime MONDAY_1100 = LocalDateTime.of(2021, 2, 1, 11, 0);
    private static final LocalDateTime TUESDAY_0900 = LocalDateTime.of(2021, 2, 2, 9, 0);
    private static final LocalDateTime WEDNESDAY_0900 = LocalDateTime.of(2021, 2, 3, 9, 0);
    private static final Person ANN = new Person(1, "Ann", new Location(1, 0), LocalDate.of(1950, 1, 1), 71);
    private static final Person BETH = new Person(2, "Beth", new Location(2, 0), LocalDate.of(1980, 1, 1), 41);
    private static final Person CARL = new Person(3, "Carl", new Location(3, 0), LocalDate.of(1970, 1, 1), 51,
            true, VaccineType.MODERNA, MONDAY);

    private final ConstraintVerifier<VaccinationScheduleConstraintProvider, VaccinationSchedule> constraintVerifier =
            ConstraintVerifier.build(new VaccinationScheduleConstraintProvider(), VaccinationSchedule.class, Injection.class);

    @Test
    void personConflict() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::personConflict)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, VaccineType.PFIZER, BETH),
                        new Injection(3, VACCINATION_CENTER_1, 0, MONDAY_1100, VaccineType.PFIZER, ANN)
                        )
                .penalizesBy(1);
    }

    @Test
    void ageLimitAstrazeneca() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::ageLimitAstrazeneca)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN)
                )
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::ageLimitAstrazeneca)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.ASTRAZENECA, ANN)
                )
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::ageLimitAstrazeneca)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.ASTRAZENECA, BETH)
                )
                .penalizesBy(0);
    }

    @Test
    void secondShotInvalidVaccineType() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotInvalidVaccineType)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, VaccineType.PFIZER, CARL)
                )
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotInvalidVaccineType)
                .given(
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, VaccineType.MODERNA, CARL)
                )
                .penalizesBy(0);
    }

    @Test
    void secondShotMustBeAssigned() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotMustBeAssigned)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, CARL)
                        )
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotMustBeAssigned)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN)
                        )
                .penalizesBy(1);
    }

    @Test
    void assignAllOlderPeople() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::assignAllOlderPeople)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN)
                        )
                .penalizesBy(51 + 41);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::assignAllOlderPeople)
                .given(ANN, BETH, CARL,
                        new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, VaccineType.PFIZER, CARL)
                        )
                .penalizesBy(41);
    }

    @Test
    void secondShotIdealDay() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotIdealDay)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.MODERNA, CARL))
                .penalizesBy(0);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotIdealDay)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, TUESDAY_0900, VaccineType.MODERNA, CARL))
                .penalizesBy(1);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::secondShotIdealDay)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, WEDNESDAY_0900, VaccineType.MODERNA, CARL))
                .penalizesBy(2);
    }

    @Test
    void distanceCost() {
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::distanceCost)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN))
                .penalizesBy((long) Location.METERS_PER_DEGREE);
        constraintVerifier.verifyThat(VaccinationScheduleConstraintProvider::distanceCost)
                .given(new Injection(1, VACCINATION_CENTER_1, 0, MONDAY_0900, VaccineType.PFIZER, ANN),
                        new Injection(2, VACCINATION_CENTER_1, 0, MONDAY_1000, VaccineType.PFIZER, BETH))
                .penalizesBy(3L * (long) Location.METERS_PER_DEGREE);
    }

}
