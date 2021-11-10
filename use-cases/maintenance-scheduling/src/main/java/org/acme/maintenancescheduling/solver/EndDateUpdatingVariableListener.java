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

package org.acme.maintenancescheduling.solver;

import java.time.LocalDate;

import org.acme.maintenancescheduling.domain.Job;
import org.acme.maintenancescheduling.domain.MaintenanceSchedule;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public class EndDateUpdatingVariableListener implements VariableListener<MaintenanceSchedule, Job> {

    @Override
    public void beforeEntityAdded(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    @Override
    public void afterEntityAdded(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        updateEndDate(scoreDirector, job);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    @Override
    public void afterVariableChanged(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        updateEndDate(scoreDirector, job);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    @Override
    public void afterEntityRemoved(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        // Do nothing
    }

    protected void updateEndDate(ScoreDirector<MaintenanceSchedule> scoreDirector, Job job) {
        scoreDirector.beforeVariableChanged(job, "endDate");
        job.setEndDate(calculateEndDate(job.getStartDate(), job.getDurationInDays()));
        scoreDirector.afterVariableChanged(job, "endDate");
    }

    public static LocalDate calculateEndDate(LocalDate startDate, int durationInDays) {
        if (startDate == null) {
            return null;
        } else {
            // Skip weekends. Does not work for holidays.
            // To skip holidays too, cache all working days in scoreDirector.getWorkingSolution().getWorkCalendar().
            // Keep in sync with MaintenanceSchedule.createStartDateList().
            int weekendPadding = 2 * ((durationInDays + (startDate.getDayOfWeek().getValue() - 1)) / 5);
            return startDate.plusDays(durationInDays + weekendPadding);
        }
    }

}
