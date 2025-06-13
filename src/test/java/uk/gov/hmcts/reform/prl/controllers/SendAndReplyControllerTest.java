package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageHistory;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class SendAndReplyControllerTest {

    @InjectMocks
    SendAndReplyController sendAndReplyController;

    @Mock
    SendAndReplyService sendAndReplyService;

    @Mock
    UploadAdditionalApplicationService uploadAdditionalApplicationService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    ElementUtils elementUtils;

    @Mock
    AllTabServiceImpl allTabService;

    CaseData replyCaseData;
    Map<String, Object> caseDataMap;
    CaseDetails sendCaseDetails;
    CaseData sendCaseData;
    CallbackRequest sendCallbackRequest;
    String auth = "authorisation";

    Message message1;

    Message message2;

    List<Element<Message>> messages;

    List<Element<Message>> listOfClosedMessages;

    LocalDateTime dateTime = LocalDateTime.of(
        LocalDate.of(2000, 1, 10),
        LocalTime.of(10, 22));
    String dateSent = dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.ENGLISH));

    Element<Message> message1Element;

    Element<Message> message2Element;

    @BeforeEach
    void setup() {
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
        listOfClosedMessages = List.of(element(message2));
    }

    @Test
    void testHandleAboutToStart() {
        Map<String, Object> aboutToStartMap = new HashMap<>();
        aboutToStartMap.put("messageObject", MessageMetaData.builder().build());

        when(sendAndReplyService.setSenderAndGenerateMessageList(sendCaseData, auth)).thenReturn(aboutToStartMap);
        sendAndReplyController.handleAboutToStart(auth, sendCallbackRequest);
        verify(sendAndReplyService).setSenderAndGenerateMessageList(sendCaseData, auth);
        verifyNoMoreInteractions(sendAndReplyService);
    }

    @Test
    void testHandleSendOrMessageAboutToStart() {
        Map<String, Object> aboutToStartMap = new HashMap<>();
        aboutToStartMap.put("messageObject", MessageMetaData.builder().build());

        when(sendAndReplyService.setSenderAndGenerateMessageReplyList(sendCaseData, auth)).thenReturn(aboutToStartMap);
        sendAndReplyController.handleSendOrMessageAboutToStart(auth, sendCallbackRequest);
        verify(sendAndReplyService).setSenderAndGenerateMessageReplyList(sendCaseData, auth);
    }

    @Test
    void testHandleMidEventSendPath() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).chooseSendOrReply(SEND).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        sendAndReplyController.handleMidEvent(auth, callbackRequest);
        verifyNoInteractions(sendAndReplyService);
    }

    @Test
    void testHandleMidEventReplyPath() {
        Map<String, Object> expectedMap = new HashMap<>();
        expectedMap.put("messageReply", Message.builder().build());
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        messages = Collections.singletonList(element(Message.builder().build()));
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
    void testHandleMidEventReplyPathNoMessages() {
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
    void testHandleAboutToSubmitSendPath() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .sendOrReplyDto(SendOrReplyDto.builder().build())
            .chooseSendOrReply(SEND).build();
        Message message = Message.builder().build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.buildNewSendMessage(caseData)).thenReturn(message);
        when(sendAndReplyService.addNewMessage(caseData, message)).thenReturn(Collections.singletonList(element(message)));

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.handleAboutToSubmit(auth, callbackRequest);
        verify(sendAndReplyService).buildNewSendMessage(caseData);
        verify(sendAndReplyService).addNewMessage(caseData, message);
    }

    @Test
    void testHandleAboutToSubmitSendPathWhenCaseTypeIsNull() {
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
    void testHandleAboutToSubmitReplyPathClose() {
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
    void testHandleAboutToSubmitReplyPathReplyWithClosedMessages() {
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
    void testSendOrReplyToMessagesMidEventForSend() {
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
        sendAndReplyController.sendOrReplyToMessagesMidEvent(auth, callbackRequest);
        verify(sendAndReplyService).populateDynamicListsForSendAndReply(caseData,auth);
    }

    @Test
    void testSendOrReplyToMessagesMidEventForReply() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
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

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesMidEvent(auth, callbackRequest);
        verify(sendAndReplyService).populateMessageReplyFields(caseData, auth);

    }

    @Test
    void testHandleAboutToSubmitReplyPathReplyWithoutClosedMessages() {
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
    void testHandleSubmittedClosedMessageNoClosedMessages() {
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
    void testHandleSubmittedClosedMessageWithClosedMessages() {
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
    void testHandleSubmittedNewMessage() {
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
    void testSendOrReplyToMessagesSubmitForMessageAboutOtherForSend() {

        DynamicList dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.randomUUID()).build()).build();

        Message newMessage = Message.builder()
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
            .applicationsList(dynamicList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
            .messageAbout(MessageAboutEnum.OTHER)
            .build();

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>(messages);
        msgListWithNewMessage.add(element(newMessage));

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                                           .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                                           .messageAbout(MessageAboutEnum.APPLICATION)
                                           .messageContent("some msg content")
                                           .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        when(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData, SEND)).thenReturn(dynamicList.getValueCode());
        when(uploadAdditionalApplicationService.updateAwpApplicationStatus(dynamicList.getValueCode(),
                                                                           caseData.getAdditionalApplicationsBundle(),
                                                                           AWP_STATUS_IN_REVIEW)).thenReturn(null);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.addMessage(caseData, auth, caseDataMap)).thenReturn(msgListWithNewMessage);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest);
        verify(sendAndReplyService).addMessage(caseData, auth, caseDataMap);
    }

    @Test
    void testSendOrReplyToMessagesSubmitForMessageAboutOtherForApplicantsRespondentsSend() {

        Message newMessage = Message.builder()
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
            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
            .messageAbout(MessageAboutEnum.OTHER)
            .build();

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>(messages);
        msgListWithNewMessage.add(element(newMessage));

        PartyDetails applicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(applicant.getPartyId()).value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .contactPreferences(ContactPreferences.post)
            .address(Address.builder().addressLine1("1 ADD Road").postCode("1XY 2AB").country("ABC").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(respondent.getPartyId()).value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);


        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();


        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .replyMessageDynamicList(DynamicList.builder().build())
            .applicants(applicantList)
            .respondents(respondentList)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                        .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                        .externalMessageWhoToSendTo(externalMessageWhoToSendTo)
                        .messageAbout(MessageAboutEnum.APPLICATION)
                        .messageContent("some msg content")
                        .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.addMessage(caseData, auth, caseDataMap)).thenReturn(msgListWithNewMessage);
        when(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData,SEND))
            .thenReturn("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355");

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest);
        verify(sendAndReplyService).addMessage(caseData, auth, caseDataMap);
    }

    @Test
    void testSendOrReplyToMessagesSubmitForMessageAboutReviewSubmittedDocForSend() {

        Message newMessage = Message.builder()
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
            .messageAbout(MessageAboutEnum.REVIEW_SUBMITTED_DOCUMENTS)
            .build();


        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123451L)
            .data(caseDataMap)
            .build();

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>(messages);
        msgListWithNewMessage.add(element(newMessage));

        CaseData caseData = CaseData.builder().id(123451L)
            .chooseSendOrReply(SEND)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(newMessage)
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.addMessage(caseData, auth, caseDataMap)).thenReturn(msgListWithNewMessage);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest);
        verify(sendAndReplyService).addMessage(caseData, auth, caseDataMap);
    }

    @Test
    void testSendOrReplyToMessagesSubmitForReplyAndClose() {



        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        UUID selectedValue = messages.getFirst().getId();

        List<Element<Message>> openMessagesBefore = messages;

        List<Element<Message>> closedMessage = messages.stream()
            .filter(m -> m.getId().equals(selectedValue))
            .findFirst()
            .map(element -> {
                openMessagesBefore.remove(element);
                element.getValue().setStatus(MessageStatus.CLOSED);
                element.getValue().setUpdatedTime(LocalDateTime.now());
                return element;
            }).stream().collect(Collectors.toList());
        closedMessage.add(listOfClosedMessages.getFirst());

        DynamicList dynamicList =  ElementUtils.asDynamicList(messages, null, Message::getLabelForDynamicList);

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder().messageReplyDynamicList(dynamicList)
                    .sendMessageObject(Message.builder()
                        .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                        .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                        .messageAbout(MessageAboutEnum.APPLICATION)
                        .messageContent("some msg content")
                        .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData,REPLY))
            .thenReturn("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355");
        LocalDateTime dateTimeNow = LocalDateTime.now();
        String dateSubmitted = dateTimeNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH));
        CaseData caseDataAfterClosed = CaseData.builder().id(12345L)
            .state(State.SUBMITTED_PAID)
            .dateSubmitted(dateSubmitted)
            .chooseSendOrReply(REPLY)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder().messageReplyDynamicList(dynamicList)
                    .sendMessageObject(Message.builder()
                        .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                        .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                        .messageAbout(MessageAboutEnum.APPLICATION)
                        .messageContent("some msg content")
                        .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(openMessagesBefore)
                    .build())
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest);
        verify(sendAndReplyService).closeMessage(caseDataAfterClosed, caseDataMap);
    }

    @Test
    void testSendOrReplyToMessagesSubmitForReplyAndAppendHistory() {

        DynamicList dynamicList =  ElementUtils.asDynamicList(messages, null, Message::getLabelForDynamicList);



        MessageHistory messageHistory = MessageHistory.builder().messageFrom("sender1@email.com")
            .messageTo("testRecipient1@email.com").messageDate(dateSent).isUrgent(YesOrNo.Yes).build();

        List<Element<Message>> messagesWithHistory = messages;

        List<Element<MessageHistory>> msgHisElemList = new ArrayList<>();

        msgHisElemList.add(element(messageHistory));

        messagesWithHistory.getFirst().getValue().setReplyHistory(msgHisElemList);

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder().messageReplyDynamicList(dynamicList)
                    .sendMessageObject(Message.builder()
                        .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                        .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                        .messageAbout(MessageAboutEnum.APPLICATION)
                        .messageContent("some msg content")
                        .build()
                    )
                    .respondToMessage(YesOrNo.Yes)
                    .messages(messages)
                    .build())
            .build();

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.replyAndAppendMessageHistory(caseData, auth, caseDataMap)).thenReturn(messagesWithHistory);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest);
        verify(sendAndReplyService).replyAndAppendMessageHistory(caseData, auth, caseDataMap);
    }

    @Test
    void testSendOrReplyToMessagesSubmitForReply() {
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder().id(123451L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123451L)
            .data(caseDataMap)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.closeMessage(caseData, caseDataMap)).thenReturn(listOfClosedMessages);

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.sendOrReplyToMessagesSubmit(auth, callbackRequest);
        verify(sendAndReplyService).closeMessage(caseData, caseDataMap);
    }

    @Test
    void testHandleSubmittedSendAndReplyWhenRespToMesgNo() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .messages(messages)
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<SubmittedCallbackResponse> response  = sendAndReplyController.handleSubmittedSendAndReply(auth, callbackRequest);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
    }

    @Test
    void testHandleSubmittedSendAndReplyWhenRespToMesgSendAndNo() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .messages(messages)
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                                           .build())
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<SubmittedCallbackResponse> response  = sendAndReplyController.handleSubmittedSendAndReply(auth, callbackRequest);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    void testHandSubmittedSendAndReplyWhenRespondToMessageYes() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.Yes)
                    .messages(messages)
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        ResponseEntity<SubmittedCallbackResponse> response  = sendAndReplyController.handleSubmittedSendAndReply(auth, callbackRequest);
        assertEquals(OK, response.getStatusCode());
    }

    @Test
    void testClearDynamicLists() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                        .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                        .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                        .messageAbout(MessageAboutEnum.APPLICATION)
                        .sendReplyJudgeName(JudicialUser.builder().personalCode("123").build())
                        .messageContent("some msg content")
                        .build())
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();

        CaseData caseDataAfterReset = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                        .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                        .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                        .messageAbout(MessageAboutEnum.APPLICATION)
                        .messageContent("some msg content")
                        .sendReplyJudgeName(JudicialUser.builder().build())
                        .build())
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(DynamicList.builder().build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.resetSendAndReplyDynamicLists(caseData)).thenReturn(caseDataAfterReset);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        sendAndReplyController.clearDynamicLists(auth, callbackRequest);
        verify(sendAndReplyService).resetSendAndReplyDynamicLists(caseData);
    }

    @Test
    void testSendOrReplyToMessagesMidEventForReplyWithNoOpenMessages() {
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
        CallbackResponse response = sendAndReplyController.sendOrReplyToMessagesMidEvent(auth, callbackRequest);
        assertNotNull(response.getErrors());
        assertFalse(response.getErrors().isEmpty());
        assertEquals("There are no messages to respond to.", response.getErrors().getFirst());
    }
}
