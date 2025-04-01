package uk.gov.hmcts.reform.prl;

import io.restassured.response.Response;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = DocumentGeneratorControllerIntegrationTest.class)
@AutoConfigureMockMvc
public class DocumentGeneratorControllerIntegrationTest extends IntegrationTest {

    private static final String VALID_INPUT_JSON = "documentgenerator/documents/jsoninput/DA-granted-letter.json";

    @Test
    public void givenTemplateShouldGeneratePdfVerifyResponse() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = given()
                                .contentType("application/json")
                                .header("Authorization", getAuthorizationToken())
                                .body(requestBody)
                                .when()
                                .post(prlDocumentGeneratorURI)
                                .andReturn();

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void generatePdfWithWrongURIShouldThrowNotFound404() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = given()
            .contentType("application/json")
            .header("Authorization", getAuthorizationToken())
            .body(requestBody)
            .when()
            .post(prlDocumentGeneratorURI + "/test")
            .andReturn();

        assertEquals(HttpStatus.SC_NOT_FOUND, response.getStatusCode());
    }
}
