package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.apache.commons.collections.ListUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalExternalMessageEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageReplyToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.UploadApplicationDraftOrder;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseLinkedData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SendAndReplyNotificationEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailConfig;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.Attributes;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedJudgeForSendAndReply;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageHistory;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendAndReplyDynamicDoc;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendReplyTempDoc;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.sql.Date;
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

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_C2_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_OTHER_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
import static uk.gov.hmcts.reform.prl.constants.PrlLaunchDarklyFlagConstants.ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    SendgridService sendgridService;

    @Mock
    private DgsService dgsService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private DocumentLanguageService documentLanguageService;

    private static final String RANDOM_ALPHA_NUMERIC = "Abc123EFGH";

    @Mock
    LaunchDarklyClient launchDarklyClient;

    @Mock
    RoleAssignmentApi roleAssignmentApi;

    private static final String randomAlphaNumeric = "Abc123EFGH";

    private static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";

    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenDashboardUrl;

    String auth = "auth-token";
    UserDetails userDetails;
    LocalDateTime dateTime = LocalDateTime.of(
        LocalDate.of(2000, 1, 10),
        LocalTime.of(10, 22)
    );
    String dateSent = dateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK));
    Message message1;
    Message message2;
    Message message3;

    Message messageWithReplyHistory;
    Element<Message> message1Element;
    Element<Message> message2Element;

    Element<MessageHistory> messageHistoryElement;
    List<Element<Message>> messages;
    List<Element<Message>> messagesWithOneAdded;

    List<Element<Message>> listOfOpenMessages;

    List<Element<Message>> listOfClosedMessages;

    List<Element<MessageHistory>> messageHistoryList;

    MessageMetaData metaData;
    DynamicList dynamicList;
    CaseData caseData;
    CaseData caseDataWithAddedMessage;
    Map<String, Object> caseDataMap;

    uk.gov.hmcts.reform.prl.models.documents.Document internalMessageDoc;
    Message messageWithReplyHistoryAndDocuments;

    String awpOtherCode;
    String awpC2Code;

    @Mock
    private HearingDataService hearingDataService;

    @Mock
    private RefDataService refDataService;

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private RefDataUserService refDataUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    private final String serviceAuthToken = "Bearer testServiceAuth";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private HearingService hearingService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    RoleAssignmentService roleAssignmentService;

    @BeforeEach
    public void init() {
        when(time.now()).thenReturn(LocalDateTime.now());
        userDetails = UserDetails.builder()
            .email("sender@email.com")
            .roles(Arrays.asList(
                "caseworker-privatelaw-courtadmin",
                "caseworker-privatelaw-judge ",
                "caseworker-privatelaw-la "
            ))
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
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
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
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        messageHistoryList = new ArrayList<>();
        MessageHistory messageHistory = MessageHistory.builder().messageFrom("sender1@email.com")
            .messageTo("testRecipient1@email.com").messageDate(dateSent).isUrgent(YesOrNo.Yes).build();
        messageHistoryElement = element(messageHistory);
        messageHistoryList.add(messageHistoryElement);

        messageWithReplyHistory = Message.builder()
            .senderEmail("sender2@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject4")
            .messageUrgency("testUrgency4")
            .dateSent(dateSent)
            .messageContent("This is message 4 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 4 latest message")
            .messageHistory("")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        metaData = MessageMetaData.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("recipient@email.com")
            .messageUrgency("Very urgent")
            .messageSubject("Message test subject")
            .build();

        dynamicList = ElementUtils.asDynamicList(messages, null, Message::getLabelForDynamicList);


        message1Element = element(message1);
        message2Element = element(message2);
        messages = new ArrayList<>();
        messages.add(message1Element);
        messages.add(message2Element);
        caseData = CaseData.builder()
            .applicantCaseName("Test name")
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(messages)
                .closedMessages(messages)
                .messageMetaData(metaData)
                .build())
            .messageReply(message1)
            .messageContent("This is the message body")
            .replyMessageDynamicList(dynamicList)
            .build();

        messagesWithOneAdded = Arrays.asList(element(message1), element(message2), element(message3));
        listOfOpenMessages = Arrays.asList(element(message1), element(message3));
        listOfClosedMessages = Arrays.asList(element(message2));

        caseDataWithAddedMessage = CaseData.builder()
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(messagesWithOneAdded)
                .messageMetaData(metaData)
                .build())
            .messageReply(message1)
            .messageContent("This is the message body")
            .replyMessageDynamicList(dynamicList)
            .build();

        caseDataMap = new HashMap<>();

        internalMessageDoc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentUrl("documentURL")
            .documentBinaryUrl("binaryUrl")
            .documentFileName("fileName")
            .documentHash("documentHash")
            .categoryId("categoryId")
            .documentCreatedOn(Date.valueOf("2024-1-1")).build();

        messageWithReplyHistoryAndDocuments = Message.builder()
            .senderEmail("sender2@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject4")
            .messageUrgency("testUrgency4")
            .dateSent(dateSent)
            .messageContent("This is message 4 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 4 latest message")
            .messageHistory("")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        awpOtherCode = AWP_OTHER_APPLICATION_SNR_CODE + UNDERSCORE + dateSent;
        awpC2Code = AWP_C2_APPLICATION_SNR_CODE + UNDERSCORE + dateSent;
        when(launchDarklyClient.isFeatureEnabled(ROLE_ASSIGNMENT_API_IN_ORDERS_JOURNEY)).thenReturn(true);
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = getRoleAssignmentServiceResponse();
        when(roleAssignmentApi.getRoleAssignments(auth, serviceAuthToken, null, null)).thenReturn(roleAssignmentServiceResponse);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

    }

    private static @NotNull RoleAssignmentServiceResponse getRoleAssignmentServiceResponse() {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("hearing-centre-admin");
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);
        return roleAssignmentServiceResponse;
    }

    @Test
    public void testUserServiceReturnsLoggedInEmail() {
        assertEquals(sendAndReplyService.getLoggedInUserEmail(auth), userDetails.getEmail());
    }

    @Test
    public void testThatDynamicListOfOpenMessagesIsGenerated() {
        List<Element<Message>> openMessages = new ArrayList<>();
        openMessages.addAll(messages);
        DynamicList dynamicList = ElementUtils.asDynamicList(openMessages, null, Message::getLabelForDynamicList);
        assertEquals(sendAndReplyService.getOpenMessagesDynamicList(caseData), dynamicList);
    }

    @Test
    public void testThatSenderEmailIsPrefilled() {
        MessageMetaData preFillMetaData = MessageMetaData.builder()
            .senderEmail("sender@email.com")
            .build();
        CaseData caseDataWithPreFill = CaseData.builder()
            .sendOrReplyDto(SendOrReplyDto.builder()
                .messageMetaData(preFillMetaData)
                .build())
            .build();
        assertTrue(sendAndReplyService.setSenderAndGenerateMessageList(caseData, auth).containsKey("messageObject"));
        assertEquals(sendAndReplyService.setSenderAndGenerateMessageList(
            caseDataWithPreFill,
            auth
        ).get("messageObject"), preFillMetaData);
    }

    @Test
    public void testThatNewMessageIsAddedToListWhenOpenMessagesPresent() {
        assertEquals(sendAndReplyService.addNewMessage(caseData, message3).size(), messagesWithOneAdded.size());
    }

    @Test
    public void testThatNewMessageIsAddedToListWhenNoOpenMessagesPresent() {
        CaseData caseDataNoMessages = CaseData.builder().sendOrReplyDto(SendOrReplyDto.builder().build()).build();
        CaseData caseDataWithMessageAdded = CaseData.builder()
            .sendOrReplyDto(SendOrReplyDto.builder()
                .openMessages(Collections.singletonList(element(message3)))
                .build())
            .build();

        sendAndReplyService.addNewMessage(caseDataNoMessages, message3);
        assertThat(caseDataWithMessageAdded.getSendOrReplyDto().getOpenMessages())
            .hasSize(1)
            .extracting(m -> m.getValue().getMessageContent())
            .containsExactly("This is message 3 body");
    }

    @Test
    public void testThatNumberOfClosedMessagesIncreasesAndOpenDecreases() {
        UUID messageToClose = caseData.getSendOrReplyDto().getOpenMessages().get(0).getId();
        long countOpen = caseData.getSendOrReplyDto().getOpenMessages().stream()
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
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        Element<Message> updatedElement = element(message1Element.getId(), updatedMessage1);
        List<Element<Message>> updatedMessageList = new ArrayList<>();
        updatedMessageList.add(updatedElement);
        when(time.now()).thenReturn(dateTime);
        assertEquals(updatedMessageList, sendAndReplyService.buildNewReplyMessage(
            selectedMessage,
            replyMessage,
            testMessages
        ));

    }

    @Test
    public void whenNewReplyTestNoMatchingMessageElement() {
        UUID selectedMessage = UUID.randomUUID();
        List<Element<Message>> testMessages = List.of(message1Element);
        Message replyMessage = Message.builder()
            .build();
        assertEquals(testMessages, sendAndReplyService.buildNewReplyMessage(
            selectedMessage,
            replyMessage,
            testMessages
        ));
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
        assertEquals(expected, sendAndReplyService.buildMessageHistory(
            "test2@gmail.com",
            "test@gmail.com - Test 1",
            "Test 2"
        ));
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
        List<CaseLinkedData> caseLinkedDataList = new ArrayList<>();
        CaseLinkedData caseLinkedData = CaseLinkedData.caseLinkedDataWith()
            .caseName("CaseName-Test10")
            .caseReference("testCaseRefNo")
            .build();
        caseLinkedDataList.add(caseLinkedData);
        List<DynamicListElement> dynamicListElementList = caseLinkedDataList.stream()
            .map(cData -> DynamicListElement.builder()
                .code(cData.getCaseReference()).label(cData.getCaseReference()).build()).collect(Collectors.toList());

        when(hearingDataService.getLinkedCasesDynamicList(anyString(), anyString())).thenReturn(dynamicListElementList);
        DynamicList linkedCasesDynamicList = sendAndReplyService.getLinkedCasesDynamicList(anyString(), anyString());
        assertEquals("testCaseRefNo", linkedCasesDynamicList.getListItems().get(0).getLabel());
    }

    @Test
    public void testGetJudiciaryTierDynmicList() {
        Map<String, String> refDataCategoryValueMap = Map.of(
            "51",
            "High Court Judge",
            "46",
            "District Judge Magistrates Court"
        );

        when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), anyString(), anyString())).thenReturn(
            refDataCategoryValueMap);

        DynamicList judiciaryTierDynmicList = sendAndReplyService.getJudiciaryTierDynamicList(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        );

        assertTrue(judiciaryTierDynmicList.getListItems().stream().anyMatch(ti -> ti.getLabel().equals(
            "High Court Judge")));
    }

    @Test
    public void testPopulateDynamicListsForSendAndReply() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        LocalDateTime hearingStartDate = LocalDateTime.now().plusDays(5).withNano(1);
        HearingDaySchedule hearingDaySchedule =
            HearingDaySchedule.hearingDayScheduleWith()
                .hearingVenueId("231596")
                .hearingJudgeId("4925644")
                .hearingStartDateTime(hearingStartDate)
                .build();
        List<HearingDaySchedule> hearingDayScheduleList = new ArrayList<>();
        hearingDayScheduleList.add(hearingDaySchedule);

        CaseHearing caseHearingWithListedStatus =
            CaseHearing.caseHearingWith().hearingID(123L)
                .hmcStatus("LISTED")
                .hearingType("hearingType1")
                .hearingDaySchedule(hearingDayScheduleList)
                .build();
        CaseHearing caseHearingWithCancelled =
            CaseHearing.caseHearingWith().hearingID(345L)
                .hmcStatus("LISTED")
                .hearingType("hearingType2")
                .hearingDaySchedule(hearingDayScheduleList)
                .build();

        CaseHearing caseHearingWithoutHearingDaySche =
            CaseHearing.caseHearingWith().hearingID(678L)
                .hmcStatus("CANCELLED")
                .hearingType("hearingType3")
                .hearingDaySchedule(null)
                .build();

        List<CaseHearing> caseHearingList = new ArrayList<>();
        caseHearingList.add(caseHearingWithListedStatus);
        caseHearingList.add(caseHearingWithCancelled);
        caseHearingList.add(caseHearingWithoutHearingDaySche);

        Hearings futureHearings =
            Hearings.hearingsWith()
                .caseRef("123")
                .hmctsServiceCode("ABA5")
                .caseHearings(caseHearingList)
                .build();

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(
            1,
            List.of(category),
            List.of(document)
        );
        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);
        when(hearingService.getFutureHearings(anyString(), anyString())).thenReturn(futureHearings);

        Map<String, String> refDataCategoryValueMap = new HashMap<>();

        refDataCategoryValueMap.put("hearingType1", "val1");
        refDataCategoryValueMap.put("hearingType2", "val2");
        refDataCategoryValueMap.put("hearingType3", "val3");

        ReflectionTestUtils.setField(
            sendAndReplyService, "serviceCode", "serviceCode");
        ReflectionTestUtils.setField(
            sendAndReplyService, "hearingTypeCategoryId", "hearingTypeCategoryId");


        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
            .otherApplicationsBundle(OtherApplicationsBundle.builder()
                .applicationStatus(AWP_STATUS_SUBMITTED)
                .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                .uploadedDateTime(dateSent).build())
            .c2DocumentBundle(C2DocumentBundle.builder()
                .applicationStatus(AWP_STATUS_SUBMITTED)
                .uploadedDateTime(dateSent).build())
            .build()));

        caseData = caseData.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), anyString(), anyString())).thenReturn(
            refDataCategoryValueMap);

        CaseData updatedCaseData = sendAndReplyService.populateDynamicListsForSendAndReply(caseData, auth);

        assertNotNull(updatedCaseData);
        assertEquals("123 - hearingType1", updatedCaseData.getSendOrReplyMessage().getSendMessageObject()
            .getFutureHearingsList().getListItems().get(0).getCode());
    }

    @Test
    public void testPopulateDynamicListsForSendAndReplyWithSubCategories() {

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category subCategory1 = new Category("subCategory1Id", "subCategory1Name", 1, List.of(document), null);
        Category subCategory2 = new Category(
            "subCategory2Id",
            "subCategory2Name",
            1,
            List.of(document),
            List.of(subCategory1)
        );

        Category category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(subCategory2));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(
            1,
            List.of(category),
            List.of(document)
        );

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
        assertEquals("categoryId->documentURL", updatedCaseData.getSendOrReplyMessage()
            .getSendMessageObject().getSubmittedDocumentsList().getListItems().get(0).getCode());
    }

    @Test
    public void testGetCategoriesAndDocumentsException() {

        when(authTokenGenerator.generate())
            .thenThrow(FeignException.errorStatus("getHearingDetails", Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(Request.HttpMethod.GET, "/hearings", Map.of(), null, null, null))
                .build()));

        DynamicList dynamicList1 = sendAndReplyService.getCategoriesAndDocuments("test", "test");

        assertNull(dynamicList1.getListItems());
    }

    @Test
    public void testGetJudiciaryTierDynmicListException() {

        when(refDataService.getRefDataCategoryValueMap(anyString(), anyString(), anyString(), anyString())).thenThrow(
            new RuntimeException());

        DynamicList judiciaryTierDynmicList = sendAndReplyService.getJudiciaryTierDynamicList(
            anyString(),
            anyString(),
            anyString(),
            anyString()
        );
        assertNull(judiciaryTierDynmicList.getListItems());
    }

    @Test
    public void testBuildSendMessageWithNullMessage() {
        CaseData caseData = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder().build())
            .build();

        Message message = sendAndReplyService.buildSendReplyMessage(
            caseData,
            caseData.getSendOrReplyMessage().getSendMessageObject(),
            auth
        );

        assertEquals(null, message.getMessageContent());
    }

    @Test
    public void testBuildSendMessageWithMessageForJudge() {
        CaseData caseData = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(userService.getUserDetails(auth)).thenReturn(UserDetails.builder()
            .roles(List.of(JUDGE_ROLE))
            .build());
        Message message = sendAndReplyService.buildSendReplyMessage(caseData,
            caseData.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
    }

    @Test
    public void testBuildSendMessageWithMessageForLegalAdvisor() {
        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.REVIEW_SUBMITTED_DOCUMENTS)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList.toBuilder()
                                .value(DynamicListElement.builder()
                                    .code(UUID.randomUUID())
                                    .label("test-document")
                                    .build())
                                .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(userService.getUserDetails(auth)).thenReturn(UserDetails.builder()
            .roles(List.of(LEGAL_ADVISER_ROLE))
            .build());

        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(caseDocumentClient
            .getMetadataForDocument(anyString(),anyString(),any(UUID.class)))
            .thenReturn(testDocument());
        Message message = sendAndReplyService.buildSendReplyMessage(data,
            data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
    }

    @Test
    public void testBuildSendMessageWithMessageForNoRolesinUserDetails() {
        CaseData caseData = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(userService.getUserDetails(auth)).thenReturn(UserDetails.builder()
            .build());
        Message message = sendAndReplyService.buildSendReplyMessage(caseData,
            caseData.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
    }

    @Test
    public void testBuildSendMessage() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);

        DynamicList dynamicList = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();

        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        CaseData caseData = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .applicationsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(
            caseData,
            caseData.getSendOrReplyMessage().getSendMessageObject(),
            auth
        );

        assertEquals("some message while sending", message.getMessageContent());
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
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList1)
                            .judicialOrMagistrateTierList(dynamicList1)
                            .applicationsList(dynamicList1)
                            .futureHearingsList(dynamicList1)
                            .sendReplyJudgeName(null)
                            .build()
                    ).build())
            .build();

        UserDetails userDetails = UserDetails.builder()
            .email("sender@email.com")
            .roles(Arrays.asList("caseworker-privatelaw-judge ", "caseworker-privatelaw-la "))
            .build();
        when(userService.getUserDetails(auth)).thenReturn(userDetails);

        Message message = sendAndReplyService.buildSendReplyMessage(
            caseData,
            caseData.getSendOrReplyMessage().getSendMessageObject(),
            auth
        );

        assertNull(message.getJudgeName());
    }

    @Test
    public void testBuildSendMessageWhenOtherApplicationDocumentSelected() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList list = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();
        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(list)
                            .judicialOrMagistrateTierList(list)
                            .applicationsList(list)
                            .futureHearingsList(list)
                            .submittedDocumentsList(list)
                            .selectedApplicationCode(awpOtherCode)
                            .applicationsList(list.toBuilder()
                                                  .value(DynamicListElement.builder()
                                                             .code(awpOtherCode)
                                                             .label("test-document")
                                                             .build())
                                                  .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                                  .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                                                                                  .uploadedDateTime(dateSent)
                                                                                  .finalDocument(List.of(element(internalMessageDoc))).build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                           .uploadedDateTime(dateSent).build())
                                                     .build()));
        data = data.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(data,
                                                                    data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
        assertEquals(internalMessageDoc, message.getInternalMessageAttachDocs().get(0).getValue());
    }

    @Test
    public void testBuildSendMessageWhenOtherApplicationDocumentSelectedAndSupportingDocumentsUploaded() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList list = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();
        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(list)
                            .judicialOrMagistrateTierList(list)
                            .applicationsList(list)
                            .futureHearingsList(list)
                            .submittedDocumentsList(list)
                            .selectedApplicationCode(awpOtherCode)
                            .applicationsList(list.toBuilder()
                                                  .value(DynamicListElement.builder()
                                                             .code(awpOtherCode)
                                                             .label("test-document")
                                                             .build())
                                                  .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                                  .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                                                                                  .uploadedDateTime(dateSent)
                                                                                  .finalDocument(List.of(element(internalMessageDoc)))
                                                                                  .supplementsBundle(List.of(element(
                                                                                      Supplement.builder().document(internalMessageDoc).build())))
                                                                                  .supportingEvidenceBundle(List.of(element(
                                                                                      SupportingEvidenceBundle
                                                                                          .builder()
                                                                                          .document(internalMessageDoc)
                                                                                          .build())))
                                                                                  .build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                           .uploadedDateTime(dateSent).build())
                                                     .build()));
        data = data.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(data,
                                                                    data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
        assertEquals(3, message.getInternalMessageAttachDocs().size());
        assertEquals(internalMessageDoc, message.getInternalMessageAttachDocs().get(0).getValue());
    }

    @Test
    public void testBuildSendMessageWhenC2ApplicationDocumentSelected() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList list = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();
        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(list)
                            .judicialOrMagistrateTierList(list)
                            .applicationsList(list)
                            .futureHearingsList(list)
                            .submittedDocumentsList(list)
                            .selectedApplicationCode(awpC2Code)
                            .applicationsList(list.toBuilder()
                                                  .value(DynamicListElement.builder()
                                                             .code(awpC2Code)
                                                             .label("test-document")
                                                             .build())
                                                  .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                                  .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                                                                                  .uploadedDateTime(dateSent)
                                                                                  .build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                           .uploadedDateTime(dateSent)
                                                                           .finalDocument(List.of(element(internalMessageDoc)))
                                                                           .build())
                                                     .build()));
        data = data.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(data,
                                                                    data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
        assertEquals(internalMessageDoc, message.getInternalMessageAttachDocs().get(0).getValue());
    }

    @Test
    public void testBuildSendMessageWhenC2ApplicationDocumentSelectedAndSupportingDocumentsUploaded() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList list = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();
        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(list)
                            .judicialOrMagistrateTierList(list)
                            .applicationsList(list)
                            .futureHearingsList(list)
                            .submittedDocumentsList(list)
                            .selectedApplicationCode(awpC2Code)
                            .applicationsList(list.toBuilder()
                                                  .value(DynamicListElement.builder()
                                                             .code(awpC2Code)
                                                             .label("test-document")
                                                             .build())
                                                  .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                                  .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                                                                                  .uploadedDateTime(dateSent)
                                                                                  .build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                           .uploadedDateTime(dateSent)
                                                                           .finalDocument(List.of(element(internalMessageDoc)))
                                                                           .supplementsBundle(List.of(element(
                                                                               Supplement.builder().document(internalMessageDoc).build())))
                                                                           .supportingEvidenceBundle(List.of(element(
                                                                               SupportingEvidenceBundle
                                                                                   .builder()
                                                                                   .document(internalMessageDoc)
                                                                                   .build())))
                                                                           .additionalDraftOrdersBundle(List.of(element(
                                                                               UploadApplicationDraftOrder
                                                                                   .builder()
                                                                                   .document(internalMessageDoc)
                                                                                   .build())))
                                                                           .build())
                                                     .build()));
        data = data.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(data,
                                                                    data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
        assertEquals(4, message.getInternalMessageAttachDocs().size());
        assertEquals(internalMessageDoc, message.getInternalMessageAttachDocs().get(0).getValue());
    }

    @Test
    public void testBuildSendMessageWhenNoOtherApplicationDocumentSelected() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList list = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();
        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(list)
                            .judicialOrMagistrateTierList(list)
                            .applicationsList(list)
                            .futureHearingsList(list)
                            .submittedDocumentsList(list)
                            .selectedApplicationCode("test")
                            .applicationsList(list.toBuilder()
                                                  .value(DynamicListElement.builder()
                                                             .code("test")
                                                             .label("test-document")
                                                             .build())
                                                  .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                                  .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                                                                                  .uploadedDateTime(dateSent)
                                                                                  .build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(AWP_STATUS_SUBMITTED)
                                                                           .uploadedDateTime(dateSent)
                                                                           .finalDocument(List.of(element(internalMessageDoc)))
                                                                           .build())
                                                     .build()));
        data = data.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(data,
                                                                    data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
        assertEquals(emptyList(), message.getInternalMessageAttachDocs());
    }

    @Test
    public void testBuildSendMessageWhenNoOtherApplicationDocumentsAreSubmittedState() {
        UUID uuid = UUID.randomUUID();
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.EMPTY;
        List<DynamicMultiselectListElement> dynamicMultiselectListElementList = new ArrayList<>();
        dynamicMultiselectListElementList.add(dynamicMultiselectListElement);
        DynamicList list = DynamicList.builder().value(DynamicListElement.builder().code(uuid).build()).build();
        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();

        CaseData data = CaseData.builder()
            .messageContent("some message while sending")
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(list)
                            .judicialOrMagistrateTierList(list)
                            .applicationsList(list)
                            .futureHearingsList(list)
                            .submittedDocumentsList(list)
                            .selectedApplicationCode(awpC2Code)
                            .applicationsList(list.toBuilder()
                                                  .value(DynamicListElement.builder()
                                                             .code(awpC2Code)
                                                             .label("test-document")
                                                             .build())
                                                  .build())
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle = new ArrayList<>();
        additionalApplicationsBundle.add(element(AdditionalApplicationsBundle.builder()
                                                     .otherApplicationsBundle(OtherApplicationsBundle.builder()
                                                                                  .applicationStatus(AWP_STATUS_IN_REVIEW)
                                                                                  .applicationType(OtherApplicationType.FC600_COMMITTAL_APPLICATION)
                                                                                  .uploadedDateTime(dateSent)
                                                                                  .build())
                                                     .c2DocumentBundle(C2DocumentBundle.builder()
                                                                           .applicationStatus(AWP_STATUS_CLOSED)
                                                                           .uploadedDateTime(dateSent)
                                                                           .finalDocument(List.of(element(internalMessageDoc)))
                                                                           .build())
                                                     .build()));
        data = data.toBuilder().additionalApplicationsBundle(additionalApplicationsBundle).build();
        List<JudicialUsersApiResponse> judicialUsersApiResponseList = Arrays.asList(JudicialUsersApiResponse.builder().build());
        uk.gov.hmcts.reform.ccd.document.am.model.Document document1 = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();

        when(sendAndReplyService.getJudgeDetails(judicialUser)).thenReturn(judicialUsersApiResponseList);
        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(document1);
        Message message = sendAndReplyService.buildSendReplyMessage(data,
                                                                    data.getSendOrReplyMessage().getSendMessageObject(), auth);

        assertEquals("some message while sending",message.getMessageContent());
        assertEquals(emptyList(), message.getInternalMessageAttachDocs());
    }

    @Test
    public void testAddNewOpenMessage() {

        List<Element<Message>> openMessagesList = new ArrayList<>();
        openMessagesList.add(element(message1));

        DynamicList dynamicList1 = DynamicList.builder().build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList1)
                            .judicialOrMagistrateTierList(dynamicList1)
                            .applicationsList(dynamicList1)
                            .futureHearingsList(dynamicList1)
                            .updatedTime(dateTime.now())
                            .build()
                    )
                    .messages(openMessagesList)
                    .build())
            .build();

        List<Element<Message>> updatedMessageList = sendAndReplyService.addMessage(caseData, auth, caseDataMap);

        assertEquals(2,updatedMessageList.size());
    }

    @Test
    public void testAddNewOpenMessageWithoutExistingJudgeAllocationFromSendAndReplyWithIdamIdAndMessageIdentifier() {

        List<Element<Message>> openMessagesList = new ArrayList<>();
        openMessagesList.add(element(message1));
        DynamicList dynamicList1 = DynamicList.builder().build();
        List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeList = new ArrayList<>();
        allocatedJudgeList.add(element(AllocatedJudgeForSendAndReply
            .builder()
            .messageIdentifier("test")
            .judgeIdamId(TEST_UUID)
            .build()));
        CaseData caseData3 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyDto(SendOrReplyDto.builder()
                .allocatedJudgeForSendAndReply(allocatedJudgeList)
                .build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList1)
                            .judicialOrMagistrateTierList(dynamicList1)
                            .applicationsList(dynamicList1)
                            .sendReplyJudgeName(JudicialUser.builder().personalCode("test").idamId(TEST_UUID).build())
                            .futureHearingsList(dynamicList1)
                            .updatedTime(dateTime.now())
                            .build()
                    )
                    .messages(openMessagesList)
                    .build())
            .chooseSendOrReply(SendOrReply.SEND)
            .build();

        List<Element<Message>> updatedMessageList = sendAndReplyService.addMessage(caseData3, auth, caseDataMap);

        assertEquals(2,updatedMessageList.size());
    }

    @Test
    public void testAddNewOpenMessageWithoutCaseAlreadyAllocatedToJudge() {

        List<Element<Message>> openMessagesList = new ArrayList<>();
        openMessagesList.add(element(message1));
        RoleAssignmentResponse roleAssignmentResponse =  new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("allocated-judge");
        roleAssignmentResponse.setAttributes(Attributes.builder().caseId("1234").build());
        List<RoleAssignmentResponse> roleAssignmentResponses =  new ArrayList<>();
        roleAssignmentResponses.add(roleAssignmentResponse);
        when(roleAssignmentService.getRoleAssignmentForActorId(anyString()))
            .thenReturn(roleAssignmentResponses);
        DynamicList dynamicList1 = DynamicList.builder().build();
        List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeList = new ArrayList<>();
        allocatedJudgeList.add(element(AllocatedJudgeForSendAndReply
            .builder()
            .messageIdentifier("test")
            .judgeIdamId("test")
            .build()));
        CaseData caseData4 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyDto(SendOrReplyDto.builder()
                .allocatedJudgeForSendAndReply(allocatedJudgeList)
                .build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList1)
                            .judicialOrMagistrateTierList(dynamicList1)
                            .applicationsList(dynamicList1)
                            .sendReplyJudgeName(JudicialUser.builder().personalCode("test").idamId(TEST_UUID).build())
                            .futureHearingsList(dynamicList1)
                            .updatedTime(dateTime.now())
                            .build()
                    )
                    .messages(openMessagesList)
                    .build())
            .id(1234L)
            .chooseSendOrReply(SendOrReply.SEND)
            .build();
        List<Element<Message>> updatedMessageList = sendAndReplyService.addMessage(caseData4, auth, caseDataMap);

        assertEquals(2,updatedMessageList.size());
    }

    @Test
    public void testRoleAddNewOpenMessageWithoutCaseAlreadyAllocatedToJudge() {

        List<Element<Message>> openMessagesList = new ArrayList<>();
        openMessagesList.add(element(message1));
        RoleAssignmentResponse roleAssignmentResponse =  new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("allocated-judge");
        roleAssignmentResponse.setAttributes(Attributes.builder().caseId("1234567").build());
        List<RoleAssignmentResponse> roleAssignmentResponses =  new ArrayList<>();
        roleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentResponse roleAssignmentResponse2 =  new RoleAssignmentResponse();
        roleAssignmentResponse2.setRoleName("allocated-judge");
        roleAssignmentResponse2.setAttributes(Attributes.builder().caseId("1234").build());
        List<RoleAssignmentResponse> roleAssignmentResponses2 =  new ArrayList<>();
        roleAssignmentResponses2.add(roleAssignmentResponse2);
        when(roleAssignmentService.getRoleAssignmentForActorId(anyString()))
            .thenReturn(roleAssignmentResponses).thenReturn(roleAssignmentResponses2);
        DynamicList dynamicList1 = DynamicList.builder().build();
        List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeList = new ArrayList<>();
        allocatedJudgeList.add(element(AllocatedJudgeForSendAndReply
            .builder()
            .messageIdentifier("test")
            .judgeIdamId("test")
            .build()));
        CaseData caseData5 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyDto(SendOrReplyDto.builder()
                .allocatedJudgeForSendAndReply(allocatedJudgeList)
                .build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList1)
                            .judicialOrMagistrateTierList(dynamicList1)
                            .applicationsList(dynamicList1)
                            .sendReplyJudgeName(JudicialUser.builder().personalCode("test").idamId(TEST_UUID).build())
                            .futureHearingsList(dynamicList1)
                            .updatedTime(dateTime.now())
                            .build()
                    )
                    .messages(openMessagesList)
                    .build())
            .id(1234L)
            .chooseSendOrReply(SendOrReply.SEND)
            .build();
        List<Element<Message>> updatedMessageList = sendAndReplyService.addMessage(caseData5, auth, caseDataMap);

        assertEquals(2, updatedMessageList.size());
    }

    @Test
    public void testSetSenderAndGenerateMessageReplyList() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messages(messagesWithOneAdded)
                    .build())
            .build();

        Map<String, Object> updatedResponse = sendAndReplyService.setSenderAndGenerateMessageReplyList(caseData, auth);
        MessageMetaData messageMetaData = (MessageMetaData) updatedResponse.get("messageObject");

        assertNotNull(updatedResponse.get("messageReplyDynamicList"));
        assertEquals("sender@email.com", messageMetaData.getSenderEmail());

    }

    @Test
    public void testPopulateMessageReplyFieldsWithNoPrevMsg() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            dynamicList,
            objectMapper
        )).thenReturn(messagesWithOneAdded.get(0).getId());
        CaseData data = sendAndReplyService.populateMessageReplyFields(caseData, auth);
        assertNotNull(data);
    }


    @Test
    public void testPopulateMessageReplyFieldsWithPrevMsgWithoutReplyHistory() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(listOfOpenMessages)
                    .build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(
            dynamicList,
            objectMapper
        )).thenReturn(listOfOpenMessages.get(0).getId());
        CaseData data = sendAndReplyService.populateMessageReplyFields(caseData, auth);
        assertNotNull(data.getSendOrReplyMessage().getMessageReplyTable());

    }

    @Test
    public void testPopulateMessageReplyFieldsWithPrevMsgWithReplyHistory() {

        List<Element<Message>> openMessagesListWithReplyHistory = new ArrayList<>();
        openMessagesListWithReplyHistory.add(element(messageWithReplyHistory));
        openMessagesListWithReplyHistory.add(element(message1));
        openMessagesListWithReplyHistory.add(element(message3));

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesListWithReplyHistory)
                    .build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(
            openMessagesListWithReplyHistory.get(0).getId());
        CaseData updatedCaseData = sendAndReplyService.populateMessageReplyFields(caseData, auth);
        String messageTo = updatedCaseData.getSendOrReplyMessage().getMessages()
            .get(0).getValue().getReplyHistory().get(0).getValue().getMessageTo();

        assertEquals("testRecipient1@email.com", messageTo);
    }

    @Test
    public void testCloseMessage() {
        when(elementUtils.getDynamicListSelectedValue(
            dynamicList,
            objectMapper
        )).thenReturn(listOfOpenMessages.get(0).getId());
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(ListUtils.union(listOfOpenMessages, listOfClosedMessages))
                    .build())
            .sendOrReplyDto(SendOrReplyDto.builder().build())
            .build();
        doNothing().when(roleAssignmentService).removeRoleAssignment(anyString());

        List<Element<Message>> closeMessages = sendAndReplyService.closeMessage(caseData, caseDataMap);

        assertEquals(3, closeMessages.size());
    }

    @Test
    public void testCloseMessageAndRemoveAllocatedJudgeIfadded() {
        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(UUID.fromString(TEST_UUID));

        List<Element<Message>> closedMessages = new ArrayList<>();
        closedMessages.add(element(UUID.fromString(TEST_UUID), Message.builder()
            .internalMessageReplyTo(InternalMessageReplyToEnum.JUDICIARY)
            .sendReplyJudgeName(JudicialUser.builder().personalCode("123").idamId(TEST_UUID).build())
            .messageIdentifier("test")
            .build()));
        List<Element<AllocatedJudgeForSendAndReply>> allocatedJudgeList = new ArrayList<>();
        allocatedJudgeList.add(element(AllocatedJudgeForSendAndReply
            .builder()
            .messageIdentifier("test")
            .judgeIdamId(TEST_UUID)
            .build()));
        CaseData caseData6 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(closedMessages)
                    .build())
            .sendOrReplyDto(SendOrReplyDto.builder()
                .allocatedJudgeForSendAndReply(allocatedJudgeList).build())
            .build();

        List<Element<Message>> closeMessages = sendAndReplyService.closeMessage(caseData6, caseDataMap);

        assertEquals(1,closeMessages.size());
    }

    @Test
    public void testReplyAndAppendMessageHistoryForReply() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        openMessagesList.add(element(message1));

        CaseData caseData = CaseData.builder()

            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .replyMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                            .internalMessageReplyTo(InternalMessageReplyToEnum.COURT_ADMIN)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .messageContent("Reply Message Content")
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(
            dynamicList,
            objectMapper
        )).thenReturn(openMessagesList.get(0).getId());
        List<Element<Message>> msgList = sendAndReplyService.replyAndAppendMessageHistory(caseData, auth, caseDataMap);

        assertEquals(1,msgList.get(0).getValue().getReplyHistory().size());
    }

    @Test
    public void testSendReplyAndAppendMessageHistoryForReply() {

        RoleAssignmentResponse roleAssignmentResponse =  new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("allocated-judge");
        roleAssignmentResponse.setAttributes(Attributes.builder().caseId("1234").build());
        roleAssignmentResponse.setRoleName("allocated-judge");
        roleAssignmentResponse.setAttributes(Attributes.builder().caseId("12345").build());
        List<RoleAssignmentResponse> roleAssignmentResponses =  new ArrayList<>();
        roleAssignmentResponses.add(roleAssignmentResponse);
        roleAssignmentResponses.add(roleAssignmentResponse);
        when(roleAssignmentService.getRoleAssignmentForActorId(anyString()))
            .thenReturn(roleAssignmentResponses);

        List<Element<Message>> openMessagesList = new ArrayList<>();

        openMessagesList.add(element(message1));
        JudicialUser judicialUser = JudicialUser.builder()
            .idamId("testIdam")
            .personalCode("123").build();

        Message message = Message.builder()
            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
            .messageAbout(MessageAboutEnum.OTHER)
            .ctscEmailList(dynamicList)
            .judgeName("JudgeName")
            .judgeEmail("judge@email.com")
            .judicialOrMagistrateTierList(dynamicList)
            .applicationsList(dynamicList)
            .futureHearingsList(dynamicList)
            .sendReplyJudgeName(judicialUser)
            .submittedDocumentsList(dynamicList)
            .build();


        SendOrReplyMessage sendOrReplyMessage = SendOrReplyMessage.builder()
            .messageReplyDynamicList(dynamicList)
            .messages(openMessagesList)
            .sendMessageObject(message)
            .replyMessageObject(
                Message.builder()
                    .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                    .internalMessageReplyTo(InternalMessageReplyToEnum.JUDICIARY)
                    .sendReplyJudgeName(judicialUser)
                    .messageAbout(MessageAboutEnum.APPLICATION)
                    .messageContent("Reply Message Content")
                    .submittedDocumentsList(dynamicList)
                    .build()
            )
            .build();

        CaseData caseData7 = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.REPLY)
            .sendOrReplyDto(SendOrReplyDto.builder().openMessages(messages)
                .closedMessages(messages)
                .messageMetaData(metaData)
                .build())
            .sendOrReplyMessage(sendOrReplyMessage)
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesList.get(0).getId());
        List<Element<Message>> msgList = sendAndReplyService.replyAndAppendMessageHistory(caseData7, auth, caseDataMap);

        assertEquals(1, msgList.get(0).getValue().getReplyHistory().size());
    }

    @Test
    public void testReplyAndAppendMessageHistoryForReplyToReply() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message sendMessage1 = getMessageInformation();

        openMessagesList.add(element(sendMessage1));

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .replyMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
                            .internalMessageReplyTo(InternalMessageReplyToEnum.COURT_ADMIN)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .messageContent("Reply Message Content")
                            .build()
                    )
                    .build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(
            dynamicList,
            objectMapper
        )).thenReturn(openMessagesList.get(0).getId());
        List<Element<Message>> msgList = sendAndReplyService.replyAndAppendMessageHistory(caseData, auth, caseDataMap);

        assertEquals(2, msgList.get(0).getValue().getReplyHistory().size());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForReplyAndResetOtherWhileJudiciarySelected() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message1 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        openMessagesList.add(element(message1));
        DynamicList dynamicList = ElementUtils.asDynamicList(openMessagesList, null, Message::getLabelForDynamicList);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .replyMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .messageContent("Reply Message Content").judicialOrMagistrateTierList(dynamicList)
                            .judgeName("John Wakefield").ctscEmailList(dynamicList)
                            .recipientEmailAddresses("recep")
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(caseData);

        assertNull(caseDataResetResp.getSendOrReplyMessage().getReplyMessageObject().getSendReplyJudgeName());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForReplyAndResetJudiciaryWhileOtherSelected() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message1 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .sendReplyJudgeName(JudicialUser.builder().personalCode("123").build())
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        openMessagesList.add(element(message1));
        DynamicList dynamicList = ElementUtils.asDynamicList(openMessagesList, null, Message::getLabelForDynamicList);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .replyMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .messageContent("Reply Message Content").judicialOrMagistrateTierList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().personalCode("123").build())
                            .ctscEmailList(dynamicList)
                            .recipientEmailAddresses("recep")
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(caseData);

        assertNull(caseDataResetResp.getSendOrReplyMessage().getReplyMessageObject().getSendReplyJudgeName());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForSendWhenOtherSelected() {
        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message1 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();
        openMessagesList.add(element(message1));
        DynamicList dynamicList = ElementUtils.asDynamicList(openMessagesList, null, Message::getLabelForDynamicList);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.OTHER)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .sendReplyJudgeName(judicialUser)
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(caseData);

        assertNull(caseDataResetResp.getSendOrReplyMessage().getSendMessageObject().getSendReplyJudgeName());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForSendWhenJudiciarySelected() {
        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message1 = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        JudicialUser judicialUser = JudicialUser.builder().personalCode("123").build();
        openMessagesList.add(element(message1));
        DynamicList dynamicList = ElementUtils.asDynamicList(openMessagesList, null, Message::getLabelForDynamicList);

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.OTHER)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .sendReplyJudgeName(judicialUser)
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(caseData);

        assertNull(caseDataResetResp.getSendOrReplyMessage().getSendMessageObject().getRecipientEmailAddresses());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForSendWhenApplicationSelected() {
        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        openMessagesList.add(element(message));

        CaseData data = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(data);

        assertEquals(DynamicListElement.EMPTY, caseDataResetResp.getSendOrReplyMessage().getSendMessageObject().getApplicationsList().getValue());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForSendWhenHearingSelected() {
        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        openMessagesList.add(element(message));

        CaseData data = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.HEARING)
                            .ctscEmailList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(data);

        assertEquals(DynamicListElement.EMPTY, caseDataResetResp.getSendOrReplyMessage().getSendMessageObject().getFutureHearingsList().getValue());
    }

    @Test
    public void testResetSendAndReplyDynamicListsForSendWhenSubmittedDocumentsSelected() {
        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message message = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();

        openMessagesList.add(element(message));

        CaseData data = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesList)
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.REVIEW_SUBMITTED_DOCUMENTS)
                            .ctscEmailList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .build())
            .build();

        CaseData caseDataResetResp = sendAndReplyService.resetSendAndReplyDynamicLists(data);

        assertEquals(DynamicListElement.EMPTY,
                     caseDataResetResp.getSendOrReplyMessage().getSendMessageObject().getSubmittedDocumentsList().getValue());
    }

    @Test
    public void testSendNotificationEmailOther() {
        EmailTemplateVars emailTemplateVars = SendAndReplyNotificationEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(manageCaseUrl + "/" + caseData.getId())
            .build();
        Message message = Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com").recipientEmailAddresses("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .messageHistory("")
            .build();
        caseData = caseData.toBuilder().sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.JUDICIARY)
                            .messageAbout(MessageAboutEnum.OTHER)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .recipientEmailAddresses("testRecipient1@email.com")
                            .submittedDocumentsList(dynamicList)
                            .build()
                    )
                    .messages(Collections.singletonList(element(message)))
                    .build())
            .build();
        sendAndReplyService.sendNotificationEmailOther(caseData);
        verify(emailService, times(1)).send(
            message.getRecipientEmail(),
            EmailTemplateNames.SEND_AND_REPLY_NOTIFICATION_OTHER,
            emailTemplateVars,
            LanguagePreference.english
        );
    }

    @Test
    public void testSendEmailNotificationToExternalPartiesC100Case() {

        PartyDetails applicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .firstName("Applicant firstname")
            .lastName("Applicant lastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .solicitorEmail("testSolicitor@xyz.com")
            .phoneNumber("1234567890")
            .contactPreferences(ContactPreferences.email)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(applicant.getPartyId()).value(
            applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .firstName("Respondent firstname")
            .lastName("Respondent lastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .contactPreferences(ContactPreferences.email)
            .address(Address.builder().addressLine1("1 ADD Road").postCode("1XY 2AB").country("ABC").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(respondent.getPartyId()).value(
            respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);


        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(wrappedRespondents.getId().toString())
            .label(respondent.getFirstName() + " " + respondent.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();


        CaseData caseDataC100Message = CaseData.builder().id(12345L)
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication("C100")
            .replyMessageDynamicList(DynamicList.builder().build())
            .applicants(applicantList)
            .messageContent("some msg content")
            .respondents(respondentList)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                                           .externalMessageWhoToSendTo(externalMessageWhoToSendTo)
                                           .messageAbout(MessageAboutEnum.APPLICATION)
                                           .messageContent("some msg content")
                                           .messageSubject("message subject")
                                           .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();

        Map<String, Object> dynamicData = getEmailDynamicData(caseDataC100Message);
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().toEmailAddress("testSolicitor@xyz.com")
            .dynamicTemplateData(dynamicData)
            .listOfAttachments(new ArrayList<>())
            .languagePreference(LanguagePreference.english)
            .build();
        sendAndReplyService.sendNotificationToExternalParties(caseDataC100Message, "authorisation");
        verify(sendgridService).sendEmailUsingTemplateWithAttachments(
            SendgridEmailTemplateNames.SEND_EMAIL_TO_EXTERNAL_PARTY,
            "authorisation",
            sendgridEmailConfig
        );
    }

    @Test
    public void testSendEmailNotificationToCafcassAndOthersC100Case() {

        PartyDetails applicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .firstName("Applicant firstname")
            .lastName("Applicant lastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .solicitorEmail("testSolicitor@xyz.com")
            .phoneNumber("1234567890")
            .contactPreferences(ContactPreferences.email)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(applicant.getPartyId()).value(
            applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .firstName("Respondent firstname")
            .lastName("Respondent lastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .contactPreferences(ContactPreferences.email)
            .address(Address.builder().addressLine1("1 ADD Road").postCode("1XY 2AB").country("ABC").build())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(respondent.getPartyId()).value(
            respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);


        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(wrappedRespondents.getId().toString())
            .label(respondent.getFirstName() + " " + respondent.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();


        CaseData caseDataC100Message = CaseData.builder().id(12345L)
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication("C100")
            .replyMessageDynamicList(DynamicList.builder().build())
            .applicants(applicantList)
            .messageContent("some msg content")
            .respondents(respondentList)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                                           .sendMessageToCafcass(Yes)
                                           .cafcassEmailAddress("cafcass@test.com")
                                           .sendMessageToOtherParties(Yes)
                                           .otherPartiesEmailAddress("test@test.com,test2@test.com")
                                           .messageAbout(MessageAboutEnum.APPLICATION)
                                           .messageContent("some msg content")
                                           .messageSubject("message subject")
                                           .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();

        Map<String, Object> dynamicData = getEmailDynamicData(caseDataC100Message);
        dynamicData.put("name","");
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().toEmailAddress("test@test.com")
            .dynamicTemplateData(dynamicData)
            .listOfAttachments(new ArrayList<>())
            .languagePreference(LanguagePreference.english)
            .build();
        sendAndReplyService.sendNotificationToExternalParties(caseDataC100Message, "authorisation");
        verify(sendgridService).sendEmailUsingTemplateWithAttachments(
            SendgridEmailTemplateNames.SEND_EMAIL_TO_EXTERNAL_PARTY,
            "authorisation",
            sendgridEmailConfig
        );
    }

    @Test
    public void testSendEmailNotificationToExternalPartiesForFL401Case() {
        PartyDetails applicant = PartyDetails.builder()
            .partyId(UUID.randomUUID())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .solicitorEmail("testSolicitor@xyz.com")
            .phoneNumber("1234567890")
            .contactPreferences(ContactPreferences.email)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(applicant.getPartyId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(respondent.getPartyId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();


        CaseData caseDataForFL401Message = CaseData.builder().id(12345L)
            .chooseSendOrReply(SendOrReply.SEND)
            .applicantCaseName("case name a")
            .caseTypeOfApplication("FL401")
            .replyMessageDynamicList(DynamicList.builder().build())
            .applicantsFL401(applicant)
            .respondentsFL401(respondent)
            .messageContent("some msg content")
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                                           .externalMessageWhoToSendTo(externalMessageWhoToSendTo)
                                           .messageAbout(MessageAboutEnum.APPLICATION)
                                           .messageContent("some msg content")
                                           .messageSubject("message subject")
                                           .build()
                    )
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();
        Map<String, Object> dynamicData = getEmailDynamicData(caseDataForFL401Message);
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().toEmailAddress("testSolicitor@xyz.com")
            .dynamicTemplateData(dynamicData)
            .listOfAttachments(new ArrayList<>())
            .languagePreference(LanguagePreference.english)
            .build();
        sendAndReplyService.sendNotificationToExternalParties(caseDataForFL401Message, "authorisation");
        verify(sendgridService).sendEmailUsingTemplateWithAttachments(
            SendgridEmailTemplateNames.SEND_EMAIL_TO_EXTERNAL_PARTY,
            "authorisation",
            sendgridEmailConfig
        );
    }

    @NotNull
    private Map<String, Object> getEmailDynamicData(CaseData caseData) {
        Map<String, Object> dynamicData = new HashMap<>();
        dynamicData.put("caseReference", "12345");
        dynamicData.put("subject", "message subject");
        dynamicData.put("messageContent", "some msg content");
        dynamicData.put("attachmentType", "pdf");
        dynamicData.put("disposition", "attachment");
        dynamicData.put("name", "Abc Xyz");
        dynamicData.put("documentSize", 0);
        dynamicData.put("messageAbout", "APPLICATION");
        dynamicData.put("caseName", caseData.getApplicantCaseName());
        return dynamicData;
    }

    @Test
    public void testSendEmailNotificationToExternalPartiesWhenMessageIsNotExternal() {
        CaseData caseDataC100V2 = CaseData.builder().id(12345L)
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication("C100")
            .replyMessageDynamicList(DynamicList.builder().build())
            .applicants(null)
            .respondents(null)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                                           .externalMessageWhoToSendTo(null)
                                           .messageAbout(MessageAboutEnum.APPLICATION)
                                           .messageContent("some msg content")
                                           .build())
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();
        Map<String, Object> dynamicData = getEmailDynamicData(caseDataC100V2);
        SendgridEmailConfig sendgridEmailConfig = SendgridEmailConfig.builder().toEmailAddress("testSolicitor@xyz.com")
            .dynamicTemplateData(dynamicData)
            .listOfAttachments(new ArrayList<>())
            .languagePreference(LanguagePreference.english)
            .build();
        sendAndReplyService.sendNotificationToExternalParties(caseDataC100V2, "authorisation");
        verify(sendgridService, times(0)).sendEmailUsingTemplateWithAttachments(
            SendgridEmailTemplateNames.SEND_EMAIL_TO_EXTERNAL_PARTY,
            "authorisation",
            sendgridEmailConfig
        );
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExist() {
        CaseData caseDataAdditionalApplication = CaseData.builder().id(12345L)
            .chooseSendOrReply(SendOrReply.SEND)
            .caseTypeOfApplication("C100")
            .replyMessageDynamicList(DynamicList.builder().build())
            .applicants(null)
            .respondents(null)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .internalOrExternalMessage(InternalExternalMessageEnum.INTERNAL)
                                           .externalMessageWhoToSendTo(null)
                                           .messageAbout(MessageAboutEnum.APPLICATION)
                                           .messageContent("some msg content")
                                           .build())
                    .respondToMessage(YesOrNo.No)
                    .messages(messages)
                    .build())
            .build();

        assertNull(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseDataAdditionalApplication, SendOrReply.SEND));
        assertNull(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseDataAdditionalApplication, SendOrReply.REPLY));
    }

    @Test
    public void testCloseAwPTask() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message sendMessage1 = getMessageInformation();

        openMessagesList.add(element(message1));
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(DynamicList.builder()
                                                  .listItems(Arrays.asList(DynamicListElement.builder()
                                                                               .code(UUID.fromString(TEST_UUID))
                                                                               .label("test")
                                                                               .build()))
                                                  .value(DynamicListElement.builder()
                                                             .code(UUID.fromString(TEST_UUID))
                                                             .label("test")
                                                             .build())
                                                  .build())
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesList.get(0).getId());
        Map<String, Object> stringObjectMap = caseData1.toMap(new ObjectMapper());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(serviceAuthToken,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            stringObjectMap, caseData1, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        sendAndReplyService.closeAwPTask(caseData1);
        Mockito.verify(allTabService,Mockito.times(1)).getStartUpdateForSpecificEvent(any(), any());
    }


    @Test
    public void testDoNotCloseCloseAwPTask() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message messageInformation = getMessageInformation();

        openMessagesList.add(element(messageInformation));
        CaseData caseData2 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .applicationsList(DynamicList.builder()
                                                  .listItems(Arrays.asList(DynamicListElement.builder()
                                                                               .code(UUID.fromString(TEST_UUID))
                                                                               .label("test")
                                                                               .build(),DynamicListElement.builder()
                                                      .code(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                                      .label("test")
                                                      .build()))
                                                  .build())
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesList.get(0).getId());
        Map<String, Object> stringObjectMap = caseData2.toMap(new ObjectMapper());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(serviceAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData2, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        sendAndReplyService.closeAwPTask(caseData2);
        Mockito.verify(allTabService,Mockito.times(0)).getStartUpdateForSpecificEvent(any(), any());
    }


    @Test
    public void testDoNotCloseCloseAwPTaskScenario2() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message messageInformation = getMessageInformation();

        openMessagesList.add(element(messageInformation));
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.SEND)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesList.get(0).getId());
        Map<String, Object> stringObjectMap = caseData1.toMap(new ObjectMapper());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(serviceAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData1, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        sendAndReplyService.closeAwPTask(caseData1);
        Mockito.verify(allTabService,Mockito.times(0)).getStartUpdateForSpecificEvent(any(), any());
    }

    @Test
    public void testDoNotCloseCloseAwPTaskScenario3() {

        List<Element<Message>> openMessagesList = new ArrayList<>();

        Message messageInformation = getMessageInformation();

        openMessagesList.add(element(messageInformation));
        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .chooseSendOrReply(SendOrReply.REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(
                        Message.builder()
                            .internalOrExternalMessage(InternalExternalMessageEnum.EXTERNAL)
                            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.OTHER)
                            .messageAbout(MessageAboutEnum.APPLICATION)
                            .ctscEmailList(dynamicList)
                            .judicialOrMagistrateTierList(dynamicList)
                            .futureHearingsList(dynamicList)
                            .submittedDocumentsList(dynamicList)
                            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").personalCode("123").build())
                            .build()
                    ).build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesList.get(0).getId());
        Map<String, Object> stringObjectMap = caseData1.toMap(new ObjectMapper());

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(serviceAuthToken,
                                                                                                        EventRequestData.builder().build(),
                                                                                                        StartEventResponse.builder().build(),
                                                                                                        stringObjectMap, caseData, null);
        when(allTabService.getStartUpdateForSpecificEvent(any(), any())).thenReturn(startAllTabsUpdateDataContent);
        sendAndReplyService.closeAwPTask(caseData1);
        Mockito.verify(allTabService,Mockito.times(0)).getStartUpdateForSpecificEvent(any(), any());
    }

    private Message getMessageInformation() {
        return Message.builder()
            .senderEmail("sender@email.com")
            .recipientEmail("testRecipient1@email.com")
            .messageSubject("testSubject1")
            .messageUrgency("testUrgency1")
            .dateSent(dateSent)
            .messageContent("This is message 1 body")
            .updatedTime(dateTime)
            .sendReplyJudgeName(JudicialUser.builder().idamId("testIdam").build())
            .internalMessageReplyTo(InternalMessageReplyToEnum.COURT_ADMIN)
            .status(OPEN)
            .latestMessage("Message 1 latest message")
            .replyHistory(messageHistoryList)
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.COURT_ADMIN)
            .internalMessageUrgent(YesOrNo.Yes)
            .build();
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExistWhileSendingMessage() {
        caseData = caseData.toBuilder()
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(Message.builder()
                    .applicationsList(DynamicList.builder()
                        .value(DynamicListElement.builder()
                            .code(UUID.randomUUID())
                            .label("test")
                            .build())
                        .build())
                    .build())
                .build())
            .build();
        Assert.assertNotNull(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData, SendOrReply.SEND));
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExistReturnNullWhileSendingMessage() {
        caseData = caseData.toBuilder()
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .sendMessageObject(null)
                .build())
            .build();
        Assert.assertNull(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData, SendOrReply.SEND));
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExistWhileReplyingMessage() {
        DynamicList messageReplyDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
                .label("test-label")
                .build())
            .listItems(List.of(DynamicListElement.builder()
                .code(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
                .label("test-label")
                .build()))
            .build();
        caseData = caseData.toBuilder()
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                                    .messageReplyDynamicList(messageReplyDynamicList)
                                    .messages(List.of(ElementUtils.element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),
                                                                           Message
                                                                               .builder()
                                                                               .selectedApplicationCode("test-code")
                                                                               .build())))
                                    .build())
            .build();
        when(elementUtils.getDynamicListSelectedValue(messageReplyDynamicList, objectMapper))
            .thenReturn(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"));
        Assert.assertNotNull(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData, SendOrReply.REPLY));
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExistReturnNullWhileReplyingMessage() {
        DynamicList messageReplyDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder()
                .code(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
                .label("test-label")
                .build())
            .listItems(List.of(DynamicListElement.builder()
                .code(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"))
                .label("test-label")
                .build()))
            .build();
        caseData = caseData.toBuilder()
            .sendOrReplyMessage(SendOrReplyMessage.builder()
                .messageReplyDynamicList(messageReplyDynamicList)
                .messages(List.of(ElementUtils.element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),Message
                    .builder().build())))
                .build())
            .build();
        Assert.assertNull(sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseData, SendOrReply.REPLY));
    }

    @Test
    public void testGetFutureHearingDynamicList() {
        Hearings futureHearings =
            Hearings.hearingsWith()
                .caseRef("1234")
                .hmctsServiceCode("ABA5")
                .caseHearings(List.of(CaseHearing.caseHearingWith()
                    .hearingDaySchedule(List.of(HearingDaySchedule
                        .hearingDayScheduleWith()
                        .build()))
                    .build()))
                .build();
        when(hearingService.getFutureHearings(auth, "1234")).thenReturn(futureHearings);
        Assert.assertNotNull(sendAndReplyService.getFutureHearingDynamicList(auth,serviceAuthToken,"1234"));
    }

    @Test
    public void testPopulateMessageReplyFieldsWithPrevMsgWithReplyHistoryAndDocuments() {

        List<Element<uk.gov.hmcts.reform.prl.models.documents.Document>> internalMessageDocs = new ArrayList<>();
        internalMessageDocs.add(element(internalMessageDoc));
        messageWithReplyHistoryAndDocuments.setInternalMessageAttachDocs(internalMessageDocs);

        List<Element<Message>> openMessagesListWithReplyHistory = new ArrayList<>();
        openMessagesListWithReplyHistory.add(element(messageWithReplyHistoryAndDocuments));
        openMessagesListWithReplyHistory.add(element(message1));
        openMessagesListWithReplyHistory.add(element(message3));

        CaseData data = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesListWithReplyHistory)
                    .build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesListWithReplyHistory.get(0).getId());
        CaseData updatedCaseData = sendAndReplyService.populateMessageReplyFields(data, auth);

        SendReplyTempDoc expectedSendReplyTempDocument = SendReplyTempDoc.builder().attachedTime(dateTime).document(internalMessageDoc).build();

        assertEquals("testRecipient1@email.com", updatedCaseData.getSendOrReplyMessage().getMessages()
            .get(0).getValue().getReplyHistory().get(0).getValue().getMessageTo());
        assertEquals(expectedSendReplyTempDocument, updatedCaseData.getSendOrReplyMessage().getInternalMessageAttachDocsList().get(0).getValue());
    }

    @Test
    public void testPopulateMessageReplyFieldsWithPrevMsgWithReplyHistoryWithDocuments() {

        List<Element<uk.gov.hmcts.reform.prl.models.documents.Document>> internalMessageDocs = new ArrayList<>();
        internalMessageDocs.add(element(internalMessageDoc));

        MessageHistory messageHistory = MessageHistory.builder().messageFrom("sender1@email.com")
            .messageTo("testRecipient1@email.com")
            .messageDate(dateSent)
            .isUrgent(YesOrNo.Yes)
            .internalMessageAttachDocs(internalMessageDocs)
            .updatedTime(dateTime)
            .messageAbout(MessageAboutEnum.REVIEW_SUBMITTED_DOCUMENTS.getDisplayedValue())
            .build();

        List<Element<MessageHistory>> msgHistoryList = new ArrayList<>();
        msgHistoryList.add(element(messageHistory));
        messageWithReplyHistoryAndDocuments.setReplyHistory(msgHistoryList);

        List<Element<Message>> openMessagesListWithReplyHistory = new ArrayList<>();
        openMessagesListWithReplyHistory.add(element(messageWithReplyHistoryAndDocuments));
        openMessagesListWithReplyHistory.add(element(message1));
        openMessagesListWithReplyHistory.add(element(message3));

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));
        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);

        CaseData data = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .messageReplyDynamicList(dynamicList)
                    .messages(openMessagesListWithReplyHistory)
                    .build())
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(openMessagesListWithReplyHistory.get(0).getId());
        CaseData updatedCaseData = sendAndReplyService.populateMessageReplyFields(data, auth);

        SendReplyTempDoc expectedSendReplyTempDocument = SendReplyTempDoc.builder().attachedTime(dateTime).document(internalMessageDoc).build();

        assertEquals("testRecipient1@email.com", updatedCaseData.getSendOrReplyMessage().getMessages()
            .get(0).getValue().getReplyHistory().get(0).getValue().getMessageTo());
        assertEquals(expectedSendReplyTempDocument, updatedCaseData.getSendOrReplyMessage().getInternalMessageAttachDocsList().get(0).getValue());
    }

    @Test
    public void testSendPostNotificationToExternalPartiesForC100()  throws Exception {

        Message newMessage = getMessage();

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>();
        msgListWithNewMessage.addAll(messages);
        msgListWithNewMessage.add(element(newMessage));

        PartyDetails applicant = getApplicant();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(applicant.getPartyId()).value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = getRespondent();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(respondent.getPartyId()).value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(wrappedRespondents.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();

        DynamicList sendMessageDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.randomUUID()).build())
            .listItems(List.of(DynamicListElement.builder().code("test1").build()))
            .build();

        Element<SendAndReplyDynamicDoc> sendAndReplyDynamicDocElement =  Element.<SendAndReplyDynamicDoc>builder()
            .id(UUID.randomUUID())
            .value(SendAndReplyDynamicDoc.builder().submittedDocsRefList(sendMessageDynamicList).build())
            .build();

        CaseData caseDataForExternalMessage = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
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
                    .externalMessageAttachDocsList(List.of(sendAndReplyDynamicDocElement))
                    .build())
            .build();


        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(
                uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().originalDocumentName("doc1")
                    .build());

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
        when(documentGenService.getTemplate(
            any(CaseData.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn("abc_template");
        when(dgsService.generateDocument(
            eq(auth), eq(String.valueOf(caseDataForExternalMessage.getId())), eq("abc_template"), anyMap()))
            .thenReturn(getGeneratedDocumentInfo());
        when(bulkPrintService.send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(UUID.randomUUID());

        sendAndReplyService.sendNotificationToExternalParties(caseDataForExternalMessage, auth);
        assertNotNull(caseDataForExternalMessage.getSendOrReplyMessage());
    }


    @Test
    public void testSendPostNotificationToExternalPartiesForC100WithAddress()  throws Exception {

        Message newMessage = getMessage();

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>();
        msgListWithNewMessage.addAll(messages);
        msgListWithNewMessage.add(element(newMessage));

        PartyDetails applicant = getApplicant();
        applicant.setAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build());

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(applicant.getPartyId()).value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = getRespondent();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(respondent.getPartyId()).value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(wrappedRespondents.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();

        DynamicList sendMessageDynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.randomUUID()).build())
            .listItems(List.of(DynamicListElement.builder().code("test1").build()))
            .build();

        Element<SendAndReplyDynamicDoc> sendAndReplyDynamicDocElement =  Element.<SendAndReplyDynamicDoc>builder()
            .id(UUID.randomUUID())
            .value(SendAndReplyDynamicDoc.builder().submittedDocsRefList(sendMessageDynamicList).build())
            .build();

        CaseData caseDataForExternalMessage = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("C100")
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
                    .externalMessageAttachDocsList(List.of(sendAndReplyDynamicDocElement))
                    .build())
            .build();


        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(
                uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().originalDocumentName("doc1")
                    .build());

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
        when(documentGenService.getTemplate(
            any(CaseData.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn("abc_template");
        when(dgsService.generateDocument(
            eq(auth), eq(String.valueOf(caseDataForExternalMessage.getId())), eq("abc_template"), anyMap()))
            .thenReturn(getGeneratedDocumentInfo());
        when(bulkPrintService.send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(UUID.randomUUID());

        sendAndReplyService.sendNotificationToExternalParties(caseDataForExternalMessage, auth);
        assertNotNull(caseDataForExternalMessage.getSendOrReplyMessage());
    }

    @Test
    public void testSendPostNotificationToExternalPartiesForFL401()  throws Exception {

        Message newMessage = getMessage();

        List<Element<Message>> msgListWithNewMessage = new ArrayList<>();
        msgListWithNewMessage.addAll(messages);
        msgListWithNewMessage.add(element(newMessage));

        PartyDetails applicant = getApplicant();
        applicant.setAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build());

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().id(applicant.getPartyId()).value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = getRespondent();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().id(respondent.getPartyId()).value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code(wrappedApplicant.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code(wrappedRespondents.getId().toString())
            .label(applicant.getFirstName() + " " + applicant.getLastName())
            .build();

        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();

        DynamicList dynamicListForExternalMessage = DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.randomUUID()).build())
            .listItems(List.of(DynamicListElement.builder().code("test1").build()))
            .build();

        Element<SendAndReplyDynamicDoc> sendAndReplyDynamicDocElement =  Element.<SendAndReplyDynamicDoc>builder()
            .id(UUID.randomUUID())
            .value(SendAndReplyDynamicDoc.builder().submittedDocsRefList(dynamicListForExternalMessage).build())
            .build();
        // need to remove applicants and respondent once XUI bug fix.
        CaseData caseDataExternalMessageFl401 = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .caseTypeOfApplication("FL401")
            .applicants(applicantList)
            .respondents(respondentList)
            .applicantsFL401(getApplicant())
            .respondentsFL401(getRespondent())
            .replyMessageDynamicList(DynamicList.builder().build())
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
                    .externalMessageAttachDocsList(List.of(sendAndReplyDynamicDocElement))
                    .build())
            .build();


        when(caseDocumentClient.getMetadataForDocument(auth, serviceAuthToken, UUID.randomUUID()))
            .thenReturn(
                uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().originalDocumentName("doc1")
                    .build());

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();

        when(documentLanguageService.docGenerateLang(any(CaseData.class))).thenReturn(documentLanguage);
        when(documentGenService.getTemplate(
            any(CaseData.class), Mockito.anyString(), Mockito.anyBoolean())).thenReturn("abc_template");
        when(dgsService.generateDocument(
            eq(auth), eq(String.valueOf(caseDataExternalMessageFl401.getId())), eq("abc_template"), anyMap()))
            .thenReturn(getGeneratedDocumentInfo());
        when(bulkPrintService.send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(UUID.randomUUID());

        sendAndReplyService.sendNotificationToExternalParties(caseDataExternalMessageFl401, auth);
        assertNotNull(caseDataExternalMessageFl401.getSendOrReplyMessage());
    }

    private Message getMessage() {

        DynamicList dynamicListMessage = DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.randomUUID()).build())
            .listItems(List.of(DynamicListElement.builder().code("test1").build()))
            .build();

        return Message.builder()
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
            .submittedDocumentsList(dynamicListMessage)
            .build();
    }

    private PartyDetails getApplicant() {
        return PartyDetails.builder()
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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();
    }

    private PartyDetails getRespondent() {
        return PartyDetails.builder()
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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();
    }

    private GeneratedDocumentInfo getGeneratedDocumentInfo() {
        return GeneratedDocumentInfo.builder().url("TestUrl").binaryUrl("binaryUrl").hashToken("testHashToken").build();
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExistForSend() {
        DynamicList dynamicListApplications = DynamicList.builder()
            .value(DynamicListElement.builder().code(UUID.randomUUID()).build()).build();

        Message newMessage = Message.builder()
            .messageSubject("testSubject1")
            .messageContent("This is message 1 body")
            .applicationsList(dynamicListApplications)
            .build();

        CaseData caseDataAwp = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .sendMessageObject(newMessage)
                    .messages(messages)
                    .build())
            .build();

        String returnString = sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseDataAwp, SEND);
        assertEquals(dynamicListApplications.getValueCode(), returnString);
    }

    @Test
    public void testFetchAdditionalApplicationCodeIfExistForReply() {

        Message message = Message.builder().isReplying(YesOrNo.Yes).build();

        CaseData caseDataMessage = CaseData.builder().id(123451L)
            .chooseSendOrReply(REPLY)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(YesOrNo.No)
                    .messageReplyDynamicList(dynamicList)
                    .messages(ListUtils.union(listOfOpenMessages, listOfClosedMessages))
                    .build())
            .messageReply(message)
            .replyMessageDynamicList(dynamicList)
            .build();

        when(elementUtils.getDynamicListSelectedValue(dynamicList, objectMapper)).thenReturn(listOfOpenMessages.get(0).getId());
        String returnString = sendAndReplyService.fetchAdditionalApplicationCodeIfExist(caseDataMessage, REPLY);
        assertEquals(dynamicList.getValueCode(), returnString);
    }


    @Test
    public void testSendAndReplyExternalPartiesAndCafcass() {

        DynamicMultiselectListElement dynamicListApplicantElement = DynamicMultiselectListElement.builder()
            .code("12345")
            .label("test" + " " + "test")
            .build();

        DynamicMultiselectListElement dynamicListRespondentElement = DynamicMultiselectListElement.builder()
            .code("123456")
            .label("test" + " " + "test")
            .build();
        DynamicMultiSelectList externalMessageWhoToSendTo = DynamicMultiSelectList.builder()
            .value(List.of(dynamicListApplicantElement, dynamicListRespondentElement)).build();
        Message message = Message.builder().messageAbout(MessageAboutEnum.APPLICATION).externalMessageWhoToSendTo(externalMessageWhoToSendTo)
            .sendMessageToCafcass(
                Yes).sendMessageToCafcass(Yes).build();
        boolean atLeastOnePartySelected = sendAndReplyService.atLeastOnePartySelectedForExternalMessage(message);
        assertEquals(true, atLeastOnePartySelected);


    }

    @Test
    public void testSendAndReplyExternalPartiesAndCafcassScenario2() {

        Message message = Message.builder().messageAbout(MessageAboutEnum.APPLICATION).externalMessageWhoToSendTo(
                DynamicMultiSelectList.builder().build())
            .sendMessageToCafcass(
                No).sendMessageToCafcass(No).build();
        boolean atLeastOnePartySelected = sendAndReplyService.atLeastOnePartySelectedForExternalMessage(message);
        assertEquals(false, atLeastOnePartySelected);
    }

    public static uk.gov.hmcts.reform.ccd.document.am.model.Document testDocument() {
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = RANDOM_ALPHA_NUMERIC;
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = RANDOM_ALPHA_NUMERIC;

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = RANDOM_ALPHA_NUMERIC;

        return document;
    }
}
