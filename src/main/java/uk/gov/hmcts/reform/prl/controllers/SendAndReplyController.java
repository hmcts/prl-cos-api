package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
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
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isEmpty;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData.temporaryFields;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToStart;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToSubmit;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.getOpenMessages;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/send-and-reply-to-messages")
@SecurityRequirement(name = "Bearer Authentication")
public class SendAndReplyController extends AbstractCallbackController {

    @Autowired
    SendAndReplyService sendAndReplyService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ElementUtils elementUtils;

    @Autowired
    AllTabServiceImpl allTabService;

    public static final String REPLY_AND_CLOSE_MESSAGE = "### What happens next \n\n A judge will review your message and advise.";
    public static final String MESSAGES = "messages";


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

                if (ofNullable(caseData.getClosedMessages()).isPresent()) {
                    closedMessages.addAll(caseData.getClosedMessages());
                }

                messages.removeAll(closedMessages);
                caseDataMap.put("closedMessages", closedMessages);
            } else {
                messages = sendAndReplyService.buildNewReplyMessage(
                    selectedValue,
                    caseData.getMessageReply(),
                    caseData.getOpenMessages()
                );
            }

            messages.sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));
            caseDataMap.put("openMessages", messages);
        }
        sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFields());

        // sort lists of messages with most recent first
        if (ofNullable(caseData.getOpenMessages()).isPresent()) {
            caseData.getOpenMessages().sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));
        }
        if (ofNullable(caseData.getClosedMessages()).isPresent()) {
            caseData.getClosedMessages().sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));
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
        List<Element<Message>> messages = caseData.getOpenMessages();
        if (ofNullable(caseData.getClosedMessages()).isPresent()) {
            messages.addAll(caseData.getClosedMessages());
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
        CaseDetails caseDetails = callbackRequest.getCaseDetails();

        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        log.info("CaseDataaa --> {}",caseData);

        //Map<String, Object> caseDataMap = callbackRequest.getCaseDetails().getData();
        //log.info("Straight Size--> {}",caseDataMap.size());
        //log.info("Straight CaseData --> {}",caseDataMap);

        Map<String, Object> caseDataMap1 = caseData.toMap(CcdObjectMapper.getObjectMapper());

        log.info("ToMap Size --> {}",caseDataMap1.size());
        log.info("ToMap CaseData --> {}",caseDataMap1);

        if (caseData.getChooseSendOrReply().equals(SEND)) {
            caseDataMap1.put(MESSAGES, sendAndReplyService.addMessage(caseData, authorisation));
            log.info("Message after created--> {}",caseDataMap1.get(MESSAGES));

            //send emails in case of sending to others with emails
            sendAndReplyService.sendNotificationEmailOther(caseData);
            //WA - clear reply field in case of SEND
            sendAndReplyService.removeTemporaryFields(caseDataMap1, "replyMessageObject");
        } else {
            if (YesOrNo.No.equals(caseData.getSendOrReplyMessage().getRespondToMessage())) {
                //Reply & close
                caseDataMap1.put(MESSAGES, sendAndReplyService.closeMessage(caseData));
                // in case of reply and close message, removing replymessageobject for wa
                sendAndReplyService.removeTemporaryFields(caseDataMap1, "replyMessageObject");
            } else {
                //Reply & append history
                caseDataMap1.put(MESSAGES, sendAndReplyService.replyAndAppendMessageHistory(caseData, authorisation));
            }
            //WA - clear send field in case of REPLY
            sendAndReplyService.removeTemporaryFields(caseDataMap1, "sendMessageObject");
        }
        //clear temp fields
        sendAndReplyService.removeTemporaryFields(caseDataMap1, temporaryFieldsAboutToSubmit());
        log.info("Straight CaseData just before saving --> {}",caseDataMap1.size());
        log.info("ToMap CaseData just before saving --> {}",caseDataMap1.size());

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap1).build();
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

        return ok(SubmittedCallbackResponse.builder().build());
    }

    @PostMapping("/send-or-reply-to-messages/clear-dynamic-lists")
    public AboutToStartOrSubmitCallbackResponse clearDynamicLists(@RequestHeader("Authorization")
                                                                  @Parameter(hidden = true) String authorisation,
                                                                  @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

        //reset dynamic list fields
        caseData = sendAndReplyService.resetSendAndReplyDynamicLists(caseData);

        Map<String, Object> caseDataMap = caseData.toMap(objectMapper);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
    }
}
