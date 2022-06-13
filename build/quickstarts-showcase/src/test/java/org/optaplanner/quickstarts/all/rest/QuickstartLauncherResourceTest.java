package org.optaplanner.quickstarts.all.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

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
