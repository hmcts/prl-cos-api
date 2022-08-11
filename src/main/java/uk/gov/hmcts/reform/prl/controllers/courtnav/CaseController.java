package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseCreationResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ResponseMessage;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavCaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.courtnav.CaseService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CaseController {

    private static final String SERVICE_AUTH = "ServiceAuthorization";

    private final CaseService caseService;
    private final AuthorisationService authorisationService;
    private final FL401ApplicationMapper fl401ApplicationMapper;

    @PostMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Third party to call this service to create a case in CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Case is created"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity createCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @RequestBody CourtNavCaseData inputData
    ) throws NotFoundException {

        log.info("s2s token inside case creation controller {}", serviceAuthorization);
        log.info("auth token inside case creation controller {}", authorisation);
        CaseData caseData = fl401ApplicationMapper.mapCourtNavData(inputData);
        log.info("CaseData=== : {}", caseData);

        if (Boolean.TRUE.equals(authorisationService.authorise(serviceAuthorization))) {

            CaseDetails caseDetails = caseService.createCourtNavCase(
                authorisation,
                caseData
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(new CaseCreationResponse(
                String.valueOf(caseDetails.getId())));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }


    @PostMapping(path = "{caseId}/document", produces = APPLICATION_JSON_VALUE, consumes = MULTIPART_FORM_DATA_VALUE)
    @Operation(description = "Uploading document for a specific case in CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document is uploaded"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity uploadDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @PathVariable("caseId") String caseId,
        @RequestParam MultipartFile file,
        @RequestParam String typeOfDocument
    ) {
        log.info("s2s token inside uploadDocument controller {}", serviceAuthorization);
        log.info("auth token inside uploadDocument controller {}", authorisation);
        if (Boolean.TRUE.equals(authorisationService.authorise(serviceAuthorization))) {
            caseService.uploadDocument(authorisation, file, typeOfDocument, caseId);
            return ResponseEntity.ok().body(new ResponseMessage("Document has been uploaded successfully: "
                                                                    + file.getOriginalFilename()));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
