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
package org.acme.common.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import org.optaplanner.core.api.domain.lookup.PlanningId;

@MappedSuperclass
public class AbstractPersistable {

    public static final String TENANT_FIELD = "problemId";
    public static final long SINGLE_PROBLEM_ID = 1L;

    @PlanningId
    @Id
    @GeneratedValue
    private Long id;

    private Long problemId;

    // No-arg constructor required for Hibernate and OptaPlanner
    public AbstractPersistable() {
    }

    public AbstractPersistable(Long problemId) {
        this.problemId = problemId;
    }

    public AbstractPersistable(Long id, Long problemId) {
        this.id = id;
        this.problemId = problemId;
    }

    public Long getId() {
        return id;
    }

    public Long getProblemId() {
        return problemId;
    }

    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }
}
