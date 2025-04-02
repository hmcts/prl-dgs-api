package uk.gov.hmcts.reform.prl.functionaltest;

import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class RootControllerFunctionalTest {

    private static final String USER_TOKEN = "Bearer testToken";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4007"
        );

    private final RequestSpecification request = given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void checkDgsRootFor200Response() {

        request
            .header("Authorization", USER_TOKEN)
            .when()
            .contentType("application/json")
            .get("/")
            .then()
            .assertThat().statusCode(200);
    }
}
