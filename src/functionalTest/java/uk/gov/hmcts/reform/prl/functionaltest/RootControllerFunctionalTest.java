package uk.gov.hmcts.reform.prl.functionaltest;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class RootControllerFunctionalTest {

    private final String userToken = "Bearer testToken";


    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4007"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void checkDgsRootFor200Response() throws Exception {

        request
            .header("Authorization", userToken)
            .when()
            .contentType("application/json")
            .get("/")
            .then()
            .assertThat().statusCode(200);
    }
}
