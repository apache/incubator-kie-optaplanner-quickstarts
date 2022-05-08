package org.acme.demoapp.rest;

import static org.assertj.core.api.Assertions.assertThat;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.common.domain.TimeTable;
import org.acme.common.event.SolverEvent;
import org.acme.common.persistence.TimeTableRepository;
import org.acme.demoapp.KafkaTestResourceLifecycleManager;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class DemoDataResourceTest {

    @Inject
    DemoDataResource demoDataResource;
    @Inject
    @Any
    InMemoryConnector connector;

    @Inject
    TimeTableRepository timeTableRepository;

    private InMemorySink<SolverEvent> solverRequestSink;

    @BeforeEach
    void setupChannels() {
        solverRequestSink = connector.sink("solver_request");
    }

    @Test
    void createTimeTable() {
        final int lessons = 10;
        Dataset dataset = demoDataResource.createTimeTable(lessons);
        assertThat(dataset.getLessons()).isEqualTo(lessons);
        assertThat(dataset.isSolved()).isFalse();

        Message<SolverEvent> solverRequestMessage = solverRequestSink.received().get(0);
        SolverEvent solverEvent = solverRequestMessage.getPayload();

        TimeTable timeTable = timeTableRepository.load(solverEvent.getProblemId());
        assertThat(timeTable.getLessonList()).hasSize(lessons);
    }
}
