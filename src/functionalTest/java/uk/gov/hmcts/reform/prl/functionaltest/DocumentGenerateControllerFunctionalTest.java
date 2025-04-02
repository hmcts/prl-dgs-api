package uk.gov.hmcts.reform.prl.functionaltest;

import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.documentgenerator.DocumentGeneratorApplication;

import static io.restassured.RestAssured.given;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
public class DocumentGenerateControllerFunctionalTest {
    private static final String USER_TOKEN = "Bearer testToken";
    private static final String VALID_REQUEST_BODY = "documentgenerator/da.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4007"
        );

    private final RequestSpecification request = given().relaxedHTTPSValidation().baseUri(targetInstance);

    @Test
    public void givenNoRequestBodyReturn400FromGenerateAndUploadPdf() {
        request
            .header("Authorization", USER_TOKEN)
            .when()
            .contentType("application/json")
            .post("/version/1/generatePDF")
            .then()
            .assertThat().statusCode(400);
    }

    @Test
    public void givenNoRequestBodyReturn400FromGenerateAndDraftPdf() {
        request
            .header("Authorization", USER_TOKEN)
            .when()
            .contentType("application/json")
            .post("/version/1/generateDraftPDF")
            .then()
            .assertThat().statusCode(400);
    }

}
