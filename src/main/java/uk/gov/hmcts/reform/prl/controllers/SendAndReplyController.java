package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendAndReplyEventData;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendAndReplyEventData.temporaryFields;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/send-and-reply-to-messages")
public class SendAndReplyController extends AbstractCallbackController {

    @Autowired
    SendAndReplyService sendAndReplyService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ElementUtils elementUtils;


    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestHeader("Authorization") String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> caseDataMap = new HashMap<>(sendAndReplyService.setSenderAndGenerateMessageList(caseData, authorisation));
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/mid-event")
    public AboutToStartOrSubmitCallbackResponse handleMidEvent(@RequestHeader("Authorization") String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        SendAndReplyEventData eventData = caseData.getSendAndReplyEventData();
        Map<String, Object> caseDataMap = new HashMap<>();

        List<String> errors = new ArrayList<>();
        if (eventData.getChooseSendOrReply().equals(REPLY)) {
            if (!sendAndReplyService.hasMessages(caseData)) {
                errors.add("There are no messages to respond to.");
            } else {
                caseDataMap.putAll(sendAndReplyService.populateReplyMessageFields(caseData, authorisation));
            }
        }
        return AboutToStartOrSubmitCallbackResponse.builder()
            .errors(errors)
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/about-to-submit")
    public AboutToStartOrSubmitCallbackResponse handleAboutToSubmit(@RequestHeader("Authorization") String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        SendAndReplyEventData eventData = caseData.getSendAndReplyEventData();
        Map<String, Object> caseDataMap = toMap(caseData);

        if (eventData.getChooseSendOrReply().equals(SEND)) {
            Message newMessage = sendAndReplyService.buildNewSendMessage(caseData);
            log.info(String.format("New message sent to %s", newMessage.getRecipientEmail()));
            List<Element<Message>> listOfMessages = sendAndReplyService.addNewMessage(caseData, newMessage);
            caseDataMap.putAll(sendAndReplyService.returnMapOfOpenMessages(listOfMessages));

        } else {
            UUID selectedValue = elementUtils
                .getDynamicListSelectedValue(caseData.getSendAndReplyEventData()
                                                 .getReplyMessageDynamicList(), objectMapper);

            List<Element<Message>> messages;
            if (eventData.getMessageReply().getIsReplying().equals(YesOrNo.No)) {
                messages = sendAndReplyService.closeMessage(selectedValue, caseData);
                log.info(String.format("Closing message with id: %s", selectedValue));
                List<Element<Message>> closedMessages = messages.stream()
                    .filter(m -> m.getValue().getStatus().equals(MessageStatus.CLOSED))
                    .collect(Collectors.toList());

                messages.removeAll(closedMessages);
                caseDataMap.put("closedMessages", closedMessages);

            } else {
                messages = sendAndReplyService.buildNewReplyMessage(
                    selectedValue,
                    eventData.getMessageReply(),
                    caseData.getOpenMessages()
                );
                log.info(String.format("Sending reply message to %s", eventData.getMessageReply().getReplyTo()));
            }
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

        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }

    @PostMapping("/submitted")
    public AboutToStartOrSubmitCallbackResponse handleSubmitted(@RequestHeader("Authorization") String authorisation,
                                                                @RequestBody CallbackRequest callbackRequest) {

        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);

        log.info(caseData.getSendAndReplyEventData().getMessageReply().toString());

        if (caseData.getSendAndReplyEventData().getChooseSendOrReply().equals(SEND)
            || (caseData.getSendAndReplyEventData().getChooseSendOrReply().equals(REPLY)
            && caseData.getSendAndReplyEventData().getMessageReply().getIsReplying().equals(YesOrNo.Yes))) {


            Message message = caseData.getOpenMessages().get(0).getValue();
            sendAndReplyService.sendNotificationEmail(caseData, message);
        }
        //if a message is being closed then no notification email is sent
        return AboutToStartOrSubmitCallbackResponse.builder()
            .build();
    }
}
