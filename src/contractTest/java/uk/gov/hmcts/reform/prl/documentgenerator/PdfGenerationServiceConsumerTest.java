package uk.gov.hmcts.reform.prl.documentgenerator;


import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.GenerateDocumentRequest;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "rpePdfService_PDFGenerationEndpointV2", port = "8891")
@PactFolder("pacts")
@SpringBootTest("service.pdf-service.uri = http://localhost:8891/pdfs")
public class PdfGenerationServiceConsumerTest {

    @Autowired
    ObjectMapper objectMapper;

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String TEMPLATE = "<html><body><div>Case number: {{ caseNo }}</div></body></html>";
    private final Map<String, Object> placeholders = Map.of("caseNo", "12345");

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "rpePdfService_PDFGenerationEndpointV2", consumer = "prl_documentGeneratorClient")
    RequestResponsePact generatePdfFromTemplate(PactDslWithProvider builder) throws JSONException, IOException {
        return builder
            .given("A request to generate a pdf document")
            .uponReceiving("a request to generate a pdf document with a template")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .body(createJsonObject(new GenerateDocumentRequest(TEMPLATE, placeholders)),
                  "application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8")
            .path("/pdfs")
            .willRespondWith()
            .matchHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                         "application/pdf")
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "generatePdfFromTemplate")
    public void verifyGeneratePdfFromTemplatePact(MockServer mockServer) throws IOException, JSONException {
        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(mockServer.getUrl() + "/pdfs");
        StringEntity json = new StringEntity(createJsonObject(new GenerateDocumentRequest(TEMPLATE, placeholders)));
        request.addHeader(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN);
        request.addHeader("content-type", "application/vnd.uk.gov.hmcts.pdf-service.v2+json;charset=UTF-8");
        request.setEntity(json);

        HttpResponse generateDocumentResponse = httpClient.execute(request);

        String responseContentType = generateDocumentResponse.getEntity().getContentType().toString();

        assertEquals(HttpStatus.SC_OK, generateDocumentResponse.getStatusLine().getStatusCode());
        assertEquals("Content-Type: application/pdf", responseContentType);

    }

    protected String createJsonObject(Object obj) throws JSONException, IOException {
        return objectMapper.writeValueAsString(obj);
    }

}
