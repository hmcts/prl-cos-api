package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SendAndReplyNotificationEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.repositories.CcdCaseApi;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICANTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getDynamicList;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.getDynamicMultiselectList;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

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

    private  final CcdCaseApi ccdCaseApi;

    private final RefDataService refDataService;

    @Value("${sendandreply.category-id}")
    private String categoryId;

    @Value("${sendandreply.service-code}")
    private String serviceCode;

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

        // TODO remove this method
        testmethod(caseData, auth);

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
     * This method will return Dynamic Multi select list for
     * applicants, respondents, cafcass and other reciepients.
     * @param caseData CaseData object.
     * @return DynamicMultiSelectList.
     */
    private DynamicMultiSelectList getExternalRecipientsDynamicMultiselectList(CaseData caseData) {
        DynamicMultiSelectListService dynamicMultiSelectListService = new DynamicMultiSelectListService();
        List<DynamicMultiselectListElement> listItems = new ArrayList<>();
        listItems.addAll(dynamicMultiSelectListService.getApplicantsMultiSelectList(caseData).get(APPLICANTS));
        listItems.addAll(dynamicMultiSelectListService.getRespondentsMultiSelectList(caseData).get(RESPONDENTS));
        listItems.add(DynamicMultiselectListElement.builder().code(CAFCASS).label(CAFCASS).build());
        listItems.add(DynamicMultiselectListElement.builder().code(OTHER).label(OTHER).build());
        return getDynamicMultiselectList(listItems);
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
    public DynamicList getJudiciaryTierDynmicList(String authorization, String s2sToken, String serviceCode, String categoryId) {

        Map<String, String> refDataCategoryValueMap = refDataService.getRefDataCategoryValueMap(
            authorization,
            s2sToken,
            serviceCode,
            categoryId
        );

        if (refDataCategoryValueMap != null && !refDataCategoryValueMap.isEmpty()) {
            List<DynamicListElement> judiciaryTierDynmicElementList = new ArrayList<>();

            refDataCategoryValueMap.forEach((k, v) -> judiciaryTierDynmicElementList.add(DynamicListElement.builder().code(
                k).label(v).build()));

            return getDynamicList(judiciaryTierDynmicElementList);
        }
        return DynamicList.builder().build();
    }


    private void testmethod(CaseData caseData, String auth) {
        // TODO Remove this logger

        log.info("DynamicList to populate Linked case dropdown ------> {}",
                 getLinkedCasesDynamicList(auth, String.valueOf(caseData.getId())));


        try {
            CaseDetails caseDetails = ccdCaseApi.getCase(auth, String.valueOf(caseData.getId()));
            caseData = CaseUtils.getCaseData(caseDetails, objectMapper);

            log.info("CaseTYPE of Application --> {}", caseData.getCaseTypeOfApplication());
        } catch (Exception e) {
            e.printStackTrace();
        }

        log.info("Dynamic List of Applicants and Respondents {}",  getExternalRecipientsDynamicMultiselectList(caseData));
        // TODO

        // TODO  remove below after testing

        String s2sToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJwcmxfY29zX2FwaSIsImV4cCI6MTY4MjcxNTA3OX0.eMEcnzxAPEnE3vPwJlz_kya"
            + "vNZN2nVRxcZ9pvUg-0HrLw3btTlNrb-10fUwfajT1d5lYxsZ6HnUmCMp0R_zEqw\n";

        // TODO end

        log.info(
            "getDynamicList(judiciaryTierDynmicElementList) ---> {}",
            getJudiciaryTierDynmicList(auth, s2sToken, serviceCode, categoryId));
    }

}
