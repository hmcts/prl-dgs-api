package uk.gov.hmcts.reform.prl.documentgenerator.service;

import lombok.NonNull;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;

import java.util.Map;
import java.util.UUID;

public interface DocumentManagementService {
    GeneratedDocumentInfo generateAndStoreDocument(String templateName, Map<String, Object> placeholders,
                                                   String authorizationToken);

    GeneratedDocumentInfo generateAndStoreDraftDocument(String templateName, Map<String, Object> placeholders,
                                                        String authorizationToken);

    GeneratedDocumentInfo storeDocument(byte[] document, String authorizationToken, String fileName);

    byte[] generateDocument(String templateName, Map<String, Object> placeholders);

    GeneratedDocumentInfo converToPdf(Map<String, Object> placeholders, String authorizationToken, String filename);

    ResponseEntity<byte[]> downloadFromDmStore(@NonNull UUID documentId) throws Exception;
}
