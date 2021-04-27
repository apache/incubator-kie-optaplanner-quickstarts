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

package org.acme.callcenter.domain;

import java.time.Duration;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.lookup.PlanningId;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public abstract class PreviousCallOrAgent {

    private Long id;

    @JsonIgnore
    @InverseRelationShadowVariable(sourceVariableName = "previousCallOrAgent")
    protected Call nextCall;

    public PreviousCallOrAgent() {
        // Required by OptaPlanner.
    }

    public PreviousCallOrAgent(long id) {
        this.id = id;
    }

    public Call getNextCall() {
        return nextCall;
    }

    public void setNextCall(Call nextCall) {
        this.nextCall = nextCall;
    }

    public abstract Duration getDurationTillPickUp();

    @PlanningId
    public Long getId() {
        return id;
    }
}
