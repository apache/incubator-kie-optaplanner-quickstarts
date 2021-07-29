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
import org.optaplanner.core.api.domain.variable.AnchorShadowVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.api.domain.variable.PlanningVariableGraphType;

@PlanningEntity
public class TrolleyStep extends PathElement {

    private OrderItem orderItem;

    @PlanningVariable(valueRangeProviderRefs = { TROLLEY_RANGE, TROLLEY_STEP_RANGE },
            graphType = PlanningVariableGraphType.CHAINED)
    private PathElement previousElement;

    /**
     * Shadow variable, let all trolley steps have a reference to the chain anchor.
     */
    @AnchorShadowVariable(sourceVariableName = PREVIOUS_ELEMENT)
    private Trolley trolley;

    public TrolleyStep() {
        //marshaling constructor.
    }

    public TrolleyStep(OrderItem orderItem, Location location) {
        super(location);
        this.orderItem = orderItem;
    }

    public OrderItem getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(OrderItem orderItem) {
        this.orderItem = orderItem;
    }

    public PathElement getPreviousElement() {
        return previousElement;
    }

    public void setPreviousElement(PathElement previousElement) {
        this.previousElement = previousElement;
    }

    public Trolley getTrolley() {
        return trolley;
    }

    public void setTrolley(Trolley trolley) {
        this.trolley = trolley;
    }

    public boolean isLast() {
        return nextElement == null;
    }
}
