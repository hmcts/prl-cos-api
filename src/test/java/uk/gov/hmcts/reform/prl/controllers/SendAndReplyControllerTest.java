package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.AuthorisationUtil;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendAndReplyEventData;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class SendAndReplyControllerTest {

    @InjectMocks
    SendAndReplyController sendAndReplyController;

    @Mock
    SendAndReplyService sendAndReplyService;

    @Mock
    AuthorisationUtil authorisationUtil;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ElementUtils elementUtils;

    CaseData replyCaseData;
    Map<String, Object> caseDataMap;
    SendAndReplyEventData sendEventData;
    CaseDetails sendCaseDetails;
    CaseData sendCaseData;
    CallbackRequest sendCallbackRequest;
    SendAndReplyEventData replyEventData;
    String auth = "authorisation";

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();

        sendEventData = SendAndReplyEventData.builder()
            .chooseSendOrReply(SEND)
            .build();
        sendCaseData = CaseData.builder()
            .id(12345678L)
            .sendAndReplyEventData(sendEventData)
            .build();

        replyEventData = SendAndReplyEventData.builder()
            .chooseSendOrReply(REPLY)
            .build();
        replyCaseData = CaseData.builder()
            .id(12345678L)
            .sendAndReplyEventData(replyEventData)
            .build();

        sendCaseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(caseDataMap)
            .build();

        sendCallbackRequest = CallbackRequest.builder()
            .caseDetails(sendCaseDetails)
            .build();

        when(objectMapper.convertValue(sendCaseDetails.getData(), CaseData.class)).thenReturn(sendCaseData);
    }

    @Test
    public void testHandleAboutToStart() {
        Map<String, Object> aboutToStartMap = new HashMap<>();
        aboutToStartMap.put("messageObject", MessageMetaData.builder().build());

        when(sendAndReplyService.setSenderAndGenerateMessageList(sendCaseData)).thenReturn(aboutToStartMap);
        sendAndReplyController.handleAboutToStart(auth, sendCallbackRequest);
        verify(sendAndReplyService).setSenderAndGenerateMessageList(sendCaseData);
        verifyNoMoreInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleMidEventSendPath() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        SendAndReplyEventData eventData = SendAndReplyEventData.builder().chooseSendOrReply(SEND).build();
        CaseData caseData = CaseData.builder().id(12345L).sendAndReplyEventData(eventData).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        sendAndReplyController.handleMidEvent(auth, callbackRequest);
        verifyNoInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleMidEventReplyPath() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        SendAndReplyEventData eventData = SendAndReplyEventData.builder().chooseSendOrReply(REPLY).build();
        List<Element<Message>> messages = Collections.singletonList(element(Message.builder().build()));
        CaseData caseData = CaseData.builder().id(12345L)
            .sendAndReplyEventData(eventData)
            .openMessages(messages)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.hasMessages(caseData)).thenReturn(true);
        when(sendAndReplyService.populateReplyMessageFields(caseData)).thenReturn(expectedMap);
        sendAndReplyController.handleMidEvent(auth, callbackRequest);
        verify(sendAndReplyService).populateReplyMessageFields(caseData);
    }

    @Test
    public void testHandleMidEventReplyPathNoMessages() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        SendAndReplyEventData eventData = SendAndReplyEventData.builder().chooseSendOrReply(REPLY).build();
        CaseData caseData = CaseData.builder().id(12345L).sendAndReplyEventData(eventData).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.hasMessages(caseData)).thenReturn(false);
        sendAndReplyController.handleMidEvent(auth, callbackRequest);
        verify(sendAndReplyService).hasMessages(caseData);
        verifyNoMoreInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleAboutToSubmitSendPath() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        SendAndReplyEventData eventData = SendAndReplyEventData.builder().chooseSendOrReply(SEND).build();
        CaseData caseData = CaseData.builder().id(12345L).sendAndReplyEventData(eventData).build();
        Message message = Message.builder().build();
        CaseData caseDataWithMessage = CaseData.builder().id(12345L).sendAndReplyEventData(eventData)
            .openMessages(Collections.singletonList(element(message)))
            .build();
        Map<String, Object> caseDataMap = new HashMap<>();

        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.buildNewSendMessage(caseData)).thenReturn(message);
        when(sendAndReplyService.addNewMessage(caseData, message)).thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).buildNewSendMessage(caseData);
        verify(sendAndReplyService).addNewMessage(caseData, message);
    }

    @Test
    public void testHandleAboutToSubmitReplyPathClose() {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().status(MessageStatus.OPEN).isReplying(YesOrNo.No).build();
        SendAndReplyEventData eventData = SendAndReplyEventData.builder()
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();
        CaseData caseDataWithMessage = CaseData.builder().id(12345L).sendAndReplyEventData(eventData)
            .openMessages(Collections.singletonList(element(message)))
            .build();
        UUID selectedValue = UUID.randomUUID();

        when(elementUtils.getDynamicListSelectedValue(eventData.getReplyMessageDynamicList(), objectMapper))
            .thenReturn(selectedValue);
        when(objectMapper.convertValue(caseDataWithMessage, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataWithMessage);
        when(sendAndReplyService.closeMessage(selectedValue, caseDataWithMessage))
            .thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).closeMessage(selectedValue, caseDataWithMessage);
    }

    @Test
    public void testHandleAboutToSubmitReplyPathReply() {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();
        SendAndReplyEventData eventData = SendAndReplyEventData.builder()
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();
        CaseData caseData = CaseData.builder().id(12345L).sendAndReplyEventData(eventData).build();
        UUID selectedValue = UUID.randomUUID();

        when(elementUtils.getDynamicListSelectedValue(eventData.getReplyMessageDynamicList(), objectMapper))
            .thenReturn(selectedValue);
        when(objectMapper.convertValue(caseData, Map.class)).thenReturn(caseDataMap);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.buildNewReplyMessage(selectedValue, message, caseData.getOpenMessages()))
            .thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).buildNewReplyMessage(selectedValue, message, caseData.getOpenMessages());
    }




}
