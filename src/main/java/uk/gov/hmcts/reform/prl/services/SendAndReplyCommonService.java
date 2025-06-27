package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_ADDTIONAL_APPLICATION_BUNDLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_IN_REVIEW;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.REPLY;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;

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

        sendAndReplyService.sendNotificationToExternalParties(
            caseData,
            authorisation
        );

        //send emails in case of sending to others with emails
        sendAndReplyService.sendNotificationEmailOther(caseData);
        //WA - clear reply field in case of SEND
        sendAndReplyService.removeTemporaryFields(caseDataMap, "replyMessageObject");
    }

}
