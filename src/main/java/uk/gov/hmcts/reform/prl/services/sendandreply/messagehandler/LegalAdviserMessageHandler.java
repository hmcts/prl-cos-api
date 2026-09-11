package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.legalofficer.StaffProfile;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_ASSIGNEE_IDAM_ID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalAdviserMessageHandler implements MessageHandler {

    private final RefDataUserService refDataUserService;

    @Override
    public boolean canHandle(MessageRequest messageRequest) {
        return isMessageToLegalAdviser(messageRequest) && isMessageToSelectedLegalAdviser(messageRequest);
    }

    private boolean isMessageToLegalAdviser(MessageRequest messageRequest) {
        return InternalMessageWhoToSendToEnum.LEGAL_ADVISER.equals(messageRequest.getMessage().getInternalMessageWhoToSendTo());
    }

    private boolean isMessageToSelectedLegalAdviser(MessageRequest request) {
        StaffUser legalAdviser = getLegalAdviserSelection(request.getCaseData());
        return legalAdviser != null && legalAdviser.getIdamId() != null;
    }

    @Override
    public void handle(MessageRequest messageRequest) {
        Message message = messageRequest.getMessage();
        StaffUser legalAdviser = getLegalAdviserSelection(messageRequest.getCaseData());
        String idamId = null;
        if (legalAdviser != null) {
            Optional<StaffProfile> la = refDataUserService.getLegalAdviserUserDetails(legalAdviser);
            if (la.isPresent()) {
                message.setLegalAdviserEmail(la.get().getEmailId());
                message.setLegalAdviserName(String.format("%s %s", la.get().getFirstName(), la.get().getLastName()));
                idamId = la.get().getId();
            }
        }
        messageRequest.getCaseDataMap().put(TASK_ASSIGNEE_IDAM_ID, idamId);
    }

    private StaffUser getLegalAdviserSelection(CaseData caseData) {
        SendOrReplyMessage sendOrReplyMessage = caseData.getSendOrReplyMessage();

        return switch (caseData.getChooseSendOrReply()) {
            case SEND -> sendOrReplyMessage.getSendMessageObject().getLegalAdviser();
            case REPLY -> sendOrReplyMessage.getReplyMessageObject().getLegalAdviser();
        };
    }
}
