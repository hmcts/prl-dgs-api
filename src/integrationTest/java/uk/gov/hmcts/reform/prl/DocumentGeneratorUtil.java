package uk.gov.hmcts.reform.prl;

import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class DocumentGeneratorUtil {
    static Response generatePDF(final String requestBody, final String generateDocURI, final String userToken) {
        return given()
            .contentType("application/json")
            .header("Authorization", userToken)
            .body(requestBody)
            .when()
            .post(generateDocURI)
            .andReturn();
    }
}
