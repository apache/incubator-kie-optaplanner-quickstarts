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

package org.acme.maintenancescheduling.domain;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Set;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@Entity
public class Job {

    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private LocalDate readyDate; // Inclusive
    private LocalDate dueDate; // Exclusive
    private int durationInDays;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> mutuallyExclusiveTagSet;

    // @PlanningVariable annotations on the getters so setStartDate() can adjust endDate too
    @ManyToOne
    private Crew crew;
    private LocalDate startDate; // Inclusive
    private LocalDate endDate; // Exclusive

    // No-arg constructor required for Hibernate and OptaPlanner
    public Job() {
    }

    public Job(String name, LocalDate readyDate, LocalDate dueDate, int durationInDays, Set<String> mutuallyExclusiveTagSet) {
        this.name = name;
        this.readyDate = readyDate;
        this.dueDate = dueDate;
        this.durationInDays = durationInDays;
        this.mutuallyExclusiveTagSet = mutuallyExclusiveTagSet;
    }

    public Job(Long id, String name, LocalDate readyDate, LocalDate dueDate, int durationInDays, Set<String> mutuallyExclusiveTagSet,
            Crew crew, LocalDate startDate) {
        this.id = id;
        this.name = name;
        this.readyDate = readyDate;
        this.dueDate = dueDate;
        this.durationInDays = durationInDays;
        this.mutuallyExclusiveTagSet = mutuallyExclusiveTagSet;
        this.crew = crew;
        setStartDate(startDate);
    }

    @Override
    public String toString() {
        return name + "(" + id + ")";
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    @PlanningId
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getReadyDate() {
        return readyDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getDurationInDays() {
        return durationInDays;
    }

    public Set<String> getMutuallyExclusiveTagSet() {
        return mutuallyExclusiveTagSet;
    }

    @PlanningVariable(valueRangeProviderRefs = {"crewRange"})
    public Crew getCrew() {
        return crew;
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    // Follows the TimeGrain Design Pattern
    @PlanningVariable(valueRangeProviderRefs = {"startDateRange"})
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        if (startDate == null) {
            endDate = null;
        } else {
            int weekendPadding = 2 * ((durationInDays + (startDate.getDayOfWeek().getValue() - 1)) / 5);
            endDate = startDate.plusDays(durationInDays + weekendPadding);
        }
    }

    public LocalDate getEndDate() {
        return endDate;
    }

}
