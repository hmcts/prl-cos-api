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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;

import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;


@Slf4j
@RestController
@RequestMapping("/manage-documents")
@SecurityRequirement(name = "Bearer Authentication")
public class ManageDocumentsController extends AbstractCallbackController {
    private final ManageDocumentsService manageDocumentsService;
    @Qualifier("allTabsService")
    private final AllTabServiceImpl tabService;
    private final UserService userService;

    public static final String CONFIRMATION_HEADER = "# Documents submitted";
    public static final String CONFIRMATION_BODY = "### What happens next \n\n The court will review the submitted documents.";

    @Autowired
    protected ManageDocumentsController(ObjectMapper objectMapper, EventService eventPublisher,
                                        ManageDocumentsService manageDocumentsService,
                                        AllTabServiceImpl tabService,
                                        UserService userService) {
        super(objectMapper, eventPublisher);
        this.manageDocumentsService = manageDocumentsService;
        this.tabService = tabService;
        this.userService = userService;
    }

    @PostMapping("/about-to-start")
    public CallbackResponse handleAboutToStart(
        @RequestHeader("Authorization") @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        //PRL-3562 - populate document categories
        caseData = manageDocumentsService.populateDocumentCategories(authorisation, caseData);
        log.info("debolina_aboutToStartResponse {}", caseData);
        return CallbackResponse.builder()
            .data(caseData)
            .build();
    }

    @PostMapping(path = "/copy-manage-docs-mid", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Checking Error")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AboutToStartOrSubmitCallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    @SecurityRequirement(name = "Bearer Authentication")
    public AboutToStartOrSubmitCallbackResponse copyManageDocsMid(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest
    ) {
        String userRole = CaseUtils.getUserRole(userService.getUserDetails(authorisation));
        List<String> errorList = new ArrayList<>();
        if (SOLICITOR.equals(userRole)) {
            errorList = manageDocumentsService.precheckDocumentField(callbackRequest);

        }
        return AboutToStartOrSubmitCallbackResponse.builder().errors(errorList).build();

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
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(manageDocumentsService.copyDocument(callbackRequest, authorisation)).build();
    }

    @PostMapping("/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmitted(@RequestBody CallbackRequest callbackRequest,
                                                                     @RequestHeader(HttpHeaders.AUTHORIZATION)
                                                                     @Parameter(hidden = true) String authorisation) {

        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

        //update all tabs
        tabService.updateAllTabsIncludingConfTab(caseData);

        return ok(SubmittedCallbackResponse.builder()
                      .confirmationHeader(CONFIRMATION_HEADER)
                      .confirmationBody(CONFIRMATION_BODY)
                      .build());
    }

    @PostMapping("/validate-court-user")
    public AboutToStartOrSubmitCallbackResponse validateUserIfCourtSelected(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestBody CallbackRequest callbackRequest) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        if (manageDocumentsService.isCourtSelectedInDocumentParty(callbackRequest)
            && !manageDocumentsService.checkIfUserIsCourtStaff(authorisation)) {
            return AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("Only court admin/Judge can select the value 'court' for 'submitting on behalf of'"))
                .build();
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(updatedCaseData).build();
    }
}
