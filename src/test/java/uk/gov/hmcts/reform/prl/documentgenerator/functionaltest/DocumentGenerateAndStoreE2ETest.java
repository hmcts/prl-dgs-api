package uk.gov.hmcts.reform.prl.documentgenerator.functionaltest;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.prl.documentgenerator.DocumentGeneratorApplication;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.FILE_URL;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.MIME_TYPE;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_DEFAULT_NAME_FOR_PDF_FILE;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_HASH_TOKEN;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = DocumentGeneratorApplication.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@PropertySource(value = "classpath:application.yml")
@AutoConfigureMockMvc
public class DocumentGenerateAndStoreE2ETest {
    private static final String API_URL = "/version/1/generatePDF";
    private static final String CASE_DOCS_API_URL = "/cases/documents";
    private static final String DOCMOSIS_API_URL = "/rs/render";
    private static final String S2S_API_URL = "/lease";

    private static final String CASE_DETAILS = "caseDetails";
    private static final String CASE_DATA = "case_data";

    private static final String TEST_EXAMPLE = "FL-DIV-GOR-ENG-00062.docx";

    @Autowired
    private MockMvc webClient;

    @ClassRule
    public static WireMockClassRule caseDocsClientApiServiceServer = new WireMockClassRule(5170);

    @ClassRule
    public static WireMockClassRule docmosisClientServiceServer = new WireMockClassRule(5501);

    @ClassRule
    public static WireMockClassRule serviceAuthServer = new WireMockClassRule(4502);

    @Test
    public void givenTemplateNameIsNull_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = null;
        final Map<String, Object> values = Collections.emptyMap();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, values);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateNameIsBlank_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = "  ";
        final Map<String, Object> values = Collections.emptyMap();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, values);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenTemplateNotFound_whenGenerateAndStoreDocument_thenReturnHttp400() throws Exception {
        final String template = "nonExistingTemplate";
        final Map<String, Object> values = Collections.emptyMap();

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(template, values);

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void givenCouldNotConnectToAuthService_whenGenerateAndStoreDocument_thenReturnHttp503() throws Exception {
        Map<String, Object> caseData = new HashMap<>();

        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData)
        );

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_EXAMPLE, requestData);

        mockDocmosisPdfService(HttpStatus.OK, new byte[] {1});
        mockServiceAuthServer(HttpStatus.SERVICE_UNAVAILABLE, "");

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isServiceUnavailable());
    }

    @Test
    public void givenAuthServiceReturnAuthenticationError_whenGenerateAndStoreDocument_thenReturnHttp401() throws Exception {
        Map<String, Object> caseData = new HashMap<>();
        Map<String, Object> requestData = Collections.singletonMap(
            CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData)
        );

        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(TEST_EXAMPLE, requestData);

        mockDocmosisPdfService(HttpStatus.OK, new byte[] {1});
        mockServiceAuthServer(HttpStatus.UNAUTHORIZED, "");

        webClient.perform(post(API_URL)
            .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized());
    }

    @Test
    public void givenAllGoesWellForTestExample_whenGenerateAndStoreDocument_thenReturn()
        throws Exception {
        assertReturnWhenAllGoesWellForGeneratingAndStoringDocuments(TEST_EXAMPLE);
    }

    private void assertReturnWhenAllGoesWellForGeneratingAndStoringDocuments(String templateId) throws Exception {
        //Given
        final Map<String, Object> caseData = Collections.emptyMap();
        final Map<String, Object> values = new HashMap<>();
        values.put(CASE_DETAILS, Collections.singletonMap(CASE_DATA, caseData));
        final String s2sAuthToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJsb2dnZWRJbkFzIjoiYWRtaW4iLCJpYXQiOjE0MjI"
            + "3Nzk2Mzh9.gzSraSYS8EXBxLN_oWnFSRgCzcmJmMjLiuyu5CSpyHI";
        final UploadResponse uploadResponse = new UploadResponse(List.of(mockCaseDocsDocuments()));

        mockDocmosisPdfService(HttpStatus.OK, new byte[]{1});
        mockCaseDocsClientApi(HttpStatus.OK, uploadResponse);
        mockServiceAuthServer(HttpStatus.OK, s2sAuthToken);

        //When
        final GenerateDocumentRequest generateDocumentRequest = new GenerateDocumentRequest(templateId, values);
        MvcResult result = webClient.perform(post(API_URL)
                .content(ObjectMapperTestUtil.convertObjectToJsonString(generateDocumentRequest))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        //Then
        final GeneratedDocumentInfo generatedDocumentInfo = getGeneratedDocumentInfo();
        assertEquals(ObjectMapperTestUtil.convertObjectToJsonString(generatedDocumentInfo), result.getResponse().getContentAsString());
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo() {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url(FILE_URL)
            .hashToken(TEST_HASH_TOKEN)
            .mimeType(MIME_TYPE)
            .build();

        return generatedDocumentInfo;
    }

    private void mockDocmosisPdfService(HttpStatus expectedResponse, byte[] body) {
        docmosisClientServiceServer.stubFor(WireMock.post(DOCMOSIS_API_URL)
            .willReturn(aResponse()
                .withStatus(expectedResponse.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(body))
            ));
    }

    private void mockServiceAuthServer(HttpStatus expectedResponse, String body) {
        serviceAuthServer.stubFor(WireMock.post(S2S_API_URL)
            .willReturn(aResponse()
                .withStatus(expectedResponse.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(body))
            ));
    }

    private void mockCaseDocsClientApi(HttpStatus expectedResponse, UploadResponse uploadResponse) {
        caseDocsClientApiServiceServer.stubFor(WireMock.post(CASE_DOCS_API_URL)
            .willReturn(aResponse()
                .withStatus(expectedResponse.value())
                .withHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .withBody(ObjectMapperTestUtil.convertObjectToJsonString(uploadResponse))
            ));
    }

    public static Document mockCaseDocsDocuments() {
        Document.Link link = new Document.Link();
        link.href = FILE_URL;

        Document.Links links = new Document.Links();
        links.self = link;

        return Document.builder()
            .links(links)
            .hashToken(TEST_HASH_TOKEN)
            .mimeType(MIME_TYPE)
            .originalDocumentName(TEST_DEFAULT_NAME_FOR_PDF_FILE)
            .build();
    }
}
