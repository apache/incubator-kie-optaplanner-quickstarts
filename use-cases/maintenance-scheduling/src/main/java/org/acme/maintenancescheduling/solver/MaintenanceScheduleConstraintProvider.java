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

import static org.optaplanner.core.api.score.stream.Joiners.equal;
import static org.optaplanner.core.api.score.stream.Joiners.filtering;
import static org.optaplanner.core.api.score.stream.Joiners.lessThan;

import org.acme.maintenancescheduling.domain.MaintenanceJobAssignment;
import org.acme.maintenancescheduling.domain.MutuallyExclusiveJobs;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class MaintenanceScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                // Hard constraints
                jobsMustStartAfterReadyTimeGrain(constraintFactory),
                jobsMustFinishBeforeDueTime(constraintFactory),
                assignAllCriticalJobs(constraintFactory),
                oneJobPerCrewPerPeriod(constraintFactory),
                mutuallyExclusiveJobs(constraintFactory),
                oneJobPerUnitPerPeriod(constraintFactory),
                // Soft constraints
                assignAllNonCriticalJobs(constraintFactory),
                jobsShouldFinishBeforeSafetyMargin(constraintFactory)
        };
    }

    // ************************************************************************
    // Hard constraints
    // ************************************************************************

    public Constraint jobsMustStartAfterReadyTimeGrain(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain() != null
                        && maintenanceJobAssignment.getStartingTimeGrain().getGrainIndex() < maintenanceJobAssignment.getMaintenanceJob().getReadyTimeGrainIndex())
                .penalizeConfigurable("Jobs must start after ready time grain",
                        maintenanceJobAssignment -> maintenanceJobAssignment.getMaintenanceJob().getReadyTimeGrainIndex()
                                - maintenanceJobAssignment.getStartingTimeGrain().getGrainIndex());
    }

    public Constraint jobsMustFinishBeforeDueTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain() != null
                        && maintenanceJobAssignment.getStartingTimeGrain().getGrainIndex()
                                + maintenanceJobAssignment.getMaintenanceJob().getDurationInGrains() > maintenanceJobAssignment.getMaintenanceJob().getDueTimeGrainIndex())
                .penalizeConfigurable("Jobs must finish before due time",
                        maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain().getGrainIndex()
                                + maintenanceJobAssignment.getMaintenanceJob().getDurationInGrains() - maintenanceJobAssignment.getMaintenanceJob().getDueTimeGrainIndex());
    }

    public Constraint assignAllCriticalJobs(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                // Critical maintenance jobs must be assigned a crew and start period
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getMaintenanceJob().isCritical() && (maintenanceJobAssignment.getAssignedCrew() == null
                        || maintenanceJobAssignment.getStartingTimeGrain() == null))
                .penalizeConfigurable("Assign all critical jobs");
    }

    public Constraint oneJobPerCrewPerPeriod(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain() != null
                        && maintenanceJobAssignment.getAssignedCrew() != null)
                .join(MaintenanceJobAssignment.class,
                        equal(MaintenanceJobAssignment::getAssignedCrew),
                        lessThan(MaintenanceJobAssignment::getId),
                        filtering((maintenanceJobAssignment, otherJob) -> maintenanceJobAssignment.calculateOverlap(otherJob) > 0))
                .penalizeConfigurable("One job per crew per period", MaintenanceJobAssignment::calculateOverlap);
    }

    public Constraint mutuallyExclusiveJobs(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain() != null)
                .join(MaintenanceJobAssignment.class,
                        lessThan(MaintenanceJobAssignment::getId),
                        filtering((maintenanceJobAssignment, otherJobAssignment) -> maintenanceJobAssignment.calculateOverlap(otherJobAssignment) > 0))
                .join(MutuallyExclusiveJobs.class,
                        filtering((maintenanceJobAssignment, otherJobAssignment, mutuallyExclusiveJobs) ->
                        mutuallyExclusiveJobs.isMutuallyExclusive(maintenanceJobAssignment.getMaintenanceJob(), otherJobAssignment.getMaintenanceJob())))
                .penalizeConfigurable("Mutually exclusive jobs cannot overlap",
                        (maintenanceJobAssignment, otherJobAssignment, mutuallyExclusiveJobs) -> maintenanceJobAssignment.calculateOverlap(otherJobAssignment));
    }

    public Constraint oneJobPerUnitPerPeriod(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain() != null)
                .join(MaintenanceJobAssignment.class,
                        equal(maintenanceJobAssignment -> maintenanceJobAssignment.getMaintenanceJob().getMaintainableUnit()),
                        lessThan(MaintenanceJobAssignment::getId),
                        filtering((maintenanceJobAssignment, otherJobAssignment) -> maintenanceJobAssignment.calculateOverlap(otherJobAssignment) > 0))
                .penalizeConfigurable("One job per unit per period", MaintenanceJobAssignment::calculateOverlap);
    }

    // TODO: Add constraint that prevents specific unit from being maintained at certain period (outside of MVP)

    // ************************************************************************
    // Soft constraints
    // ************************************************************************

    public Constraint assignAllNonCriticalJobs(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                // Non critical maintenance jobs must be assigned a crew and start period
                .filter(maintenanceJobAssignment -> !maintenanceJobAssignment.getMaintenanceJob().isCritical()
                        && (maintenanceJobAssignment.getAssignedCrew() == null || maintenanceJobAssignment.getStartingTimeGrain() == null))
                .penalizeConfigurable("Assign all non critical jobs");
    }

    public Constraint jobsShouldFinishBeforeSafetyMargin(ConstraintFactory constraintFactory) {
        return constraintFactory.forEachIncludingNullVars(MaintenanceJobAssignment.class)
                .filter(maintenanceJobAssignment -> maintenanceJobAssignment.getStartingTimeGrain() != null
                        && maintenanceJobAssignment.calculateSafetyMarginPenalty() > 0)
                .penalizeConfigurable("Jobs should finish before safety margin",
                        MaintenanceJobAssignment::calculateSafetyMarginPenalty);
    }

}
