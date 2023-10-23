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
        return constraintFactory.forEach(Call.class)
                .filter(call -> call.getMissingSkillCount() > 0)
                .penalize(HardSoftScore.ONE_HARD, Call::getMissingSkillCount)
                .asConstraint("No required skills are missing");
    }

    Constraint minimizeWaitingTime(ConstraintFactory constraintFactory) {
        return constraintFactory.forEach(Call.class)
                .filter(call -> call.getNextCall() == null)
                .penalize(HardSoftScore.ONE_SOFT, call -> Math.toIntExact(call.getEstimatedWaiting().getSeconds()
                                * call.getEstimatedWaiting().getSeconds()))
                .asConstraint("Minimize waiting time");
    }
}
