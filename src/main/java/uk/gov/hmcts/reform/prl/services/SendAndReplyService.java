package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageReplyToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.CodeAndLabel;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.OtherApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SendAndReplyNotificationEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageHistory;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendReplyTempDoc;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.concat;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_C2_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_OTHER_APPLICATION_SNR_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_SUBMITTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDICIARY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_ADVISER_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.UNDERSCORE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.documents.Document.buildFromDocument;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formatDateTime;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getDynamicList;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getPersonalCode;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendAndReplyService {

    private final EmailService emailService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final ElementUtils elementUtils;

    private final Time dateTime;
    private final AllTabServiceImpl allTabService;

    @Value("${xui.url}")
    private String manageCaseUrl;

    private final HearingDataService hearingDataService;

    private final RefDataService refDataService;

    @Value("${sendandreply.category-id}")
    private String categoryId;

    @Value("${sendandreply.service-code}")
    private String serviceCode;

    @Value("${refdata.category-id}")
    private String hearingTypeCategoryId;

    private final AuthTokenGenerator authTokenGenerator;

    private final CoreCaseDataApi coreCaseDataApi;

    private final RefDataUserService refDataUserService;

    private final HearingService hearingService;

    private final CaseDocumentClient caseDocumentClient;

    private static final String TABLE_BEGIN = "<table>";
    private static final String TABLE_END = "</table>";
    private static final String TABLE_ROW_BEGIN = "<tr>";
    private static final String TABLE_ROW_END = "</tr>";
    private static final String TABLE_ROW_DATA_BEGIN = "<td width=\"50%\" class='govuk-header__logotype-crown'>";
    private static final String TABLE_ROW_DATA_END = "</td>";
    private static final String MESSAGE_TABLE_HEADER =
        "<div class='govuk-grid-column-two-thirds govuk-grid-row'><span class=\"heading-h4\">Message</span>";
    private static final String TABLE_ROW_LABEL = "<span class='heading-h4'>";
    private static final String TABLE_ROW_VALUE = "<span class='form-label'>";
    private static final String SPAN_END = "</span>";
    private static final String DIV_END = "</div>";
    private static final String DATE_SENT = "Date and time sent";
    public static final String SENDERS_NAME = "Sender's name";
    public static final String SENDERS_EMAIL = "Sender's email";
    public static final String SENDER_ROLE = "Sender role";
    public static final String RECIPIENT_ROLE = "Recipient role";
    public static final String JUDICIAL_OR_MAGISTRATE_TIER = "Judicial or magistrate Tier";
    public static final String JUDGE_NAME = "Judge name";
    public static final String JUDGE_EMAIL = "Judge email";
    public static final String URGENCY = "Urgency";
    public static final String MESSAGE_SUBJECT = "Subject";
    public static final String MESSAGE_ABOUT = "What is it about";
    public static final String APPLICATION = "Application";
    public static final String HEARING = "Hearing";
    public static final String DOCUMENT = "Document";
    public static final String MESSAGE_DETAILS = "Message details";
    public static final String NO_MESSAGE_FOUND_ERROR = "No message found with that ID";
    public static final String APPLICATION_LINK = "#Other%20applications";
    public static final String HEARINGS_LINK = "/hearings";
    public static final String OTHER_APPLICATION = "Other application";
    public static final String HEARINGS = "Hearings";
    public static final String ANCHOR_HREF_START = "<a href='";
    public static final String OTHER_APPLICATION_ANCHOR_END = "'>Other application</a>";
    public static final String HEARINGS_ANCHOR_END = "'>Hearings</a>";
    public static final String ARROW_SEPARATOR = "->";

    public EmailTemplateVars buildNotificationEmail(CaseData caseData, Message message) {
        String caseName = caseData.getApplicantCaseName();
        String subject = message.getMessageSubject();
        String senderEmail = message.getSenderEmail();
        String urgency = message.getMessageUrgency();
        String content = message.getLatestMessage();
        String caseLink = manageCaseUrl + "/" + caseData.getId();

        return SendAndReplyNotificationEmail.builder()
            .caseName(caseName)
            .messageSubject(subject)
            .senderEmail(senderEmail)
            .messageUrgency(urgency)
            .messageContent(content)
            .caseLink(caseLink)
            .build();
    }

    public void sendNotificationEmail(CaseData caseData, Message message) {
        emailService.send(
            message.getRecipientEmail(),
            EmailTemplateNames.SEND_AND_REPLY_NOTIFICATION,
            buildNotificationEmail(caseData, message),
            LanguagePreference.english
        );
    }


    public String getLoggedInUserEmail(String authorisation) {
        return userService.getUserDetails(authorisation).getEmail();
    }

    public Map<String, Object> setSenderAndGenerateMessageList(CaseData caseData, String auth) {
        Map<String, Object> data = new HashMap<>();
        MessageMetaData messageMetaData = MessageMetaData.builder()
            .senderEmail(getLoggedInUserEmail(auth))
            .build();
        data.put("messageObject", messageMetaData);

        if (hasMessages(caseData)) {
            data.put("replyMessageDynamicList", getOpenMessagesDynamicList(caseData));
        }
        return data;
    }

    public DynamicList getOpenMessagesDynamicList(CaseData caseData) {
        List<Element<Message>> openMessages = caseData.getOpenMessages();

        return ElementUtils.asDynamicList(
            openMessages,
            null,
            Message::getLabelForDynamicList
        );
    }

    public List<Element<Message>> addNewMessage(CaseData caseData, Message newMessage) {
        List<Element<Message>> messages = new ArrayList<>();
        Element<Message> messageElement = element(newMessage);
        if (hasMessages(caseData)) {
            messages = caseData.getOpenMessages();
        }
        messages.add(messageElement);
        return messages;
    }

    public List<Element<Message>> closeMessage(UUID messageId, CaseData caseData) {
        List<Element<Message>> messages = caseData.getOpenMessages();
        messages.stream()
            .filter(m -> m.getId().equals(messageId))
            .map(Element::getValue)
            .forEach(message -> {
                message.setStatus(MessageStatus.CLOSED);
                message.setUpdatedTime(dateTime.now());
            });
        return messages;
    }

    public List<Element<Message>> closeMessage(CaseData caseData) {
        UUID messageId = elementUtils.getDynamicListSelectedValue(
            caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);
        //find & update status - CLOSED
        return caseData.getSendOrReplyMessage()
            .getMessages().stream()
            .map(messageElement -> {
                if (messageElement.getId().equals(messageId)) {
                    messageElement.getValue().setStatus(MessageStatus.CLOSED);
                    messageElement.getValue().setUpdatedTime(dateTime.now());
                }
                return messageElement;
            })
            .sorted(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()))
            .toList();
    }


    public Message buildNewSendMessage(CaseData caseData) {
        MessageMetaData metaData = caseData.getMessageMetaData();

        return Message.builder()
            .status(OPEN)
            .dateSent(dateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK)))
            .senderEmail(metaData.getSenderEmail())
            .recipientEmail(metaData.getRecipientEmail())
            .messageSubject(metaData.getMessageSubject())
            .messageHistory(buildMessageHistory(metaData.getSenderEmail(), caseData.getMessageContent()))
            .messageUrgency(ofNullable(metaData.getMessageUrgency()).orElse(""))
            .latestMessage(caseData.getMessageContent())
            .updatedTime(dateTime.now())
            .build();
    }

    public Map<String, Object> populateReplyMessageFields(CaseData caseData, String auth) {
        Map<String, Object> data = new HashMap<>();
        UUID messageId = elementUtils.getDynamicListSelectedValue(
            caseData.getReplyMessageDynamicList(), objectMapper);

        Optional<Message> previousMessageOptional = caseData.getOpenMessages().stream()
            .filter(element -> element.getId().equals(messageId))
            .map(Element::getValue)
            .findFirst();

        if (previousMessageOptional.isEmpty()) {
            log.info(NO_MESSAGE_FOUND_ERROR);
            return data;
        }

        Message m = previousMessageOptional.get();

        Message populatedReply = Message.builder()
            .senderEmail(m.getSenderEmail())
            .messageSubject(m.getMessageSubject())
            .recipientEmail(m.getSenderEmail())
            .messageUrgency(m.getMessageUrgency())
            .messageHistory(m.getMessageHistory())
            .latestMessage(m.getLatestMessage())
            .replyFrom(getLoggedInUserEmail(auth))
            .replyTo(m.getSenderEmail())
            .build();

        data.put("messageReply", populatedReply);
        return data;
    }

    public List<Element<Message>> buildNewReplyMessage(UUID selectedMessage,
                                                       Message replyMessage,
                                                       List<Element<Message>> messages) {
        return messages.stream()
            .map(messageElement -> {
                if (selectedMessage.equals(messageElement.getId())) {
                    Message message = messageElement.getValue();

                    String senderEmail = replyMessage.getReplyFrom();

                    Message updatedMessage = message.toBuilder()
                        .dateSent(dateTime.now().format(DateTimeFormatter.ofPattern(
                            "d MMMM yyyy 'at' h:mma",
                            Locale.UK
                        )))
                        .updatedTime(dateTime.now())
                        .senderEmail(senderEmail)
                        .recipientEmail(replyMessage.getReplyTo())
                        .messageHistory(buildMessageHistory(replyMessage, message, senderEmail))
                        .latestMessage(replyMessage.getMessageContent())
                        .build();
                    return element(messageElement.getId(), updatedMessage);
                }
                return messageElement;
            }).toList();
    }

    public Map<String, Object> returnMapOfOpenMessages(List<Element<Message>> messages) {
        return Map.of("openMessages", messages);
    }

    public Map<String, Object> returnMapOfClosedMessages(List<Element<Message>> messages) {
        return Map.of("closedMessages", messages);
    }

    public boolean hasMessages(CaseData caseData) {
        return (caseData.getOpenMessages() != null);
    }

    public String buildMessageHistory(String sender, String message) {
        return buildMessageHistory(sender, "", message);
    }

    private String buildMessageHistory(Message reply, Message previousMessage, String sender) {
        return buildMessageHistory(sender, previousMessage.getMessageHistory(), reply.getMessageContent());
    }

    public String buildMessageHistory(String sender, String history, String message) {
        String messageDetails = String.format("%s - %s", sender, message);
        if (history.isBlank()) {
            return messageDetails;
        }
        return String.join("\n \n", history, messageDetails);
    }

    public void removeTemporaryFields(Map<String, Object> caseData, String... fields) {
        for (String field : fields) {
            caseData.remove(field);
        }
    }

    public CaseData populateDynamicListsForSendAndReply(CaseData caseData, String authorization) {
        String caseReference = String.valueOf(caseData.getId());
        DynamicList documentCategoryList = getCategoriesAndDocuments(authorization, caseReference);
        String s2sToken = authTokenGenerator.generate();
        final String loggedInUserEmail = getLoggedInUserEmail(authorization);
        return caseData.toBuilder().sendOrReplyMessage(
                SendOrReplyMessage.builder()
                    .sendMessageObject(Message.builder()
                                           .judicialOrMagistrateTierList(getJudiciaryTierDynamicList(
                                               authorization,
                                               s2sToken,
                                               serviceCode,
                                               categoryId
                                           ))
                                           .applicationsList(getOtherApplicationsList(caseData))
                                           .submittedDocumentsList(documentCategoryList)
                                           .ctscEmailList(getDynamicList(List.of(DynamicListElement.builder()
                                                                                     .label(loggedInUserEmail).code(
                                                   loggedInUserEmail).build())))
                                           .futureHearingsList(getFutureHearingDynamicList(
                                               authorization,
                                               s2sToken,
                                               caseReference
                                           ))
                                           .legalAdvisersList(getLegalAdvisersList())
                                           .build())
                    .build())
            .build();
    }

    private DynamicList getLegalAdvisersList() {
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY)
            .listItems(refDataUserService.getLegalAdvisorList())
            .build();
    }

    public DynamicList getFutureHearingDynamicList(String authorization, String s2sToken, String caseId) {
        Hearings futureHearings = hearingService.getFutureHearings(authorization, caseId);

        if (futureHearings != null && futureHearings.getCaseHearings() != null && !futureHearings.getCaseHearings().isEmpty()) {

            Map<String, String> refDataCategoryValueMap = getRefDataMap(
                authorization,
                s2sToken,
                serviceCode,
                hearingTypeCategoryId
            );

            List<DynamicListElement> hearingDropdowns = futureHearings.getCaseHearings().stream()
                .map(caseHearing -> {
                    //get hearingId
                    String hearingId = String.valueOf(caseHearing.getHearingID());
                    final String hearingType = caseHearing.getHearingType();
                    String hearingTypeValue = !refDataCategoryValueMap.isEmpty() ? refDataCategoryValueMap.get(
                        hearingType) : EMPTY_STRING;
                    //return hearingId concatenated with hearingDate
                    Optional<List<HearingDaySchedule>> hearingDaySchedules = Optional.ofNullable(caseHearing.getHearingDaySchedule());
                    return hearingDaySchedules.map(daySchedules -> daySchedules.stream().map(hearingDaySchedule -> {
                        if (null != hearingDaySchedule && null != hearingDaySchedule.getHearingStartDateTime()) {
                            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                            String hearingDate = hearingDaySchedule.getHearingStartDateTime().format(dateTimeFormatter);
                            return CodeAndLabel.builder().label(concat(hearingTypeValue, " - ").concat(hearingDate))
                                .code(concat(hearingId, " - ").concat(hearingType)).build();
                        }
                        return null;
                    }).filter(Objects::nonNull).toList()).orElse(Collections.emptyList());
                }).map(this::getDynamicListElements)
                .flatMap(Collection::stream)
                .toList();

            return getDynamicList(hearingDropdowns);
        }

        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    private List<DynamicListElement> getDynamicListElements(List<CodeAndLabel> dropdowns) {
        return dropdowns.stream().map(dropdown -> DynamicListElement.builder().code(dropdown.getCode()).label(dropdown.getLabel()).build()).toList();
    }

    private Map<String, String> getRefDataMap(String authorization, String s2sToken, String serviceCode, String hearingTypeCategoryId) {

        try {
            return refDataService.getRefDataCategoryValueMap(
                authorization,
                s2sToken,
                serviceCode,
                hearingTypeCategoryId
            );
        } catch (Exception e) {
            log.error("Error while calling Ref data api in getRefDataMap method --->  ", e);
        }
        return emptyMap();
    }

    /**
     * This method will return linked cases dynamic list.
     *
     * @param authorization Auth token.
     * @param caseId        CaseData object.
     * @return DynamicList.
     */
    public DynamicList getLinkedCasesDynamicList(String authorization, String caseId) {

        return getDynamicList(hearingDataService.getLinkedCasesDynamicList(
            authorization,
            caseId
        ));
    }

    public DynamicList getOtherApplicationsList(CaseData caseData) {
        String otherApplicationLabel = "Other applications - ";
        String c2ApplicationLabel = "C2 application - ";

        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements;

        if (caseData.getAdditionalApplicationsBundle() != null && !caseData.getAdditionalApplicationsBundle().isEmpty()) {
            List<DynamicListElement> dynamicListElements = new ArrayList<>();
            additionalApplicationElements = caseData.getAdditionalApplicationsBundle();
            additionalApplicationElements.stream().forEach(additionalApplicationsBundleElement -> {
                AdditionalApplicationsBundle additionalApplicationsBundle = additionalApplicationsBundleElement.getValue();

                getOtherApplicationBundleDynamicList(
                    additionalApplicationsBundle,
                    dynamicListElements,
                    otherApplicationLabel
                );

                getC2ApplicationBundleDynamicList(
                    additionalApplicationsBundle,
                    dynamicListElements,
                    c2ApplicationLabel
                );
            });
            return getDynamicList(dynamicListElements);
        }

        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    private void getC2ApplicationBundleDynamicList(AdditionalApplicationsBundle additionalApplicationsBundle,
                                                   List<DynamicListElement> dynamicListElements,
                                                   String c2ApplicationLabel) {
        if (null != additionalApplicationsBundle.getC2DocumentBundle()) {
            C2DocumentBundle c2DocumentBundle = additionalApplicationsBundle.getC2DocumentBundle();
            if (null != c2DocumentBundle.getApplicationStatus()
                && c2DocumentBundle.getApplicationStatus().equals(AWP_STATUS_SUBMITTED)) {
                dynamicListElements.add(DynamicListElement.builder()
                                            .code(AWP_C2_APPLICATION_SNR_CODE
                                                      .concat(UNDERSCORE)
                                                      .concat(c2DocumentBundle.getUploadedDateTime()))
                                            .label(c2ApplicationLabel
                                                       .concat(additionalApplicationsBundle
                                                                   .getC2DocumentBundle().getUploadedDateTime()))
                                            .build());
            }

        }
    }

    private void getOtherApplicationBundleDynamicList(AdditionalApplicationsBundle additionalApplicationsBundle,
                                                      List<DynamicListElement> dynamicListElements,
                                                      String otherApplicationLabel) {
        if (null != additionalApplicationsBundle.getOtherApplicationsBundle()) {
            OtherApplicationsBundle otherApplicationsBundle = additionalApplicationsBundle.getOtherApplicationsBundle();
            if (null != otherApplicationsBundle.getApplicationStatus()
                && otherApplicationsBundle.getApplicationStatus().equals(AWP_STATUS_SUBMITTED)) {
                dynamicListElements.add(DynamicListElement.builder()
                                            .code(AWP_OTHER_APPLICATION_SNR_CODE
                                                      .concat(UNDERSCORE)
                                                      .concat(otherApplicationsBundle.getUploadedDateTime()))
                                            .label(otherApplicationLabel
                                                       .concat(otherApplicationsBundle.getApplicationType().getDisplayedValue())
                                                       .concat(HYPHEN_SEPARATOR)
                                                       .concat(otherApplicationsBundle.getUploadedDateTime()))
                                            .build());
            }
        }
    }

    /**
     * This method will call refdata api and create Dynamic List
     * for Judicier tier.
     *
     * @param authorization Authoriszation token.
     * @param s2sToken      service token.
     * @param serviceCode   Service code e.g. ABA5 for PRL.
     * @param categoryId    e.g. JudgeType.
     * @return DynamicList
     */
    public DynamicList getJudiciaryTierDynamicList(String authorization, String s2sToken, String serviceCode, String categoryId) {

        try {
            Map<String, String> refDataCategoryValueMap = getRefDataMap(
                authorization,
                s2sToken,
                serviceCode,
                categoryId
            );

            if (refDataCategoryValueMap != null && !refDataCategoryValueMap.isEmpty()) {
                List<DynamicListElement> judiciaryTierDynamicElementList = new ArrayList<>();

                refDataCategoryValueMap.forEach((k, v) -> judiciaryTierDynamicElementList.add(DynamicListElement.builder().code(
                    k).label(v).build()));

                return getDynamicList(judiciaryTierDynamicElementList);
            }
        } catch (Exception e) {
            log.error("Error in getJudiciaryTierDynamicList method", e);
        }
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }


    public DynamicList getCategoriesAndDocuments(String authorisation, String caseReference) {
        try {
            CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
                authorisation,
                authTokenGenerator.generate(),
                caseReference
            );
            return createDynamicList(categoriesAndDocuments);
        } catch (Exception e) {
            log.error("Error in getCategoriesAndDocuments method", e);
        }
        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    private DynamicList createDynamicList(CategoriesAndDocuments categoriesAndDocuments) {

        List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .toList();

        List<DynamicListElement> dynamicListElementList = new ArrayList<>();
        createDynamicListFromSubCategories(parentCategories, dynamicListElementList, null, null);

        categoriesAndDocuments.getUncategorisedDocuments().forEach(document -> {
            DynamicListElement dynamicListElement = DynamicListElement.builder()
                .code(fetchDocumentIdFromUrl(document.getDocumentURL()))
                .label(document.getDocumentFilename()).build();
            if (dynamicListElementList.stream().noneMatch(dynamicListElement1 -> dynamicListElement1.getCode()
                .contains(dynamicListElement.getCode()))) {
                dynamicListElementList.add(dynamicListElement);
            }
        });
        return DynamicList.builder().value(DynamicListElement.EMPTY)
            .listItems(dynamicListElementList).build();
    }

    private void createDynamicListFromSubCategories(List<Category> categoryList,
                                                    List<DynamicListElement> dynamicListElementList,
                                                    final String parentLabelString,
                                                    final String parentCodeString) {
        categoryList.forEach(category -> {
            if (parentLabelString == null) {
                if (category.getDocuments() != null) {
                    category.getDocuments().forEach(document -> dynamicListElementList.add(
                        DynamicListElement.builder().code(category.getCategoryId() + ARROW_SEPARATOR
                                                              + fetchDocumentIdFromUrl(document.getDocumentURL()))
                            .label(category.getCategoryName() + " -> " + document.getDocumentFilename()).build()
                    ));
                }
                if (category.getSubCategories() != null) {
                    createDynamicListFromSubCategories(
                        category.getSubCategories(),
                        dynamicListElementList,
                        category.getCategoryName(),
                        category.getCategoryId()
                    );
                }
            } else {
                if (category.getDocuments() != null) {
                    category.getDocuments().forEach(document -> dynamicListElementList.add(
                        DynamicListElement.builder()
                            .code(parentCodeString + " -> " + category.getCategoryId() + ARROW_SEPARATOR
                                      + fetchDocumentIdFromUrl(document.getDocumentURL()))
                            .label(parentLabelString + " -> " + category.getCategoryName() + " -> "
                                       + document.getDocumentFilename()).build()
                    ));
                }
                if (category.getSubCategories() != null) {
                    createDynamicListFromSubCategories(category.getSubCategories(), dynamicListElementList,
                                                       parentLabelString + " -> " + category.getCategoryName(),
                                                       parentCodeString + " -> " + category.getCategoryId()
                    );
                }
            }
        });
    }

    public String fetchDocumentIdFromUrl(String documentUrl) {

        return documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

    }

    public Message buildSendReplyMessage(CaseData caseData, Message message, String authorization) {
        if (null == message) {
            return Message.builder().build();
        }

        UserDetails userDetails = userService.getUserDetails(authorization);
        final Optional<JudicialUsersApiResponse> judicialUsersApiResponseOptional =
            getJudicialUserDetails(message.getSendReplyJudgeName());
        JudicialUsersApiResponse judicialUsersApiResponse = judicialUsersApiResponseOptional.orElse(null);
        final String otherApplicationsUrl = manageCaseUrl + URL_STRING + caseData.getId() + APPLICATION_LINK;
        final String hearingsUrl = manageCaseUrl + URL_STRING + caseData.getId() + HEARINGS_LINK;

        Message newMessage = Message.builder()
            // in case of Other, change status to Close while sending message
            .status(InternalMessageWhoToSendToEnum.OTHER
                        .equals(message.getInternalMessageWhoToSendTo()) ? CLOSED : OPEN)
            .dateSent(formatDateTime(DATE_TIME_PATTERN, dateTime.now()))
            .internalOrExternalMessage(message.getInternalOrExternalMessage())
            .internalMessageUrgent(message.getInternalMessageUrgent())
            .internalMessageWhoToSendTo(REPLY.equals(caseData.getChooseSendOrReply())
                                            ? InternalMessageWhoToSendToEnum.fromDisplayValue(message.getInternalMessageReplyTo().getDisplayedValue())
                                            : message.getInternalMessageWhoToSendTo())
            .messageAbout(message.getMessageAbout())
            .judgeName(null != judicialUsersApiResponse ? judicialUsersApiResponse.getFullName() : null)
            .judgeEmail(null != judicialUsersApiResponse ? judicialUsersApiResponse.getEmailId() : null)
            .messageSubject(message.getMessageSubject())
            .recipientEmailAddresses(message.getRecipientEmailAddresses())
            .selectedCtscEmail(getValueCode(message.getCtscEmailList()))
            .judicialOrMagistrateTierCode(getValueCode(message.getJudicialOrMagistrateTierList()))
            .judicialOrMagistrateTierValue(getValueLabel(message.getJudicialOrMagistrateTierList()))
            .selectedApplicationCode(getValueCode(message.getApplicationsList()))
            .selectedApplicationValue(getValueLabel(message.getApplicationsList()))
            .selectedFutureHearingCode(getValueCode(message.getFutureHearingsList()))
            .selectedFutureHearingValue(getValueLabel(message.getFutureHearingsList()))
            .selectedSubmittedDocumentCode(getValueCode(message.getSubmittedDocumentsList()))
            .selectedSubmittedDocumentValue(getValueLabel(message.getSubmittedDocumentsList()))
            .updatedTime(dateTime.now())
            .messageContent(SEND.equals(caseData.getChooseSendOrReply()) ? caseData.getMessageContent() : message.getMessageContent())
            .internalMessageAttachDocs(SEND.equals(caseData.getChooseSendOrReply())
                                           ? getSendAttachedDocs(caseData, message, authorization) : emptyList())
            .senderEmail(null != userDetails ? userDetails.getEmail() : null)
            .senderName(null != userDetails ? userDetails.getFullName() : null)
            .senderRole(null != userDetails ? getUserRole(userDetails.getRoles()) : null)
            //setting null to avoid empty data showing in Messages tab
            .sendReplyJudgeName(null)
            .replyHistory(null)
            .otherApplicationLink(isNotBlank(getValueCode(message.getApplicationsList())) ? otherApplicationsUrl : null)
            .hearingsLink(isNotBlank(getValueCode(message.getFutureHearingsList())) ? hearingsUrl : null)
            .build();

        log.info("Send message internalMessageAttachDocs: {}", newMessage.getInternalMessageAttachDocs());

        return newMessage;
    }

    private List<Element<Document>> getSendAttachedDocs(CaseData caseData, Message message, String authorization) {
        return MessageAboutEnum.APPLICATION.equals(message.getMessageAbout())
            ? getApplicationDocument(
            message.getApplicationsList(),
            caseData,
            getValueCode(message.getApplicationsList())
        )
            : List.of(element(getSelectedDocument(authorization, message.getSubmittedDocumentsList())));
    }

    private String getValueCode(DynamicList dynamicListObj) {
        if (dynamicListObj != null) {
            return dynamicListObj.getValueCode();
        }
        return null;
    }

    private String getValueLabel(DynamicList dynamicListObj) {
        if (dynamicListObj != null) {
            return dynamicListObj.getValueLabel();
        }
        return null;
    }

    private String getUserRole(List<String> roles) {
        if (isNotEmpty(roles)) {
            if (roles.contains(COURT_ADMIN_ROLE)) {
                return COURT_ADMIN;
            } else if (roles.contains(JUDGE_ROLE)) {
                return JUDICIARY;
            } else if (roles.contains(LEGAL_ADVISER_ROLE)) {
                return LEGAL_ADVISER;
            } else {
                return "";
            }
        }
        return "";
    }

    private uk.gov.hmcts.reform.prl.models.documents.Document getSelectedDocument(String authorization,
                                                                                  DynamicList submittedDocumentList) {
        if (null == submittedDocumentList || null == submittedDocumentList.getValueCode()) {
            return null;
        }

        if (isNotBlank(submittedDocumentList.getValueCode())) {
            final String[] documentPath = submittedDocumentList.getValueCode().split(ARROW_SEPARATOR);
            final String documentId = documentPath[documentPath.length - 1];

            final uk.gov.hmcts.reform.ccd.document.am.model.Document document = caseDocumentClient
                .getMetadataForDocument(authorization, authTokenGenerator.generate(), UUID.fromString(documentId));

            if (document != null) {
                return buildFromDocument(document);
            }
        }
        return null;
    }

    private List<Element<Document>> getApplicationDocument(DynamicList applicationDocumentList,
                                                           CaseData caseData,
                                                           String selectedApplicationCode) {

        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements = caseData.getAdditionalApplicationsBundle();
        if (null == applicationDocumentList || null == applicationDocumentList.getValueCode() || null == additionalApplicationElements) {
            return emptyList();
        }

        Optional<Element<AdditionalApplicationsBundle>> otherApplicationDocumentsElement = additionalApplicationElements
            .stream().filter(additionalApplicationsBundleElement -> {
                OtherApplicationsBundle otherApplicationsBundle = additionalApplicationsBundleElement.getValue().getOtherApplicationsBundle();

                if (null != otherApplicationsBundle && null != otherApplicationsBundle.getApplicationStatus()
                    && otherApplicationsBundle.getApplicationStatus().equals(AWP_STATUS_SUBMITTED)
                    && selectedApplicationCode.equals(AWP_OTHER_APPLICATION_SNR_CODE
                                                     .concat(UNDERSCORE)
                                                     .concat(otherApplicationsBundle.getUploadedDateTime()))) {
                    return true;
                }
                return false;
            }).findFirst();

        if (otherApplicationDocumentsElement.isPresent()) {
            return otherApplicationDocumentsElement.get().getValue().getOtherApplicationsBundle().getFinalDocument();
        }

        Optional<Element<AdditionalApplicationsBundle>> c2ApplicationDocumentsElement = additionalApplicationElements
            .stream().filter(additionalApplicationsBundleElement -> {
                C2DocumentBundle c2ApplicationsBundle = additionalApplicationsBundleElement.getValue().getC2DocumentBundle();

                if (null != c2ApplicationsBundle && null != c2ApplicationsBundle.getApplicationStatus()
                    && c2ApplicationsBundle.getApplicationStatus().equals(AWP_STATUS_SUBMITTED)
                    && selectedApplicationCode.equals(AWP_C2_APPLICATION_SNR_CODE
                                                          .concat(UNDERSCORE)
                                                          .concat(c2ApplicationsBundle.getUploadedDateTime()))) {
                    return true;
                }
                return false;
            }).findFirst();

        if (c2ApplicationDocumentsElement.isPresent()) {
            return c2ApplicationDocumentsElement.get().getValue().getC2DocumentBundle().getFinalDocument();
        }

        return emptyList();
    }

    public List<JudicialUsersApiResponse> getJudgeDetails(JudicialUser judicialUser) {

        String[] judgePersonalCode = getPersonalCode(judicialUser);
        return refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                                                                .personalCode(judgePersonalCode).build());

    }


    private Optional<JudicialUsersApiResponse> getJudicialUserDetails(JudicialUser judicialUser) {

        if (null != judicialUser && isNotBlank(judicialUser.getPersonalCode())) {
            final Optional<List<JudicialUsersApiResponse>> judicialUsersApiResponseList = ofNullable(getJudgeDetails(
                judicialUser));

            if (judicialUsersApiResponseList.isPresent()) {
                Optional<JudicialUsersApiResponse> judicialUsersApiResponse = judicialUsersApiResponseList.get().stream().findFirst();
                if (judicialUsersApiResponse.isPresent()) {
                    return judicialUsersApiResponse;
                }
            }
        }
        return Optional.empty();
    }

    public Map<String, Object> setSenderAndGenerateMessageReplyList(CaseData caseData, String authorisation) {
        Map<String, Object> data = new HashMap<>();
        MessageMetaData messageMetaData = MessageMetaData.builder()
            .senderEmail(getLoggedInUserEmail(authorisation))
            .build();
        data.put("messageObject", messageMetaData);

        List<Element<Message>> openMessages = getOpenMessages(caseData.getSendOrReplyMessage().getMessages());
        if (isNotEmpty(openMessages)) {
            data.put("messageReplyDynamicList", getReplyMessagesList(openMessages));
        }
        return data;
    }

    public DynamicList getReplyMessagesList(List<Element<Message>> openMessages) {
        return ElementUtils.asDynamicList(
            openMessages,
            null,
            Message::getLabelForReplyDynamicList
        );
    }

    public static List<Element<Message>> getOpenMessages(List<Element<Message>> messages) {
        return nullSafeCollection(messages).stream()
            .filter(element -> OPEN.equals(element.getValue().getStatus()))
            .toList();
    }

    public CaseData populateMessageReplyFields(CaseData caseData, String authorization) {
        UUID messageId = elementUtils.getDynamicListSelectedValue(
            caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);

        Optional<Message> previousMessage = nullSafeCollection(
            caseData.getSendOrReplyMessage().getMessages()).stream()
            .filter(element -> element.getId().equals(messageId)
                && OPEN.equals(element.getValue().getStatus()))
            .map(Element::getValue)
            .findFirst();

        if (previousMessage.isEmpty()) {
            log.info(NO_MESSAGE_FOUND_ERROR);
            return caseData;
        }

        //populate message table
        String messageReply = renderMessageTable(previousMessage.get());
        //PRL-4411 - consolidate & add docs to display in reply history
        List<Element<SendReplyTempDoc>> sendReplyTempDocs = getSendReplyTempDocs(previousMessage.get());

        final String loggedInUserEmail = getLoggedInUserEmail(authorization);
        return caseData.toBuilder()
            .sendOrReplyMessage(
                caseData.getSendOrReplyMessage().toBuilder()
                    .messageReplyTable(messageReply)
                    .replyMessageObject(
                        Message.builder()
                            .judicialOrMagistrateTierList(getJudiciaryTierDynamicList(
                                authorization,
                                authTokenGenerator.generate(),
                                serviceCode,
                                categoryId
                            ))
                            .ctscEmailList(getDynamicList(List.of(DynamicListElement.builder()
                                                                      .label(loggedInUserEmail).code(loggedInUserEmail).build())))
                            .legalAdvisersList(getLegalAdvisersList())
                            .build())
                    .internalMessageAttachDocsList(isNotEmpty(sendReplyTempDocs) ? sendReplyTempDocs : null)
                    .build())
            .build();
    }

    private String renderMessageTable(Message message) {
        final List<String> lines = new LinkedList<>();

        //latest message at top
        lines.add(MESSAGE_TABLE_HEADER);
        lines.add(TABLE_BEGIN);
        addRowToMessageTable(lines, DATE_SENT, formatDateTime(DATE_TIME_PATTERN, message.getUpdatedTime()));
        addRowToMessageTable(lines, SENDER_ROLE, message.getSenderRole());
        addRowToMessageTable(lines, SENDERS_NAME, message.getSenderName());
        addRowToMessageTable(lines, SENDERS_EMAIL, message.getSenderEmail());
        addRowToMessageTable(lines, RECIPIENT_ROLE, message.getInternalMessageWhoToSendTo() != null
            ? message.getInternalMessageWhoToSendTo().getDisplayedValue() : null);
        addRowToMessageTable(lines, JUDICIAL_OR_MAGISTRATE_TIER, message.getJudicialOrMagistrateTierValue());
        addRowToMessageTable(lines, JUDGE_NAME, message.getJudgeName());
        addRowToMessageTable(lines, JUDGE_EMAIL, message.getJudgeEmail());
        addRowToMessageTable(lines, URGENCY, message.getInternalMessageUrgent() != null
            ? message.getInternalMessageUrgent().getDisplayedValue() : null);
        addRowToMessageTable(lines, MESSAGE_ABOUT, message.getMessageAbout() != null
            ? message.getMessageAbout().getDisplayedValue() : null);
        addRowToMessageTable(lines, APPLICATION, message.getSelectedApplicationValue());
        addRowToMessageTable(lines, OTHER_APPLICATION, isNotBlank(message.getOtherApplicationLink())
            ? ANCHOR_HREF_START + message.getOtherApplicationLink() + OTHER_APPLICATION_ANCHOR_END : null);
        addRowToMessageTable(lines, HEARING, message.getSelectedFutureHearingValue());
        addRowToMessageTable(lines, HEARINGS, isNotBlank(message.getHearingsLink())
            ? ANCHOR_HREF_START + message.getHearingsLink() + HEARINGS_ANCHOR_END : null);
        addRowToMessageTable(lines, DOCUMENT, message.getSelectedSubmittedDocumentValue());
        addRowToMessageTable(lines, MESSAGE_SUBJECT, message.getMessageSubject());
        addRowToMessageTable(lines, MESSAGE_DETAILS, message.getMessageContent());
        lines.add(TABLE_END);
        lines.add(DIV_END);

        //followed by history
        if (null != message.getReplyHistory()) {
            message.getReplyHistory().stream()
                .map(Element::getValue)
                .forEach(history -> {
                    lines.add(MESSAGE_TABLE_HEADER);
                    lines.add(TABLE_BEGIN);
                    addRowToMessageTable(lines, DATE_SENT, history.getMessageDate());
                    addRowToMessageTable(lines, SENDER_ROLE, history.getSenderRole());
                    addRowToMessageTable(lines, SENDERS_NAME, history.getSenderName());
                    addRowToMessageTable(lines, SENDERS_EMAIL, history.getMessageFrom());
                    addRowToMessageTable(lines, RECIPIENT_ROLE, history.getInternalMessageWhoToSendTo());
                    addRowToMessageTable(
                        lines,
                        JUDICIAL_OR_MAGISTRATE_TIER,
                        history.getJudicialOrMagistrateTierValue()
                    );
                    addRowToMessageTable(lines, JUDGE_NAME, history.getJudgeName());
                    addRowToMessageTable(lines, JUDGE_EMAIL, history.getJudgeEmail());
                    addRowToMessageTable(lines, URGENCY, history.getIsUrgent() != null
                        ? history.getIsUrgent().getDisplayedValue() : null);
                    addRowToMessageTable(lines, MESSAGE_ABOUT, history.getMessageAbout());
                    addRowToMessageTable(lines, APPLICATION, history.getSelectedApplicationValue());
                    addRowToMessageTable(lines, OTHER_APPLICATION, isNotBlank(message.getOtherApplicationLink())
                        ? ANCHOR_HREF_START + history.getOtherApplicationLink() + OTHER_APPLICATION_ANCHOR_END : null);
                    addRowToMessageTable(lines, HEARING, history.getSelectedFutureHearingValue());
                    addRowToMessageTable(lines, HEARINGS, isNotBlank(message.getHearingsLink())
                        ? ANCHOR_HREF_START + history.getHearingsLink() + HEARINGS_ANCHOR_END : null);
                    addRowToMessageTable(lines, DOCUMENT, history.getSelectedSubmittedDocumentValue());
                    addRowToMessageTable(lines, MESSAGE_SUBJECT, history.getMessageSubject());
                    addRowToMessageTable(lines, MESSAGE_DETAILS, history.getMessageContent());
                    lines.add(TABLE_END);
                    lines.add(DIV_END);
                });
        }

        return String.join("\n\n", lines);
    }

    private void addRowToMessageTable(List<String> lines,
                                      String label,
                                      String value) {
        if (isNotBlank(value)) {
            lines.add(TABLE_ROW_BEGIN);
            lines.add(TABLE_ROW_DATA_BEGIN + TABLE_ROW_LABEL + label + SPAN_END);
            lines.add(TABLE_ROW_DATA_END);
            lines.add(TABLE_ROW_DATA_BEGIN + TABLE_ROW_VALUE + value + SPAN_END);
            lines.add(TABLE_ROW_DATA_END);
            lines.add(TABLE_ROW_END);
        }

    }

    /**
     * This method will send notification, when internal
     * other message is sent.
     *
     * @param caseData CaseData
     */
    public void sendNotificationEmailOther(CaseData caseData) {
        //get the latest message
        Message message = caseData.getSendOrReplyMessage().getSendMessageObject();

        if (null != message && ObjectUtils.isNotEmpty(message.getRecipientEmailAddresses())) {
            final String[] recipientEmailAddresses = message.getRecipientEmailAddresses().split(COMMA);

            if (recipientEmailAddresses.length > 0) {
                final EmailTemplateVars emailTemplateVars = buildNotificationEmailOther(caseData);

                for (String recipientEmailAddress : recipientEmailAddresses) {
                    emailService.send(
                        recipientEmailAddress,
                        EmailTemplateNames.SEND_AND_REPLY_NOTIFICATION_OTHER,
                        emailTemplateVars,
                        LanguagePreference.getPreferenceLanguage(caseData)
                    );
                }
            }
        }
    }

    private EmailTemplateVars buildNotificationEmailOther(CaseData caseData) {
        String caseLink = manageCaseUrl + "/" + caseData.getId();

        return SendAndReplyNotificationEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(caseLink)
            .build();
    }

    public List<Element<Message>> replyAndAppendMessageHistory(CaseData caseData,
                                                               String authorization) {
        UUID replyMessageId = elementUtils.getDynamicListSelectedValue(
            caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);

        Message replyMessage = this.buildSendReplyMessage(
            caseData,
            caseData.getSendOrReplyMessage().getReplyMessageObject(),
            authorization
        );

        List<Element<MessageHistory>> messageHistoryList = new ArrayList<>();

        //append history
        return caseData.getSendOrReplyMessage()
            .getMessages().stream()
            .map(messageElement -> {
                if (messageElement.getId().equals(replyMessageId)) {
                    Message message = messageElement.getValue();

                    MessageHistory messageHistory = buildReplyMessageHistory(message);
                    if (isNotEmpty(message.getReplyHistory())) {
                        messageHistoryList.addAll(message.getReplyHistory());
                    }
                    messageHistoryList.add(element(messageHistory));

                    messageHistoryList.sort(
                        Comparator.comparing(m -> m.getValue().getMessageDate(), Comparator.reverseOrder()));

                    replyMessage.setReplyHistory(messageHistoryList);
                    replyMessage.setUpdatedTime(dateTime.now());
                    //retain the original subject & date sent
                    replyMessage.setMessageSubject(message.getMessageSubject());
                    replyMessage.setDateSent(message.getDateSent());
                    replyMessage.setSelectedApplicationCode(StringUtils.stripToNull(message.getSelectedApplicationCode()));
                    replyMessage.setSelectedFutureHearingCode(StringUtils.stripToNull(message.getSelectedFutureHearingCode()));
                    replyMessage.setJudicialOrMagistrateTierCode(StringUtils.stripToNull(message.getJudicialOrMagistrateTierCode()));
                    replyMessage.setSelectedSubmittedDocumentCode(StringUtils.stripToNull(message.getSelectedSubmittedDocumentCode()));

                    return element(messageElement.getId(), replyMessage);
                }
                return messageElement;
            })
            .sorted(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()))
            .toList();
    }

    private MessageHistory buildReplyMessageHistory(Message message) {
        log.info("Reply message internalMessageAttachDocs: {}", message.getInternalMessageAttachDocs());
        return MessageHistory.builder()
            .messageFrom(message.getSenderEmail())
            .messageTo(message.getInternalMessageWhoToSendTo() != null
                           ? message.getInternalMessageWhoToSendTo().getDisplayedValue() : null)
            .messageDate(formatDateTime(DATE_TIME_PATTERN, message.getUpdatedTime()))
            .messageSubject(message.getMessageSubject())
            .isUrgent(message.getInternalMessageUrgent())
            .messageContent(message.getMessageContent())
            .internalMessageWhoToSendTo(null != message.getInternalMessageWhoToSendTo()
                                            ? message.getInternalMessageWhoToSendTo().getDisplayedValue() : null)
            .internalOrExternalMessage(null != message.getInternalOrExternalMessage()
                                           ? message.getInternalOrExternalMessage().getDisplayedValue() : null)
            .messageAbout(null != message.getMessageAbout()
                              ? message.getMessageAbout().getDisplayedValue() : null)
            .judgeName(message.getJudgeName())
            .recipientEmailAddresses(message.getRecipientEmailAddresses())
            .selectedCtscEmail(message.getSelectedCtscEmail())
            .selectedApplicationValue(message.getSelectedApplicationValue())
            .selectedFutureHearingValue(message.getSelectedFutureHearingValue())
            .selectedSubmittedDocumentValue(message.getSelectedSubmittedDocumentValue())
            .judicialOrMagistrateTierValue(message.getJudicialOrMagistrateTierValue())
            .selectedDocument(message.getSelectedDocument())
            .judgeEmail(message.getJudgeEmail())
            .senderName(message.getSenderName())
            .senderRole(message.getSenderRole())
            .updatedTime(message.getUpdatedTime())
            .internalMessageAttachDocs(message.getInternalMessageAttachDocs())
            .build();
    }

    public CaseData resetSendAndReplyDynamicLists(CaseData caseData) {
        Message sendMessageObject = null;
        Message replyMessageObject = null;
        if (null != caseData.getSendOrReplyMessage().getSendMessageObject()) {
            sendMessageObject = resetSendMessageDynamicLists(caseData);
        }

        if (null != caseData.getSendOrReplyMessage().getReplyMessageObject()) {
            replyMessageObject = caseData.getSendOrReplyMessage().getReplyMessageObject();
            if (!InternalMessageReplyToEnum.JUDICIARY.equals(replyMessageObject.getInternalMessageReplyTo())
                && isNotNull(replyMessageObject.getJudicialOrMagistrateTierList())) {
                replyMessageObject.setJudicialOrMagistrateTierList(replyMessageObject.getJudicialOrMagistrateTierList().toBuilder()
                                                                       .value(DynamicListElement.EMPTY).build());
                replyMessageObject.setSendReplyJudgeName(null);
            }
            if (!InternalMessageReplyToEnum.LEGAL_ADVISER.equals(replyMessageObject.getInternalMessageReplyTo())
                && isNotNull(replyMessageObject.getLegalAdvisersList())) {
                replyMessageObject.setLegalAdvisersList(replyMessageObject.getLegalAdvisersList().toBuilder()
                                                                       .value(DynamicListElement.EMPTY).build());
            }
        }

        return caseData.toBuilder().sendOrReplyMessage(
            caseData.getSendOrReplyMessage().toBuilder()
                .sendMessageObject(sendMessageObject)
                .replyMessageObject(replyMessageObject)
                .build()
        ).build();
    }

    private Message resetSendMessageDynamicLists(CaseData caseData) {
        Message sendMessageObject = caseData.getSendOrReplyMessage().getSendMessageObject();

        if (canClearInternalWhoToSendFields(
            sendMessageObject.getInternalMessageWhoToSendTo(),
            InternalMessageWhoToSendToEnum.LEGAL_ADVISER,
            sendMessageObject.getLegalAdvisersList()
        )) {
            sendMessageObject.setLegalAdvisersList(sendMessageObject.getLegalAdvisersList().toBuilder()
                                                       .value(DynamicListElement.EMPTY).build());
        }

        if (canClearInternalWhoToSendFields(
            sendMessageObject.getInternalMessageWhoToSendTo(),
            InternalMessageWhoToSendToEnum.JUDICIARY,
            sendMessageObject.getJudicialOrMagistrateTierList()
        )) {
            sendMessageObject.setJudicialOrMagistrateTierList(sendMessageObject.getJudicialOrMagistrateTierList().toBuilder()
                                                                  .value(DynamicListElement.EMPTY).build());
            sendMessageObject.setSendReplyJudgeName(null);
        }

        if (canClearInternalWhoToSendFields(
            sendMessageObject.getInternalMessageWhoToSendTo(),
            InternalMessageWhoToSendToEnum.OTHER,
            sendMessageObject.getCtscEmailList()
        )) {
            sendMessageObject.setCtscEmailList(sendMessageObject.getCtscEmailList().toBuilder()
                                                   .value(DynamicListElement.EMPTY).build());
            sendMessageObject.setRecipientEmailAddresses(null);
        }

        if (canClearMessageAboutFields(
            sendMessageObject.getMessageAbout(),
            MessageAboutEnum.APPLICATION,
            sendMessageObject.getApplicationsList()
        )) {
            sendMessageObject.setApplicationsList(sendMessageObject.getApplicationsList().toBuilder()
                                                      .value(DynamicListElement.EMPTY).build());
        }

        if (canClearMessageAboutFields(
            sendMessageObject.getMessageAbout(),
            MessageAboutEnum.HEARING,
            sendMessageObject.getFutureHearingsList()
        )) {
            sendMessageObject.setFutureHearingsList(sendMessageObject.getFutureHearingsList().toBuilder()
                                                        .value(DynamicListElement.EMPTY).build());
        }

        if (canClearMessageAboutFields(
            sendMessageObject.getMessageAbout(),
            MessageAboutEnum.REVIEW_SUBMITTED_DOCUMENTS,
            sendMessageObject.getSubmittedDocumentsList()
        )) {
            sendMessageObject.setSubmittedDocumentsList(sendMessageObject.getSubmittedDocumentsList().toBuilder()
                                                            .value(DynamicListElement.EMPTY).build());
        }

        return sendMessageObject;
    }

    private boolean canClearInternalWhoToSendFields(InternalMessageWhoToSendToEnum sendObjectInternalMsgWhoToSendToEnum,
                                                    InternalMessageWhoToSendToEnum whoToSendToEnum,
                                                    DynamicList dynamicList) {
        return !whoToSendToEnum.equals(sendObjectInternalMsgWhoToSendToEnum) && isNotNull(dynamicList);
    }

    private boolean canClearMessageAboutFields(MessageAboutEnum sendObjectMessageAbout,
                                               MessageAboutEnum messageAboutEnum,
                                               DynamicList dynamicList) {
        return !messageAboutEnum.equals(sendObjectMessageAbout) && isNotNull(dynamicList);
    }

    private boolean isNotNull(DynamicList dynamicListObj) {
        return dynamicListObj != null;
    }

    public List<Element<Message>> addMessage(CaseData caseData, String authorisation) {

        Message newMessage = buildSendReplyMessage(
            caseData,
            caseData.getSendOrReplyMessage().getSendMessageObject(),
            authorisation
        );

        List<Element<Message>> messages = new ArrayList<>();
        if (isNotEmpty(caseData.getSendOrReplyMessage().getMessages())) {
            messages.addAll(caseData.getSendOrReplyMessage().getMessages());
        }
        messages.add(element(newMessage));
        messages.sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));

        return messages;
    }

    public String fetchAdditionalApplicationCodeIfExist(CaseData caseData, SendOrReply sendOrReply) {
        Message message = null;
        if (SEND.equals(sendOrReply)) {
            message = caseData.getSendOrReplyMessage()
                .getSendMessageObject();
            return message != null ? getValueCode(message.getApplicationsList()) : null;
        } else {
            UUID messageId = elementUtils.getDynamicListSelectedValue(
                caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);

            Optional<Element<Message>> optionalMessageElement = caseData.getSendOrReplyMessage()
                .getMessages().stream()
                .filter(messageElement -> messageElement.getId().equals(messageId))
                .findFirst();
            if (optionalMessageElement.isPresent()) {
                message = optionalMessageElement.get().getValue();
            }

            return message != null ? message.getSelectedApplicationCode() : null;
        }
    }

    private List<Element<SendReplyTempDoc>> getSendReplyTempDocs(Message message) {
        List<Element<SendReplyTempDoc>> sendReplyTempDocs = new ArrayList<>();

        //document from latest message
        if (isNotEmpty(message.getInternalMessageAttachDocs())) {
            message.getInternalMessageAttachDocs().stream()
                .map(Element::getValue)
                .forEach(document -> sendReplyTempDocs.add(
                    element(SendReplyTempDoc.builder()
                                .attachedTime(message.getUpdatedTime())
                                .document(document).build())));
        }

        //documents from message history
        if (isNotEmpty(message.getReplyHistory())) {
            message.getReplyHistory().stream()
                .map(Element::getValue)
                .forEach(history -> {
                    LocalDateTime attachedTime = history.getUpdatedTime();
                    if (isNotEmpty(history.getInternalMessageAttachDocs())) {
                        history.getInternalMessageAttachDocs().stream()
                            .map(Element::getValue)
                            .forEach(document -> sendReplyTempDocs.add(
                                element(SendReplyTempDoc.builder()
                                            .attachedTime(attachedTime)
                                            .document(document).build())));
                    }
                });
        }

        return sendReplyTempDocs;
    }

    public void closeAwPTask(CaseData caseData) {
        if (SEND.equals(caseData.getChooseSendOrReply())
            && caseData.getSendOrReplyMessage() != null
            && caseData.getSendOrReplyMessage().getSendMessageObject().getApplicationsList() != null) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent
                = allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(caseData.getId()),
                CaseEvent.ALL_AWP_IN_REVIEW.getValue()
            );

            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                String.valueOf(caseData.getId()),
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                startAllTabsUpdateDataContent.caseDataMap()
            );
        }

    }
}
