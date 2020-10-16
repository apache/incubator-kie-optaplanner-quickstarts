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

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.core.api.solver.SolverStatus;

@PlanningSolution
public class FactorioLayout {

    @ProblemFactCollectionProperty
    private List<Recipe> recipeList;
    @ProblemFactCollectionProperty
    private List<Requirement> requirementList;
    @ProblemFactCollectionProperty
    @ValueRangeProvider(id = "areaRange")
    private List<Area> areaList;
    @PlanningEntityCollectionProperty
    private List<Assembly> assemblyList;

    @PlanningScore
    private HardSoftLongScore score;

    // Ignored by OptaPlanner, used by the UI to display solve or stop solving button
    private SolverStatus solverStatus;

    // No-arg constructor required for OptaPlanner
    public FactorioLayout() {
    }

    public FactorioLayout(List<Recipe> recipeList, List<Requirement> requirementList, List<Area> areaList, List<Assembly> assemblyList) {
        this.recipeList = recipeList;
        this.requirementList = requirementList;
        this.areaList = areaList;
        this.assemblyList = assemblyList;
    }
    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public List<Recipe> getRecipeList() {
        return recipeList;
    }

    public List<Requirement> getRequirementList() {
        return requirementList;
    }

    public List<Area> getAreaList() {
        return areaList;
    }

    public List<Assembly> getAssemblyList() {
        return assemblyList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }

}
