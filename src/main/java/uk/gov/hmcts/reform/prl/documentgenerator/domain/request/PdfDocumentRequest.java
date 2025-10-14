package uk.gov.hmcts.reform.prl.documentgenerator.domain.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.beans.JavaBean;
import java.util.Map;

@Slf4j
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdfDocumentRequest {

    private String accessKey;

    private String templateName;

    private String outputName;

    private String devMode;

    private boolean pdfTagged;

    private String pdfTitle;

    private Map<String,Object> data;
}
