package org.acme.vehiclerouting.rest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

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

        assertTrue(given()
                .when()
                .contentType(ContentType.JSON)
                .get("/vrp/status")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getBoolean("isSolving"));
        given()
                .when()
                .contentType(ContentType.JSON)
                .post("/vrp/stopSolving")
                .then()
                .statusCode(204);

        assertFalse(given()
                .when()
                .contentType(ContentType.JSON)
                .get("/vrp/status")
                .then()
                .statusCode(200)
                .extract().body().jsonPath().getBoolean("isSolving"));
    }
}
