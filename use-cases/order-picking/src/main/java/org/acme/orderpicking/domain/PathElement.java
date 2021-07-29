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

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

@PlanningEntity
public abstract class PathElement {

    public static final String TROLLEY_RANGE = "trolleyRange";
    public static final String TROLLEY_STEP_RANGE = "trolleyStepRange";
    public static final String PREVIOUS_ELEMENT = "previousElement";

    protected Location location;

    @InverseRelationShadowVariable(sourceVariableName = PREVIOUS_ELEMENT)
    protected TrolleyStep nextElement;

    protected PathElement() {
        //marshalling constructor
    }

    protected PathElement(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public TrolleyStep getNextElement() {
        return nextElement;
    }

    public void setNextElement(TrolleyStep nextElement) {
        this.nextElement = nextElement;
    }
}
