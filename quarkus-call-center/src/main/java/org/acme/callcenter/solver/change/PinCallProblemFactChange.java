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

package org.acme.callcenter.solver.change;

import java.time.LocalTime;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.ProblemFactChange;

public class PinCallProblemFactChange implements ProblemFactChange<CallCenter> {

    private final Call call;

    public PinCallProblemFactChange(Call call) {
        this.call = call;
    }

    @Override
    public void doChange(ScoreDirector<CallCenter> scoreDirector) {
        Call workingCall = scoreDirector.lookUpWorkingObjectOrReturnNull(call);
        if (workingCall != null) {
            scoreDirector.beforeProblemPropertyChanged(workingCall);
            workingCall.setPinned(true);
            workingCall.setPickUpTime(LocalTime.now());
            scoreDirector.afterProblemPropertyChanged(workingCall);
            scoreDirector.triggerVariableListeners();
        }
    }
}
