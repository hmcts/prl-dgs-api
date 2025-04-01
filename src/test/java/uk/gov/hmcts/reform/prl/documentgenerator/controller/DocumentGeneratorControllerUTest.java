package uk.gov.hmcts.reform.prl.documentgenerator.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.service.DocumentManagementService;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DocumentGeneratorControllerUTest {
    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentGeneratorController classUnderTest;

    @Test
    void whenGeneratePDFThenReturnGeneratedPDFDocumentInfo() {
        final String templateName = "templateName";
        final Map<String, Object> placeholder = Collections.emptyMap();

        final GeneratedDocumentInfo expected = GeneratedDocumentInfo.builder().build();

        when(documentManagementService.generateAndStoreDocument(templateName, placeholder, "testToken"))
            .thenReturn(expected);

        GeneratedDocumentInfo actual = classUnderTest
                .generateAndUploadPdf("testToken", new GenerateDocumentRequest(templateName, placeholder));

        assertEquals(expected, actual);

        verify(documentManagementService, times(1))
                .generateAndStoreDocument(templateName, placeholder, "testToken");
    }

    @Test
    void whenGeneratePDFThenReturnGeneratedDraftPDFDocumentInfo() {
        final String templateName = "templateName";
        final Map<String, Object> placeholder = Collections.emptyMap();

        final GeneratedDocumentInfo expected = GeneratedDocumentInfo.builder().build();

        when(documentManagementService.generateAndStoreDraftDocument(templateName, placeholder, "testToken"))
            .thenReturn(expected);

        GeneratedDocumentInfo actual = classUnderTest
                .generateAndUploadDraftPdf("testToken", new GenerateDocumentRequest(templateName, placeholder));

        assertEquals(expected, actual);

        verify(documentManagementService)
                .generateAndStoreDraftDocument(templateName, placeholder, "testToken");
    }

    @Test
    void whenConvertPDFThenReturnConvertedPDFDocumentInfo() {
        final String templateName = "templateName";
        Map<String, Object> placeholder = new HashMap<>();

        final GeneratedDocumentInfo expected = GeneratedDocumentInfo.builder().build();

        when(documentManagementService.convertToPdf(placeholder, "authToken","fileName"))
            .thenReturn(expected);

        GeneratedDocumentInfo actual = classUnderTest
            .convertDocumentToPdf("fileName","authToken", new GenerateDocumentRequest(templateName, placeholder));

        assertEquals(expected, actual);

        verify(documentManagementService, times(1))
            .convertToPdf(placeholder, "authToken","fileName");
    }

}
