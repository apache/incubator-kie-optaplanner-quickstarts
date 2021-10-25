/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.maintenancescheduling.solver;

import java.time.LocalDate;
import java.util.Set;
import javax.inject.Inject;

import org.acme.maintenancescheduling.domain.Crew;
import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class MaintenanceSchedulingConstraintProviderTest {

    private static final Crew ALPHA_CREW = new Crew(1L, "Alpha crew");
    private static final Crew BETA_CREW = new Crew(2L, "Beta crew");
    private static final LocalDate DAY_1 = LocalDate.of(2021, 2, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2021, 2, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2021, 2, 3);

    @Inject
    ConstraintVerifier<MaintenanceScheduleConstraintProvider, MaintenanceSchedule> constraintVerifier;

    @Test
    public void crewConflict() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW,
                        new Job(1L, "Downtown tunnel", null, null, 1, null, ALPHA_CREW, DAY_1),
                        new Job(2L, "Uptown bridge", null, null, 1, null, ALPHA_CREW, DAY_1))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW,
                        new Job(1L, "Downtown tunnel", null, null, 1, null, ALPHA_CREW, DAY_1),
                        new Job(2L, "Uptown bridge", null, null, 1, null, ALPHA_CREW, DAY_2))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW,
                        new Job(1L, "Downtown tunnel", null, null, 3, null, ALPHA_CREW, DAY_1),
                        new Job(2L, "Uptown bridge", null, null, 3, null, ALPHA_CREW, DAY_2))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::crewConflict)
                .given(ALPHA_CREW, BETA_CREW,
                        new Job(1L, "Downtown tunnel", null, null, 1, null, ALPHA_CREW, DAY_1),
                        new Job(2L, "Uptown bridge", null, null, 1, null, BETA_CREW, DAY_1))
                .penalizesBy(0);
    }

    @Test
    public void readyDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job(1L, "Downtown tunnel", DAY_2, null, 1, null, ALPHA_CREW, DAY_2))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job(1L, "Downtown tunnel", DAY_1, null, 1, null, ALPHA_CREW, DAY_3))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job(1L, "Downtown tunnel", DAY_3, null, 1, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::readyDate)
                .given(new Job(1L, "Downtown tunnel", DAY_3, null, 4, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
    }

    @Test
    public void dueDate() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job(1L, "Downtown tunnel", null, DAY_2, 1, null, ALPHA_CREW, DAY_2))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job(1L, "Downtown tunnel", null, DAY_1, 1, null, ALPHA_CREW, DAY_3))
                .penalizesBy(3);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job(1L, "Downtown tunnel", null, DAY_3, 1, null, ALPHA_CREW, DAY_1))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::dueDate)
                .given(new Job(1L, "Downtown tunnel", null, DAY_3, 4, null, ALPHA_CREW, DAY_1))
                .penalizesBy(2);
    }

    @Test
    public void mutuallyExclusiveTag() {
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveTag)
                .given(
                        new Job(1L, "Downtown tunnel", null, null, 1, Set.of("Downtown"), ALPHA_CREW, DAY_1),
                        new Job(2L, "Downtown bridge", null, null, 1, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_3))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveTag)
                .given(
                        new Job(1L, "Downtown tunnel", null, null, 1, Set.of("Downtown"), ALPHA_CREW, DAY_1),
                        new Job(2L, "Downtown bridge", null, null, 1, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_1))
                .penalizesBy(1);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveTag)
                .given(
                        new Job(1L, "Downtown tunnel", null, null, 1, Set.of("Downtown"), ALPHA_CREW, DAY_1),
                        new Job(2L, "Uptown bridge", null, null, 1, Set.of("Uptown", "Crane"), ALPHA_CREW, DAY_1))
                .penalizesBy(0);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveTag)
                .given(
                        new Job(1L, "Downtown tunnel", null, null, 1, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_2),
                        new Job(2L, "Downtown bridge", null, null, 1, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_2))
                .penalizesBy(2);
        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveTag)
                .given(
                        new Job(1L, "Downtown tunnel", null, null, 5, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_1),
                        new Job(2L, "Downtown bridge", null, null, 3, Set.of("Downtown", "Crane"), ALPHA_CREW, DAY_2))
                .penalizesBy(2 * 3);
    }

}
