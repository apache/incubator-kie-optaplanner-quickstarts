package org.acme.schooltimetabling.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.inject.Inject;

import org.acme.common.domain.Lesson;
import org.acme.common.domain.Room;
import org.acme.common.domain.TimeTable;
import org.acme.common.domain.Timeslot;
import org.acme.common.event.SolverEvent;
import org.acme.common.event.SolverEventType;
import org.acme.common.persistence.TimeTableRepository;
import org.assertj.core.api.SoftAssertions;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.reactive.messaging.providers.connectors.InMemoryConnector;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySink;
import io.smallrye.reactive.messaging.providers.connectors.InMemorySource;

@QuarkusTest
@QuarkusTestResource(KafkaTestResourceLifecycleManager.class)
public class MessageHandlerTest {

    private static final long PROBLEM_ID = 1L;

    private static final Duration AWAIT_TIMEOUT = Duration.ofSeconds(10L);

    @Inject
    TimeTableRepository repository;

    @Inject
    @Any
    InMemoryConnector connector;

    private InMemorySource<SolverEvent> solverInSource;
    private InMemorySink<SolverEvent> solverOutSink;

    @BeforeEach
    void setupChannels() {
        solverInSource = connector.source("solver_in");
        solverOutSink = connector.sink("solver_out");
    }

    @Timeout(60)
    @Test
    void solve() {
        TimeTable inputProblem = createTimetable(PROBLEM_ID);
        repository.persist(inputProblem);

        solverInSource.send(new SolverEvent(PROBLEM_ID, SolverEventType.SOLVER_REQUEST));

        await()
                .timeout(AWAIT_TIMEOUT)
                .until(solverOutSink::received, messages -> !messages.isEmpty());

        Message<SolverEvent> solverResponseMessage = solverOutSink.received().get(0);
        SolverEvent solverResponseEvent = solverResponseMessage.getPayload();

        TimeTable timeTable = repository.load(solverResponseEvent.getProblemId());
        assertThat(timeTable.getLessonList()).hasSize(2);


        SoftAssertions.assertSoftly(softly ->
                timeTable.getLessonList().forEach(lesson -> {
                    softly.assertThat(lesson.getRoom()).isNotNull();
                    softly.assertThat(lesson.getTimeslot()).isNotNull();
                }));
    }

    private TimeTable createTimetable(long problemId) {
        Lesson english = new Lesson(problemId, "English", "I. Jones", "9th grade");
        Lesson math = new Lesson(problemId, "Math", "A. Turing", "10th grade");

        Timeslot mondayMorning = new Timeslot(problemId, DayOfWeek.MONDAY, LocalTime.of(8, 30), LocalTime.of(9, 30));
        Timeslot mondayNoon = new Timeslot(problemId, DayOfWeek.MONDAY, LocalTime.NOON, LocalTime.NOON.plusHours(1L));

        Room roomA = new Room("Room A");
        roomA.setProblemId(problemId);
        Room roomB = new Room("Room B");
        roomB.setProblemId(problemId);

        return new TimeTable(List.of(mondayMorning, mondayNoon), List.of(roomA, roomB), List.of(english, math));
    }
}
