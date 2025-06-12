package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
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
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyCommonService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToStart;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToSubmit;

@Slf4j
@SuppressWarnings({"squid:S5665"})
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewAdditionalApplicationController extends AbstractCallbackController {

    private final ReviewAdditionalApplicationService reviewAdditionalApplicationService;
    private final SendAndReplyService sendAndReplyService;
    private final AuthorisationService authorisationService;
    private final SendAndReplyCommonService sendAndReplyCommonService;

    public static final String CONFIRMATION_HEADER = "# Order approved";

    @Autowired
    public ReviewAdditionalApplicationController(ObjectMapper objectMapper,
                                                 EventService eventPublisher,
                                                 ReviewAdditionalApplicationService reviewAdditionalApplicationService,
                                                 SendAndReplyService sendAndReplyService,
                                                 AuthorisationService authorisationService,
                                                 SendAndReplyCommonService sendAndReplyCommonService) {
        super(objectMapper, eventPublisher);
        this.reviewAdditionalApplicationService = reviewAdditionalApplicationService;
        this.sendAndReplyService = sendAndReplyService;
        this.authorisationService = authorisationService;
        this.sendAndReplyCommonService = sendAndReplyCommonService;
    }

    @PostMapping(path = "/review-additional-application/about-to-start",
        consumes = APPLICATION_JSON, produces = APPLICATION_JSON)
    @Operation(description = "Remove dynamic list from the caseData")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Callback to populate review additional application"),
        @ApiResponse(responseCode = "400", description = "Bad Request", content = @Content)})
    public AboutToStartOrSubmitCallbackResponse aboutToStartReviewAdditionalApplication(
        @RequestHeader(HttpHeaders.AUTHORIZATION) @Parameter(hidden = true) String authorisation,
        @RequestHeader(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER) String s2sToken,
        @RequestHeader(value = PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER, required = false) String clientContext,
        @RequestBody CallbackRequest callbackRequest) {

        if (authorisationService.isAuthorized(authorisation, s2sToken)) {
            Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();

            //clear temp fields
            caseDataMap.remove("isAdditionalApplicationReviewed");
            caseDataMap.remove("selectedAdditionalApplicationsBundle");
            sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFieldsAboutToStart());

            CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
            caseDataMap = reviewAdditionalApplicationService.populateReviewAdditionalApplication(
                caseData, caseDataMap, clientContext, callbackRequest.getEventId());

            caseDataMap.putAll(sendAndReplyService.setSenderAndGenerateMessageReplyList(caseData, authorisation));

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataMap)
                .build();
        } else {
            throw (new RuntimeException(INVALID_CLIENT));
        }
    }

    @PostMapping("/review-additional-application/mid-event")
    public CallbackResponse reviewAdditionalApplicatonMidEvent(@RequestHeader("Authorization")
                                                          @Parameter(hidden = true) String authorisation,
                                                          @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<String> errors = new ArrayList<>();
        if (caseData.getReviewAdditionalApplicationWrapper() != null
            && YesOrNo.No.equals(caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed())) {
            errors.add("Please review this application");
        } else if (caseData.getReviewAdditionalApplicationWrapper() == null
            || caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed() == null) {
            errors.add("Have you reviewed the additional application? is required");
        } else if (caseData.getReviewAdditionalApplicationWrapper() != null
            && YesOrNo.Yes.equals(caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed())) {
            caseData.setChooseSendOrReply(SEND);
            return populateApplication(caseData, authorisation);
        }
        return CallbackResponse.builder().data(caseData).errors(errors).build();
    }

    private CallbackResponse populateApplication(CaseData caseData, String authorisation) {

        List<String> errors = new ArrayList<>();
        if (SEND.equals(caseData.getChooseSendOrReply())) {
            caseData = sendAndReplyService.populateDynamicListsForSendAndReply(caseData, authorisation);
            if (caseData.getReviewAdditionalApplicationWrapper() != null
                && caseData.getReviewAdditionalApplicationWrapper().getSelectedAdditionalApplicationsBundle() != null) {
                Message message = caseData.getSendOrReplyMessage().getSendMessageObject();
                String applicationCode = reviewAdditionalApplicationService
                    .getApplicationBundleDynamicCode(caseData.getReviewAdditionalApplicationWrapper()
                                                         .getSelectedAdditionalApplicationsBundle());
                DynamicListElement dynamicListElement = message.getApplicationsList().getListItems().stream()
                    .filter(d -> d.getCode().equals(applicationCode)).findAny().orElse(null);
                if (Objects.nonNull(dynamicListElement)) {
                    message.setMessageAbout(MessageAboutEnum.APPLICATION);
                    message.getApplicationsList().setValue(dynamicListElement);
                }
            }
        }

        return CallbackResponse.builder().data(caseData).errors(errors).build();
    }

    @PostMapping("/review-additional-application/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse aboutToSubmitReviewAdditionalApplication(@RequestHeader("Authorization")
                                                                            @Parameter(hidden = true) String authorisation,
                                                                            @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        caseData.setChooseSendOrReply(SEND);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (caseData.getReviewAdditionalApplicationWrapper() != null
            && YesOrNo.Yes.equals(caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed())) {
            if (caseData.getChooseSendOrReply().equals(SEND)) {
                sendAndReplyCommonService.sendMessages(authorisation, caseData, caseDataMap);
            }
        }

        //clear temp fields
        sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFieldsAboutToSubmit());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
    }


    @PostMapping("/review-additional-application/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmittedSendAndReply(@RequestHeader("Authorization")
                                                                                 @Parameter(hidden = true) String authorisation,
                                                                                 @RequestBody CallbackRequest callbackRequest) {
        return sendAndReplyService.sendAndReplySubmitted(callbackRequest);
    }

    @PostMapping("/review-additional-application/clear-dynamic-lists")
    public AboutToStartOrSubmitCallbackResponse clearDynamicLists(@RequestHeader("Authorization")
                                                                  @Parameter(hidden = true) String authorisation,
                                                                  @RequestBody CallbackRequest callbackRequest) {
        return sendAndReplyService.clearDynamicLists(callbackRequest);
    }
}
