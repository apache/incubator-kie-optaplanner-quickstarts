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
package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

@QuarkusTest
public class SolverResourceTest {

    @Test
    public void solve() {
        given()
                .when()
                .contentType(ContentType.JSON)
                .post("/vrp/solve")
                .then()
                .statusCode(204);

        await().until(() -> !given()
                .when()
                .contentType(ContentType.JSON)
                .get("/vrp/status")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getBoolean("isSolving"));

        String scoreString = given()
                .when()
                .contentType(ContentType.JSON)
                .get("/vrp/status")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath().get("solution.score");
        assertTrue(HardSoftScore.parseScore(scoreString).isFeasible());
    }
}
