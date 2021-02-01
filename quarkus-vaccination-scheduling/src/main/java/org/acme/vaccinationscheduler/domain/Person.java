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

public class Person {

    @PlanningId
    private Long id;

    private String name;
    private Location homeLocation;
    private LocalDate birthdate;
    private int age;

    private boolean firstShotInjected;
    private VaccineType firstShotVaccineType;
    private LocalDate secondShotIdealDate;

    public Person(Long id, String name, Location homeLocation, LocalDate birthdate, int age,
            boolean firstShotInjected, VaccineType firstShotVaccineType, LocalDate secondShotIdealDate) {
        this.id = id;
        this.name = name;
        this.homeLocation = homeLocation;
        this.birthdate = birthdate;
        this.age = age;
        this.firstShotInjected = firstShotInjected;
        this.firstShotVaccineType = firstShotVaccineType;
        this.secondShotIdealDate = secondShotIdealDate;
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

    public boolean isFirstShotInjected() {
        return firstShotInjected;
    }

    public VaccineType getFirstShotVaccineType() {
        return firstShotVaccineType;
    }

    public LocalDate getSecondShotIdealDate() {
        return secondShotIdealDate;
    }
}
