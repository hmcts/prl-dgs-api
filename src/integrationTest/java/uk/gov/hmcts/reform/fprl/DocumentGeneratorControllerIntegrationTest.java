package uk.gov.hmcts.reform.fprl;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.HttpStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = DocumentGeneratorControllerIntegrationTest.class)
@AutoConfigureMockMvc
public class DocumentGeneratorControllerIntegrationTest extends IntegrationTest {

    private static final String VALID_INPUT_JSON = "documentgenerator/documents/jsoninput/DA-granted-letter.json";

    @Value("${prl.document.generator.uri}")
    private String prlDocumentGeneratorURI;

    @Test
    public void givenTemplateShouldGeneratePdf_VerifyResponse() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = SerenityRest.given()
                                .contentType("application/json")
                                .header("Authorization", getAuthorizationToken())
                                .body(requestBody)
                                .when()
                                .post(prlDocumentGeneratorURI)
                                .andReturn();

        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void generatePdfWithWrongURI_ShouldThrowNotFound404() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = SerenityRest.given()
            .contentType("application/json")
            .header("Authorization", getAuthorizationToken())
            .body(requestBody)
            .when()
            .post(prlDocumentGeneratorURI + "/test")
            .andReturn();

        Assert.assertEquals(response.getStatusCode(), HttpStatus.SC_NOT_FOUND);
    }
}
