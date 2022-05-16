package uk.gov.hmcts.reform.prl.functionaltest;

import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.documentgenerator.DocumentGeneratorApplication;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
public class DocumentGenerateControllerFunctionalTest {
    private final String userToken = "Bearer testToken";

    private static final String VALID_REQUEST_BODY = "documentgenerator/da.json";

    private final String targetInstance =
        StringUtils.defaultIfBlank(
            System.getenv("TEST_URL"),
            "http://localhost:4007"
        );

    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);


    @Test
    public void givenNoRequestBodyReturn400FromGenerateAndUploadPdf() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", userToken)
            .when()
            .contentType("application/json")
            .post("/version/1/generatePDF")
            .then()
            .assertThat().statusCode(400);
    }

    @Test
    public void givenNoRequestBodyReturn400FromGenerateAndDraftPdf() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
        request
            .header("Authorization", userToken)
            .when()
            .contentType("application/json")
            .post("/version/1/generateDraftPDF")
            .then()
            .assertThat().statusCode(400);
    }

}
