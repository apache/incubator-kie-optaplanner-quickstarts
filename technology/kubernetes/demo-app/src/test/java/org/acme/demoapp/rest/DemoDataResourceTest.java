package org.acme.demoapp.rest;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.enterprise.inject.Any;
import jakarta.inject.Inject;

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
import io.smallrye.reactive.messaging.memory.InMemoryConnector;
import io.smallrye.reactive.messaging.memory.InMemorySink;

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
