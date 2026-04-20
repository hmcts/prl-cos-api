package uk.gov.hmcts.reform.prl.services.sendandreply;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.sendmessages.MessageAboutEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.UploadAdditionalApplicationService;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_ADDTIONAL_APPLICATION_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_ACCESS_CATEGORY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;
import static uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage.temporaryFieldsAboutToSubmit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendAndReplyCommonService {

    public static final String MESSAGES = "messages";

    private final SendAndReplyService sendAndReplyService;
    private final UploadAdditionalApplicationService uploadAdditionalApplicationService;

    public void replyMessages(String authorisation, CaseData caseData, Map<String, Object> caseDataMap) {
        if (YesOrNo.No.equals(caseData.getSendOrReplyMessage().getRespondToMessage())) {
            //Reply & close
            caseDataMap.put(MESSAGES, sendAndReplyService.closeMessage(caseData, caseDataMap));

            // Update status of Additional applications if selected to Closed
            String additionalApplicationCodeSelected = sendAndReplyService.fetchAdditionalApplicationCodeIfExist(
                caseData, REPLY
            );
            log.info("additionalApplicationCodeSelected while closing message {}", additionalApplicationCodeSelected);
            if (null != additionalApplicationCodeSelected) {
                caseDataMap.put(
                    AWP_ADDTIONAL_APPLICATION_BUNDLE,
                    uploadAdditionalApplicationService
                        .updateAwpApplicationStatus(
                            additionalApplicationCodeSelected,
                            caseData.getAdditionalApplicationsBundle(),
                            AWP_STATUS_CLOSED
                        )
                );
            }

            // in case of reply and close message, removing replymessageobject for wa
            sendAndReplyService.removeTemporaryFields(caseDataMap, "replyMessageObject");
        } else {
            //Reply & append history
            caseDataMap.put(MESSAGES, sendAndReplyService.replyAndAppendMessageHistory(
                caseData,
                authorisation,
                caseDataMap
            ));
        }
        //WA - clear send field in case of REPLY
        sendAndReplyService.removeTemporaryFields(caseDataMap, "sendMessageObject");
    }

    public void sendMessages(String authorisation, CaseData caseData, Map<String, Object> caseDataMap) {
        caseDataMap.put(MESSAGES, sendAndReplyService.addMessage(caseData, authorisation, caseDataMap));
        String additionalApplicationCodeSelected = sendAndReplyService.fetchAdditionalApplicationCodeIfExist(
            caseData, SEND
        );

        if (null != additionalApplicationCodeSelected) {
            caseDataMap.put(
                AWP_ADDTIONAL_APPLICATION_BUNDLE,
                uploadAdditionalApplicationService
                    .updateAwpApplicationStatus(
                        additionalApplicationCodeSelected,
                        caseData.getAdditionalApplicationsBundle(),
                        AWP_STATUS_IN_REVIEW
                    )
            );
        }

        // ensure the message content is set in sendMessageObject for access in submitted cb
        caseDataMap.put("sendMessageObject", caseData.getSendOrReplyMessage().getSendMessageObject()
            .toBuilder()
                .messageContent(caseData.getMessageContent())
            .build()
        );

        //WA - clear reply field in case of SEND
        sendAndReplyService.removeTemporaryFields(caseDataMap, "replyMessageObject");
    }

    /**
     * Orchestrates the about-to-submit callback for both /send-or-reply-to-messages/about-to-submit
     * and its -task variant. Sends or replies to messages, clears temporary fields, stamps the
     * case access category, and — if the message is about a specific hearing — records today as the
     * Request Order task completion date for that hearing so the cron's per-hearing re-trigger
     * cadence restarts from here (FPVTL-2408/2409).
     */
    public AboutToStartOrSubmitCallbackResponse processAboutToSubmit(String authorisation,
                                                                     CaseData caseData,
                                                                     Map<String, Object> caseDataMap) {
        if (SEND.equals(caseData.getChooseSendOrReply())) {
            sendMessages(authorisation, caseData, caseDataMap);
        } else {
            replyMessages(authorisation, caseData, caseDataMap);
        }

        sendAndReplyService.removeTemporaryFields(caseDataMap, temporaryFieldsAboutToSubmit());
        caseDataMap.put(CASE_ACCESS_CATEGORY, caseData.getCaseTypeOfApplication());
        recordRequestOrderCompletionForSelectedHearing(caseData, caseDataMap);

        return AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
    }

    private void recordRequestOrderCompletionForSelectedHearing(CaseData caseData,
                                                                Map<String, Object> caseDataMap) {
        List<Element<RequestOrderHearingTracking>> existing = caseData.getRequestOrderTaskTrackingByHearing() == null
            ? List.of() : caseData.getRequestOrderTaskTrackingByHearing();
        String hearingId = extractHearingIdFromCurrentMessage(caseData);
        if (existing.isEmpty() && hearingId == null) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneId.of("Europe/London"));
        Map<String, Element<RequestOrderHearingTracking>> byHearingId = new LinkedHashMap<>();
        existing.forEach(e -> byHearingId.put(e.getValue().getHearingId(), e));

        // WA's completion DMN auto-completes the requestSolicitorOrder task on ANY
        // send-and-reply. Clear the in-flight flag (lastFiredDate) on every tracked
        // hearing so the cron re-evaluates cadence on the next run.
        byHearingId.values().forEach(e -> e.getValue().setLastFiredDate(null));

        if (hearingId != null) {
            Element<RequestOrderHearingTracking> entry = byHearingId.get(hearingId);
            if (entry != null) {
                entry.getValue().setLastCompletedDate(today);
            } else {
                byHearingId.put(hearingId, Element.<RequestOrderHearingTracking>builder()
                    .id(UUID.randomUUID())
                    .value(RequestOrderHearingTracking.builder()
                        .hearingId(hearingId)
                        .lastCompletedDate(today)
                        .build())
                    .build());
            }
        }

        caseDataMap.put("requestOrderTaskTrackingByHearing", new ArrayList<>(byHearingId.values()));
    }

    private String extractHearingIdFromCurrentMessage(CaseData caseData) {
        SendOrReplyMessage wrapper = caseData.getSendOrReplyMessage();
        if (wrapper == null) {
            return null;
        }
        Message message = SEND.equals(caseData.getChooseSendOrReply())
            ? wrapper.getSendMessageObject()
            : wrapper.getReplyMessageObject();
        if (message == null
            || !MessageAboutEnum.HEARING.equals(message.getMessageAbout())
            || message.getSelectedFutureHearingCode() == null
            || message.getSelectedFutureHearingCode().isBlank()) {
            return null;
        }
        String code = message.getSelectedFutureHearingCode().trim();
        int separator = code.indexOf(" - ");
        String hearingId = separator < 0 ? code : code.substring(0, separator).trim();
        return hearingId.isBlank() ? null : hearingId;
    }

}
