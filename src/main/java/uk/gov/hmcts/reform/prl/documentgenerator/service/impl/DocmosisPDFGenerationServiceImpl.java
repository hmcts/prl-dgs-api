package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.PdfDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.exception.PDFGenerationException;
import uk.gov.hmcts.reform.prl.documentgenerator.mapper.TemplateDataMapper;
import uk.gov.hmcts.reform.prl.documentgenerator.service.PDFGenerationService;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;

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

    @Value("${docmosis.service.pdf-service.convert}")
    private String docmosisPdfConvertEndpoint;

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
    public byte[] converToPdf(Map<String, Object> placeholders, String fileName) {

        try {
            String filename = FilenameUtils.getBaseName(fileName) + ".pdf";
            ObjectMapper objectMapper = new ObjectMapper();
            byte[] docInBytes = objectMapper.convertValue(placeholders.get("fileName"), byte[].class);
            // byte[] docInBytes = (byte[]) placeholders.get("fileName");
            File file = new File(fileName);
            Files.write(docInBytes, file);

            return restTemplate
                .postForObject(
                    docmosisPdfConvertEndpoint,
                    createRequest(file, filename),
                    byte[].class
                );

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    private HttpEntity<MultiValueMap<String, Object>> createRequest(
        File file,
        String outputFilename
    ) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("accessKey", docmosisPdfServiceAccessKey);
        body.add("outputName", outputFilename);
        body.add("outputFormat", "pdf");
        body.add("file", new FileSystemResource(file));

        return new HttpEntity<>(body, headers);
    }

}
