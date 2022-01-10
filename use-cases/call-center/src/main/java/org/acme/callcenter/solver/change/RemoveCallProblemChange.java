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

import java.util.Optional;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.PreviousCallOrAgent;
import org.optaplanner.core.api.solver.change.ProblemChange;
import org.optaplanner.core.api.solver.change.ProblemChangeDirector;

public class RemoveCallProblemChange implements ProblemChange<CallCenter> {

    private final long callId;

    public RemoveCallProblemChange(long callId) {
        this.callId = callId;
    }

    @Override
    public void doChange(CallCenter workingCallCenter, ProblemChangeDirector problemChangeDirector) {
        Call call = new Call(callId, null);
        Optional<Call> workingCallOptional = problemChangeDirector.lookUpWorkingObjectOptionally(call);
        workingCallOptional.ifPresent(workingCall -> {
            PreviousCallOrAgent previousCallOrAgent = workingCall.getPreviousCallOrAgent();

            Call nextCall = workingCall.getNextCall();
            if (nextCall != null) {
                problemChangeDirector.changeVariable(nextCall, "previousCallOrAgent",
                        workingNextCall -> workingNextCall.setPreviousCallOrAgent(previousCallOrAgent));
            }

            problemChangeDirector.removeEntity(workingCall, workingCallCenter.getCalls()::remove);
        });
    }
}
