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

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.InverseRelationShadowVariable;

/**
 * Base class for implementing the CHAINED graph modelling strategy.
 * 
 * @see Trolley
 * @see TrolleyStep
 */
@PlanningEntity
public abstract class TrolleyOrTrolleyStep {

    public static final String PREVIOUS_ELEMENT = "previousElement";

    /**
     * Shadow variable: Is automatically set by the Solver and facilitates that all the elements in the chain, the
     * Trolley and the TrolleyStep, can have a reference to the next element in that chain.
     */
    @InverseRelationShadowVariable(sourceVariableName = PREVIOUS_ELEMENT)
    protected TrolleyStep nextElement;

    protected TrolleyOrTrolleyStep() {
        //marshalling constructor
    }

    public abstract WarehouseLocation getLocation();

    public TrolleyStep getNextElement() {
        return nextElement;
    }

    public void setNextElement(TrolleyStep nextElement) {
        this.nextElement = nextElement;
    }
}
