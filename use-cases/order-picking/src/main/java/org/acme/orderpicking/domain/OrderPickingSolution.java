/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.acme.orderpicking.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;

@PlanningSolution
public class OrderPickingSolution {

    /**
     * Defines the available Trolleys.
     * 
     * @see TrolleyStep for more information about the model constructed by the Solver.
     */
    @ValueRangeProvider
    @ProblemFactCollectionProperty
    private List<Trolley> trolleyList;

    /**
     * Defines the available TrolleySteps.
     * 
     * @see TrolleyStep for more information about the model constructed by the Solver.
     */
    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    private List<TrolleyStep> trolleyStepList;

    @PlanningScore
    private HardSoftLongScore score;

    public OrderPickingSolution() {
        // Marshalling constructor
    }

    public OrderPickingSolution(List<Trolley> trolleyList, List<TrolleyStep> trolleyStepList) {
        this.trolleyList = trolleyList;
        this.trolleyStepList = trolleyStepList;
    }

    public List<Trolley> getTrolleyList() {
        return trolleyList;
    }

    public void setTrolleyList(List<Trolley> trolleyList) {
        this.trolleyList = trolleyList;
    }

    public List<TrolleyStep> getTrolleyStepList() {
        return trolleyStepList;
    }

    public void setTrolleyStepList(List<TrolleyStep> trolleyStepList) {
        this.trolleyStepList = trolleyStepList;
    }

    public HardSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardSoftLongScore score) {
        this.score = score;
    }
}
