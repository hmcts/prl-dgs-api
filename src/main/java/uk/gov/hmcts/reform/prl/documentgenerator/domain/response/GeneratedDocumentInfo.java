package uk.gov.hmcts.reform.prl.documentgenerator.domain.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GeneratedDocumentInfo {
    private String url;
    private String mimeType;
    private String createdOn;
    private String hashToken;
    private String binaryUrl;
    private String docName;
}
