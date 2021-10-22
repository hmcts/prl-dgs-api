package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.prl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.service.PDFGenerationService;

import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.documentgenerator.functionaltest.DocumentGenerateAndStoreE2ETest.mockCaseDocsDocuments;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.FILE_URL;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.JURISDICTION;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.MIME_TYPE;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_GENERATED_DOCUMENT;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_HASH_TOKEN;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_S2S_TOKEN;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_TEMPLATE;
import static uk.gov.hmcts.reform.prl.documentgenerator.util.TestData.TEST_TEMPLATE_FILE_NAME;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceImplUTest {

    @Mock
    private PDFGenerationService pdfGenerationService;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private TemplatesConfiguration templatesConfiguration;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private DocumentManagementServiceImpl classUnderTest;

    private UploadResponse expectedUploadResponse;

    @Before
    public void setUp() {
        expectedUploadResponse = new UploadResponse(asList(mockCaseDocsDocuments()));
        when(authTokenGenerator.generate()).thenReturn(TEST_S2S_TOKEN);
    }

    @Test
    public void givenTemplateNameIsAosInvitation_whenGenerateAndStoreDocument_thenProceedAsExpected() {
        when(pdfGenerationService.generate(eq(TEST_TEMPLATE), any())).thenReturn(TEST_GENERATED_DOCUMENT);
        when(templatesConfiguration.getFileNameByTemplateName(TEST_TEMPLATE)).thenReturn(TEST_TEMPLATE_FILE_NAME);
        when(caseDocumentClient.uploadDocuments(
            eq(TEST_AUTH_TOKEN), eq(TEST_S2S_TOKEN), eq(CASE_TYPE), eq(JURISDICTION), any()
        )).thenReturn(expectedUploadResponse);

        GeneratedDocumentInfo generatedDocumentInfo = classUnderTest
            .generateAndStoreDocument(TEST_TEMPLATE, new HashMap<>(), TEST_AUTH_TOKEN);

        assertGeneratedDocumentInfoIsAsExpected(generatedDocumentInfo);
    }

    @Test
    public void givenTemplateNameIsInvalid_whenGenerateAndStoreDocument_thenThrowException() {
        String unknownTemplateName = "unknown-template";
        HashMap<String, Object> placeholders = new HashMap<>();
        when(templatesConfiguration.getFileNameByTemplateName(unknownTemplateName))
            .thenThrow(new IllegalArgumentException("Unknown template: " + unknownTemplateName));

        IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class, () -> {
            classUnderTest.generateAndStoreDocument(unknownTemplateName, placeholders, "some-auth-token");
        });

        assertThat(illegalArgumentException.getMessage(), equalTo("Unknown template: " + unknownTemplateName));
    }

    private void assertGeneratedDocumentInfoIsAsExpected(GeneratedDocumentInfo generatedDocumentInfo) {
        assertThat(generatedDocumentInfo.getUrl(), equalTo(FILE_URL));
        assertThat(generatedDocumentInfo.getMimeType(), equalTo(MIME_TYPE));
        assertThat(generatedDocumentInfo.getHashToken(), equalTo(TEST_HASH_TOKEN));
    }
}
