package uk.gov.hmcts.reform.prl.documentgenerator.domain.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatedDocumentInfo {
    private String fileName;
    private byte[] document;
}
