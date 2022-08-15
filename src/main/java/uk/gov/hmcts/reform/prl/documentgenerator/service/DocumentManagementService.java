package uk.gov.hmcts.reform.prl.documentgenerator.service;

import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.CreatedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;

import java.util.Map;

public interface DocumentManagementService {
    GeneratedDocumentInfo generateAndStoreDocument(String templateName, Map<String, Object> placeholders,
                                                   String authorizationToken);

    GeneratedDocumentInfo generateAndStoreDraftDocument(String templateName, Map<String, Object> placeholders,
                                                   String authorizationToken);

    GeneratedDocumentInfo storeDocument(byte[] document, String authorizationToken, String fileName);

    byte[] generateDocument(String templateName, Map<String, Object> placeholders);

    CreatedDocumentInfo createDocument(String templateName, Map<String, Object> placeholders,
                                       String authorizationToken);
}
