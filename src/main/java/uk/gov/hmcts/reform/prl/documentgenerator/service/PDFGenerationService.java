package uk.gov.hmcts.reform.prl.documentgenerator.service;

import java.util.Map;

public interface PDFGenerationService {

    byte[] generate(String templateName, Map<String, Object> placeholders);

    byte[] convertToPdf(Map<String, Object> placeholders, String fileName);

}
