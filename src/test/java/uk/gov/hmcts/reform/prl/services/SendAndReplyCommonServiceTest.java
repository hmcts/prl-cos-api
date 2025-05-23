package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageHistory;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class SendAndReplyCommonServiceTest {

    @InjectMocks
    private SendAndReplyCommonService sendAndReplyCommonService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    SendAndReplyService sendAndReplyService;

    @Mock
    UploadAdditionalApplicationService uploadAdditionalApplicationService;

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
    public void testSendOrReplyToMessagesSubmitForMessageAboutOtherForSend() {

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

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>();
        msgListWithNewMessage.addAll(messages);
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

        sendAndReplyCommonService.sendMessages(auth, caseData, caseDataMap);
        verify(sendAndReplyService).addMessage(caseData, auth, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForMessageAboutOtherForApplicantsResponentsSend() {

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

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>();
        msgListWithNewMessage.addAll(messages);
        msgListWithNewMessage.add(element(newMessage));

        PartyDetails applicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
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
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
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

        sendAndReplyCommonService.sendMessages(auth, caseData, caseDataMap);
        verify(sendAndReplyService).addMessage(caseData, auth, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForMessageAboutReviewSubmittedDocForSend() {

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

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>();
        msgListWithNewMessage.addAll(messages);
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

        sendAndReplyCommonService.sendMessages(auth, caseData, caseDataMap);
        verify(sendAndReplyService).addMessage(caseData, auth, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForReplyAndClose() {

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();
        UUID selectedValue = messages.get(0).getId();

        List<Element<Message>> openMessagesBefore = messages;

        List<Element<Message>> closedMessage = messages.stream()
            .filter(m -> m.getId().equals(selectedValue))
            .findFirst()
            .map(element -> {
                openMessagesBefore.remove(element);
                element.getValue().setStatus(MessageStatus.CLOSED);
                element.getValue().setUpdatedTime(dateTime.now());
                return element;
            }).stream().collect(Collectors.toList());
        closedMessage.add(listOfClosedMessages.get(0));

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

        sendAndReplyCommonService.replyMessages(auth, caseData, caseDataMap);
        verify(sendAndReplyService).closeMessage(caseDataAfterClosed, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForReplyAndAppendHistory() {

        DynamicList dynamicList =  ElementUtils.asDynamicList(messages, null, Message::getLabelForDynamicList);



        MessageHistory messageHistory = MessageHistory.builder().messageFrom("sender1@email.com")
            .messageTo("testRecipient1@email.com").messageDate(dateSent).isUrgent(YesOrNo.Yes).build();

        List<Element<Message>> messagesWithHistory = messages;

        List<Element<MessageHistory>> msgHisElemList = new ArrayList<>();

        msgHisElemList.add(element(messageHistory));

        messagesWithHistory.get(0).getValue().setReplyHistory(msgHisElemList);

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

        sendAndReplyCommonService.replyMessages(auth, caseData, caseDataMap);
        verify(sendAndReplyService).replyAndAppendMessageHistory(caseData, auth, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForReply() {
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

        sendAndReplyCommonService.replyMessages(auth, caseData, caseDataMap);
        verify(sendAndReplyService).closeMessage(caseData, caseDataMap);
    }
}
