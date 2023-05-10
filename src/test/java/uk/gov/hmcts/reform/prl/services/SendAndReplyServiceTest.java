package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.ExternalPartyDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.notify.SendAndReplyNotificationEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.time.Time;
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

import static java.util.Optional.ofNullable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.class)
public class SendAndReplyServiceTest {
    @InjectMocks
    SendAndReplyService sendAndReplyService;

    @Mock
    UserService userService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    Time time;

    @Mock
    ElementUtils elementUtils;

    @Mock
    EmailService emailService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    String auth = "auth-token";
    UserDetails userDetails;
    LocalDateTime dateTime = LocalDateTime.of(LocalDate.of(2000, 1, 10),
                                              LocalTime.of(10, 22));
    String dateSent = dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK));
    Message message1;
    Message message2;
    Message message3;
    Element<Message> message1Element;
    Element<Message> message2Element;
    List<Element<Message>> messages;
    List<Element<Message>> messagesWithOneAdded;

    List<Element<ExternalPartyDocument>> externalPartyDocsList;
    MessageMetaData metaData;
    DynamicList dynamicList;
    CaseData caseData;
    CaseData caseDataWithAddedMessage;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    private  RefDataService refDataService;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private RefDataUserService refDataUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final String serviceAuthToken = "Bearer testServiceAuth";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Before
    public void init() {
        when(time.now()).thenReturn(LocalDateTime.now());
        userDetails = UserDetails.builder()
            .email("sender@email.com")
            .build();
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
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
            .build();
        message3 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient3@email.com")
            .messageSubject("testSubject3")
            .messageUrgency("testUrgency3")
            .dateSent(dateSent)
            .messageContent("This is message 3 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 3 latest message")
            .messageHistory("Message 3 message history")
            .build();

        metaData = MessageMetaData.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("recipient@email.com")
            .messageUrgency("Very urgent")
            .messageSubject("Message test subject")
            .build();

        dynamicList =  ElementUtils.asDynamicList(messages, null, Message::getLabelForDynamicList);


        message1Element = element(message1);
        message2Element = element(message2);
        messages = new ArrayList<>();
        messages.add(message1Element);
        messages.add(message2Element);
        caseData = CaseData.builder()
            .applicantCaseName("Test name")
            .openMessages(messages)
            .closedMessages(messages)
            .messageMetaData(metaData)
            .messageReply(message1)
            .messageContent("This is the message body")
            .replyMessageDynamicList(dynamicList)
            .build();

        messagesWithOneAdded = Arrays.asList(element(message1), element(message2), element(message3));

        ExternalPartyDocument externalPartyDocument1 = ExternalPartyDocument.builder()
            .documentCategoryList(DynamicList.builder().value(DynamicListElement.builder().code("123").label("label").build())
                                      .build())
            .build();

        externalPartyDocsList = Arrays.asList(element(externalPartyDocument1));

        caseDataWithAddedMessage = CaseData.builder()
            .openMessages(messagesWithOneAdded)
            .messageMetaData(metaData)
            .messageReply(message1)
            .messageContent("This is the message body")
            .replyMessageDynamicList(dynamicList)
            .build();
    }

    @Test
    public void testUserServiceReturnsLoggedInEmail() {
        assertEquals(sendAndReplyService.getLoggedInUserEmail(auth), userDetails.getEmail());
    }

    @Test
    public void testThatDynamicListOfOpenMessagesIsGenerated() {
        List<Element<Message>> openMessages = new ArrayList<>();
        openMessages.addAll(messages);
        DynamicList dynamicList =  ElementUtils.asDynamicList(openMessages, null, Message::getLabelForDynamicList);
        assertEquals(sendAndReplyService.getOpenMessagesDynamicList(caseData), dynamicList);
    }

    @Test
    public void testThatSenderEmailIsPrefilled() {
        MessageMetaData preFillMetaData = MessageMetaData.builder()
            .senderEmail("sender@email.com")
            .build();
        CaseData caseDataWithPreFill = CaseData.builder()
            .messageMetaData(preFillMetaData)
            .build();
        assertTrue(sendAndReplyService.setSenderAndGenerateMessageList(caseData, auth).containsKey("messageObject"));
        assertEquals(sendAndReplyService.setSenderAndGenerateMessageList(caseDataWithPreFill, auth).get("messageObject"), preFillMetaData);
    }

    @Test
    public void testThatNewMessageIsAddedToListWhenOpenMessagesPresent() {
        assertEquals(sendAndReplyService.addNewMessage(caseData, message3).size(), messagesWithOneAdded.size());
    }

    @Test
    public void testThatNewMessageIsAddedToListWhenNoOpenMessagesPresent() {
        CaseData caseDataNoMessages = CaseData.builder().build();
        CaseData caseDataWithMessageAdded = CaseData.builder()
            .openMessages(Collections.singletonList(element(message3)))
            .build();

        sendAndReplyService.addNewMessage(caseDataNoMessages, message3);
        assertThat(caseDataWithMessageAdded.getOpenMessages())
            .hasSize(1)
                .extracting(m -> m.getValue().getMessageContent())
                    .containsExactly("This is message 3 body");
    }

    @Test
    public void testThatNumberOfClosedMessagesIncreasesAndOpenDecreases() {
        UUID messageToClose = caseData.getOpenMessages().get(0).getId();
        long countOpen = caseData.getOpenMessages().stream()
            .filter(m -> m.getValue().getStatus().equals(OPEN))
            .count();
        countOpen -= 1; // we expect one less message to be open.
        List<Element<Message>> serviceUpdated = sendAndReplyService.closeMessage(messageToClose, caseData);
        long actualCount = serviceUpdated.stream()
            .filter(m -> m.getValue().getStatus().equals(OPEN))
            .count();
        assertEquals(countOpen, actualCount);
    }

    @Test
    public void testThatNewSendMessageGeneratesMessageObject() {
        when(time.now()).thenReturn(dateTime);
        Message builtMessage = Message.builder()
            .status(OPEN)
            .dateSent(dateSent)
            .senderEmail(metaData.getSenderEmail())
            .recipientEmail(metaData.getRecipientEmail())
            .messageSubject(metaData.getMessageSubject())
            .messageHistory("sender@email.com - This is the message body")
            .messageUrgency(ofNullable(metaData.getMessageUrgency()).orElse(""))
            .latestMessage(caseData.getMessageContent())
            .updatedTime(dateTime)
            .build();

        assertEquals(sendAndReplyService.buildNewSendMessage(caseData), builtMessage);
    }

    @Test
    public void testPopulateReplyMessageFields() {
        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(message1Element.getId());
        Message populatedReply = Message.builder()
            .senderEmail(message1.getSenderEmail())
            .messageSubject(message1.getMessageSubject())
            .recipientEmail(message1.getSenderEmail())
            .messageUrgency(message1.getMessageUrgency())
            .messageHistory(message1.getMessageHistory())
            .latestMessage(message1.getLatestMessage())
            .replyFrom("sender@email.com")
            .replyTo(message1.getSenderEmail())
            .build();

        Map<String, Object> expectedMap = Map.of("messageReply", populatedReply);
        assertEquals(expectedMap, sendAndReplyService.populateReplyMessageFields(caseData, auth));

    }

    @Test
    public void testPopulateReplyMessageFieldsWhenIdNotPresent() {
        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(UUID.randomUUID());
        Message populatedReply = Message.builder().build();

        Map<String, Object> expectedMap = Collections.emptyMap();
        assertEquals(expectedMap, sendAndReplyService.populateReplyMessageFields(caseData, auth));

    }

    @Test
    public void whenNewReplyThenExistingMessageObjectIsUpdated() {
        UUID selectedMessage = message1Element.getId();
        List<Element<Message>> testMessages = List.of(message1Element);
        Message replyMessage = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient2@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 2 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 2 latest message")
            .messageHistory("")
            .replyFrom("testReply@email.com")
            .replyTo("sender@email.com")
            .build();

        Message updatedMessage1 = Message.builder()
            .senderEmail("testReply@email.com")
            .recipientEmail("sender@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("This is message 2 body")
            .messageHistory("testReply@email.com - This is message 2 body")
            .build();

        Element<Message> updatedElement = element(message1Element.getId(), updatedMessage1);
        List<Element<Message>> updatedMessageList = new ArrayList<>();
        updatedMessageList.add(updatedElement);
        when(time.now()).thenReturn(dateTime);
        assertEquals(updatedMessageList, sendAndReplyService.buildNewReplyMessage(selectedMessage,
                                                                                  replyMessage,
                                                                                  testMessages));

    }

    @Test
    public void whenNewReplyTestNoMatchingMessageElement() {
        UUID selectedMessage = UUID.randomUUID();
        List<Element<Message>> testMessages = List.of(message1Element);
        Message replyMessage = Message.builder()
            .build();
        assertEquals(testMessages, sendAndReplyService.buildNewReplyMessage(selectedMessage,
                                                                                  replyMessage,
                                                                                  testMessages));
    }

    @Test
    public void returnsMapWithCorrectOpenField() {
        Map<String, Object> expectedMap = Map.of("openMessages", messages);
        assertEquals(expectedMap, sendAndReplyService.returnMapOfOpenMessages(messages));
    }

    @Test
    public void returnsMapWithCorrectClosedField() {
        Map<String, Object> expectedMap = Map.of("closedMessages", messages);
        assertEquals(expectedMap, sendAndReplyService.returnMapOfClosedMessages(messages));
    }

    @Test
    public void returnsTrueIfMessagesIsNotNull() {
        assertTrue(sendAndReplyService.hasMessages(caseData));
    }

    @Test
    public void testBuildHistoryString() {
        String expected = "test@gmail.com - Test 1\n \ntest2@gmail.com - Test 2";
        assertEquals(expected, sendAndReplyService.buildMessageHistory("test2@gmail.com",
                                                                       "test@gmail.com - Test 1",
                                                                       "Test 2"));
    }

    @Test
    public void fieldsAreRemovedFromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("one", "this is the first field");
        map.put("two", "this is the second field");
        map.put("three", "this is the third field");
        map.put("four", "this is the fourth field");
        String[] tempFields = {"one", "two", "four"};

        Map<String, Object> expectedMap = Map.of("three", "this is the third field");
        sendAndReplyService.removeTemporaryFields(map, tempFields);
        assertEquals(expectedMap, map);
    }

    @Test
    public void testThatEmailNotificationIsBuilt() {
        SendAndReplyNotificationEmail email =
            SendAndReplyNotificationEmail.builder()
                .caseName(caseData.getApplicantCaseName())
                .messageSubject(message1.getMessageSubject())
                .senderEmail(message1.getSenderEmail())
                .messageUrgency(message1.getMessageUrgency())
                .messageContent(message1.getLatestMessage())
                .caseLink(manageCaseUrl + "/" + caseData.getId())
                .build();
        assertEquals(email, sendAndReplyService.buildNotificationEmail(caseData, message1));
    }

    @Test
    public void testThatEmailServiceIsCalledWithEmailTemplate() {
        SendAndReplyNotificationEmail email =
            SendAndReplyNotificationEmail.builder()
                .caseName(caseData.getApplicantCaseName())
                .messageSubject(message1.getMessageSubject())
                .senderEmail(message1.getSenderEmail())
                .messageUrgency(message1.getMessageUrgency())
                .messageContent(message1.getLatestMessage())
                .caseLink(manageCaseUrl + "/" + caseData.getId())
                .build();

        sendAndReplyService.sendNotificationEmail(caseData, message1);
        verify(emailService, times(1)).send(
            message1.getRecipientEmail(),
            EmailTemplateNames.SEND_AND_REPLY_NOTIFICATION,
            email,
            LanguagePreference.english
        );
    }

    @Test
    public void testGetLinkedCasesDynamicList() {
        DynamicList linkedCasesDynamicList = sendAndReplyService.getLinkedCasesDynamicList(anyString(), anyString());
        assertNotNull(linkedCasesDynamicList);
    }

    @Test
    public void testGetJudiciaryTierDynmicList() {
        Map<String, String> refDataCategoryValueMap = Map.of("51", "High Court Judge", "46", "District Judge Magistrates Court");

        when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), anyString(), anyString())).thenReturn(refDataCategoryValueMap);

        DynamicList judiciaryTierDynmicList = sendAndReplyService.getJudiciaryTierDynamicList(anyString(),anyString(), anyString(), anyString());
        assertNotNull(judiciaryTierDynmicList);
    }

    @Test
    public void testGetExternalRecipientsDynamicMultiselectList() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        Map<String, List<DynamicMultiselectListElement>> listItems = new HashMap<>();
        listItems.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        listItems.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));
        when(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData)).thenReturn(listItems);
        when(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData)).thenReturn(listItems);

        DynamicMultiSelectList externalParties = sendAndReplyService.getExternalRecipientsDynamicMultiselectList(caseData);

        assertNotNull(externalParties);
    }

    @Test
    public void testGetExternalRecipientsDynamicMultiselectListException() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        Map<String, List<DynamicMultiselectListElement>> listItems = new HashMap<>();
        listItems.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        listItems.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));
        when(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData)).thenThrow(new RuntimeException());

        DynamicMultiSelectList externalParties = sendAndReplyService.getExternalRecipientsDynamicMultiselectList(caseData);

        assertNull(externalParties.getListItems());
    }


    @Test
    public void testPopulateDynamicListsForSendAndReply() {

        Map<String, List<DynamicMultiselectListElement>> listItems = new HashMap<>();
        listItems.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        listItems.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));
        when(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData)).thenReturn(listItems);
        when(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData)).thenReturn(listItems);

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);

        CaseData updatedCaseData = sendAndReplyService.populateDynamicListsForSendAndReply(caseData, auth);

        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getSendOrReplyMessage());
    }

    @Test
    public void testPopulateDynamicListsForSendAndReplyWithSubCategories() {

        Map<String, List<DynamicMultiselectListElement>> listItems = new HashMap<>();
        listItems.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        listItems.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));
        when(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData)).thenReturn(listItems);
        when(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData)).thenReturn(listItems);

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category subCategory1 = new Category("subCategory1Id", "subCategory1Name", 1, List.of(document), null);
        Category subCategory2 = new Category("subCategory2Id", "subCategory2Name", 1, List.of(document), List.of(subCategory1));

        Category category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(subCategory2));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);

        userDetails = UserDetails.builder()
            .email("sender@email.com")
            .build();
        when(userService.getUserDetails(auth)).thenReturn(userDetails);

        CaseData updatedCaseData = sendAndReplyService.populateDynamicListsForSendAndReply(caseData, auth);

        assertNotNull(updatedCaseData);
        assertNotNull(updatedCaseData.getSendOrReplyMessage());
    }

    @Test
    public void testGetCategoriesAndDocumentsException() {

        Map<String, List<DynamicMultiselectListElement>> listItems = new HashMap<>();
        listItems.put("applicants", List.of(DynamicMultiselectListElement.EMPTY));
        listItems.put("respondents", List.of(DynamicMultiselectListElement.EMPTY));

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);

        when(authTokenGenerator.generate())
            .thenThrow(new RuntimeException());

        DynamicList dynamicList1 = sendAndReplyService.getCategoriesAndDocuments("test", "test");

        assertNull(dynamicList1.getListItems());
    }

    @Test
    public void testGetJudiciaryTierDynmicListException() {

        when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), anyString(), anyString())).thenThrow(new RuntimeException());

        DynamicList judiciaryTierDynmicList = sendAndReplyService.getJudiciaryTierDynamicList(anyString(),anyString(), anyString(), anyString());
        assertNull(judiciaryTierDynmicList.getListItems());
    }

    @Test
    public void testBuildSendMessage() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList dynamicList1 = DynamicList.builder().build();

        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .externalPartiesList(DynamicMultiSelectList.builder().value(dynamicMultiselectListElementList).build())
                    .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                    .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                    .messageAbout(MessageAboutEnum.APPLICATION)
                    .ctscEmailList(dynamicList1)
                    .judicialOrMagistrateTierList(dynamicList1)
                    .linkedApplicationsList(dynamicList1)
                    .futureHearingsList(dynamicList1)
                    .submittedDocumentsList(dynamicList1).externalPartyDocuments(externalPartyDocsList)
                    .sendReplyJudgeName(judicialUser)
                    .build())
            .build();

        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        Message message = sendAndReplyService.buildSendMessage(caseData);

        assertNotNull(message);
    }

    @Test
    public void testBuildSendMessageWithoutJudgeNameAndExternalPartyDocs() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList dynamicList1 = DynamicList.builder().build();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                    .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                    .messageAbout(MessageAboutEnum.APPLICATION)
                    .ctscEmailList(dynamicList1)
                    .judicialOrMagistrateTierList(dynamicList1)
                    .linkedApplicationsList(dynamicList1)
                    .futureHearingsList(dynamicList1)
                    .sendReplyJudgeName(null)
                    .build())
            .build();

        Message message = sendAndReplyService.buildSendMessage(caseData);

        assertNotNull(message);
    }

    @Test
    public void testAddNewOpenMessage() {
        DynamicList dynamicList1 = DynamicList.builder().build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .externalPartiesList(DynamicMultiSelectList.builder().build())
                    .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                    .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                    .messageAbout(MessageAboutEnum.APPLICATION)
                    .ctscEmailList(dynamicList1)
                    .judicialOrMagistrateTierList(dynamicList1)
                    .linkedApplicationsList(dynamicList1)
                    .futureHearingsList(dynamicList1)
                    .submittedDocumentsList(dynamicList1)
                    .openMessagesList(messagesWithOneAdded)
                    .build())
            .build();

        List<Element<Message>> message = sendAndReplyService.addNewOpenMessage(caseData,message1);

        assertNotNull(message);
    }

}
