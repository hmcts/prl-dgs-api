package uk.gov.hmcts.reform.prl.documentgenerator.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import javax.validation.constraints.NotBlank;

@Value
@Schema(description = "Request body model for Document Generation Request")
public class UploadDocumentRequest {
    @Schema(description = "Name of the file", required = true)
    @JsonProperty(value = "fileName", required = true)
    @NotBlank
    private final String fileName;
    @JsonProperty(value = "document", required = true)
    @Schema(description = "Placeholder for the documents to be uploaded", required = true)
    private final byte[] document;
}
