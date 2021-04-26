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

    // Inclusive: a 55 year old can get a minimumAge=55 vaccine
    private Integer minimumAge;
    // Inclusive: a 55 year old can get a maximumAge=55 vaccine
    private Integer maximumAge;

    // No-arg constructor required for Jackson
    public VaccineType() {}

    public VaccineType(String name) {
        this(name, null, null);
    }

    public VaccineType(String name, Integer minimumAge, Integer maximumAge) {
        this.name = name;
        this.minimumAge = minimumAge;
        this.maximumAge = maximumAge;
        if (minimumAge != null && maximumAge != null && minimumAge > maximumAge) {
            throw new IllegalArgumentException("The minimumAge (" + minimumAge
                    + ") cannot be higher than the maximumAge (" + maximumAge + ").");
        }
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

    public Integer getMinimumAge() {
        return minimumAge;
    }

    public Integer getMaximumAge() {
        return maximumAge;
    }

}
