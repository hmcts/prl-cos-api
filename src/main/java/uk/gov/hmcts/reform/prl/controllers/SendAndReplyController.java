package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_ADDTIONAL_APPLICATION_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData.temporaryFields;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToStart;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToSubmit;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.getOpenMessages;


@Slf4j
@RestController
@RequestMapping("/send-and-reply-to-messages")
@SecurityRequirement(name = "Bearer Authentication")
public class SendAndReplyController extends AbstractCallbackController {
    private final SendAndReplyService sendAndReplyService;
    private final ElementUtils elementUtils;
    private final AllTabServiceImpl allTabService;
    private final UploadAdditionalApplicationService uploadAdditionalApplicationService;

    public static final String REPLY_AND_CLOSE_MESSAGE = "### What happens next \n\n A judge will review your message and advise.";
    public static final String SEND_AND_CLOSE_EXTERNAL_MESSAGE = """
        ### What happens next

        The court will send this message in a notification to the external party or parties.
        """;
    public static final String MESSAGES = "messages";

    @Autowired
    public SendAndReplyController(ObjectMapper objectMapper,
                                  EventService eventPublisher,
                                  SendAndReplyService sendAndReplyService,
                                  ElementUtils elementUtils,
                                  AllTabServiceImpl allTabService,
                                  UploadAdditionalApplicationService uploadAdditionalApplicationService) {
        super(objectMapper, eventPublisher);
        this.sendAndReplyService = sendAndReplyService;
        this.elementUtils = elementUtils;
        this.allTabService = allTabService;
        this.uploadAdditionalApplicationService = uploadAdditionalApplicationService;
    }

    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestHeader("Authorization")
                                                                   @Parameter(hidden = true) String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        caseDataMap.putAll(sendAndReplyService.setSenderAndGenerateMessageList(caseData, authorisation));

