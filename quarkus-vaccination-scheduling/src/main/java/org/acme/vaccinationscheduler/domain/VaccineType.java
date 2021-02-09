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

package org.acme.vaccinationscheduler.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "name")
public class VaccineType {

    private String name;

    // For example 19 days for Pfizer
    private int secondDoseReadyDays;
    // For example 21 days for Pfizer
    private int secondDoseIdealDays;

    // For example 55 for AstraZeneca
    private Integer maximumAge;

    public VaccineType(String name, int secondDoseReadyDays, int secondDoseIdealDays) {
        this(name, secondDoseReadyDays, secondDoseIdealDays, null);
    }

    public VaccineType(String name, int secondDoseReadyDays, int secondDoseIdealDays, Integer maximumAge) {
        this.name = name;
        this.secondDoseReadyDays = secondDoseReadyDays;
        this.secondDoseIdealDays = secondDoseIdealDays;
        this.maximumAge = maximumAge;
    }

    public boolean isOkForMaximumAge(int age) {
        return maximumAge == null || age <= maximumAge;
    }

    @Override
    public String toString() {
        return name;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getName() {
        return name;
    }

    public int getSecondDoseReadyDays() {
        return secondDoseReadyDays;
    }

    public int getSecondDoseIdealDays() {
        return secondDoseIdealDays;
    }

    public Integer getMaximumAge() {
        return maximumAge;
    }

}
