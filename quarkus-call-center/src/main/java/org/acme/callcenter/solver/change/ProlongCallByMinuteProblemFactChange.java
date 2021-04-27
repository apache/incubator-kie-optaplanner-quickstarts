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

import java.time.Duration;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.optaplanner.core.api.score.director.ScoreDirector;
import org.optaplanner.core.api.solver.ProblemFactChange;

public class ProlongCallByMinuteProblemFactChange implements ProblemFactChange<CallCenter> {

    private static final Duration PROLONGATION = Duration.ofMinutes(1L);
    private final long callId;

    public ProlongCallByMinuteProblemFactChange(long callId) {
        this.callId = callId;
    }

    @Override
    public void doChange(ScoreDirector<CallCenter> scoreDirector) {
        Call call = new Call(callId, null);
        Call workingCall = scoreDirector.lookUpWorkingObjectOrReturnNull(call);

        if (workingCall != null) {
            scoreDirector.beforeProblemPropertyChanged(workingCall);
            workingCall.setDuration(workingCall.getDuration().plus(PROLONGATION));
            scoreDirector.afterProblemPropertyChanged(workingCall);
            scoreDirector.triggerVariableListeners();
        }
    }
}
