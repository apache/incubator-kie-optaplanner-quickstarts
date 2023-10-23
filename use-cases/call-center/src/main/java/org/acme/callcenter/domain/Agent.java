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
package org.acme.callcenter.domain;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Agent extends PreviousCallOrAgent {

    private String name;
    private Set<Skill> skills;

    public Agent() {
        // Required by OptaPlanner.
    }

    public Agent(long id, String name) {
        super(id);
        this.name = name;
        this.skills = EnumSet.noneOf(Skill.class);
    }

    public Agent(long id, String name, Set<Skill> skills) {
        super(id);
        this.name = name;
        this.skills = EnumSet.copyOf(skills);
    }

    public Agent(long id, String name, Skill... skills) {
        this(id, name, EnumSet.copyOf(Arrays.asList(skills)));
    }

    @JsonProperty(value = "calls", access = JsonProperty.Access.READ_ONLY)
    public List<Call> getAssignedCalls() {
        Call nextCall = getNextCall();
        List<Call> assignedCalls = new ArrayList<>();
        while (nextCall != null) {
            assignedCalls.add(nextCall);
            nextCall = nextCall.getNextCall();
        }
        return assignedCalls;
    }

    @Override
    public Duration getDurationTillPickUp() {
        return Duration.ZERO;
    }

    public String getName() {
        return name;
    }

    public Set<Skill> getSkills() {
        return skills;
    }
}
