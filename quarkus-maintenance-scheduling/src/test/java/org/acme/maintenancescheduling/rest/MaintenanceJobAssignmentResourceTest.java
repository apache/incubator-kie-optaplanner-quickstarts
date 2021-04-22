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
import org.acme.maintenancescheduling.domain.MaintenanceJobAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class MaintenanceJobAssignmentResourceTest {

    @Test
    public void getAll() {
        List<MaintenanceJobAssignment> jobList = given()
                .when().get("/jobAssignments")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getList(".", MaintenanceJobAssignment.class);
        assertFalse(jobList.isEmpty());
        MaintenanceJobAssignment firstJobAssignment = jobList.get(0);
        assertNotNull(firstJobAssignment.getMaintenanceJob().getJobName());
        assertNotNull(firstJobAssignment.getMaintenanceJob().getMaintainableUnit().getUnitName());
    }

    @Test
    public void addAndRemove() {
        MaintenanceJob job = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new MaintenanceJob("Test job", null, 0, 8, 1, 2, true))
                .post("/jobs")
                .then()
                .statusCode(201)
                .extract().as(MaintenanceJob.class);

        MaintenanceJobAssignment jobAssignment = given()
                .when()
                .contentType(ContentType.JSON)
                .body(new MaintenanceJobAssignment(job))
                .post("/jobAssignments")
                .then()
                .statusCode(201)
                .extract().as(MaintenanceJobAssignment.class);

        given()
                .when()
                .delete("/jobAssignments/{id}", jobAssignment.getId())
                .then()
                .statusCode(204);

        given()
                .when()
                .delete("/jobs/{id}", job.getId())
                .then()
                .statusCode(204);
    }
}
