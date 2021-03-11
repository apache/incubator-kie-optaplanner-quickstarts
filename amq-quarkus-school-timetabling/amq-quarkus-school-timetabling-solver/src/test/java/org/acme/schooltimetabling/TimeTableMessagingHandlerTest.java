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

package org.acme.schooltimetabling;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Session;

import org.acme.schooltimetabling.domain.Lesson;
import org.acme.schooltimetabling.domain.Room;
import org.acme.schooltimetabling.domain.TimeTable;
import org.acme.schooltimetabling.domain.Timeslot;
import org.acme.schooltimetabling.messaging.SolverRequest;
import org.acme.schooltimetabling.messaging.SolverResponse;
import org.acme.schooltimetabling.messaging.TimeTableMessagingHandler;
import org.apache.activemq.artemis.jms.client.ActiveMQConnectionFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
@QuarkusTestResource(AMQEmbeddedBroker.class)
public class TimeTableMessagingHandlerTest {

    private static final long TEST_TIMEOUT_SECONDS = 60L;
    private static final int MESSAGE_RECEIVE_TIMEOUT_SECONDS = 10;

    private ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");

    // Native test does not support @Inject.
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .registerModule(OptaPlannerJacksonModule.createModule());

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void solve() {
        long problemId = 1L;
        TimeTable unsolvedTimeTable = createTestTimeTable();
        sendSolverRequest(new SolverRequest(problemId, unsolvedTimeTable));
        SolverResponse solverResponse = receiveSolverResponse(MESSAGE_RECEIVE_TIMEOUT_SECONDS);

        assertThat(SolverResponse.ResponseStatus.SUCCESS == solverResponse.getResponseStatus());
        assertThat(problemId == solverResponse.getProblemId());
        assertThat(solverResponse.getTimeTable().getLessonList()).hasSameSizeAs(unsolvedTimeTable.getLessonList());
        assertThat(solverResponse.getTimeTable().getScore().isFeasible()).isTrue();
    }

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void solvingThrowsException() {
        long problemId = 10L;
        TimeTable unsolvedTimeTable = createTestTimeTable();
        unsolvedTimeTable.getLessonList().get(0).setId(null); // OptaPlanner doesn't tolerate null planningId.
        sendSolverRequest(new SolverRequest(problemId, unsolvedTimeTable));

        SolverResponse solverResponse = receiveSolverResponse(MESSAGE_RECEIVE_TIMEOUT_SECONDS);
        assertThat(SolverResponse.ResponseStatus.FAILURE == solverResponse.getResponseStatus());
        assertThat(problemId == solverResponse.getProblemId());
        assertThat(solverResponse.getErrorInfo().getExceptionClassName()).isEqualTo(IllegalArgumentException.class.getName());
        assertThat(solverResponse.getErrorInfo().getExceptionMessage()).startsWith("The planningId (null)");
    }

    @Test
    @Timeout(TEST_TIMEOUT_SECONDS)
    void badRequest_DLQ() {
        final String wrongMessage = "Bad request!";
        sendMessage(wrongMessage);
        String messageFromDLQ = receiveMessage("DLQ", 10);
        assertThat(messageFromDLQ).isEqualTo(wrongMessage);
    }

    private SolverResponse receiveSolverResponse(int timeoutSeconds) {
        String solverResponsePayload = receiveMessage(TimeTableMessagingHandler.SOLVER_RESPONSE_CHANNEL, timeoutSeconds);
        try {
            return objectMapper.readValue(solverResponsePayload, SolverResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private String receiveMessage(String queueName, int timeoutSeconds) {
        try (JMSContext context = connectionFactory.createContext("quarkus", "quarkus", Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue(queueName));
            javax.jms.Message message = consumer.receive(timeoutSeconds * 1_000);
            return message.getBody(String.class);
        } catch (JMSException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendSolverRequest(SolverRequest solverRequest) {
        try {
            String message = objectMapper.writeValueAsString(solverRequest);
            sendMessage(message);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMessage(String message) {
        try (JMSContext context = connectionFactory.createContext("quarkus", "quarkus", Session.AUTO_ACKNOWLEDGE)) {
            JMSProducer producer = context.createProducer();
            Destination solverRequestQueue = context.createQueue(TimeTableMessagingHandler.SOLVER_REQUEST_CHANNEL);
            producer.send(solverRequestQueue, message);
        }
    }

    private TimeTable createTestTimeTable() {
        List<Room> rooms = Collections.singletonList(new Room(1L, "room-A"));
        List<Timeslot> timeslots = Arrays.asList(
                new Timeslot(1L, DayOfWeek.MONDAY, LocalTime.NOON),
                new Timeslot(2L, DayOfWeek.TUESDAY, LocalTime.NOON),
                new Timeslot(3L, DayOfWeek.WEDNESDAY, LocalTime.NOON));
        List<Lesson> lessons = Arrays.asList(
                new Lesson(1L, "Math", "A. Touring", "10th grade"),
                new Lesson(2L, "Biology", "C. Darwin", "11th grade"),
                new Lesson(3L, "Physics", "M. Curie", "12th grade"));
        return new TimeTable(timeslots, rooms, lessons);
    }
}