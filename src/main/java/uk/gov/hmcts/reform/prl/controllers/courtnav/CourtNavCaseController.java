package uk.gov.hmcts.reform.prl.controllers.courtnav;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassUploadDocService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.CAFCASS_USER_ROLE;

@Slf4j
@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class CourtNavCaseController {

    private static final String SERVICE_AUTH = "ServiceAuthorization";

    private final CourtNavCaseService courtNavCaseService;
    private final AuthorisationService authorisationService;
    private final FL401ApplicationMapper fl401ApplicationMapper;
    private  final CafcassUploadDocService cafcassUploadDocService;
    private final SystemUserService systemUserService;

    @PostMapping(path = "/case", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Third party to call this service to create a case in CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Case is created"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "403", description = "Forbidden"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Object> createCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @Valid @RequestBody CourtNavFl401 inputData
    ) throws Exception {

        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
            authorisationService.authoriseService(serviceAuthorization))) {
            CaseData caseData = fl401ApplicationMapper.mapCourtNavData(inputData);
            CaseDetails caseDetails = courtNavCaseService.createCourtNavCase(
                authorisation,
                caseData
            );
            log.info("Case has been created {}", caseDetails.getId());
            courtNavCaseService.refreshTabs(authorisation, String.valueOf(caseDetails.getId()));
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
    public ResponseEntity<Object> uploadDocument(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(SERVICE_AUTH) String serviceAuthorization,
        @PathVariable("caseId") String caseId,
        @RequestParam MultipartFile file,
        @RequestParam String typeOfDocument
    ) {
        if (Boolean.TRUE.equals(authorisationService.authoriseUser(authorisation)) && Boolean.TRUE.equals(
            authorisationService.authoriseService(serviceAuthorization))) {

            if (authorisationService.getUserInfo().getRoles().contains(CAFCASS_USER_ROLE)) {
                log.info("uploading cafcass document");
                cafcassUploadDocService.uploadDocument(systemUserService.getSysUserToken(), file, typeOfDocument, caseId);
            } else {
                courtNavCaseService.uploadDocument(authorisation, file, typeOfDocument, caseId);
            }
            return ResponseEntity.ok().body(new ResponseMessage("Document has been uploaded successfully: "
                                                                    + file.getOriginalFilename()));
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
