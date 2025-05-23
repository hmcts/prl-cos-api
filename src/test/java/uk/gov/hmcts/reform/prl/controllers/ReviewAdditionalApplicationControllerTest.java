package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewAdditionalApplicationWrapper;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ReviewAdditionalApplicationService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyCommonService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.ResponseEntity.ok;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_C2_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_OTHER_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class ReviewAdditionalApplicationControllerTest {

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;
    @Mock
    private SendAndReplyService sendAndReplyService;
    @Mock
    SendAndReplyCommonService sendAndReplyCommonService;

    @InjectMocks
    private ReviewAdditionalApplicationController controller;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";
    public static Map<String, Object> clientContext = new HashMap<>();

    String awpOtherCode;
    String awpC2Code;

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
    public void setUp() {
        clientContext.put("test", "test");
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

        awpOtherCode = AWP_OTHER_APPLICATION_SNR_CODE + UNDERSCORE + dateSent;
        awpC2Code = AWP_C2_APPLICATION_SNR_CODE + UNDERSCORE + dateSent;

        when(objectMapper.convertValue(sendCaseDetails.getData(), CaseData.class)).thenReturn(sendCaseData);
    }

    @Test
    public void shouldAboutToStartReviewAdditionalApplication() {
        Element<AdditionalApplicationsBundle> reviewAdditionalApplicationElement = Element.<AdditionalApplicationsBundle>builder().build();
        List<Element<AdditionalApplicationsBundle>> reviewAdditionalApplicationCollection = new ArrayList<>();
        reviewAdditionalApplicationCollection.add(reviewAdditionalApplicationElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .additionalApplicationsBundle(reviewAdditionalApplicationCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(reviewAdditionalApplicationService.populateReviewAdditionalApplication(caseData,
                                                                                    caseDataMap,
                                                                                    "clcx",
                                                                                    Event.REVIEW_ADDITIONAL_APPLICATION.getId()))
            .thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = controller
            .aboutToStartReviewAdditionalApplication(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void testPopulateApplicationForSendWhenSelectedApplicationIsNull() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder().isReplying(YesOrNo.No).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .reviewAdditionalApplicationWrapper(ReviewAdditionalApplicationWrapper.builder().build())
            .messageReply(message)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .respondToMessage(No)
                    .messages(messages)
                    .build())
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().closedMessages(Collections.singletonList(element(message))).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.populateDynamicListsForSendAndReply(caseData,auth)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        controller.populateApplication(auth, callbackRequest);
        verify(sendAndReplyService).populateDynamicListsForSendAndReply(caseData,auth);
        verifyNoInteractions(reviewAdditionalApplicationService);
    }

    @Test
    public void testPopulateApplicationForSendWhenSelectedApplicationIsNotNull() {
        UUID uuid = UUID.randomUUID();
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code(awpOtherCode)
            .label("test-document")
            .build();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(dynamicListElement);
        DynamicList list = DynamicList.builder()
            .listItems(dynamicListElements)
            .value(DynamicListElement.builder().code(uuid).build()).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        Message message = Message.builder()
            .applicationsList(DynamicList.builder()
                                  .listItems(list.getListItems())
                                  .value(list.getValue())
                                  .build())
            .isReplying(YesOrNo.No).build();
        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .reviewAdditionalApplicationWrapper(ReviewAdditionalApplicationWrapper.builder()
                                                    .selectedAdditionalApplicationsBundle(AdditionalApplicationsBundle.builder()
                                                                                              .build())
                                                    .build())
            .messageReply(message)
            .sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(message)
                    .respondToMessage(No)
                    .messages(messages)
                    .build())
            .replyMessageDynamicList(DynamicList.builder().build())
            .sendOrReplyDto(SendOrReplyDto.builder().closedMessages(Collections.singletonList(element(message))).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(sendAndReplyService.populateDynamicListsForSendAndReply(caseData,auth)).thenReturn(caseData);
        when(reviewAdditionalApplicationService.getApplicationBundleDynamicCode(any(AdditionalApplicationsBundle.class)))
            .thenReturn(awpOtherCode);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        controller.populateApplication(auth, callbackRequest);
        verify(sendAndReplyService).populateDynamicListsForSendAndReply(caseData,auth);
        verify(reviewAdditionalApplicationService).getApplicationBundleDynamicCode(any(AdditionalApplicationsBundle.class));
    }

    @Test
    public void testPopulateApplicationForReply() {
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
        controller.populateApplication(auth, callbackRequest);
        verify(sendAndReplyService).populateMessageReplyFields(caseData, auth);

    }

    @Test
    public void testHandSubmittedSendAndReply() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyService.sendAndReplySubmitted(callbackRequest)).thenReturn(ok(SubmittedCallbackResponse.builder().build()));
        ResponseEntity<SubmittedCallbackResponse> response  = controller.handleSubmittedSendAndReply(auth, callbackRequest);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(sendAndReplyService).sendAndReplySubmitted(callbackRequest);

    }

    @Test
    public void testClearDynamicLists() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(sendAndReplyService.clearDynamicLists(callbackRequest)).thenReturn(AboutToStartOrSubmitCallbackResponse.builder().build());
        controller.clearDynamicLists(auth, callbackRequest);
        verify(sendAndReplyService).clearDynamicLists(callbackRequest);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForSend() {

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(SEND)
            .reviewAdditionalApplicationWrapper(ReviewAdditionalApplicationWrapper.builder().isAdditionalApplicationReviewed(
                Yes).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        controller.aboutToSubmitReviewAdditionalApplication(auth, callbackRequest);
        verify(sendAndReplyCommonService).sendMessages(auth, caseData, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitForReply() {

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID.getValue())
            .data(caseDataMap)
            .build();

        CaseData caseData = CaseData.builder().id(12345L)
            .chooseSendOrReply(REPLY)
            .reviewAdditionalApplicationWrapper(ReviewAdditionalApplicationWrapper.builder().isAdditionalApplicationReviewed(
                Yes).build())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        controller.aboutToSubmitReviewAdditionalApplication(auth, callbackRequest);
        verify(sendAndReplyCommonService).replyMessages(auth, caseData, caseDataMap);
    }

    @Test
    public void testSendOrReplyToMessagesSubmitIfAdditionalApplicationIsNotReviewed() {

        caseDataMap = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        controller.aboutToSubmitReviewAdditionalApplication(auth, callbackRequest);
        verifyNoInteractions(sendAndReplyCommonService);
    }
}
