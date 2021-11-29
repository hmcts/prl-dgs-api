package uk.gov.hmcts.reform.fprl;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.fprl.documentgenerator.domain.response.GeneratedDocumentInfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class DocumentManagementServiceImplIntegrationTest extends IntegrationTest {

    private static final String INVALID_TEMPLATE_DATA_JSON = "documentgenerator/documents/jsoninput/invalid-template"
        + "-data.json";
    private static final String VALID_INPUT_JSON = "documentgenerator/documents/jsoninput/DA-granted-letter.json";

    private static final String IDAMAPI = "IDAM";
    private static final String DOCMOSISAPI = "DOCMOSIS";
    private static final String CCDDOCUMENTAPI = "CCDDOCUMENT";

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    @Value("${ccd.document.gateway.url}")
    private String ccdGatewayUrl;

    @Test
    public void givenTemplateAndJsonInput_ReturnStatus200() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callPrlDocumentGenerator(requestBody);

        assertEquals(200, response.getStatusCode());
    }

    @Test
    public void givenRequestBodyAndInvalidAuthToken_ReturnStatus401() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);

        Response response = callInvalidPrlDocumentGenerator(requestBody);

        assertEquals(401, response.getStatusCode());
    }

    @Test
    public void checkHealthStatusOfIdamApiIsUp_thenReturn200Status() throws Exception {

        Response response = SerenityRest.given()
            .when()
            .get(constructHealthUrl(IDAMAPI))
            .andReturn();

        assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void checkHealthStatusOfDocmosisApiIsUp_thenReturn200Status() throws Exception {

        Response response = SerenityRest.given()
            .when()
            .get(constructHealthUrl(DOCMOSISAPI))
            .andReturn();

        assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void checkHealthStatusOfCCDCaseDocumentApiIsUp_thenReturn200Status() throws Exception {

        Response response = SerenityRest.given()
            .when()
            .get(constructHealthUrl(CCDDOCUMENTAPI))
            .andReturn();

        assertEquals(response.getStatusCode(), HttpStatus.SC_OK);
    }

    @Test
    public void givenInvalidTemplate_whenRequestMade_thenReturn400Response() throws Exception {

        String requestBody = ResourceLoader.loadJson(INVALID_TEMPLATE_DATA_JSON);

        Response response = callPrlDocumentGenerator(requestBody);

        assertEquals(
            400,
            response.getStatusCode()
        );
    }

    @Test
    public void givenTemplateAndJson_whenDocumentGenerated_thenCorrectFormat() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String mimeType = generatedDocumentInfo.getMimeType();
        String expectedMimeType = "application/pdf";

        assertEquals(mimeType, expectedMimeType);
    }

    @Test
    public void givenValidAccessToken_whenAccessingDocumentPath_thenAccessProvided() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String[] dmStoreUrlParts = generatedDocumentInfo.getUrl().split("/");

        String documentId = dmStoreUrlParts[dmStoreUrlParts.length - 1];

        String url = ccdGatewayUrl + "/" + documentId;

        String bearerToken = getAuthorizationToken();

        HttpGet request = new HttpGet(url);

        request.setHeader(HttpHeaders.AUTHORIZATION, bearerToken);
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

        assertEquals(
            httpResponse.getStatusLine().getStatusCode(),
            HttpStatus.SC_OK);
    }

    @Test
    public void givenInvalidAccessToken_whenAccessingDocumentPath_thenNoAccessProvided() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String[] dmStoreUrlParts = generatedDocumentInfo.getUrl().split("/");

        String documentId = dmStoreUrlParts[dmStoreUrlParts.length - 1];

        String url = ccdGatewayUrl + "/" + documentId;

        HttpGet request = new HttpGet(url);
        request.setHeader(HttpHeaders.AUTHORIZATION, "Bearer ***INVALID***");
        HttpResponse httpResponse = HttpClientBuilder.create().build().execute( request );

        JSONObject responseJson = new JSONObject(EntityUtils.toString(httpResponse.getEntity()));
        String responseMessage = responseJson.get("message").toString();

        assertTrue(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED
            || (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR
            && responseMessage.equals("You are not authorised to access that resource")));
    }

    @Test
    public void givenValidTemplateAndJson_whenDocumentGenerated_thenResponseContainsHashToken() throws Exception {

        String requestBody = ResourceLoader.loadJson(VALID_INPUT_JSON);
        Response response = callPrlDocumentGenerator(requestBody);
        GeneratedDocumentInfo generatedDocumentInfo = response.as(GeneratedDocumentInfo.class);

        String hashToken = generatedDocumentInfo.getHashToken();

        assertNotNull(hashToken);
    }



}
