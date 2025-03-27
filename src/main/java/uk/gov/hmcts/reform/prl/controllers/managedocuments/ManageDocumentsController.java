package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;


@Slf4j
@RestController
@RequestMapping("/manage-documents")
@SecurityRequirement(name = "Bearer Authentication")
public class ManageDocumentsController extends AbstractCallbackController {
    private final ManageDocumentsService manageDocumentsService;
    private final UserService userService;
    public static final String CONFIRMATION_HEADER = "# Documents submitted";
    public static final String CONFIRMATION_BODY = "### What happens next \n\n The court will review the submitted documents.";

    @Autowired
    protected ManageDocumentsController(ObjectMapper objectMapper, EventService eventPublisher,
                                        ManageDocumentsService manageDocumentsService,
                                        UserService userService) {
        super(objectMapper, eventPublisher);
        this.manageDocumentsService = manageDocumentsService;
        this.userService = userService;
    }

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        //PRL-3562 - populate document categories
        caseData = manageDocumentsService.populateDocumentCategories(authorisation, caseData);
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/mid-event", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Checking Error")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse validateManageDocumentsData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        UserDetails userDetails = userService.getUserDetails(authorisation);
        //validation for empty restricted reason for solicitor

        final String[] surname = {null};
        userDetails.getSurname().ifPresent(snm -> surname[0] = snm);
        UserDetails updatedUserDetails = UserDetails.builder()
            .email(userDetails.getEmail())
            .id(userDetails.getId())
            .surname(surname[0])
            .forename(userDetails.getForename() != null ? userDetails.getForename() : null)
            .roles(manageDocumentsService.getLoggedInUserType(authorisation))
            .build();

        List<String> errorList = manageDocumentsService.validateRestrictedReason(callbackRequest, userDetails);

        //validation for documentParty - COURT to be selected only for court staff
        errorList.addAll(manageDocumentsService.validateCourtUser(callbackRequest, updatedUserDetails));
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        if (CollectionUtils.isNotEmpty(errorList)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(errorList)
                .build();
        }

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData).build();

    }

    @PostMapping(path = "/copy-manage-docs", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Copy manage docs for tabs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse copyManageDocs(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        return AboutToStartOrSubmitCallbackResponse
            .builder()
            .data(manageDocumentsService.copyDocument(callbackRequest, authorisation))
            .build();
    }

    @PostMapping("/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                     @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                     @Parameter(hidden = true) String authorisation) {

        manageDocumentsService.appendConfidentialDocumentNameForCourtAdminAndUpdate(
            callbackRequest,
            authorisation
        );

        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(CONFIRMATION_HEADER)
                      .confirmationBody(CONFIRMATION_BODY)
                      .build());
    }
}