        caseDataMap.putAll(allTabService.getAllTabsFields(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestHeader("Authorization")
                                                               @Parameter(hidden = true) String authorisation,
                                                               @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());
        List<String> errors = new ArrayList<>();
        if (caseData.getChooseSendOrReply().equals(REPLY)) {
            if (!sendAndReplyService.hasMessages(caseData)) {
                errors.add("There are no messages to respond to.");
            } else {
                caseDataMap.putAll(sendAndReplyService.populateReplyMessageFields(caseData, authorisation));
            }
        }

        caseDataMap.putAll(allTabService.getAllTabsFields(caseData));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestHeader("Authorization")
                                                                    @Parameter(hidden = true) String authorisation,
                                                                    @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> caseDataMap = caseData.toMap(CcdObjectMapper.getObjectMapper());

        if (caseData.getChooseSendOrReply().equals(SEND)) {
            Message newMessage = sendAndReplyService.buildNewSendMessage(caseData);
            List<Element<Message>> listOfMessages = sendAndReplyService.addNewMessage(caseData, newMessage);
            caseDataMap.putAll(sendAndReplyService.returnMapOfOpenMessages(listOfMessages));

        } else {
            UUID selectedValue = elementUtils
                .getDynamicListSelectedValue(caseData.getReplyMessageDynamicList(), objectMapper);

            List<Element<Message>> messages;
            if (caseData.getMessageReply().getIsReplying().equals(YesOrNo.No)) {
                messages = sendAndReplyService.closeMessage(selectedValue, caseData);
                List<Element<Message>> closedMessages = messages.stream()
                    .filter(m -> m.getValue().getStatus().equals(MessageStatus.CLOSED))
                    .collect(Collectors.toList());

                if (ofNullable(caseData.getSendOrReplyDto().getClosedMessages()).isPresent()) {
                    closedMessages.addAll(caseData.getSendOrReplyDto().getClosedMessages());
                }

                messages.removeAll(closedMessages);
                caseDataMap.put("closedMessages", closedMessages);
            } else {
                messages = sendAndReplyService.buildNewReplyMessage(
                    selectedValue,
                    caseData.getMessageReply(),
                    caseData.getSendOrReplyDto().getOpenMessages()
                );
            }

            messages.sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));
            caseDataMap.put("openMessages", messages);
        }
        sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFields());

        // sort lists of messages with most recent first
        if (ofNullable(caseData.getSendOrReplyDto().getOpenMessages()).isPresent()) {
            caseData.getSendOrReplyDto().getOpenMessages().sort(Comparator.comparing(
                m -> m.getValue().getUpdatedTime(),
                Comparator.reverseOrder()
            ));
        }
        if (ofNullable(caseData.getSendOrReplyDto().getClosedMessages()).isPresent()) {
            caseData.getSendOrReplyDto().getClosedMessages().sort(Comparator.comparing(
                m -> m.getValue().getUpdatedTime(),
                Comparator.reverseOrder()
            ));
        }
        caseDataMap.putAll(allTabService.getAllTabsFields(caseData));

        if (caseDataMap.containsKey(CASE_TYPE_OF_APPLICATION) && caseDataMap.get(CASE_TYPE_OF_APPLICATION) == null) {
            caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getSelectedCaseTypeID());
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestHeader("Authorization")
                                                                @Parameter(hidden = true) String authorisation,
                                                                @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        List<Element<Message>> messages = caseData.getSendOrReplyDto().getOpenMessages();
        if (ofNullable(caseData.getSendOrReplyDto().getClosedMessages()).isPresent()) {
            messages.addAll(caseData.getSendOrReplyDto().getClosedMessages());
        }
        messages.sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));

        Message mostRecentMessage = messages.get(0).getValue();
        if (mostRecentMessage.getStatus().equals(MessageStatus.OPEN)) {
            sendAndReplyService.sendNotificationEmail(caseData, mostRecentMessage);
        }
        //if a message is being closed then no notification email is sent
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }


    @PostMapping("/send-or-reply-to-messages/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleSendOrMessageAboutToStart(@RequestHeader("Authorization")
                                                                                @Parameter(hidden = true) String authorisation,
                                                                                @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();

        //clear temp fields
        sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFieldsAboutToStart());

        caseDataMap.putAll(sendAndReplyService.setSenderAndGenerateMessageReplyList(caseData, authorisation));

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/send-or-reply-to-messages/mid-event")
    public CallbackResponse sendOrReplyToMessagesMidEvent(@RequestHeader("Authorization")
                                                               @Parameter(hidden = true) String authorisation,
                                                          @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        List<String> errors = new ArrayList<>();
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

    @PostMapping("/send-or-reply-to-messages/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse sendOrReplyToMessagesSubmit(@RequestHeader("Authorization")
                                                                            @Parameter(hidden = true) String authorisation,
                                                                            @RequestBody CallbackRequest callbackRequest) {
        try {
            log.info(
                "casedata from submitted callback for snr callbackRequest {}",
                objectMapper.writeValueAsString(callbackRequest)
            );
        } catch (JsonProcessingException e) {
            log.info("Json processing Exception as been thrown");
        }
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);
        Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();

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
                log.info("additionalApplicationCodeSelected while closing message {}", additionalApplicationCodeSelected);
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
                caseDataMap.put(MESSAGES, sendAndReplyService.replyAndAppendMessageHistory(caseData, authorisation, caseDataMap));
            }
            //WA - clear send field in case of REPLY
            sendAndReplyService.removeTemporaryFields(caseDataMap, "sendMessageObject");
        }

        //clear temp fields
        sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFieldsAboutToSubmit());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
    }


    @PostMapping("/send-or-reply-to-messages/submitted")
    public ResponseEntity<SubmittedCallbackResponse> handleSubmittedSendAndReply(@RequestHeader("Authorization")
                                                                                 @Parameter(hidden = true) String authorisation,
                                                                                 @RequestBody CallbackRequest callbackRequest) {
        CaseData caseData = getCaseData(callbackRequest.getCaseDetails());

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

        return ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping("/send-or-reply-to-messages/clear-dynamic-lists")
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
