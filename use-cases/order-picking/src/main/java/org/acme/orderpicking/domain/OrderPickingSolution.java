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
