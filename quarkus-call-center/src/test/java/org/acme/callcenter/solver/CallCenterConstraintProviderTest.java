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

package org.acme.callcenter.solver;

import java.time.Duration;

import javax.inject.Inject;

import org.acme.callcenter.domain.Agent;
import org.acme.callcenter.domain.Call;
import org.acme.callcenter.domain.CallCenter;
import org.acme.callcenter.domain.Skill;
import org.junit.jupiter.api.Test;
import org.optaplanner.test.api.score.stream.ConstraintVerifier;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class CallCenterConstraintProviderTest {

    @Inject
    ConstraintVerifier<CallCenterConstraintsProvider, CallCenter> constraintVerifier;

    @Test
    void noRequiredSkillMissing() {
        Agent agent = new Agent(1L, "Carl", Skill.ENGLISH);
        Call call = new Call(1L,"123-456-7890", Skill.ENGLISH, Skill.PROPERTY_INSURANCE, Skill.CAR_INSURANCE);
        call.setPreviousCallOrAgent(agent);
        call.setAgent(agent);
        constraintVerifier.verifyThat(CallCenterConstraintsProvider::noRequiredSkillMissing)
                .given(call, agent)
                .penalizesBy(2);
    }

    @Test
    void minimizeWaitingTime() {
        Agent agent = new Agent(1L, "Carl", Skill.ENGLISH);
        Call call1 = new Call(1L,"123-456-7890", Skill.ENGLISH);
        Call call2 = new Call(2L,"123-456-7891", Skill.ENGLISH);
        Call call3 = new Call(3L,"123-456-7892", Skill.ENGLISH);

        call1.setPreviousCallOrAgent(agent);
        call1.setAgent(agent);
        call1.setEstimatedWaiting(Duration.ZERO);

        call2.setPreviousCallOrAgent(call1);
        call2.setAgent(agent);
        call2.setEstimatedWaiting(Duration.ofSeconds(10));

        call3.setPreviousCallOrAgent(call2);
        call3.setAgent(agent);
        call3.setEstimatedWaiting(Duration.ofSeconds(20));

        constraintVerifier.verifyThat(CallCenterConstraintsProvider::minimizeWaitingTime)
                .given(call1, call2, call3, agent)
                .penalizesBy(0 + 100 + 400);
    }
}
