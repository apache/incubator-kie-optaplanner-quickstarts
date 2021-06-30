package org.acme.callcenter.solver;

import org.acme.callcenter.domain.Call;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class CallCenterConstraintsProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[] {
                noRequiredSkillMissing(constraintFactory),
                minimizeWaitingTime(constraintFactory),
        };
    }

    Constraint noRequiredSkillMissing(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Call.class)
                .filter(call -> call.getMissingSkillCount() > 0)
                .penalize("No required skills are missing", HardSoftScore.ONE_HARD, call -> call.getMissingSkillCount());
    }

    Constraint minimizeWaitingTime(ConstraintFactory constraintFactory) {
        return constraintFactory.from(Call.class)
                .filter(call -> call.getNextCall() == null)
                .penalize("Minimize waiting time",
                        HardSoftScore.ONE_SOFT, call -> Math.toIntExact(call.getEstimatedWaiting().getSeconds()
                                * call.getEstimatedWaiting().getSeconds()));
    }
}
