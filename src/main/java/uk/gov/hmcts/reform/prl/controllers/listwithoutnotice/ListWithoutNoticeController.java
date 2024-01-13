package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NOTES;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;

@Slf4j
@RestController
@SecurityRequirement(name = "Bearer Authentication")
@SuppressWarnings({"java:S107"})
public class ListWithoutNoticeController extends AbstractCallbackController {
    public static final String LISTING_INSTRUCTIONS_SENT_TO_ADMIN = "Listing instructions sent to admin";
    private final AddCaseNoteService addCaseNoteService;
    private final UserService userService;
    private final AuthorisationService authorisationService;
    public static final String CONFIRMATION_HEADER = "# Listing instructions sent to admin";
    public static final String CONFIRMATION_BODY_PREFIX = """
        ### What happens next
        Admin will be notified to list the case without notice
        The hearing instructions will be saved in case notes""";

    @Autowired
    public ListWithoutNoticeController(ObjectMapper objectMapper,
                                       EventService eventPublisher,
                                       AddCaseNoteService addCaseNoteService,
                                       UserService userService,
                                       AuthorisationService authorisationService) {
        super(objectMapper, eventPublisher);
        this.addCaseNoteService = addCaseNoteService;
        this.userService = userService;
        this.authorisationService = authorisationService;
    }


    @PostMapping(path = "/pre-populate-hearingPage-Data", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Callback to populate Hearing page details")
    public AboutToStartOrSubmitCallbackResponse prePopulateHearingPageData(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) throws NotFoundException {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            String caseReferenceNumber = String.valueOf(callbackRequest.getCaseDetails().getId());
            log.info("Inside Prepopulate prePopulateHearingPageData for the case id {}", caseReferenceNumber);
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataUpdated).build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping(path = "/listWithoutNotice", consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "List Without Notice submission flow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List Without notice submission is success"),
        @ApiResponse(responseCode = "400", description = "Bad Request")})
    public AboutToStartOrSubmitCallbackResponse listWithoutNoticeSubmission(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody CallbackRequest callbackRequest) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            log.info("Without Notice Submission flow - case id : {}", callbackRequest.getCaseDetails().getId());
            Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            if (!StringUtils.isEmpty(caseData.getListWithoutNoticeDetails().getListWithoutNoticeHearingInstruction())) {
                caseData = caseData.toBuilder()
                    .caseNote(caseData.getListWithoutNoticeDetails().getListWithoutNoticeHearingInstruction())
                    .subject(LISTING_INSTRUCTIONS_SENT_TO_ADMIN)
                        .build();
                caseDataUpdated.put(
                    CASE_NOTES,
                    addCaseNoteService.addCaseNoteDetails(
                        caseData,
                        userService.getUserDetails(authorisation)
                    )
                );
                addCaseNoteService.clearFields(caseDataUpdated);
            }
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
    public ResponseEntity<SubmittedCallbackResponse> ccdSubmitted(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestBody uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest callbackRequest
    ) {
        if (authorisationService.isAuthorized(authorisation,s2sToken)) {
            return ok(SubmittedCallbackResponse.builder().confirmationHeader(
                CONFIRMATION_HEADER).confirmationBody(
                CONFIRMATION_BODY_PREFIX
            ).build());
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }
}
