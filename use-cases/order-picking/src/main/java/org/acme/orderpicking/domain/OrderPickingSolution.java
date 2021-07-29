/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.acme.orderpicking.domain;

import java.util.List;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardmediumsoftlong.HardMediumSoftLongScore;

import static org.acme.orderpicking.domain.PathElement.TROLLEY_RANGE;
import static org.acme.orderpicking.domain.PathElement.TROLLEY_STEP_RANGE;

@PlanningSolution
public class OrderPickingSolution {

    @ValueRangeProvider(id = TROLLEY_RANGE)
    @ProblemFactCollectionProperty
    private List<Trolley> trolleyList;

    @ValueRangeProvider(id = TROLLEY_STEP_RANGE)
    @PlanningEntityCollectionProperty
    private List<TrolleyStep> trolleyStepList;

    @PlanningScore
    private HardMediumSoftLongScore score;

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

    public HardMediumSoftLongScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftLongScore score) {
        this.score = score;
    }
}
