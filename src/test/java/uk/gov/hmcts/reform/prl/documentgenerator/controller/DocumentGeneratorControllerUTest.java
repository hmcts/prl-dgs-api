package uk.gov.hmcts.reform.prl.documentgenerator.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.UploadDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.CreatedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.service.DocumentManagementService;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentGeneratorControllerUTest {

    @Mock
    private DocumentManagementService documentManagementService;

    @InjectMocks
    private DocumentGeneratorController classUnderTest;

    @Test
    public void whenGeneratePDF_thenReturnGeneratedPDFDocumentInfo() {
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
    public void whenGeneratePDF_thenReturnGeneratedDraftPDFDocumentInfo() {
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
    public void whenCreateDcoument_thenReturnCreatedDocumentInfo() {
        final String templateName = "templateName";
        final Map<String, Object> placeholder = Collections.emptyMap();

        final CreatedDocumentInfo expected = CreatedDocumentInfo.builder().build();

        when(documentManagementService.createDocument(templateName, placeholder, "testToken"))
            .thenReturn(expected);

        CreatedDocumentInfo actual = classUnderTest
            .createPdf("testToken", new GenerateDocumentRequest(templateName, placeholder));

        assertEquals(expected, actual);

        verify(documentManagementService)
            .createDocument(templateName, placeholder, "testToken");
    }


    @Test
    public void whenUploadDcoument_thenReturnGeneratedDocumentInfo() {
        final String templateName = "templateName";
        final String fileName = "TestFile";
        final byte[] document = "Any String you want".getBytes();

        final GeneratedDocumentInfo expected = GeneratedDocumentInfo.builder().build();

        when(documentManagementService.storeDocument(document,"testToken", fileName))
            .thenReturn(expected);

        GeneratedDocumentInfo actual = classUnderTest
            .uploadPdf("testToken", new UploadDocumentRequest(fileName,document));

        assertEquals(expected, actual);

        verify(documentManagementService)
            .storeDocument(document, "testToken",fileName);
    }

}
