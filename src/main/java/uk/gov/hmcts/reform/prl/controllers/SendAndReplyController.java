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
import uk.gov.hmcts.reform.prl.models.AuthorisationUtil;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendAndReplyEventData;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendAndReplyEventData.temporaryFields;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.hasMessages;
import static uk.gov.hmcts.reform.prl.services.SendAndReplyService.removeTemporaryFields;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/send-and-reply-to-messages")
public class SendAndReplyController extends AbstractCallbackController {

    @Autowired
    SendAndReplyService sendAndReplyService;

    @Autowired
    AuthorisationUtil authorisationUtil;

    @Autowired
    ObjectMapper objectMapper;


    @PostMapping("/about-to-start")
    public AboutToStartOrSubmitCallbackResponse handleAboutToStart(@RequestHeader("Authorization") String authorisation,
                                                                   @RequestBody CallbackRequest callbackRequest) {
        authorisationUtil.setTokenAndId(authorisation, callbackRequest.getCaseDetails().getId());
        CaseDetails caseDetails = callbackRequest.getCaseDetails();
        CaseData caseData = getCaseData(caseDetails);
        Map<String, Object> caseDataMap = new HashMap<>(sendAndReplyService.setSenderAndGenerateMessageList(caseData));
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
            if (!hasMessages(caseData)) {
                errors.add("There are no messages to respond to.");
            }
            caseDataMap.putAll(sendAndReplyService.populateReplyMessageFields(caseData));
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

        if(eventData.getChooseSendOrReply().equals(SEND)) {
            Message newMessage = sendAndReplyService.buildNewSendMessage(caseData);
            List<Element<Message>> listOfMessages = sendAndReplyService.addNewMessage(caseData, newMessage);
            caseDataMap.putAll(sendAndReplyService.returnMapOfNewMessages(listOfMessages));
        }
        else {
            UUID selectedValue = ElementUtils
                .getDynamicListSelectedValue(caseData.getSendAndReplyEventData()
                                                 .getReplyMessageDynamicList(), objectMapper);
            List<Element<Message>> updatedMessageList;

            if (eventData.getMessageReply().getIsReplying().equals(YesOrNo.No)) {
                updatedMessageList = sendAndReplyService.closeMessage(selectedValue, caseData);
            }
            else {
                updatedMessageList = sendAndReplyService.buildNewReplyMessage(
                    selectedValue,
                    eventData.getMessageReply(),
                    caseData.getMessages()
                );
            }
            caseDataMap.put("messages", updatedMessageList);
        }
        removeTemporaryFields(caseDataMap, temporaryFields());
        return AboutToStartOrSubmitCallbackResponse.builder()
            .data(caseDataMap)
            .build();
    }
}
