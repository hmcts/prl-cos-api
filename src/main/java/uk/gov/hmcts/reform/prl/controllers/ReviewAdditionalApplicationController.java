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
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_ADDTIONAL_APPLICATION_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToStart;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToSubmit;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.getOpenMessages;

@Slf4j
@SuppressWarnings({"squid:S5665"})
@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class ReviewAdditionalApplicationController extends AbstractCallbackController {

    private final ReviewAdditionalApplicationService reviewAdditionalApplicationService;
    private final SendAndReplyService sendAndReplyService;
    private final AuthorisationService authorisationService;
    private final UploadAdditionalApplicationService uploadAdditionalApplicationService;

    public static final String SEND_AND_CLOSE_EXTERNAL_MESSAGE = """
        ### What happens next

        The court will send this message in a notification to the external party or parties.
        """;
    public static final String MESSAGES = "messages";
    public static final String CONFIRMATION_HEADER = "# Order approved";
    public static final String REPLY_AND_CLOSE_MESSAGE = "### What happens next \n\n Your message has been sent.";


    @Autowired
    public ReviewAdditionalApplicationController(ObjectMapper objectMapper,
                                                 EventService eventPublisher,
                                                 ReviewAdditionalApplicationService reviewAdditionalApplicationService,
                                                 SendAndReplyService sendAndReplyService,
                                                 AuthorisationService authorisationService,
                                                 UploadAdditionalApplicationService uploadAdditionalApplicationService) {
        super(objectMapper, eventPublisher);
        this.reviewAdditionalApplicationService = reviewAdditionalApplicationService;
        this.sendAndReplyService = sendAndReplyService;
        this.authorisationService = authorisationService;
        this.uploadAdditionalApplicationService = uploadAdditionalApplicationService;
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
            CaseData caseData = objectMapper.convertValue(
                callbackRequest.getCaseDetails().getData(),
                CaseData.class
            );
            Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
            //clear temp fields
            caseDataMap.remove("isAdditionalApplicationReviewed");
            caseDataMap.remove("selectedAdditionalApplicationsBundle");
            caseDataMap.remove("selectedAdditionalApplicationsId");
            sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFieldsAboutToStart());

            caseDataMap = reviewAdditionalApplicationService.populateReviewAdditionalApplication(
                caseData, caseDataMap, authorisation, clientContext, callbackRequest.getEventId());

            caseDataMap.putAll(sendAndReplyService.setSenderAndGenerateMessageList(caseData, authorisation));

            return AboutToStartOrSubmitCallbackResponse.builder()
                .data(caseDataMap).build();

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

            errors.add("Please review other applications");
            return CallbackResponse.builder().data(caseData).errors(errors).build();
        }
        if (REPLY.equals(caseData.getChooseSendOrReply())) {
            if (isEmpty(getOpenMessages(caseData.getSendOrReplyMessage().getMessages()))) {
                errors.add("There are no messages to respond to.");
            } else {
                caseData = sendAndReplyService.populateMessageReplyFields(caseData, authorisation);
            }
        } else {
            caseData = sendAndReplyService.populateDynamicListsForSendAndReply(caseData, authorisation);
        }

        return CallbackResponse.builder().data(caseData).errors(errors).build();
    }

    @PostMapping("/review-additional-application/populate-application")
    public CallbackResponse populateApplication(@RequestHeader("Authorization")
                                                          @Parameter(hidden = true) String authorisation,
                                                          @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<String> errors = new ArrayList<>();
        if (caseData.getReviewAdditionalApplicationWrapper() != null
            && YesOrNo.Yes.equals(caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed())) {

            if (SEND.equals(caseData.getChooseSendOrReply())
                && caseData.getReviewAdditionalApplicationWrapper().getSelectedAdditionalApplicationsBundle() != null) {
                Message message = caseData.getSendOrReplyMessage().getSendMessageObject();
                DynamicListElement dynamicListElement = message.getApplicationsList().getListItems().stream()
                    .filter(d -> d.getCode().equals(caseData.getReviewAdditionalApplicationWrapper()
                                           .getSelectedAdditionalApplicationsId())).findAny().orElse(null);
                if (Objects.nonNull(dynamicListElement)) {
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
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        if (caseData.getReviewAdditionalApplicationWrapper() != null
            && YesOrNo.Yes.equals(caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed())) {
            if (caseData.getChooseSendOrReply().equals(SEND)) {
                caseDataMap.put(MESSAGES, sendAndReplyService.addMessage(caseData, authorisation, caseDataMap));
                String additionalApplicationCodeSelected = sendAndReplyService.fetchAdditionalApplicationCodeIfExist(
                    caseData, SEND
                );

                if (null != additionalApplicationCodeSelected) {
                    caseDataMap.put(
                        AWP_ADDTIONAL_APPLICATION_BUNDLE,
                        uploadAdditionalApplicationService
                            .updateAwpApplicationStatus(
                                additionalApplicationCodeSelected,
                                caseData.getAdditionalApplicationsBundle(),
                                AWP_STATUS_IN_REVIEW
                            )
                    );
                }

                sendAndReplyService.sendNotificationToExternalParties(
                    caseData,
                    authorisation
                );

                //send emails in case of sending to others with emails
                sendAndReplyService.sendNotificationEmailOther(caseData);
                //WA - clear reply field in case of SEND
                sendAndReplyService.removeTemporaryFields(caseDataMap, "replyMessageObject");
            } else {
                if (YesOrNo.No.equals(caseData.getSendOrReplyMessage().getRespondToMessage())) {
                    //Reply & close
                    caseDataMap.put(MESSAGES, sendAndReplyService.closeMessage(caseData, caseDataMap));

                    // Update status of Additional applications if selected to Closed
                    String additionalApplicationCodeSelected = sendAndReplyService.fetchAdditionalApplicationCodeIfExist(
                        caseData, REPLY
                    );
                    log.info(
                        "additionalApplicationCodeSelected while closing message {}",
                        additionalApplicationCodeSelected
                    );
                    if (null != additionalApplicationCodeSelected) {
                        caseDataMap.put(
                            AWP_ADDTIONAL_APPLICATION_BUNDLE,
                            uploadAdditionalApplicationService
                                .updateAwpApplicationStatus(
                                    additionalApplicationCodeSelected,
                                    caseData.getAdditionalApplicationsBundle(),
                                    AWP_STATUS_CLOSED
                                )
                        );
                    }

                    // in case of reply and close message, removing replymessageobject for wa
                    sendAndReplyService.removeTemporaryFields(caseDataMap, "replyMessageObject");
                } else {
                    //Reply & append history
                    caseDataMap.put(
                        MESSAGES,
                        sendAndReplyService.replyAndAppendMessageHistory(
                            caseData,
                            authorisation,
                            caseDataMap
                        )
                    );
                }
                //WA - clear send field in case of REPLY
                sendAndReplyService.removeTemporaryFields(caseDataMap, "sendMessageObject");
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
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

        if (caseData.getReviewAdditionalApplicationWrapper() != null
            && YesOrNo.Yes.equals(caseData.getReviewAdditionalApplicationWrapper().getIsAdditionalApplicationReviewed())) {
            if (REPLY.equals(caseData.getChooseSendOrReply())
                && YesOrNo.Yes.equals(caseData.getSendOrReplyMessage().getRespondToMessage())) {
                return ok(SubmittedCallbackResponse.builder().confirmationBody(
                    REPLY_AND_CLOSE_MESSAGE
                ).build());
            }

            if (SEND.equals(caseData.getChooseSendOrReply()) && InternalExternalMessageEnum.EXTERNAL.equals(
                caseData.getSendOrReplyMessage().getSendMessageObject().getInternalOrExternalMessage())) {
                return ok(SubmittedCallbackResponse.builder().confirmationBody(
                    SEND_AND_CLOSE_EXTERNAL_MESSAGE
                ).build());
            }

            sendAndReplyService.closeAwPTask(caseData);
        }

        return ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping("/review-additional-application/clear-dynamic-lists")
    public AboutToStartOrSubmitCallbackResponse clearDynamicLists(@RequestHeader("Authorization")
                                                                  @Parameter(hidden = true) String authorisation,
                                                                  @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        Message message = caseData.getSendOrReplyMessage().getSendMessageObject();
        if (Objects.nonNull(message) && InternalExternalMessageEnum.EXTERNAL.equals(message.getInternalOrExternalMessage())) {
            if (!sendAndReplyService.atLeastOnePartySelectedForExternalMessage(message)) {
                return AboutToStartOrSubmitCallbackResponse.builder().errors(List.of(
                    "No recipients selected to send your message. Select at least one party")).build();
            }
        }
        //reset dynamic list fields
        caseData = sendAndReplyService.resetSendAndReplyDynamicLists(caseData);

        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
    }
}
