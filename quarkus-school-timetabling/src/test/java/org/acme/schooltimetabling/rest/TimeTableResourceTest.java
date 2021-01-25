/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.acme.schooltimetabling.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.acme.schooltimetabling.domain.TimeTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.solver.SolverStatus;
import org.optaplanner.persistence.jackson.api.OptaPlannerJacksonModule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class TimeTableResourceTest {

    private static final Long PROBLEM_ID = 1L;
    private static final String RESOURCE_URI = "/time-table";

    private static String uri(String operation) {
        return RESOURCE_URI + "/" + PROBLEM_ID + "/" + operation;
    }

    private static String solve() {
        return uri("solve");
    }

    private static String status() {
        return uri("status");
    }

    @BeforeEach
    void terminateSolving() {
        given().delete(RESOURCE_URI + "/" + PROBLEM_ID);
    }

    @Test
    void duplicateProblemId_immediateErrorMessage() {
        given().post(solve()).then().assertThat().statusCode(200);
        given()
                .post(solve())
                .then()
                .assertThat()
                .statusCode(409)
                .extract()
                .response()
                .asString().equals("The problemId (" + PROBLEM_ID + ") is already solving.");
    }

    @Test
    void solveTillTermination() throws InterruptedException, JsonProcessingException {
        given().post(solve())
                .then()
                .statusCode(200);

        Assertions.assertEquals(SolverStatus.SOLVING_ACTIVE, getSolverStatus());
        while (getSolverStatus() != SolverStatus.NOT_SOLVING) {
            Thread.sleep(100L);
        }

        TimeTable solution = getSolution();
        assertNotNull(solution);
        assertTrue(solution.getScore().isFeasible());
    }

    private TimeTable getSolution() throws JsonProcessingException {
        String solutionJson = RestAssured
                .get(RESOURCE_URI + "/" + PROBLEM_ID)
                .then()
                .statusCode(200)
                .extract().asString();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(OptaPlannerJacksonModule.createModule());
        return mapper.readValue(solutionJson, TimeTable.class);
    }

    private SolverStatus getSolverStatus() {
        return given()
                .get(status())
                .then()
                .statusCode(200)
                .extract()
                .as(SolverStatus.class);
    }
}
