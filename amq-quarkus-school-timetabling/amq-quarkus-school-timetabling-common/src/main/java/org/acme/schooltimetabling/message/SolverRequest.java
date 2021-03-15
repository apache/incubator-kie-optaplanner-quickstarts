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

package org.acme.schooltimetabling.message;

import org.acme.schooltimetabling.domain.TimeTable;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class SolverRequest {
    private Long problemId;
    private TimeTable timeTable;

    SolverRequest() {
        // Required for JSON deserialization.
    }

    public SolverRequest(Long problemId, TimeTable timeTable) {
        this.problemId = problemId;
        this.timeTable = timeTable;
    }

    public Long getProblemId() {
        return problemId;
    }

    public TimeTable getTimeTable() {
        return timeTable;
    }
}
