package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.controllers.AbstractCallbackController;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REASONS_SELECTED_FOR_LIST_ON_NOTICE;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@SuppressWarnings({"java:S107","java:S5665"})
public class ListWithoutNoticeController extends AbstractCallbackController {
    public static final String LIST_WITHOUT_NOTICE_HEARING_INSTRUCTION = "listWithoutNoticeHearingInstruction";
    private final AddCaseNoteService addCaseNoteService;
    private final UserService userService;
    private final AuthorisationService authorisationService;
    public static final String CONFIRMATION_HEADER = "# Listing instructions sent to admin";
    public static final String CONFIRMATION_BODY_PREFIX_CA = """
        ### What happens next
        Admin will be notified to list the case without notice.
        The hearing instructions will be saved in case notes.
        """;

    @Autowired
    public ListWithoutNoticeController(ObjectMapper objectMapper,
                                       EventService eventPublisher,
                                       AuthorisationService authorisationService,
                                       AddCaseNoteService addCaseNoteService,
                                       UserService userService) {
        super(objectMapper, eventPublisher);
        this.authorisationService = authorisationService;
        this.addCaseNoteService = addCaseNoteService;
        this.userService = userService;
    }

    @PostMapping(path = "/listWithoutNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse c100ListWithoutNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            CaseNoteDetails currentCaseNoteDetails = addCaseNoteService.getCurrentCaseNoteDetails(
                REASONS_SELECTED_FOR_LIST_ON_NOTICE,
                caseData.getListWithoutNoticeDetails().getListWithoutNoticeHearingInstruction(),
                userService.getUserDetails(authorisation)
            );
            caseDataUpdated.put(
                CASE_NOTES,
                addCaseNoteService.getCaseNoteDetails(caseData, currentCaseNoteDetails)
            );
            caseDataUpdated.remove(LIST_WITHOUT_NOTICE_HEARING_INSTRUCTION);
            CaseUtils.setCaseState(callbackRequest, caseDataUpdated);
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }


    @PostMapping(path = "/listWithoutNotice-confirmation", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to List without notice confirmation . Returns service request reference if "
        + "successful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback processed.",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = uk.gov.hmcts.reform.ccd.client.model.CallbackResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public ResponseEntity<SubmittedCallbackResponse> c100CcdSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER).confirmationBody(
                CONFIRMATION_BODY_PREFIX_CA
            ).build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
