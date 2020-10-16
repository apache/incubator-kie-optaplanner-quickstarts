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

package org.acme.factoriolayout.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.entity.PlanningPin;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Assembly {

    private String id;

    @JsonIdentityReference(alwaysAsId = true)
    private Recipe recipe;

    @JsonIdentityReference(alwaysAsId = true)
    private List<Assembly> inputAssemblyList = new ArrayList<>();

    @PlanningPin
    private boolean pinned = false;
    @PlanningVariable(valueRangeProviderRefs = "areaRange")
    private Area area = null;

    // No-arg constructor required for Jackson and OptaPlanner
    public Assembly() {
    }

    public Assembly(String id, Recipe recipe) {
        this.id = id;
        this.recipe = recipe;
    }

    @Override
    public String toString() {
        return recipe.getName();
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public String getId() {
        return id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public List<Assembly> getInputAssemblyList() {
        return inputAssemblyList;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

}
