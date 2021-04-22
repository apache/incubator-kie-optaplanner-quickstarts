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

import javax.inject.Inject;

import io.quarkus.test.junit.QuarkusTest;
import org.acme.maintenancescheduling.domain.MaintainableUnit;
import org.acme.maintenancescheduling.domain.MaintenanceCrew;
import org.acme.maintenancescheduling.domain.MaintenanceJob;
import org.acme.maintenancescheduling.domain.MaintenanceJobAssignment;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.acme.maintenancescheduling.domain.MutuallyExclusiveJobs;
import org.acme.maintenancescheduling.domain.TimeGrain;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

@QuarkusTest
public class MaintenanceSchedulingConstraintProviderTest {

    @Inject
    ConstraintVerifier<MaintenanceScheduleConstraintProvider, MaintenanceSchedule> constraintVerifier;

    @Test
    public void jobsMustStartAfterReadyTimeGrainUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::jobsMustStartAfterReadyTimeGrain)
                .given(maintenanceJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void jobsMustStartAfterReadyTimeGrainPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 2, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::jobsMustStartAfterReadyTimeGrain)
                .given(maintenanceJobAssignment)
                .penalizesBy(2);
    }

    @Test
    public void jobsMustFinishBeforeDueTimeUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::jobsMustFinishBeforeDueTime)
                .given(maintenanceJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void jobsMustFinishBeforeDueTimePenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(8);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::jobsMustFinishBeforeDueTime)
                .given(maintenanceJobAssignment)
                .penalizesBy(2);
    }

    @Test
    public void assignAllCriticalJobsUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, false);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::assignAllCriticalJobs)
                .given(maintenanceJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void assignAllCriticalJobsPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::assignAllCriticalJobs)
                .given(maintenanceJobAssignment)
                .penalizesBy(1);
    }

    @Test
    public void oneJobPerCrewPerPeriodUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);
        MaintenanceCrew maintenanceCrew = new MaintenanceCrew("Maintenance crew");
        maintenanceJobAssignment.setAssignedCrew(maintenanceCrew);
        maintenanceJobAssignment.setId(0L);

        MaintainableUnit otherUnit = new MaintainableUnit("Other unit");
        MaintenanceJob otherJob = new MaintenanceJob("Other job", otherUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment otherJobAssignment = new MaintenanceJobAssignment(otherJob);
        TimeGrain otherTimeGrain = new TimeGrain(4);
        otherJobAssignment.setStartingTimeGrain(otherTimeGrain);
        otherJobAssignment.setAssignedCrew(maintenanceCrew);
        otherJobAssignment.setId(1L);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::oneJobPerCrewPerPeriod)
                .given(maintenanceJobAssignment, otherJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void oneJobPerCrewPerPeriodPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);
        MaintenanceCrew maintenanceCrew = new MaintenanceCrew("Maintenance crew");
        maintenanceJobAssignment.setAssignedCrew(maintenanceCrew);
        maintenanceJobAssignment.setId(0L);

        MaintainableUnit otherUnit = new MaintainableUnit("Other unit");
        MaintenanceJob otherJob = new MaintenanceJob("Other job", otherUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment otherJobAssignment = new MaintenanceJobAssignment(otherJob);
        TimeGrain otherTimeGrain = new TimeGrain(2);
        otherJobAssignment.setStartingTimeGrain(otherTimeGrain);
        otherJobAssignment.setAssignedCrew(maintenanceCrew);
        otherJobAssignment.setId(1L);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::oneJobPerCrewPerPeriod)
                .given(maintenanceJobAssignment, otherJobAssignment)
                .penalizesBy(2);
    }

    @Test
    public void mutuallyExclusiveJobsUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);
        maintenanceJobAssignment.setId(0L);

        MaintainableUnit otherUnit = new MaintainableUnit("Other unit");
        MaintenanceJob otherJob = new MaintenanceJob("Other job", otherUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment otherJobAssignment = new MaintenanceJobAssignment(otherJob);
        TimeGrain otherTimeGrain = new TimeGrain(2);
        otherJobAssignment.setStartingTimeGrain(otherTimeGrain);
        otherJobAssignment.setId(1L);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveJobs)
                .given(maintenanceJobAssignment, otherJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void mutuallyExclusiveJobsPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        MaintenanceCrew maintenanceCrew = new MaintenanceCrew("Maintenance crew");
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setAssignedCrew(maintenanceCrew);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);
        maintenanceJobAssignment.setId(0L);

        MaintainableUnit otherUnit = new MaintainableUnit("Other unit");
        MaintenanceJob otherJob = new MaintenanceJob("Other job", otherUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment otherJobAssignment = new MaintenanceJobAssignment(otherJob);
        TimeGrain otherTimeGrain = new TimeGrain(2);
        otherJobAssignment.setAssignedCrew(maintenanceCrew);
        otherJobAssignment.setStartingTimeGrain(otherTimeGrain);
        otherJobAssignment.setId(1L);

        MutuallyExclusiveJobs mutuallyExclusiveJobs =
                new MutuallyExclusiveJobs("Exclusive tag", maintenanceJobAssignment.getMaintenanceJob(), otherJobAssignment.getMaintenanceJob());

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::mutuallyExclusiveJobs)
                .given(maintenanceJobAssignment, otherJobAssignment, mutuallyExclusiveJobs)
                .penalizesBy(2);
    }

    @Test
    public void oneJobPerUnitPerPeriodUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        MaintenanceCrew maintenanceCrew = new MaintenanceCrew("Maintenance crew");
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);
        maintenanceJobAssignment.setAssignedCrew(maintenanceCrew);
        maintenanceJobAssignment.setId(0L);

        MaintainableUnit otherUnit = new MaintainableUnit("Test unit");
        MaintenanceJob otherJob = new MaintenanceJob("Other job", otherUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment otherJobAssignment = new MaintenanceJobAssignment(otherJob);
        TimeGrain otherTimeGrain = new TimeGrain(0);
        otherJobAssignment.setStartingTimeGrain(otherTimeGrain);
        otherJobAssignment.setAssignedCrew(maintenanceCrew);
        otherJobAssignment.setId(1L);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::oneJobPerUnitPerPeriod)
                .given(maintenanceJobAssignment, otherJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void oneJobPerUnitPerPeriodPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        MaintenanceCrew maintenanceCrew = new MaintenanceCrew("Maintenance crew");
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setAssignedCrew(maintenanceCrew);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);
        maintenanceJobAssignment.setId(0L);

        MaintenanceJob otherJob = new MaintenanceJob("Other job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment otherJobAssignment = new MaintenanceJobAssignment(otherJob);
        TimeGrain otherTimeGrain = new TimeGrain(2);
        otherJobAssignment.setAssignedCrew(maintenanceCrew);
        otherJobAssignment.setStartingTimeGrain(otherTimeGrain);
        otherJobAssignment.setId(1L);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::oneJobPerUnitPerPeriod)
                .given(maintenanceJobAssignment, otherJobAssignment)
                .penalizesBy(2);
    }

    @Test
    public void assignAllNonCriticalJobsUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::assignAllNonCriticalJobs)
                .given(maintenanceJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void assignAllNonCriticalJobsPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, false);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::assignAllNonCriticalJobs)
                .given(maintenanceJobAssignment)
                .penalizesBy(1);
    }

    @Test
    public void jobsShouldFinishBeforeSafetyMarginUnpenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(0);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::jobsShouldFinishBeforeSafetyMargin)
                .given(maintenanceJobAssignment)
                .penalizesBy(0);
    }

    @Test
    public void jobsShouldFinishBeforeSafetyMarginPenalized() {
        MaintainableUnit maintainableUnit = new MaintainableUnit("Test unit");
        MaintenanceJob maintenanceJob = new MaintenanceJob("Maintenance job", maintainableUnit, 0, 10, 4, 2, true);
        MaintenanceJobAssignment maintenanceJobAssignment = new MaintenanceJobAssignment(maintenanceJob);
        TimeGrain startingTimeGrain = new TimeGrain(6);
        maintenanceJobAssignment.setStartingTimeGrain(startingTimeGrain);

        constraintVerifier.verifyThat(MaintenanceScheduleConstraintProvider::jobsShouldFinishBeforeSafetyMargin)
                .given(maintenanceJobAssignment)
                .penalizesBy(4);
    }
}
