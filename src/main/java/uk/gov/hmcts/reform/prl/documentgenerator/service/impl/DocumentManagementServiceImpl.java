package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.documentgenerator.config.TemplatesConfiguration;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.service.DocumentManagementService;
import uk.gov.hmcts.reform.prl.documentgenerator.service.PDFGenerationService;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentManagementServiceImpl implements DocumentManagementService {

    private static final String CURRENT_DATE_KEY = "current_date";
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'hh:mm:ss.SSS";

    private static final String DRAFT_PREFIX = "Draft";
    private static final String IS_DRAFT = "isDraft";
    public static final String DYNAMIC_FILE_NAME = "dynamic_fileName";

    private final Clock clock = Clock.systemDefaultZone();

    private final PDFGenerationService generatorService;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final TemplatesConfiguration templatesConfiguration;

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${docmosis.service.pdf-service.accessKey}")
    private String docmosisSecret;

    @Value("${idam.s2s-auth.totp_secret}")
    private String s2sSecret;

    private static final String USER_ROLES = "user-roles";
    private static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    private static final String COURT_ADMIN_ROLE = "caseworker-privatelaw-courtadmin";

    @Override
    public GeneratedDocumentInfo generateAndStoreDocument(String templateName, Map<String, Object> placeholders,
                                                          String authorizationToken) {
        String fileName = "";
        if (placeholders.containsKey(DYNAMIC_FILE_NAME)) {
            fileName = String.valueOf(placeholders.get(DYNAMIC_FILE_NAME));
        } else {
            fileName = templatesConfiguration.getFileNameByTemplateName(templateName);
        }
        return getGeneratedDocumentInfo(templateName, placeholders, authorizationToken, fileName);
    }

    @Override
    public GeneratedDocumentInfo generateAndStoreDraftDocument(String templateName,
                                                               Map<String, Object> placeholders,
                                                               String authorizationToken) {
        String fileName = "";
        if (placeholders.containsKey(DYNAMIC_FILE_NAME)) {
            fileName = String.valueOf(placeholders.get(DYNAMIC_FILE_NAME));
        } else {
            fileName = templatesConfiguration.getFileNameByTemplateName(templateName);
        }
        if (!fileName.startsWith(DRAFT_PREFIX)) {
            fileName = String.join("", DRAFT_PREFIX, fileName);
        }
        placeholders.put(IS_DRAFT, true);

        return getGeneratedDocumentInfo(templateName, placeholders, authorizationToken, fileName);
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo(String templateName, Map<String, Object> placeholders,
                                                           String authorizationToken, String fileName) {
        String caseId = getCaseId(placeholders);
        if (caseId == null) {
            log.warn("caseId is null for template \"" + templateName + "\"");
        }

        log.info("Generating document for case Id {}", caseId);

        placeholders.put(
            CURRENT_DATE_KEY,
            new SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                .format(Date.from(clock.instant())
                )
        );

        byte[] generatedDocument = generateDocument(templateName, placeholders);
        log.info("Document generated for case Id {}", caseId);
        return storeDocument(generatedDocument, authorizationToken, fileName);
    }

    @Override
    public GeneratedDocumentInfo storeDocument(byte[] document, String authorizationToken, String fileName) {
        log.debug("Store document requested with document of size [{}]", document.length);
        String serviceAuthToken = authTokenGenerator.generate();

        UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
            authorizationToken,
            serviceAuthToken,
            "PRLAPPS",
            "PRIVATELAW",
            Arrays.asList(new InMemoryMultipartFile("files", fileName, APPLICATION_PDF_VALUE, document
            ))
        );

        Document uploadedDocument = uploadResponse.getDocuments().get(0);

        return GeneratedDocumentInfo.builder()
            .url(uploadedDocument.links.self.href)
            .mimeType(uploadedDocument.mimeType)
            .hashToken(uploadedDocument.hashToken)
            .binaryUrl(uploadedDocument.links.binary.href)
            .docName(fileName)
            .build();
    }

    @Override
    public byte[] generateDocument(String templateName, Map<String, Object> placeholders) {
        log.debug("Generate document requested with templateName [{}], placeholders of size[{}]",
                  templateName, placeholders.size()
        );

        return generatorService.generate(templateName, placeholders);
    }

    private String getCaseId(Map<String, Object> placeholders) {
        Map<String, Object> caseDetails = (Map<String, Object>) placeholders.getOrDefault("caseDetails", emptyMap());
        return (String) caseDetails.get("id");
    }

    @Override
    public GeneratedDocumentInfo converToPdf(Map<String, Object> placeholders, String authorizationToken, String fileName) {
        log.debug(
            "Generate document requested with templateName [{}], placeholders of size[{}]",
            placeholders.size()
        );

        byte[] generatedDocument = generatorService.converToPdf(placeholders, fileName);
        return storeDocument(generatedDocument, authorizationToken, FilenameUtils.getBaseName(fileName) + ".pdf");
    }

    @Override
    public ResponseEntity<byte[]> downloadFromDmStore(@NonNull final UUID documentId) throws Exception {
        String binaryFileUrl = "http://dm-store-aat.service.core-compute-aat.internal/documents/" + documentId + "/binary";
        log.info("DmStore Download file: {}", binaryFileUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set(SERVICE_AUTHORIZATION, authTokenGenerator.generate());
        headers.set(USER_ROLES, COURT_ADMIN_ROLE);
        HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
        String url;
        try {
            url = "http://dm-store-aat.service.core-compute-aat.internal" + new URI(binaryFileUrl).getPath();
        } catch (URISyntaxException e) {
            log.error("Failed to rewrite the url for document for {}, error message {}", binaryFileUrl, e.getMessage());
            throw new Exception(format("Failed to rewrite the url for document for %s and error %s",
                                       binaryFileUrl, e.getMessage()));
        }

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, byte[].class);
        if (response.getStatusCode() != HttpStatus.OK) {
            log.error("Failed to get bytes from document store for document {} ", binaryFileUrl);
            throw new RuntimeException(format("Unexpected code from DM store: %s ", response.getStatusCode()));
        }

        log.info("File download status : {} ", response.getStatusCode());
        return response;
    }
}
