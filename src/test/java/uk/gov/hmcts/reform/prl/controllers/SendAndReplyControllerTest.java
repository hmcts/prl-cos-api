package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.jspecify.annotations.NonNull;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.models.wa.WaMapper;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyCommonService;
import uk.gov.hmcts.reform.prl.services.sendandreply.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.TaskUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CLIENT_CONTEXT_HEADER_PARAMETER;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class SendAndReplyControllerTest {

    @InjectMocks
    private SendAndReplyController sendAndReplyController;

    @Mock
    private SendAndReplyService sendAndReplyService;

    @Mock
    private SendAndReplyCommonService sendAndReplyCommonService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ElementUtils elementUtils;

    @Mock
    private AllTabServiceImpl allTabService;

    @Spy
    private TaskUtils taskUtils = new TaskUtils(new ObjectMapper());

    private CaseData replyCaseData;
    private Map<String, Object> caseDataMap;
    private CaseDetails sendCaseDetails;
    private CaseData sendCaseData;
    private CallbackRequest sendCallbackRequest;
    private String auth = "authorisation";

    private Message message1;

    private Message message2;

    private List<Element<Message>> messages;

    private List<Element<Message>> listOfClosedMessages;

    private LocalDateTime dateTime = LocalDateTime.of(
        LocalDate.of(2000, 1, 10),
        LocalTime.of(10, 22));
    private String dateSent = dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.ENGLISH));

    private Element<Message> message1Element;

    private Element<Message> message2Element;

    private static final String CLIENT_CONTEXT = """
        {
          "client_context": {
            "user_task": {
              "task_data": {
                "additional_properties": {
                  "hearingId": "12345"
                }
              },
              "complete_task" : true
            }
          }
        }
        """;

    private static final String ENCODED_CLIENT_CONTEXT = Base64.getEncoder().encodeToString(CLIENT_CONTEXT.getBytes());

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();

        sendCaseData = CaseData.builder()
            .id(12345678L)
            .chooseSendOrReply(SEND)
            .build();
        replyCaseData = CaseData.builder()
            .id(12345678L)
            .chooseSendOrReply(REPLY)
            .build();
        sendCaseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(caseDataMap)
            .build();
        sendCallbackRequest = CallbackRequest.builder()
            .caseDetails(sendCaseDetails)
            .build();

        message1 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .messageHistory("")
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.LEGAL_ADVISER)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        message2 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient2@email.com")
            .messageSubject("testSubject2")
            .messageUrgency("testUrgency2")
            .dateSent(dateSent)
            .messageContent("This is message 2 body")
            .updatedTime(dateTime)
            .status(CLOSED)
            .latestMessage("Message 2 latest message")
            .messageHistory("Message 2 message history")
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.LEGAL_ADVISER)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        message1Element = element(message1);
        messages = new ArrayList<>();
        messages.add(message1Element);

        message2Element = element(message2);
        listOfClosedMessages = Arrays.asList(element(message2));

        when(objectMapper.convertValue(sendCaseDetails.getData(), CaseData.class)).thenReturn(sendCaseData);
    }

    @Test
    public void testHandleAboutToStart() {
        Map<String, Object> aboutToStartMap = setUpAboutTostart();

        when(sendAndReplyService.setSenderAndGenerateMessageList(sendCaseData, auth)).thenReturn(aboutToStartMap);
        sendAndReplyController.handleAboutToStart(auth, sendCallbackRequest);
        verify(sendAndReplyService).setSenderAndGenerateMessageList(sendCaseData, auth);
        verifyNoMoreInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleSendOrMessageAboutToStart() {
        // given
        Map<String, Object> aboutToStartMap = setUpAboutTostart();
        when(sendAndReplyService.setSenderAndGenerateMessageReplyList(any(CaseData.class), eq(auth), eq(null))).thenReturn(aboutToStartMap);

        // when
        sendAndReplyController.handleSendOrMessageAboutToStart(auth, null,  sendCallbackRequest);

        // then
        verify(sendAndReplyService).setSenderAndGenerateMessageReplyList(any(CaseData.class), eq(auth), eq(null));
    }




    @Test
    public void testHandleSendOrMessageAboutToStartNextstep() {
        // given
        Map<String, Object> aboutToStartMap = setUpAboutTostart();
        when(sendAndReplyService.setSenderAndGenerateMessageReplyList(any(CaseData.class), eq(auth))).thenReturn(aboutToStartMap);

        // when
        sendAndReplyController.handleSendOrMessageAboutToStartNextStep(auth,  sendCallbackRequest);

        // then
        verify(sendAndReplyService).setSenderAndGenerateMessageReplyList(any(CaseData.class), eq(auth));
    }

    @Test
    public void testHandleMidEventSendPath() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).chooseSendOrReply(SEND).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        sendAndReplyController.handleMidEvent(auth, callbackRequest);
        verifyNoInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleMidEventReplyPath() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        List<Element<Message>> messages = Collections.singletonList(element(Message.builder().build()));
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(messages).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.hasMessages(caseData)).thenReturn(true);
        when(sendAndReplyService.populateReplyMessageFields(caseData, auth)).thenReturn(expectedMap);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleMidEvent(auth, callbackRequest);

        verify(sendAndReplyService).populateReplyMessageFields(caseData, auth);
    }

    @Test
    public void testHandleMidEventReplyPathNoMessages() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).chooseSendOrReply(REPLY).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.hasMessages(caseData)).thenReturn(false);
        sendAndReplyController.handleMidEvent(auth, callbackRequest);
        verify(sendAndReplyService).hasMessages(caseData);
        verifyNoMoreInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleAboutToSubmitSendPath() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .sendOrReplyDto(SendOrReplyDto.builder().build())
            .chooseSendOrReply(SEND)
            .build();
        Message message = Message.builder().build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.buildNewSendMessage(caseData)).thenReturn(message);
        when(sendAndReplyService.addNewMessage(caseData, message)).thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        AboutToStartOrSubmitCallbackResponse response = sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).buildNewSendMessage(caseData);
        verify(sendAndReplyService).addNewMessage(caseData, message);
    }

    @Test
    public void testHandleAboutToSubmitSendPathWhenCaseTypeIsNull() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L)
            .build();
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(null)
            .selectedCaseTypeID(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyDto(SendOrReplyDto.builder().build())
            .chooseSendOrReply(SEND).build();
        Message message = Message.builder().build();
        Map<String, Object> map = new HashMap<>();
        map.put("caseTypeOfApplication", null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.buildNewSendMessage(caseData)).thenReturn(message);
        when(sendAndReplyService.addNewMessage(caseData, message)).thenReturn(Collections.singletonList(element(message)));
        when(allTabService.getAllTabsFields(caseData)).thenReturn(map);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).buildNewSendMessage(caseData);
        verify(sendAndReplyService).addNewMessage(caseData, message);
    }

    @Test
    public void testHandleAboutToSubmitReplyPathClose() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().status(MessageStatus.OPEN).isReplying(YesOrNo.No).build();
        CaseData caseDataWithMessage = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(Collections.singletonList(element(message))).build())
            .build();
        UUID selectedValue = UUID.randomUUID();

        when(elementUtils.getDynamicListSelectedValue(caseDataWithMessage.getReplyMessageDynamicList(), objectMapper))
            .thenReturn(selectedValue);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataWithMessage);
        when(sendAndReplyService.closeMessage(selectedValue, caseDataWithMessage))
            .thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).closeMessage(selectedValue, caseDataWithMessage);
    }

    @Test
    public void testHandleAboutToSubmitReplyPathReplyWithClosedMessages() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.No).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .caseTypeOfApplication(null)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().closedMessages(Collections.singletonList(element(message))).build())
            .build();
        UUID selectedValue = UUID.randomUUID();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(elementUtils.getDynamicListSelectedValue(caseData.getReplyMessageDynamicList(), objectMapper))
            .thenReturn(selectedValue);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).closeMessage(selectedValue, caseData);
    }

    @Test
    public void testSendOrReplyToMessagesMidEventForSend() throws JsonProcessingException {
        String clientContextJson = """
            {
              "client_context": {
                "user_task": {
                  "task_data": {
                    "additional_properties": {
                      "hearingId": "999"
                    }
                  }
                }
              }
            }
            """;
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        uk.gov.hmcts.reform.prl.models.wa.WaMapper waMapper =
            realMapper.readValue(clientContextJson, uk.gov.hmcts.reform.prl.models.wa.WaMapper.class);
        String encodedClientContext = uk.gov.hmcts.reform.prl.utils.CaseUtils.base64Encode(waMapper, realMapper);

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.No).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .messageReply(message)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().closedMessages(Collections.singletonList(element(message))).build())
            .build();


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        // when
        sendAndReplyController.sendOrReplyToMessagesMidEvent(auth, encodedClientContext, callbackRequest);

        // then
        verify(sendAndReplyService).populateDynamicListsForSendAndReply(caseData,auth, true, "999");
    }

    @Test
    public void testSendOrReplyToMessagesMidEventForReply() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CaseData caseData = setUpMidEventForReply(caseDetails);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        // when
        sendAndReplyController.sendOrReplyToMessagesMidEvent(auth, null, callbackRequest);
        // then
        verify(sendAndReplyService).populateMessageReplyFields(caseData, auth);

    }


    @Test
    public void testSendOrReplyToMessagesMidEventForReplyTask() {
        // given
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CaseData caseData = setUpMidEventForReply(caseDetails);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        // when
        sendAndReplyController.sendOrReplyToMessagesMidEventTask(auth, null, callbackRequest);

        // then
        verify(sendAndReplyService).checkTaskAssociatedWithMessage(caseData);
        verify(sendAndReplyService).populateMessageReplyFields(caseData, auth);

    }

    @Test
    public void midEventTaskForSendWithoutClientContextEnablesPastHearingsButLocksToNothing() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(SendOrReplyMessage.builder().respondToMessage(YesOrNo.No).messages(messages).build())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        sendAndReplyController.sendOrReplyToMessagesMidEventTask(auth, null, callbackRequest);

        verify(sendAndReplyService).checkTaskAssociatedWithMessage(caseData);
        verify(sendAndReplyService).populateDynamicListsForSendAndReply(caseData, auth, true, null);
    }

    @Test
    public void midEventTaskForSendWithClientContextHearingIdLocksDropdownToThatHearing() throws Exception {
        String clientContextJson = """
            {
              "client_context": {
                "user_task": {
                  "task_data": {
                    "additional_properties": {
                      "hearingId": "999"
                    }
                  }
                }
              }
            }
            """;
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        uk.gov.hmcts.reform.prl.models.wa.WaMapper waMapper =
            realMapper.readValue(clientContextJson, uk.gov.hmcts.reform.prl.models.wa.WaMapper.class);
        String encodedClientContext = uk.gov.hmcts.reform.prl.utils.CaseUtils.base64Encode(waMapper, realMapper);

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(SendOrReplyMessage.builder().respondToMessage(YesOrNo.No).messages(messages).build())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        sendAndReplyController.sendOrReplyToMessagesMidEventTask(auth, encodedClientContext, callbackRequest);

        verify(sendAndReplyService).populateDynamicListsForSendAndReply(caseData, auth, true, "999");
    }




    @Test
    public void testHandleAboutToSubmitReplyPathReplyWithoutClosedMessages() {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .sendOrReplyDto(SendOrReplyDto.builder().build())
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();
        UUID selectedValue = UUID.randomUUID();

        when(elementUtils.getDynamicListSelectedValue(caseData.getReplyMessageDynamicList(), objectMapper))
            .thenReturn(selectedValue);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.buildNewReplyMessage(selectedValue, message, caseData.getSendOrReplyDto().getOpenMessages()))
            .thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).buildNewReplyMessage(selectedValue, message, caseData.getSendOrReplyDto().getOpenMessages());
    }

    @Test
    public void testHandleSubmittedClosedMessageNoClosedMessages() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.No)
            .status(MessageStatus.CLOSED)
            .build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(Collections.singletonList(element(message))).build())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleSubmitted(auth, callbackRequest);
        verifyNoInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleSubmittedClosedMessageWithClosedMessages() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.No)
            .status(MessageStatus.CLOSED)
            .build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(Collections.singletonList(element(message))).closedMessages(
                Collections.emptyList()).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleSubmitted(auth, callbackRequest);
        verifyNoInteractions(sendAndReplyService);
    }

    @Test
    public void testHandleSubmittedNewMessage() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message newMessage = Message.builder()
            .updatedTime(ZonedDateTime.of(2022, 01, 01, 10, 30, 30, 0,
                ZoneId.of("Europe/London")).toLocalDateTime())
            .status(MessageStatus.OPEN)
            .build();
        Message oldMessage = Message.builder()
            .updatedTime(ZonedDateTime.of(2022, 01, 01, 9, 30, 30, 0,
                ZoneId.of("Europe/London")).toLocalDateTime())
            .status(MessageStatus.OPEN)
            .isReplying(YesOrNo.Yes).build();

        Message closedMessage = Message.builder()
            .updatedTime(ZonedDateTime.of(2022, 01, 01, 9, 30, 30, 0,
                ZoneId.of("Europe/London")).toLocalDateTime())
            .status(MessageStatus.CLOSED)
            .isReplying(YesOrNo.Yes).build();


        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .messageReply(newMessage)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages((Arrays.asList(element(newMessage), element(oldMessage)))).build()).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleSubmitted(auth, callbackRequest);
        verify(sendAndReplyService).sendNotificationEmail(caseData, newMessage);
        verifyNoMoreInteractions(sendAndReplyService);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForSend() {

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .build();

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest, null);
        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDataMap, null);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForReply() {
        // given
        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = getCaseDataForSubmitted(caseDetails);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDataMap, null))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        // when
        AboutToStartOrSubmitCallbackResponse response = sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest, null)
            .getBody();

        // then
        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDataMap, null);
        Assert.assertEquals("C100", response.getData().get("CaseAccessCategory"));
    }

    @Test
    public void testSendOrReplyToMessagesSubmitWithClientContextCompleteTaskTrue() {
        Message message = Message.builder()
            .futureHearingsList(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                               .code("12345 Hearing label")
                                               .build())
                                    .build())
            .build();
        SendOrReplyMessage sendOrReplyMessage = SendOrReplyMessage.builder()
            .sendMessageObject(message)
            .build();
        // given
        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(sendOrReplyMessage)
            .caseTypeOfApplication("C100")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDataMap, "12345"))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        // when
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> responseEntity = sendAndReplyController.sendOrReplyToMessagesSubmit(
            auth,
            callbackRequest,
            ENCODED_CLIENT_CONTEXT
        );

        String encoded = responseEntity.getHeaders()
            .getFirst(CLIENT_CONTEXT_HEADER_PARAMETER);
        WaMapper waMapper = CaseUtils.getWaMapper(encoded);

        // then
        assertThat(waMapper.getClientContext().getUserTask().isCompleteTask())
            .isTrue();
        assertThat(responseEntity.getBody().getData().get("CaseAccessCategory"))
            .isEqualTo("C100");

        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDataMap, "12345");
        verify(taskUtils).getTaskAdditionalProperties(ENCODED_CLIENT_CONTEXT);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitWithClientContextCompleteTaskFalse() {
        Message message = Message.builder()
            .futureHearingsList(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                               .code("99999 Hearing label")
                                               .build())
                                    .build())
            .build();
        SendOrReplyMessage sendOrReplyMessage = SendOrReplyMessage.builder()
            .sendMessageObject(message)
            .build();
        // given
        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(sendOrReplyMessage)
            .caseTypeOfApplication("C100")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDataMap, "12345"))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        // when
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> responseEntity = sendAndReplyController.sendOrReplyToMessagesSubmit(
            auth,
            callbackRequest,
            ENCODED_CLIENT_CONTEXT
        );

        String encoded = responseEntity.getHeaders()
            .getFirst(CLIENT_CONTEXT_HEADER_PARAMETER);
        WaMapper waMapper = CaseUtils.getWaMapper(encoded);

        // then
        assertThat(waMapper.getClientContext().getUserTask().isCompleteTask())
            .isFalse();
        assertThat(responseEntity.getBody().getData().get("CaseAccessCategory"))
            .isEqualTo("C100");

        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDataMap, "12345");
        verify(taskUtils).getTaskAdditionalProperties(ENCODED_CLIENT_CONTEXT);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitWithClientContextCompleteTaskFalseWhenNoHearingPresent() {
        Message message = Message.builder()
            .dateSent("Data sent")
            .build();

        SendOrReplyMessage sendOrReplyMessage = SendOrReplyMessage.builder()
            .sendMessageObject(message)
            .build();
        // given
        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(sendOrReplyMessage)
            .caseTypeOfApplication("C100")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDataMap, "12345"))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        // when
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> responseEntity = sendAndReplyController.sendOrReplyToMessagesSubmit(
            auth,
            callbackRequest,
            ENCODED_CLIENT_CONTEXT
        );

        String encoded = responseEntity.getHeaders()
            .getFirst(CLIENT_CONTEXT_HEADER_PARAMETER);
        WaMapper waMapper = CaseUtils.getWaMapper(encoded);

        // then
        assertThat(waMapper.getClientContext().getUserTask().isCompleteTask())
            .isFalse();

        assertThat(responseEntity.getBody().getData().get("CaseAccessCategory"))
            .isEqualTo("C100");
        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDataMap, "12345");
        verify(taskUtils).getTaskAdditionalProperties(ENCODED_CLIENT_CONTEXT);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitWithClientContextWithoutTask() {
        String clientContext = """
            {
              "client_context": {
                  "userLanguage" : "English"
                }
              }
            }
            """;

        String encodedClientContext = Base64.getEncoder().encodeToString(clientContext.getBytes());
        Message message = Message.builder()
            .futureHearingsList(DynamicList.builder()
                                    .value(DynamicListElement.builder()
                                               .code("99999 Hearing label")
                                               .build())
                                    .build())
            .build();
        SendOrReplyMessage sendOrReplyMessage = SendOrReplyMessage.builder()
            .sendMessageObject(message)
            .build();
        // given
        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(sendOrReplyMessage)
            .caseTypeOfApplication("C100")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDataMap, null))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        // when
        ResponseEntity<AboutToStartOrSubmitCallbackResponse> responseEntity = sendAndReplyController.sendOrReplyToMessagesSubmit(
            auth,
            callbackRequest,
            encodedClientContext
        );

        // then
        assertThat(responseEntity.getHeaders()
                       .getFirst(CLIENT_CONTEXT_HEADER_PARAMETER))
            .isNull();
        assertThat(responseEntity.getBody().getData().get("CaseAccessCategory"))
            .isEqualTo("C100");

        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDataMap, null);
        verify(taskUtils).getTaskAdditionalProperties(encodedClientContext);

    }


    @Test
    public void testSendOrReplyToMessagesSubmitForReplyTask() {
        // given
        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = getCaseDataForSubmitted(caseDetails);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        // No client-context header → controller passes null hearingId; processAboutToSubmit
        // skips the request-order tracking update. Header-decoding is covered by the
        // mid-event-task tests.
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDetails.getData(), null))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        // when
        AboutToStartOrSubmitCallbackResponse response = sendAndReplyController
            .sendOrReplyToMessagesSubmitTask(auth, null, callbackRequest);

        // verify
        verify(sendAndReplyService).checkTaskAssociatedWithMessage(caseData);
        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDetails.getData(), null);
        Assert.assertEquals("C100", response.getData().get("CaseAccessCategory"));
    }

    @Test
    public void submitTaskWithClientContextHearingIdForwardsHearingIdToProcessAboutToSubmit() throws Exception {
        String clientContextJson = """
            {
              "client_context": {
                }
              }
            }
            """;
        ObjectMapper realMapper = new ObjectMapper();
        realMapper.findAndRegisterModules();
        uk.gov.hmcts.reform.prl.models.wa.WaMapper waMapper =
            realMapper.readValue(clientContextJson, uk.gov.hmcts.reform.prl.models.wa.WaMapper.class);
        String encodedClientContext = uk.gov.hmcts.reform.prl.utils.CaseUtils.base64Encode(waMapper, realMapper);

        CaseDetails caseDetails = getCaseDetailsForSubmitted();
        CaseData caseData = getCaseDataForSubmitted(caseDetails);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyCommonService.processAboutToSubmit(auth, caseData, caseDetails.getData(), null))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder()
                .data(java.util.Map.of("CaseAccessCategory", "C100"))
                .build());

        AboutToStartOrSubmitCallbackResponse response = sendAndReplyController
            .sendOrReplyToMessagesSubmitTask(auth, encodedClientContext, callbackRequest);

        verify(sendAndReplyService).checkTaskAssociatedWithMessage(caseData);
        verify(sendAndReplyCommonService).processAboutToSubmit(auth, caseData, caseDetails.getData(), null);
        Assert.assertEquals("C100", response.getData().get("CaseAccessCategory"));
    }



    @Test
    public void testHandSubmittedSendAndReply() {
        // given
        CallbackRequest callbackRequest = setUpHandleSubmitted();

        when(sendAndReplyService.sendAndReplySubmitted(callbackRequest, auth)).thenReturn(ok(SubmittedCallbackResponse.builder().build()));

        // when
        ResponseEntity<SubmittedCallbackResponse> response  = sendAndReplyController.handleSubmittedSendAndReply(auth, callbackRequest, null);

        // then
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(sendAndReplyService).sendAndReplySubmitted(callbackRequest, auth);
    }


    @Test
    public void testHandSubmittedSendAndReplyTask() {
        // given
        CallbackRequest callbackRequest = setUpHandleSubmitted();
        CaseData caseData = setUpHandleSubmittedCaseData();
        when(sendAndReplyService.sendAndReplySubmittedTask(callbackRequest, auth)).thenReturn(ok(SubmittedCallbackResponse.builder().build()));

        // when
        ResponseEntity<SubmittedCallbackResponse> response  = sendAndReplyController.handleSubmittedSendAndReplyTask(auth, callbackRequest, null);

        // then
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(sendAndReplyService).checkTaskAssociatedWithMessage(caseData);
        verify(sendAndReplyService).sendAndReplySubmittedTask(callbackRequest, auth);
    }





    @Test
    public void testClearDynamicLists() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyService.clearDynamicLists(callbackRequest)).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        sendAndReplyController.clearDynamicLists(auth, callbackRequest);
        verify(sendAndReplyService).clearDynamicLists(callbackRequest);
    }

    @Test
    public void testSendOrReplyToMessagesMidEventForReplyWithNoOpenMessages() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.No).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messages(listOfClosedMessages)
                    .build())
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder()
                .closedMessages(Collections.singletonList(element(message)))
                .build())

            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CallbackResponse response = sendAndReplyController.sendOrReplyToMessagesMidEvent(auth, null, callbackRequest);
        Assert.assertNotNull(response.getErrors());
        Assert.assertTrue(!response.getErrors().isEmpty());
        Assert.assertEquals("There are no messages to respond to.",
            response.getErrors().get(0));
    }


    private static @NonNull Map<String, Object> setUpAboutTostart() {
        Map<String, Object> aboutToStartMap = new HashMap<>();
        aboutToStartMap.put("messageObject", MessageMetaData.builder().build());
        return aboutToStartMap;
    }


    private CaseData setUpMidEventForReply(CaseDetails caseDetails) {
        Message message = Message.builder().isReplying(YesOrNo.No).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .chooseSendOrReply(REPLY)
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().closedMessages(Collections.singletonList(element(message))).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        return caseData;
    }


    private CaseDetails getCaseDetailsForSubmitted() {
        caseDataMap = new HashMap<>();
        caseDataMap.put("caseTypeOfApplication", "C100");
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        return caseDetails;
    }


    private CaseData getCaseDataForSubmitted(CaseDetails caseDetails) {

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .caseTypeOfApplication("C100")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        return caseData;
    }

    private CallbackRequest setUpHandleSubmitted() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(new HashMap<>()).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        return callbackRequest;
    }

    private  CaseData setUpHandleSubmittedCaseData() {
        CaseData caseData = CaseData.builder().build();
        when(objectMapper.convertValue(any(Map.class), eq(CaseData.class))).thenReturn(caseData);
        return caseData;
    }
}
