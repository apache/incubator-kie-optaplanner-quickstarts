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

package org.acme.vaccinationscheduler.domain;

import java.time.LocalDate;

import org.optaplanner.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

public class Person {

    @PlanningId
    private Long id;

    private String name;
    private Location homeLocation;
    private LocalDate birthdate;
    private int age;

    private boolean firstDoseInjected;
    @JsonIdentityReference(alwaysAsId = true)
    private VaccineType firstDoseVaccineType;
    private LocalDate firstDoseDate;

    public Person(long id, String name, Location homeLocation, LocalDate birthdate, int age) {
        this(id, name, homeLocation, birthdate, age, false, null, null);
    }

    public Person(long id, String name, Location homeLocation, LocalDate birthdate, int age,
            boolean firstDoseInjected, VaccineType firstDoseVaccineType, LocalDate firstDoseDate) {
        this.id = id;
        this.name = name;
        this.homeLocation = homeLocation;
        this.birthdate = birthdate;
        this.age = age;
        this.firstDoseInjected = firstDoseInjected;
        this.firstDoseVaccineType = firstDoseVaccineType;
        this.firstDoseDate = firstDoseDate;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getHomeLocation() {
        return homeLocation;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public int getAge() {
        return age;
    }

    public boolean isFirstDoseInjected() {
        return firstDoseInjected;
    }

    public VaccineType getFirstDoseVaccineType() {
        return firstDoseVaccineType;
    }

    public LocalDate getFirstDoseDate() {
        return firstDoseDate;
    }

}
