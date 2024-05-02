package uk.gov.hmcts.reform.prl.documentgenerator.service;

import java.io.IOException;
import java.util.Map;

public interface PDFGenerationService {

    byte[] generate(String templateName, Map<String, Object> placeholders);

    byte[] converToPdf(Map<String, Object> placeholders, String fileName) throws IOException;

}
