package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.prl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.prl.documentgenerator.service.PDFGenerationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.documentgenerator.functionaltest.DocumentGenerateAndStoreE2ETest.mockCaseDocsDocuments;

@RunWith(MockitoJUnitRunner.class)
public class DocumentManagementServiceImplTest {
    private static final String IS_DRAFT = "isDraft";
    private static final String MINI_PETITION_NAME_FOR_WELSH_PDF_FILE = "DivorcePetitionWelsh.pdf";
    private static final String DRAFT_MINI_PETITION_NAME_FOR_PDF_FILE = "DraftDivorcePetition.pdf";
    private static final String D8_PETITION_WELSH_TEMPLATE = "FL-DIV-GNO-WEL-00256.docx";

    private static final String DRAFT_MINI_PETITION_TEMPLATE_ID = "divorcedraftminipetition";
    private static final String FINAL_MINI_PETITION_TEMPLATE_ID = "divorcedraftminipetition";

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

    @Captor
    ArgumentCaptor<Map<String, Object>> placeHolderCaptor;

    static final Map<String, Object> placeholderMap = new HashMap<>();
    static final String authToken = "userAuthToken";
    static final String s2sToken = "s2sAuthToken";
    static final byte[] data = {126};
    static final UploadResponse uploadResponse = new UploadResponse(List.of(mockCaseDocsDocuments()));

    @Test
    public void testGenerateAndStoreDraftDocumentMock() {
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Mockito.when(pdfGenerationService.generate(D8_PETITION_WELSH_TEMPLATE, placeholderMap)).thenReturn(data);
        Mockito.when(templatesConfiguration.getFileNameByTemplateName(D8_PETITION_WELSH_TEMPLATE))
                .thenReturn(MINI_PETITION_NAME_FOR_WELSH_PDF_FILE);
        Mockito.when(caseDocumentClient.uploadDocuments(eq(authToken),
                        eq(s2sToken), eq("PRLAPPS"), eq("PRIVATELAW"), any()))
            .thenReturn(uploadResponse);

        classUnderTest.generateAndStoreDraftDocument(D8_PETITION_WELSH_TEMPLATE, placeholderMap, authToken);

        verify(pdfGenerationService).generate(same(D8_PETITION_WELSH_TEMPLATE), placeHolderCaptor.capture());
        Map<String, Object> value = placeHolderCaptor.getValue();
        assertThat("Draft value set ", value.get(IS_DRAFT), is(true));
    }

    @Test
    public void testGenerateAndStoreDraftDocumentMockWithDynamicName() {
        placeholderMap.put("dynamic_fileName","test-file.pdf");
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Mockito.when(pdfGenerationService.generate(D8_PETITION_WELSH_TEMPLATE, placeholderMap)).thenReturn(data);
        Mockito.when(caseDocumentClient.uploadDocuments(eq(authToken),
                                                        eq(s2sToken), eq("PRLAPPS"), eq("PRIVATELAW"), any()))
            .thenReturn(uploadResponse);

        classUnderTest.generateAndStoreDraftDocument(D8_PETITION_WELSH_TEMPLATE, placeholderMap, authToken);

        verify(pdfGenerationService).generate(same(D8_PETITION_WELSH_TEMPLATE), placeHolderCaptor.capture());
        Map<String, Object> value = placeHolderCaptor.getValue();
        assertThat("Draft value set ", value.get(IS_DRAFT), is(true));
    }

    @Test
    public void testGenerateAndStoreDraftDocument_WithDraftPrefixMock() {
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Mockito.when(pdfGenerationService.generate(DRAFT_MINI_PETITION_TEMPLATE_ID, placeholderMap)).thenReturn(data);
        Mockito.when(caseDocumentClient.uploadDocuments(eq(authToken),
                        eq(s2sToken), eq("PRLAPPS"), eq("PRIVATELAW"), any()))
            .thenReturn(uploadResponse);

        classUnderTest.generateAndStoreDraftDocument(DRAFT_MINI_PETITION_TEMPLATE_ID, placeholderMap, authToken);

        verify(pdfGenerationService).generate(same(DRAFT_MINI_PETITION_TEMPLATE_ID), placeHolderCaptor.capture());
        Map<String, Object> value = placeHolderCaptor.getValue();
        assertThat("Draft value set ", value.get(IS_DRAFT), is(true));
    }

    @Test
    public void testConvertToPdf() {
        final Map<String, Object> placeholders = new HashMap<>();

        byte[] test = "Any String you want".getBytes();
        placeholders.put("fileName",test);
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Mockito.when(pdfGenerationService.converToPdf(placeholders,"fileName")).thenReturn(test);

        Mockito.when(caseDocumentClient.uploadDocuments(eq(authToken),
                                                        eq(s2sToken), eq("PRLAPPS"), eq("PRIVATELAW"), any()))
            .thenReturn(uploadResponse);

        classUnderTest.converToPdf(placeholders, authToken,"fileName");

        verify(pdfGenerationService).converToPdf(placeholders,"fileName");
    }


    @Test
    public void testGenerateAndStoreFinalDocument_WithDynamicFileName() {
        placeholderMap.put("dynamic_fileName","test-file.pdf");
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Mockito.when(pdfGenerationService.generate(FINAL_MINI_PETITION_TEMPLATE_ID, placeholderMap)).thenReturn(data);
        Mockito.when(caseDocumentClient.uploadDocuments(eq(authToken),
                                                        eq(s2sToken), eq("PRLAPPS"), eq("PRIVATELAW"), any()))
            .thenReturn(uploadResponse);

        classUnderTest.generateAndStoreDocument(FINAL_MINI_PETITION_TEMPLATE_ID, placeholderMap, authToken);

        verify(pdfGenerationService).generate(same(FINAL_MINI_PETITION_TEMPLATE_ID), placeHolderCaptor.capture());
        Map<String, Object> value = placeHolderCaptor.getValue();
        Assert.assertNotNull(value);
    }

    @Test
    public void testGenerateAndStoreFinalDocument_WithOutDynamicFileName() {
        placeholderMap.remove("dynamic_fileName");
        Mockito.when(authTokenGenerator.generate()).thenReturn(s2sToken);
        Mockito.when(pdfGenerationService.generate(FINAL_MINI_PETITION_TEMPLATE_ID, placeholderMap)).thenReturn(data);
        Mockito.when(templatesConfiguration.getFileNameByTemplateName(FINAL_MINI_PETITION_TEMPLATE_ID))
            .thenReturn(DRAFT_MINI_PETITION_NAME_FOR_PDF_FILE);
        Mockito.when(caseDocumentClient.uploadDocuments(eq(authToken),
                                                        eq(s2sToken), eq("PRLAPPS"), eq("PRIVATELAW"), any()))
            .thenReturn(uploadResponse);

        classUnderTest.generateAndStoreDocument(FINAL_MINI_PETITION_TEMPLATE_ID, placeholderMap, authToken);

        verify(pdfGenerationService).generate(same(FINAL_MINI_PETITION_TEMPLATE_ID), placeHolderCaptor.capture());
        Map<String, Object> value = placeHolderCaptor.getValue();
        Assert.assertNotNull(value);
    }
}
