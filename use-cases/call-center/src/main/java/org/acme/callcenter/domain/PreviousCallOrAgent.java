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
