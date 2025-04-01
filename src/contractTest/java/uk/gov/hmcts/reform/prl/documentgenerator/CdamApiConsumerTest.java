package uk.gov.hmcts.reform.prl.documentgenerator;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.json.JSONException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "CCD_CASE_DOCS_AM_API", port = "5170")
@PactFolder("pacts")
@SpringBootTest("CCD_CASE_DOCS_AM_API = http://localhost:5170")
public class CdamApiConsumerTest {

    private static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    private static final String SERVICE_AUTH_TOKEN = "someServiceAuthToken";
    private static final String INVALID_SERVICE_AUTH_TOKEN = "invalidServiceAuthToken";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String SOME_AUTH_TOKEN = "someAuthToken";
    private static final String SOME_DOCUMENT_ID = "456c0976-3178-46dd-b9ce-5ab5d47c625a";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @AfterEach
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "CCD_CASE_DOCS_AM_API", consumer = "prl-dgs-api")
    RequestResponsePact downloadDocument(PactDslWithProvider builder) throws JSONException {

        return builder
            .given("A request to download a document")
            .uponReceiving("a request to download a valid document")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .headers(AUTHORIZATION_HEADER, SOME_AUTH_TOKEN)
            .path("/cases/documents/" + SOME_DOCUMENT_ID)
            .willRespondWith()
            .matchHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8")
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "downloadDocument")
    public void verifyDownloadDocument(MockServer mockServer) throws IOException {

        HttpResponse downloadDocumentResponse = Request.Get(
            mockServer.getUrl() + "/cases/documents/" + SOME_DOCUMENT_ID
            )
            .addHeader(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .addHeader(AUTHORIZATION_HEADER, SOME_AUTH_TOKEN)
            .execute().returnResponse();

        assertEquals(HttpStatus.SC_OK, downloadDocumentResponse.getStatusLine().getStatusCode());
    }

    @Pact(provider = "CCD_CASE_DOCS_AM_API", consumer = "prl-dgs-api")
    RequestResponsePact noAuthDownloadDocument(PactDslWithProvider builder) throws JSONException {

        return builder
            .given("A request to download a document")
            .uponReceiving("a request to download a valid document with invalid authorisation")
            .method("GET")
            .headers(SERVICE_AUTHORIZATION_HEADER, INVALID_SERVICE_AUTH_TOKEN)
            .path("/cases/documents/" + SOME_DOCUMENT_ID)
            .willRespondWith()
            .matchHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE,
                "application/vnd.uk.gov.hmcts.dm.document.v1+hal+json;charset=UTF-8")
            .status(HttpStatus.SC_INTERNAL_SERVER_ERROR)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "noAuthDownloadDocument")
    public void verifyNoAuthDownloadDocument(MockServer mockServer) throws IOException {

        HttpResponse downloadDocumentResponse = Request.Get(
            mockServer.getUrl() + "/cases/documents/" + SOME_DOCUMENT_ID
            )
            .addHeader(SERVICE_AUTHORIZATION_HEADER, INVALID_SERVICE_AUTH_TOKEN).execute().returnResponse();

        assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, downloadDocumentResponse.getStatusLine().getStatusCode());
    }

    @Pact(provider = "CCD_CASE_DOCS_AM_API", consumer = "prl-dgs-api")
    RequestResponsePact uploadDocument(PactDslWithProvider builder) throws JSONException {
        return builder
            .given("A request to upload a document")
            .uponReceiving("a request to upload a document with valid authorization")
            .method("POST")
            .headers(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .headers(AUTHORIZATION_HEADER, SOME_AUTH_TOKEN)
            .path("/cases/documents")
            .willRespondWith()
            .status(HttpStatus.SC_OK)
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "uploadDocument")
    public void verifyUploadDocument(MockServer mockServer) throws IOException {

        HttpResponse downloadDocumentResponse = Request.Post(mockServer.getUrl() + "/cases/documents")
            .addHeader(SERVICE_AUTHORIZATION_HEADER, SERVICE_AUTH_TOKEN)
            .addHeader(AUTHORIZATION_HEADER, SOME_AUTH_TOKEN)
            .execute().returnResponse();

        assertEquals(HttpStatus.SC_OK, downloadDocumentResponse.getStatusLine().getStatusCode());
    }

}
