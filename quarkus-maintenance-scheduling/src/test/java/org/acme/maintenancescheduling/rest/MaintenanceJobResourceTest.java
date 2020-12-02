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

package org.acme.maintenancescheduling.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.acme.maintenancescheduling.domain.MaintenanceJob;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class MaintenanceJobResourceTest {

    @Test
    public void getAll() {
        List<MaintenanceJob> jobList = given()
                .when().get("/jobs")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", MaintenanceJob.class);
        assertFalse(jobList.isEmpty());
        MaintenanceJob firstJob = jobList.get(0);
        assertNotNull(firstJob.getJobName());
        assertNotNull(firstJob.getMaintainableUnit().getUnitName());
    }

    @Test
    public void addAndRemove() {
        MaintenanceJob job = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new MaintenanceJob("Test job", null, 0, 8, 1, true))
                .post("/jobs")
                .then()
                .statusCode(201)
                .extract().as(MaintenanceJob.class);

        given()
                .when()
                .delete("/jobs/{id}", job.getId())
                .then()
                .statusCode(204);
    }
}
