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

import java.time.Duration;

import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.PreviousCallOrAgent;
import org.optaplanner.core.api.domain.variable.VariableListener;
import org.optaplanner.core.api.score.director.ScoreDirector;

public class ResponseTimeUpdatingVariableListener implements VariableListener<CallCenter, Call> {

    @Override
    public void beforeEntityAdded(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    @Override
    public void afterEntityAdded(ScoreDirector<CallCenter> scoreDirector, Call call) {
        updateResponseTime(scoreDirector, call);
    }

    @Override
    public void beforeVariableChanged(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    @Override
    public void afterVariableChanged(ScoreDirector<CallCenter> scoreDirector, Call call) {
        updateResponseTime(scoreDirector, call);
    }

    @Override
    public void beforeEntityRemoved(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    @Override
    public void afterEntityRemoved(ScoreDirector<CallCenter> scoreDirector, Call call) {

    }

    protected void updateResponseTime(ScoreDirector<CallCenter> scoreDirector, Call call) {
        PreviousCallOrAgent previous = call.getPreviousCallOrAgent();
        Call shadowCall = call;
        Duration previousDurationTillPickUp = (previous == null ? null : previous.getDurationTillPickUp());
        Duration estimatedWaiting = calculateWaitingTimeEstimate(shadowCall, previousDurationTillPickUp);
        while (shadowCall != null) {
            scoreDirector.beforeVariableChanged(shadowCall, "estimatedWaiting");
            shadowCall.setEstimatedWaiting(estimatedWaiting);
            scoreDirector.afterVariableChanged(shadowCall, "estimatedWaiting");
            previousDurationTillPickUp = shadowCall.getDurationTillPickUp();
            shadowCall = shadowCall.getNextCall();
            estimatedWaiting = calculateWaitingTimeEstimate(shadowCall, previousDurationTillPickUp);
        }
    }

    private Duration calculateWaitingTimeEstimate(Call call, Duration previousEndTime) {
        if (call == null || previousEndTime == null) {
            return null;
        }
        return previousEndTime;
    }
}
