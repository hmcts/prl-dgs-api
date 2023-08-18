package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.PdfDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;
import uk.gov.hmcts.reform.prl.documentgenerator.mapper.TemplateDataMapper;
import uk.gov.hmcts.reform.prl.documentgenerator.service.PDFGenerationService;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static org.apache.commons.fileupload.FileUploadBase.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

@Service
@Slf4j
public class DocmosisPDFGenerationServiceImpl implements PDFGenerationService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private TemplateDataMapper templateDataMapper;

    @Value("${docmosis.service.pdf-service.uri}")
    private String docmosisPdfServiceEndpoint;

    @Value("${docmosis.service.pdf-service.accessKey}")
    private String docmosisPdfServiceAccessKey;

    @Value("${docmosis.service.pdf-service.devMode}")
    private String docmosisDevMode;

    @Override
    public byte[] generate(String templateName, Map<String, Object> placeholders) {
        checkArgument(!isNullOrEmpty(templateName), "document generation template cannot be empty");
        checkNotNull(placeholders, "placeholders map cannot be null");

        log.info("Making request to pdf service to generate pdf document with template [{}]"
            + " and placeholders of size [{}]", templateName, placeholders.size());

        try {
            // Remove this log when tested
            log.info("Making Docmosis Request From {}", docmosisPdfServiceEndpoint);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<PdfDocumentRequest> httpEntity = new HttpEntity<>(request(templateName, placeholders), headers);

            ResponseEntity<byte[]> response =
                restTemplate.exchange(docmosisPdfServiceEndpoint, HttpMethod.POST, httpEntity, byte[].class);

            return response.getBody();
        } catch (Exception e) {
            throw new PDFGenerationException("Failed to request PDF from REST endpoint " + e.getMessage(), e);
        }
    }

    private PdfDocumentRequest request(String templateName, Map<String, Object> placeholders) {
        return PdfDocumentRequest.builder()
            .accessKey(docmosisPdfServiceAccessKey)
            .templateName(templateName)
            .outputName("result.pdf")
            .devMode(docmosisDevMode)
            .data(templateDataMapper.map(placeholders))
            .build();
    }

    @Override
    public byte[] converToPdf(Map<String, Object> placeholders) {
        checkNotNull(placeholders, "placeholders map cannot be null");

        log.info("Making request to pdf service to generate pdf document "
                     + " and placeholders of size [{}]", placeholders.size());

        try {
            // Remove this log when tested
            log.info("Making Docmosis Request From {}", docmosisPdfServiceEndpoint);
            final ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .build();

            final MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
            fileMap.add(CONTENT_DISPOSITION, contentDisposition.toString());

            final MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new HttpEntity<>(placeholders.get("fileName"), fileMap));
            body.add("accessKey", docmosisPdfServiceAccessKey);

            final HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MULTIPART_FORM_DATA);

            final HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            try {
                return restTemplate
                    .exchange(
                        docmosisPdfServiceEndpoint + "/rs/convert",
                        HttpMethod.POST,
                        requestEntity,
                        byte[].class)
                    .getBody();
            } catch (HttpClientErrorException.BadRequest ex) {
                log.error("Document conversion failed" + ex.getResponseBodyAsString());
                throw ex;
            }

        } catch (Exception e) {
            throw new PDFGenerationException("Failed to request PDF from REST endpoint " + e.getMessage(), e);
        }
    }

}
