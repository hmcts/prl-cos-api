package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SendAndReplyNotificationEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.services.time.Time;
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
import static uk.gov.hmcts.reform.prl.enums.sendmessages.MessageStatus.OPEN;
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



}
