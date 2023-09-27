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
package org.acme.demoapp.rest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.common.domain.TimeTable;
import org.acme.common.message.SolverRequest;
import org.acme.common.persistence.TimeTableRepository;
import org.acme.demoapp.InMemoryBrokerLifecycleManager;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;

@QuarkusTest
@QuarkusTestResource(InMemoryBrokerLifecycleManager.class)
public class DemoDataResourceTest {

    @Inject
    DemoDataResource demoDataResource;
    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    TimeTableRepository timeTableRepository;

    @Inject
    ObjectMapper objectMapper;

    private InMemorySink<String> solverRequestSink;

    @BeforeEach
    void setupChannels() {
        solverRequestSink = connector.sink("solver_request");
    }

    @Test
    void createTimeTable() throws JsonProcessingException {
        final int lessons = 10;
        Dataset dataset = demoDataResource.createTimeTable(lessons);
        assertThat(dataset.getLessons()).isEqualTo(lessons);
        assertThat(dataset.isSolved()).isFalse();

        Message<String> solverRequestMessage = solverRequestSink.received().get(0);
        SolverRequest solverEvent = objectMapper.readValue(solverRequestMessage.getPayload(), SolverRequest.class);

        TimeTable timeTable = timeTableRepository.load(solverEvent.getProblemId());
        assertThat(timeTable.getLessonList()).hasSize(lessons);
    }
}
