package uk.gov.hmcts.reform.prl.documentgenerator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.request.GenerateDocumentRequest;
import uk.gov.hmcts.reform.prl.documentgenerator.domain.response.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.documentgenerator.service.DocumentManagementService;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/version/1")
@Tag(name = "Document Generation", description = "Document Generation")
@Slf4j
public class DocumentGeneratorController {

    @Autowired
    private DocumentManagementService documentManagementService;

    @Operation(description = "Generate PDF document based on the supplied template name and placeholder "
            + "texts and saves it in the evidence management.", tags = {"Document Generation"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF was generated successfully and stored in the "
                    + "evidence management. Returns the url to the stored document.", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Returned when input parameters are invalid "
                    + "or template not found", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "503", description = "Returned when the PDF Service or Evidence "
                    +  "Management Client Api cannot be reached", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Returned when there is an unknown server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
        })
    @PostMapping("/generatePDF")
    public GeneratedDocumentInfo generateAndUploadPdf(
        @RequestHeader(value = "Authorization", required = false)
            String authorizationToken,
        @Parameter(name = "GenerateDocumentRequest",description = "JSON object containing "
                + "the templateName and the placeholder text map", required = true)
        @RequestBody
        @Valid
            GenerateDocumentRequest templateData) {
        //This service is internal to PRL system. No need to do service authentication here
        log.info("Document generation requested with templateName [{}], placeholders map of size[{}]",
                templateData.getTemplate(), templateData.getValues().size());
        return documentManagementService.generateAndStoreDocument(templateData.getTemplate(), templateData.getValues(),
            authorizationToken);
    }

    @Operation(description = "Generate draft PDF document based on the supplied template name and placeholder texts "
            + "and saves it in the evidence management.", tags = {"Document Generation"})
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF was generated successfully and stored in the "
                    + "evidence management. Returns the url to the stored document.",content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "400", description = "Returned when input parameters are invalid or "
                    + "template not found", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "503", description = "Returned when the PDF Service or Evidence Management "
                    + "Client Api cannot be reached", content =
            @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "500", description = "Returned when there is an unknown server error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class)))
        })
    @PostMapping("/generateDraftPDF")
    public GeneratedDocumentInfo generateAndUploadDraftPdf(
        @RequestHeader(value = "Authorization", required = false)
            String authorizationToken,
        @Parameter(name = "GenerateDocumentRequest",description = "JSON object containing the "
                + "templateName and the placeholder text map", required = true)
        @RequestBody
        @Valid
            GenerateDocumentRequest templateData) {
        //This service is internal to Divorce system. No need to do service authentication here
        log.info("Document generation requested with templateName [{}], placeholders map of size[{}]",
                templateData.getTemplate(), templateData.getValues().size());
        return documentManagementService.generateAndStoreDraftDocument(templateData.getTemplate(),
                templateData.getValues(), authorizationToken);
    }

}
