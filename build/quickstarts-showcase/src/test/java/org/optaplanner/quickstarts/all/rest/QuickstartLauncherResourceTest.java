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
package org.optaplanner.quickstarts.all.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.optaplanner.quickstarts.all.domain.QuickstartMeta;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class QuickstartLauncherResourceTest {

    @Test
    public void getQuickstartMetaList() {
        List<QuickstartMeta> quickstartMetaList = given()
                .when().get("/quickstart")
                .then()
                .statusCode(200)
                .extract().body().jsonPath()
                .getList(".", QuickstartMeta.class);
        assertFalse(quickstartMetaList.isEmpty());
    }

}
