package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.CodeAndLabel;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SendAndReplyNotificationEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
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
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
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
    private static final String TABLE_ROW_DATA_BEGIN = "<td>";
    private static final String TABLE_ROW_DATA_END = "</td>";

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

        List<Element<Message>> openMessages = caseData.getSendOrReplyMessage().getOpenMessagesList();
        List<Element<Message>> closedMessages = caseData.getSendOrReplyMessage().getClosedMessagesList();

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
                .judicialOrMagistrateTierList(getJudiciaryTierDynamicList(
                    authorization,
                    s2sToken,
                    serviceCode,
                    categoryId
                ))
                .linkedApplicationsList(getLinkedCasesDynamicList(authorization, caseReference))
                .submittedDocumentsList(documentCategoryList)
                .ctscEmailList(getDynamicList(List.of(DynamicListElement.builder().label(loggedInUserEmail).code(loggedInUserEmail).build())))
                .futureHearingsList(getFutureHearingDynamicList(authorization, s2sToken, caseReference))
                .build()).build();
    }

    private DynamicList getFutureHearingDynamicList(String authorization, String s2sToken, String caseId) {
        Hearings futureHearings = hearingService.getFutureHearings(authorization, caseId);

        // label - hearingtypevalue - date
        // code - hearing id - hearingtype

        if (futureHearings != null && futureHearings.getCaseHearings() != null && !futureHearings.getCaseHearings().isEmpty()) {

            Map<String, String> refDataCategoryValueMap = getHearingTypeRefDataMap(authorization, s2sToken, serviceCode);

            List<DynamicListElement> hearingDropdowns = futureHearings.getCaseHearings().stream()
                .map(caseHearing -> {
                    //get hearingId
                    String hearingId = String.valueOf(caseHearing.getHearingID());
                    final String hearingType = caseHearing.getHearingType();
                    String hearingTypeValue = refDataCategoryValueMap.get(hearingType);
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

    private Map<String, String> getHearingTypeRefDataMap(String authorization, String s2sToken, String serviceCode) {

        return refDataService.getRefDataCategoryValueMap(
            authorization,
            s2sToken,
            serviceCode,
            hearingTypeCategoryId
        );
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
            Map<String, String> refDataCategoryValueMap = refDataService.getRefDataCategoryValueMap(
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
            .collect(Collectors.toList());

        List<DynamicListElement> dynamicListElementList = new ArrayList<>();
        createDynamicListFromSubCategories(parentCategories, dynamicListElementList, null, null);

        categoriesAndDocuments.getUncategorisedDocuments().forEach(document -> dynamicListElementList.add(
            DynamicListElement.builder().code(fetchDocumentIdFromUrl(document.getDocumentURL()))
                .label(document.getDocumentFilename()).build()
        ));

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
                        DynamicListElement.builder().code(category.getCategoryId() + "___"
                                                              + fetchDocumentIdFromUrl(document.getDocumentURL()))
                            .label(category.getCategoryName() + " --- " + document.getDocumentFilename()).build()
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
                            .code(parentCodeString + " -> " + category.getCategoryId() + "___"
                                      + fetchDocumentIdFromUrl(document.getDocumentURL()))
                            .label(parentLabelString + " -> " + category.getCategoryName() + " --- "
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

    private String fetchDocumentIdFromUrl(String documentUrl) {

        return documentUrl.substring(documentUrl.lastIndexOf("/") + 1);

    }

    public Message buildSendMessage(CaseData caseData) {

        final SendOrReplyMessage sendOrReplyMessage = caseData.getSendOrReplyMessage();

        return Message.builder()
            .status(OPEN)
            .dateSent(dateTime.now().format(DateTimeFormatter.ofPattern("d MMMM yyyy 'at' h:mma", Locale.UK)))
            .internalOrExternalMessage(sendOrReplyMessage.getInternalOrExternalMessage() != null
                                           ? sendOrReplyMessage.getInternalOrExternalMessage().name() : null)
            .internalMessageUrgent(sendOrReplyMessage.getInternalMessageUrgent())
            .internalMessageWhoToSendTo(sendOrReplyMessage.getInternalMessageWhoToSendTo() != null
                                            ? sendOrReplyMessage.getInternalMessageWhoToSendTo().name() : null)
            .messageAbout(sendOrReplyMessage.getMessageAbout() != null
                          ? sendOrReplyMessage.getMessageAbout().name() : null)
            .judgeName(getJudgeName(sendOrReplyMessage.getSendReplyJudgeName()))
            .messageSubject(sendOrReplyMessage.getMessageSubject())
            .recipientEmailAddresses(sendOrReplyMessage.getRecipientEmailAddresses())
            .selectedCtscEmail(sendOrReplyMessage.getCtscEmailList() != null
                                   ? sendOrReplyMessage.getCtscEmailList().getValueCode() : null)
            .judicialOrMagistrateTierCode(sendOrReplyMessage.getJudicialOrMagistrateTierList() != null
                                              ? sendOrReplyMessage.getJudicialOrMagistrateTierList().getValueCode() : null)
            .judicialOrMagistrateTierValue(sendOrReplyMessage.getJudicialOrMagistrateTierList() != null
                                               ? sendOrReplyMessage.getJudicialOrMagistrateTierList().getValueLabel() : null)
            .selectedLinkedApplicationCode(sendOrReplyMessage.getLinkedApplicationsList() != null
                                               ? sendOrReplyMessage.getLinkedApplicationsList().getValueCode() : null)
            .selectedLinkedApplicationValue(sendOrReplyMessage.getLinkedApplicationsList() != null
                                                ? sendOrReplyMessage.getLinkedApplicationsList().getValueLabel() : null)
            .selectedFutureHearingCode(sendOrReplyMessage.getFutureHearingsList() != null
                                           ? sendOrReplyMessage.getFutureHearingsList().getValueCode() : null)
            .selectedFutureHearingValue(sendOrReplyMessage.getFutureHearingsList() != null
                                            ? sendOrReplyMessage.getFutureHearingsList().getValueLabel() : null)
            .selectedSubmittedDocumentCode(sendOrReplyMessage.getSubmittedDocumentsList() != null
                                               ? sendOrReplyMessage.getSubmittedDocumentsList().getValueCode() : null)
            .selectedSubmittedDocumentValue(sendOrReplyMessage.getSubmittedDocumentsList() != null
                                                ? sendOrReplyMessage.getSubmittedDocumentsList().getValueLabel() : null)
            .latestMessage(caseData.getMessageContent())
            .updatedTime(dateTime.now())
            .messageContent(caseData.getMessageContent())
            .replyHistory(Collections.emptyList())
            .build();
    }


    public List<JudicialUsersApiResponse> getJudgeDetails(JudicialUser judicialUser) {

        String[] judgePersonalCode = getPersonalCode(judicialUser);
        return refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder()
                                                                .personalCode(judgePersonalCode).build());

    }

    private String getJudgeName(JudicialUser judicialUser) {
        if (judicialUser != null && judicialUser.getPersonalCode() != null) {
            final Optional<List<JudicialUsersApiResponse>> judicialUsersApiResponseList = ofNullable(getJudgeDetails(
                judicialUser));

            if (judicialUsersApiResponseList.isPresent()) {
                Optional<JudicialUsersApiResponse> judicialUsersApiResponse = judicialUsersApiResponseList.get().stream().findFirst();
                if (judicialUsersApiResponse.isPresent()) {
                    return judicialUsersApiResponse.get().getFullName();
                }
            }
        }
        return null;
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
            Message::getLabelForDynamicList
        );
    }

    public CaseData populateMessageReplyFields(CaseData caseData) {
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

        return caseData.toBuilder()
            .sendOrReplyMessage(
                caseData.getSendOrReplyMessage().toBuilder()
                    .replyMessage(previousMessage.get())
                    .messageReplyTable(messageReply).build())
            .build();
    }

    private String renderMessageTable(Message message) {
        final List<String> lines = new LinkedList<>();

        lines.add("<div class='width-50'>");
        lines.add("<table>");

        lines.add(TABLE_ROW_BEGIN);
        lines.add(TABLE_ROW_DATA_BEGIN);
        lines.add("Date of the message");
        lines.add(TABLE_ROW_DATA_END);
        lines.add(TABLE_ROW_DATA_BEGIN);
        lines.add(message.getDateSent());
        lines.add(TABLE_ROW_DATA_END);
        lines.add(TABLE_ROW_END);

        lines.add(TABLE_ROW_BEGIN);
        lines.add(TABLE_ROW_DATA_BEGIN);
        lines.add("Urgent");
        lines.add(TABLE_ROW_DATA_END);
        lines.add(TABLE_ROW_DATA_BEGIN);
       // lines.add(message.getInternalMessageUrgent().getDisplayedValue());
        lines.add("test22222");
        lines.add(TABLE_ROW_DATA_END);
        lines.add(TABLE_ROW_END);

        lines.add("</table>");
        lines.add("</div>");

        return String.join("\n\n", lines);
    }

}
