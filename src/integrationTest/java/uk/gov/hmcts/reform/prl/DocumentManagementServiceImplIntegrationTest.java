package uk.gov.hmcts.reform.prl;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DocumentManagementServiceImplIntegrationTest extends IntegrationTest {

    private static final String INVALID_TEMPLATE_DATA_JSON = "requests/invalid-template-data.json"
        + "-data.json";
    private static final String VALID_INPUT_JSON = "requests/C100-case-data.json";
    private static final String IDAMAPI = "IDAM";
    private static final String DOCMOSISAPI = "DOCMOSIS";
    private static final String CCDDOCUMENTAPI = "CCDDOCUMENT";

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${ccd.document.gateway.url}")
    private String ccdGatewayUrl;

    @Test
    public void givenTemplateAndJsonInputReturnStatus200() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callPrlDocumentGenerator(requestBody);

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void givenRequestBodyAndInvalidAuthTokenReturnStatus401() throws Exception {
        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callInvalidPrlDocumentGenerator(requestBody);

        assertEquals(HttpStatus.SC_UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void checkStatusOfIdamApiIsUpThenReturn200Status() {
        Response response = given()
            .when()
            .get(constructHealthUrl(IDAMAPI))
            .andReturn();

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void checkStatusOfDocmosisApiIsUpThenReturn200Status() {

        Response response = given()
            .when()
            .get(constructHealthUrl(DOCMOSISAPI))
            .andReturn();

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void checkStatusOfCCDCaseDocumentApiIsUpThenReturn200Status() {

        Response response = given()
            .when()
            .get(constructHealthUrl(CCDDOCUMENTAPI))
            .andReturn();

        assertEquals(HttpStatus.SC_OK, response.getStatusCode());
    }

    @Test
    public void givenInvalidTemplateWhenRequestMadeThenReturn400Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(INVALID_TEMPLATE_DATA_JSON);

        Response response = callPrlDocumentGenerator(requestBody);

        assertEquals(
            400,
            response.getStatusCode()
        );
    }

    @Test
    public void givenTemplateAndJsonWhenDocumentGeneratedThenCorrectFormat() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String mimeType = generatedDocumentInfo.getMimeType();
        String expectedMimeType = "application/pdf";

        assertEquals(expectedMimeType, mimeType);
    }

    @Test
    public void givenValidAccessTokenWhenAccessingDocumentPathThenAccessProvided() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String[] dmStoreUrlParts = generatedDocumentInfo.getUrl().split("/");

        String documentId = dmStoreUrlParts[dmStoreUrlParts.length - 1];

        String url = ccdGatewayUrl + "/" + documentId;

        String bearerToken = getAuthorizationToken();

        HttpGet request = new HttpGet(url);

        request.setHeader(HttpHeaders.AUTHORIZATION, bearerToken);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        assertEquals(HttpStatus.SC_OK, httpResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void givenInvalidAccessTokenWhenAccessingDocumentPathThenNoAccessProvided() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String[] dmStoreUrlParts = generatedDocumentInfo.getUrl().split("/");

        String documentId = dmStoreUrlParts[dmStoreUrlParts.length - 1];

        String url = ccdGatewayUrl + "/" + documentId;

        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer ***INVALID***");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute(request);

        JSONObject responseJson = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        String responseMessage = responseJson.get("message").toString();

        assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED
            || (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR
            && responseMessage.equals("You are not authorised to access that resource")));
    }

    @Test
    public void givenValidTemplateAndJsonWhenDocumentGeneratedThenResponseContainsHashToken() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String hashToken = generatedDocumentInfo.getHashToken();

        assertNotNull(hashToken);
    }

}
