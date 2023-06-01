package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageReplyToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.CodeAndLabel;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
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
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.services.hearings.HearingService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

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
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.logging.log4j.util.Strings.concat;
import static org.apache.logging.log4j.util.Strings.isNotBlank;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COMMA;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.CLOSED;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getDynamicList;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getPersonalCode;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SendAndReplyService {

    public static final String SEND_AND_REPLY_CATEGORY_ID = "sendAndReply";
    public static final String APPLICATION_LINK = "#Other%20applications";
    public static final String DIV_CLASS_WIDTH_50 = "<div class='width-50'>";
    public static final String DIV = "</div>";

    private final EmailService emailService;

    private final UserService userService;

    private final ObjectMapper objectMapper;

    private final ElementUtils elementUtils;

    private final Time dateTime;

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

    private static final String TABLE_ROW_BEGIN = "<tr>";
    private static final String TABLE_ROW_END = "</tr>";
    private static final String TABLE_ROW_DATA_BEGIN = "<td width=\"50%\" class='govuk-header__logotype-crown'>";
    private static final String TABLE_ROW_DATA_END = "</td>";

    private Map<String, Document> documentMap;

    public EmailTemplateVars buildNotificationEmail(CaseData caseData, Message message) {
        String caseName = caseData.getApplicantCaseName();
        String subject = message.getMessageSubject();
        String senderEmail = message.getSenderEmail();
        String urgency = message.getMessageUrgency();
        String content = message.getLatestMessage();
        String caseLink = manageCaseUrl + "/" + caseData.getId();

        return  SendAndReplyNotificationEmail.builder()
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
                LanguagePreference.english);
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

    public CaseData closeMessage(CaseData caseData) {
        UUID messageId = elementUtils.getDynamicListSelectedValue(
            caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);
        List<Element<Message>> openMessages = new ArrayList<>();
        openMessages.addAll(caseData.getSendOrReplyMessage().getOpenMessagesList());

        List<Element<Message>> closedMessages = new ArrayList<>();
        closedMessages.addAll(caseData.getSendOrReplyMessage().getClosedMessagesList());

        //find & remove from open messages list
        Optional<Element<Message>> closedMessage = openMessages.stream()
                .filter(m -> m.getId().equals(messageId))
                .findFirst()
                .map(element -> {
                    openMessages.remove(element);
                    element.getValue().setStatus(MessageStatus.CLOSED);
                    element.getValue().setUpdatedTime(dateTime.now());
                    return element;
                });

        //add to closed messages list
        closedMessage.ifPresent(element -> nullSafeCollection(closedMessages).add(element));

        return caseData.toBuilder()
            .sendOrReplyMessage(caseData.getSendOrReplyMessage().toBuilder()
                                    .closedMessagesList(closedMessages)
                                    .openMessagesList(openMessages)
                                    .build())
            .build();
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
            log.info("No message found with that ID");
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
                        .dateSent(dateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK)))
                        .updatedTime(dateTime.now())
                        .senderEmail(senderEmail)
                        .recipientEmail(replyMessage.getReplyTo())
                        .messageHistory(buildMessageHistory(replyMessage, message, senderEmail))
                        .latestMessage(replyMessage.getMessageContent())
                        .build();
                    return element(messageElement.getId(), updatedMessage);
                }
                return messageElement;
            }).collect(Collectors.toList());
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
                                       .applicationsList(getOtherApllicationsList(caseData))
                                       .submittedDocumentsList(documentCategoryList)
                                       .ctscEmailList(getDynamicList(List.of(DynamicListElement.builder()
                                               .label(loggedInUserEmail).code(loggedInUserEmail).build())))
                                       .futureHearingsList(getFutureHearingDynamicList(authorization, s2sToken, caseReference))
                                       .build())
                .build())
            .build();
    }

    private DynamicList getFutureHearingDynamicList(String authorization, String s2sToken, String caseId) {
        Hearings futureHearings = hearingService.getFutureHearings(authorization, caseId);

        if (futureHearings != null && futureHearings.getCaseHearings() != null && !futureHearings.getCaseHearings().isEmpty()) {

            Map<String, String> refDataCategoryValueMap = getRefDataMap(authorization, s2sToken, serviceCode, hearingTypeCategoryId);

            List<DynamicListElement> hearingDropdowns = futureHearings.getCaseHearings().stream()
                .map(caseHearing -> {
                    //get hearingId
                    String hearingId = String.valueOf(caseHearing.getHearingID());
                    final String hearingType = caseHearing.getHearingType();
                    String hearingTypeValue = !refDataCategoryValueMap.isEmpty() ? refDataCategoryValueMap.get(hearingType) : EMPTY_STRING;
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
                    }).filter(Objects::nonNull).collect(Collectors.toList())).orElse(Collections.emptyList());
                }).map(this::getDynamicListElements)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

            log.info("getDynamicList(hearingDropdowns) -----> {}", getDynamicList(hearingDropdowns));

            return getDynamicList(hearingDropdowns);
        }

        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    private List<DynamicListElement> getDynamicListElements(List<CodeAndLabel> dropdowns) {
        return dropdowns.stream().map(dropdown -> DynamicListElement.builder().code(dropdown.getCode()).label(dropdown.getLabel()).build()).collect(
            Collectors.toList());
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
        return Collections.EMPTY_MAP;
    }

    /**
     * This method will return linked cases dynamic list.
     * @param authorization Auth token.
     * @param caseId CaseData object.
     * @return DynamicList.
     */
    public DynamicList getLinkedCasesDynamicList(String authorization, String caseId) {

        return getDynamicList(hearingDataService.getLinkedCasesDynamicList(
            authorization,
            caseId
        ));
    }

    public DynamicList getOtherApllicationsList(CaseData caseData) {

        List<Element<AdditionalApplicationsBundle>> additionalApplicationElements;

        if (caseData.getAdditionalApplicationsBundle() != null && !caseData.getAdditionalApplicationsBundle().isEmpty()) {
            List<DynamicListElement> dynamicListElements = new ArrayList<>();
            additionalApplicationElements = caseData.getAdditionalApplicationsBundle();
            additionalApplicationElements.stream().forEach(additionalApplicationsBundleElement ->  {
                if (additionalApplicationsBundleElement.getValue().getOtherApplicationsBundle() != null) {
                    dynamicListElements.add(DynamicListElement.builder().code("Other applications")
                        .label("Other applications - "
                                   .concat(additionalApplicationsBundleElement.getValue().getOtherApplicationsBundle().getUploadedDateTime()))
                        .build());
                }
                if (additionalApplicationsBundleElement.getValue().getC2DocumentBundle() != null) {
                    dynamicListElements.add(DynamicListElement.builder().code("C2 application")
                        .label("C2 application - "
                                   .concat(additionalApplicationsBundleElement.getValue().getC2DocumentBundle().getUploadedDateTime()))
                        .build());
                }
            });
            return  getDynamicList(dynamicListElements);
        }

        return DynamicList.builder()
            .value(DynamicListElement.EMPTY).build();
    }

    /**
     *  This method will call refdata api and create Dynamic List
     *  for Judicier tier.
     * @param authorization Authoriszation token.
     * @param s2sToken service token.
     * @param serviceCode Service code e.g. ABA5 for PRL.
     * @param categoryId e.g. JudgeType.
     * @return
     */
    public DynamicList getJudiciaryTierDynamicList(String authorization, String s2sToken, String serviceCode, String categoryId) {

        try {
            Map<String, String> refDataCategoryValueMap = getRefDataMap(authorization, s2sToken, serviceCode, categoryId);

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

        documentMap = new HashMap<>();

        List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
            .filter(category ->  !SEND_AND_REPLY_CATEGORY_ID.equals(category.getCategoryId()))
            .sorted(Comparator.comparing(Category::getCategoryName))
            .collect(Collectors.toList());

        List<DynamicListElement> dynamicListElementList = new ArrayList<>();
        createDynamicListFromSubCategories(parentCategories, dynamicListElementList, null, null);

        categoriesAndDocuments.getUncategorisedDocuments().forEach(document -> {

            dynamicListElementList.add(
                DynamicListElement.builder().code(fetchDocumentIdFromUrl(document.getDocumentURL()))
                    .label(document.getDocumentFilename()).build()
            );

            documentMap.put(fetchDocumentIdFromUrl(document.getDocumentURL()), document);
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
                    category.getDocuments().forEach(document -> {
                        dynamicListElementList.add(
                            DynamicListElement.builder().code(category.getCategoryId() + "->"
                                                                  + fetchDocumentIdFromUrl(document.getDocumentURL()))
                                .label(category.getCategoryName() + " -> " + document.getDocumentFilename()).build()
                        );
                        documentMap.put(fetchDocumentIdFromUrl(document.getDocumentURL()), document);

                    });
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
                    category.getDocuments().forEach(document -> {
                        dynamicListElementList.add(
                            DynamicListElement.builder()
                                .code(parentCodeString + " -> " + category.getCategoryId() + "->"
                                          + fetchDocumentIdFromUrl(document.getDocumentURL()))
                                .label(parentLabelString + " -> " + category.getCategoryName() + " -> "
                                           + document.getDocumentFilename()).build()
                        );
                        documentMap.put(fetchDocumentIdFromUrl(document.getDocumentURL()), document);
                    });
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

    private String fetchDocumentIdFromUrl(String documentUrl) {

        return documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

    }

    public Message buildSendReplyMessage(CaseData caseData, Message message, String authorization) {
        log.info("Message :{}", message);
        if (null == message) {
            return Message.builder().build();
        }

        final List<String> lines = new LinkedList<>();

        final String otherApplicationsUrl = manageCaseUrl + URL_STRING + caseData.getId() + APPLICATION_LINK;

        lines.add("<div class='width-50'>");

        lines.add("<p><a href=\"" + otherApplicationsUrl + "\">Other applications</a></p>");

        lines.add("</div>");


        final String otherApplicationsUrlLink = String.join("\n\n", lines);

        UserDetails userDetails = userService.getUserDetails(authorization);
        final Optional<JudicialUsersApiResponse> judicialUsersApiResponseOptional =
            getJudicialUserDetails(message.getSendReplyJudgeName());
        JudicialUsersApiResponse judicialUsersApiResponse = judicialUsersApiResponseOptional.orElse(null);

        return Message.builder()
            // in case of Other, change status to Close while sending message
            .status(InternalMessageWhoToSendToEnum.OTHER
                        .equals(message.getInternalMessageWhoToSendTo()) ? CLOSED : OPEN)
            .dateSent(dateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK)))
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
            .selectedCtscEmail(message.getCtscEmailList() != null
                                   ? message.getCtscEmailList().getValueCode() : null)
            .judicialOrMagistrateTierCode(message.getJudicialOrMagistrateTierList() != null
                                              ? message.getJudicialOrMagistrateTierList().getValueCode() : null)
            .judicialOrMagistrateTierValue(message.getJudicialOrMagistrateTierList() != null
                                               ? message.getJudicialOrMagistrateTierList().getValueLabel() : null)
            .selectedApplicationCode(message.getApplicationsList() != null
                                               ? message.getApplicationsList().getValueCode() : null)
            .selectedApplicationValue(message.getApplicationsList() != null
                                                ? message.getApplicationsList().getValueLabel() : null)
            .selectedFutureHearingCode(message.getFutureHearingsList() != null
                                           ? message.getFutureHearingsList().getValueCode() : null)
            .selectedFutureHearingValue(message.getFutureHearingsList() != null
                                            ? message.getFutureHearingsList().getValueLabel() : null)
            .selectedSubmittedDocumentCode(message.getSubmittedDocumentsList() != null
                                               ? message.getSubmittedDocumentsList().getValueCode() : null)
            .selectedSubmittedDocumentValue(message.getSubmittedDocumentsList() != null
                                                ? message.getSubmittedDocumentsList().getValueLabel() : null)
            .updatedTime(dateTime.now())
            .messageContent(SEND.equals(caseData.getChooseSendOrReply()) ? caseData.getMessageContent() : message.getMessageContent())
            .replyHistory(Collections.emptyList())
            .selectedDocument(getSelectedDocument(documentMap, message.getSubmittedDocumentsList() != null
                ? message.getSubmittedDocumentsList().getValueCode() : null))
            .senderEmail(null != userDetails ? userDetails.getEmail() : null)
            .senderNameAndRole(getSenderNameAndRole(userDetails))
            .otherApplicationsLink((message.getApplicationsList() != null && message.getApplicationsList().getValueCode() != null)
                                       ? otherApplicationsUrlLink : null)
            //.otherApplicationsLink((message.getApplicationsList() != null && message.getApplicationsList().getValueCode() != null)
            //                         ? "<a href='".concat(otherApplicationsUrl).concat("'>Other applications</a>") : null)
            .build();
    }

    private String getSenderNameAndRole(UserDetails userDetails) {
        if (null == userDetails) {
            return null;
        }

        return concat(concat(concat(userDetails.getFullName(), "("), getUserRole(userDetails.getRoles())),")");
    }

    private String getUserRole(List<String> roles) {
        if (isNotEmpty(roles)) {
            if (roles.contains("caseworker-privatelaw-courtadmin")) {
                return "Court admin";
            } else if (roles.contains("caseworker-privatelaw-judge ")) {
                return "Judge";
            } else if (roles.contains("caseworker-privatelaw-la ")) {
                return "Legal adviser";
            } else {
                return "";
            }
        }
        return "";
    }

    private uk.gov.hmcts.reform.prl.models.documents.Document getSelectedDocument(Map<String, Document> documentMap,
                                                                                  String selectedSubmittedDocumentCode) {

        if (documentMap != null && !documentMap.isEmpty()) {
            if (selectedSubmittedDocumentCode != null) {
                final String[] documentPath = selectedSubmittedDocumentCode.split("->");
                final String documentId = documentPath[documentPath.length - 1];
                final Document document = documentMap.get(documentId);
                if (document != null) {
                    return uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                        .documentUrl(document.getDocumentURL())
                        .documentBinaryUrl(document.getDocumentBinaryURL())
                        .documentFileName(document.getDocumentFilename())
                        .build();
                }
            }
        }
        return null;
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

    public List<Element<Message>> addNewOpenMessage(CaseData caseData, Message newMessage) {

        List<Element<Message>> messages = new ArrayList<>();
        Element<Message> messageElement = element(newMessage);
        if (isNotEmpty(caseData.getSendOrReplyMessage().getOpenMessagesList())) {
            messages.addAll(caseData.getSendOrReplyMessage().getOpenMessagesList());
        }
        messages.add(messageElement);
        messages.sort(Comparator.comparing(m -> m.getValue().getUpdatedTime(), Comparator.reverseOrder()));
        return messages;
    }


    public Map<String,Object> setSenderAndGenerateMessageReplyList(CaseData caseData, String authorisation) {
        Map<String, Object> data = new HashMap<>();
        MessageMetaData messageMetaData = MessageMetaData.builder()
            .senderEmail(getLoggedInUserEmail(authorisation))
            .build();
        data.put("messageObject", messageMetaData);

        if (isNotEmpty(caseData.getSendOrReplyMessage().getOpenMessagesList())) {
            data.put("messageReplyDynamicList", getOpenMessagesReplyList(caseData));
        }
        return data;
    }

    public DynamicList getOpenMessagesReplyList(CaseData caseData) {
        List<Element<Message>> openMessages = caseData.getSendOrReplyMessage().getOpenMessagesList();

        return ElementUtils.asDynamicList(
            openMessages,
            null,
            Message::getLabelForReplyDynamicList
        );
    }

    public CaseData populateMessageReplyFields(CaseData caseData, String authorization) {
        UUID messageId = elementUtils.getDynamicListSelectedValue(
            caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);

        Optional<Message> previousMessage = nullSafeCollection(
            caseData.getSendOrReplyMessage().getOpenMessagesList()).stream()
            .filter(element -> element.getId().equals(messageId))
            .map(Element::getValue)
            .findFirst();

        if (previousMessage.isEmpty()) {
            log.info("No message found with that ID");
            return caseData;
        }

        //populate message table
        String messageReply = renderMessageTable(previousMessage.get());

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
                            .build())
                    .build())
            .build();
    }

    private String renderMessageTable(Message message) {
        final List<String> lines = new LinkedList<>();

        //latest message at top
        lines.add("<div class='govuk-grid-column-two-thirds govuk-grid-row'><span class=\"heading-h3\">Message</span>");
        lines.add("<table>");
        addRowToMessageTable(lines, "Date Sent", message.getDateSent());
        addRowToMessageTable(lines, "Sender's name", message.getSenderNameAndRole());
        addRowToMessageTable(lines, "Sender's email", message.getSenderEmail());
        addRowToMessageTable(lines, "To", message.getInternalMessageWhoToSendTo() != null
            ? message.getInternalMessageWhoToSendTo().getDisplayedValue() : null);
        addRowToMessageTable(lines, "Judicial or magistrate Tier", message.getJudicialOrMagistrateTierValue());
        addRowToMessageTable(lines, "Judge name", message.getJudgeName());
        addRowToMessageTable(lines, "Judge email", message.getJudgeEmail());
        addRowToMessageTable(lines, "Recipient email addresses", message.getRecipientEmailAddresses());
        addRowToMessageTable(lines, "Urgent",  message.getInternalMessageUrgent() != null
            ? message.getInternalMessageUrgent().getDisplayedValue() : null);
        addRowToMessageTable(lines, "Subject", message.getMessageSubject());
        addRowToMessageTable(lines, "What is it about", message.getMessageAbout() != null
            ? message.getMessageAbout().getDisplayedValue() : null);
        addRowToMessageTable(lines, "Application", message.getSelectedApplicationValue());
        addRowToMessageTable(lines, "Hearing", message.getSelectedFutureHearingValue());
        addRowToMessageTable(lines, "Document", message.getSelectedSubmittedDocumentValue());
        addRowToMessageTable(lines, "The message", message.getMessageContent());
        lines.add("</table>");
        lines.add("</div>");

        //followed by history
        log.info("Message history :{}", message.getReplyHistory());
        if (null != message.getReplyHistory()) {
            message.getReplyHistory().stream()
                .map(Element::getValue)
                .forEach(history -> {
                    lines.add("<div class='govuk-grid-column-two-thirds govuk-grid-row'><span class=\"heading-h3\">Message</span>");
                    lines.add("<table>");
                    addRowToMessageTable(lines, "Date sent", history.getMessageDate());
                    addRowToMessageTable(lines, "Sender's name", history.getSenderNameAndRole());
                    addRowToMessageTable(lines, "Sender's email", history.getMessageFrom());
                    addRowToMessageTable(lines, "To", history.getInternalMessageWhoToSendTo());
                    addRowToMessageTable(lines, "Judicial or magistrate Tier", history.getJudicialOrMagistrateTierValue());
                    addRowToMessageTable(lines, "Judge name", history.getJudgeName());
                    addRowToMessageTable(lines, "Judge Email", history.getJudgeEmail());
                    addRowToMessageTable(lines, "Recipient email addresses", history.getRecipientEmailAddresses());
                    addRowToMessageTable(lines, "Urgent", history.getIsUrgent() != null
                        ? history.getIsUrgent().getDisplayedValue() : null);
                    addRowToMessageTable(lines, "Subject", history.getMessageSubject());
                    addRowToMessageTable(lines, "What is it about", history.getMessageAbout());
                    addRowToMessageTable(lines, "Application", history.getSelectedApplicationValue());
                    addRowToMessageTable(lines, "Hearing", history.getSelectedFutureHearingValue());
                    addRowToMessageTable(lines, "Document", history.getSelectedSubmittedDocumentValue());
                    addRowToMessageTable(lines, "The message", history.getMessageContent());
                    lines.add("</table>");
                    lines.add("</div>");
                });
        }

        return String.join("\n\n", lines);
    }

    private void addRowToMessageTable(List<String> lines,
                                      String label,
                                      String value) {
        if (value != null) {
            lines.add(TABLE_ROW_BEGIN);
            lines.add(TABLE_ROW_DATA_BEGIN + "<span class='heading-h4'>" + label + "</span>");
            lines.add(TABLE_ROW_DATA_END);
            lines.add(TABLE_ROW_DATA_BEGIN + "<span class='form-label'>" + value + "</span>");
            lines.add(TABLE_ROW_DATA_END);
            lines.add(TABLE_ROW_END);
        }

    }

    /**
     *  This method will send notification, when internal
     *  other message is sent.
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

        return  SendAndReplyNotificationEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .caseLink(caseLink)
            .build();
    }

    public List<Element<Message>> replyAndAppendMessageHistory(CaseData caseData,
                                                               String authorization) {
        UUID replyMessageId = elementUtils.getDynamicListSelectedValue(
            caseData.getSendOrReplyMessage().getMessageReplyDynamicList(), objectMapper);

        Message replyMessage = this.buildSendReplyMessage(caseData,
                                                          caseData.getSendOrReplyMessage().getReplyMessageObject(),
                                                          authorization
        );

        List<Element<MessageHistory>> messageHistoryList = new ArrayList<>();
        return caseData.getSendOrReplyMessage().getOpenMessagesList().stream()
            .map(messageElement -> {
                if (replyMessageId.equals(messageElement.getId())) {
                    Message message = messageElement.getValue();

                    MessageHistory messageHistory = buildReplyMessageHistory(message);
                    if (null != message.getReplyHistory()) {
                        messageHistoryList.addAll(message.getReplyHistory());
                    }

                    messageHistoryList.add(element(messageHistory));

                    messageHistoryList.sort(Comparator.comparing(m -> m.getValue().getMessageDate(), Comparator.reverseOrder()));

                    replyMessage.setReplyHistory(messageHistoryList);
                    replyMessage.setUpdatedTime(dateTime.now());
                    //retain the original subject
                    replyMessage.setMessageSubject(message.getMessageSubject());

                    return element(messageElement.getId(), replyMessage);
                }
                return messageElement;
            }).collect(Collectors.toList());
    }

    private MessageHistory buildReplyMessageHistory(Message message) {
        return MessageHistory.builder()
            .messageFrom(message.getSenderEmail())
            .messageTo(message.getInternalMessageWhoToSendTo() != null
                           ? message.getInternalMessageWhoToSendTo().getDisplayedValue() : null)
            .messageDate(message.getUpdatedTime().format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK)))
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
            .senderNameAndRole(message.getSenderNameAndRole())
            .build();
    }

    public CaseData resetSendAndReplyDynamicLists(CaseData caseData) {
        Message sendMessageObject = null;
        Message replyMessageObject = null;
        if (null != caseData.getSendOrReplyMessage().getSendMessageObject()) {
            sendMessageObject = caseData.getSendOrReplyMessage().getSendMessageObject();
            if (!InternalMessageWhoToSendToEnum.JUDICIARY.equals(sendMessageObject.getInternalMessageWhoToSendTo())
                && null != sendMessageObject.getJudicialOrMagistrateTierList()) {
                sendMessageObject.setJudicialOrMagistrateTierList(sendMessageObject.getJudicialOrMagistrateTierList().toBuilder()
                                                                      .value(DynamicListElement.EMPTY).build());
                sendMessageObject.setSendReplyJudgeName(JudicialUser.builder().build());
            }
            if (!InternalMessageWhoToSendToEnum.OTHER.equals(sendMessageObject.getInternalMessageWhoToSendTo())
                && null != sendMessageObject.getCtscEmailList()) {
                sendMessageObject.setCtscEmailList(sendMessageObject.getCtscEmailList().toBuilder()
                                                       .value(DynamicListElement.EMPTY).build());
                sendMessageObject.setRecipientEmailAddresses(null);
            }
            if (!MessageAboutEnum.APPLICATION.equals(sendMessageObject.getMessageAbout())
                && null != sendMessageObject.getApplicationsList()) {
                sendMessageObject.setApplicationsList(sendMessageObject.getApplicationsList().toBuilder()
                                                                      .value(DynamicListElement.EMPTY).build());
            }
            if (!MessageAboutEnum.HEARING.equals(sendMessageObject.getMessageAbout())
                && null != sendMessageObject.getFutureHearingsList()) {
                sendMessageObject.setFutureHearingsList(sendMessageObject.getFutureHearingsList().toBuilder()
                                                                      .value(DynamicListElement.EMPTY).build());
            }
            if (!MessageAboutEnum.REVIEW_SUBMITTED_DOCUMENTS.equals(sendMessageObject.getMessageAbout())
                && null != sendMessageObject.getSubmittedDocumentsList()) {
                sendMessageObject.setSubmittedDocumentsList(sendMessageObject.getSubmittedDocumentsList().toBuilder()
                                                                      .value(DynamicListElement.EMPTY).build());
            }
        }

        if (null != caseData.getSendOrReplyMessage().getReplyMessageObject()) {
            replyMessageObject = caseData.getSendOrReplyMessage().getReplyMessageObject();
            if (!InternalMessageReplyToEnum.JUDICIARY.equals(replyMessageObject.getInternalMessageReplyTo())
                && null != replyMessageObject.getJudicialOrMagistrateTierList()) {
                replyMessageObject.setJudicialOrMagistrateTierList(replyMessageObject.getJudicialOrMagistrateTierList().toBuilder()
                                                                      .value(DynamicListElement.EMPTY).build());
                replyMessageObject.setSendReplyJudgeName(JudicialUser.builder().build());
            }
        }

        return caseData.toBuilder().sendOrReplyMessage(
            caseData.getSendOrReplyMessage().toBuilder()
                .sendMessageObject(sendMessageObject)
                .replyMessageObject(replyMessageObject)
                .build()
            ).build();
    }
}
