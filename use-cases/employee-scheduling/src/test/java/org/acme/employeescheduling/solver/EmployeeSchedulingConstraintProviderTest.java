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

package org.acme.employeescheduling.solver;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import javax.inject.Inject;

import org.acme.employeescheduling.domain.Availability;
import org.acme.employeescheduling.domain.AvailabilityType;
import org.acme.employeescheduling.domain.Employee;
import org.acme.employeescheduling.domain.EmployeeSchedule;
import org.acme.employeescheduling.domain.Shift;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class EmployeeSchedulingConstraintProviderTest {
    private static final LocalDate DAY_1 = LocalDate.of(2021, 2, 1);
    private static final LocalDate DAY_2 = LocalDate.of(2021, 2, 2);
    private static final LocalDate DAY_3 = LocalDate.of(2021, 2, 3);

    LocalDateTime DAY_START_TIME = DAY_1.atTime(LocalTime.of(9, 0));
    LocalDateTime DAY_END_TIME = DAY_1.atTime(LocalTime.of(17, 0));
    LocalDateTime AFTERNOON_START_TIME = DAY_1.atTime(LocalTime.of(13, 0));
    LocalDateTime AFTERNOON_END_TIME = DAY_1.atTime(LocalTime.of(21, 0));

    @Inject
    ConstraintVerifier<EmployeeSchedulingConstraintProvider, EmployeeSchedule> constraintVerifier;

    @Test
    public void testRequiredSkill() {
        Employee employee = new Employee("Amy", Set.of());
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::requiredSkill)
                .given(employee,
                       new Shift(DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee))
                .penalizes(1);

        employee = new Employee("Beth", Set.of("Skill"));
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::requiredSkill)
                .given(employee,
                       new Shift(DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee))
                .penalizes(0);
    }

    @Test
    public void testOverlappingShifts() {
        Employee employee1 = new Employee("Amy", Set.of("Skill"));
        Employee employee2 = new Employee("Beth", Set.of("Skill"));
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee1))
                .penalizesBy((int) Duration.ofHours(8).toMinutes());

        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee2))
                .penalizes(0);

        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, AFTERNOON_START_TIME, AFTERNOON_END_TIME, "Location 2", "Skill", employee1))
                .penalizesBy((int) Duration.ofHours(4).toMinutes());
    }

    @Test
    public void testOneShiftPerDay() {
        Employee employee1 = new Employee("Amy", Set.of("Skill"));
        Employee employee2 = new Employee("Beth", Set.of("Skill"));
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee1))
                .penalizes(1);

        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, DAY_START_TIME, DAY_END_TIME, "Location 2", "Skill", employee2))
                .penalizes(0);

        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, AFTERNOON_START_TIME, AFTERNOON_END_TIME, "Location 2", "Skill", employee1))
                .penalizes(1);

        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::noOverlappingShifts)
                .given(employee1,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1),
                       new Shift(2L, DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location 2", "Skill", employee1))
                .penalizes(0);
    }

    @Test
    public void testUnavailableEmployee() {
        Employee employee1 = new Employee("Amy", Set.of("Skill"));
        Employee employee2 = new Employee("Beth", Set.of("Skill"));
        Availability unavailability = new Availability(employee1, DAY_1, AvailabilityType.UNAVAILABLE);
        Availability desired = new Availability(employee1, DAY_1, AvailabilityType.DESIRED);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::unavailableEmployee)
                .given(employee1,
                       unavailability,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
                .penalizesBy((int) Duration.ofHours(8).toMinutes());
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::unavailableEmployee)
                .given(employee1,
                       unavailability,
                       new Shift(1L, DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location", "Skill", employee1))
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::unavailableEmployee)
                .given(employee1,
                       unavailability,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee2))
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::unavailableEmployee)
                .given(employee1,
                       desired,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
                .penalizes(0);
    }

    @Test
    public void testDesiredDayForEmployee() {
        Employee employee1 = new Employee("Amy", Set.of("Skill"));
        Employee employee2 = new Employee("Beth", Set.of("Skill"));
        Availability unavailability = new Availability(employee1, DAY_1, AvailabilityType.UNAVAILABLE);
        Availability desired = new Availability(employee1, DAY_1, AvailabilityType.DESIRED);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::desiredDayForEmployee)
                .given(employee1,
                       desired,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
                .rewardsWith((int) Duration.ofHours(8).toMinutes());
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::desiredDayForEmployee)
                .given(employee1,
                       desired,
                       new Shift(1L, DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location", "Skill", employee1))
                .rewards(0);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::desiredDayForEmployee)
                .given(employee1,
                       desired,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee2))
                .rewards(0);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::desiredDayForEmployee)
                .given(employee1,
                       unavailability,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
                .rewards(0);
    }

    @Test
    public void testUndesiredDayForEmployee() {
        Employee employee1 = new Employee("Amy", Set.of("Skill"));
        Employee employee2 = new Employee("Beth", Set.of("Skill"));
        Availability unavailability = new Availability(employee1, DAY_1, AvailabilityType.UNAVAILABLE);
        Availability undesired = new Availability(employee1, DAY_1, AvailabilityType.UNDESIRED);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::undesiredDayForEmployee)
                .given(employee1,
                       undesired,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
                .penalizesBy((int) Duration.ofHours(8).toMinutes());
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::undesiredDayForEmployee)
                .given(employee1,
                       undesired,
                       new Shift(1L, DAY_START_TIME.plusDays(1), DAY_END_TIME.plusDays(1), "Location", "Skill", employee1))
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::undesiredDayForEmployee)
                .given(employee1,
                       undesired,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee2))
                .penalizes(0);
        constraintVerifier.verifyThat(EmployeeSchedulingConstraintProvider::undesiredDayForEmployee)
                .given(employee1,
                       unavailability,
                       new Shift(1L, DAY_START_TIME, DAY_END_TIME, "Location", "Skill", employee1))
                .penalizes(0);
    }
}
