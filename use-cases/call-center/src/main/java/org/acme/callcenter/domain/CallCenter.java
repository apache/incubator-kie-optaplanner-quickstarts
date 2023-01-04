package org.acme.callcenter.domain;

import java.util.List;
import java.util.Set;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@PlanningSolution
public class CallCenter {

    @ProblemFactCollectionProperty
    private Set<Skill> skills;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Agent> agents;

    @PlanningEntityCollectionProperty
    @ValueRangeProvider
    private List<Call> calls;

    @PlanningScore
    private HardSoftScore score;

    private boolean solving;

    public CallCenter() {
        // Required by OptaPlanner.
    }

    public CallCenter(Set<Skill> skills, List<Agent> agents, List<Call> calls) {
        this.skills = skills;
        this.agents = agents;
        this.calls = calls;
    }

    public Set<Skill> getSkills() {
        return skills;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public List<Call> getCalls() {
        return calls;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public boolean isSolving() {
        return solving;
    }

    public void setSolving(boolean solving) {
        this.solving = solving;
    }
}
