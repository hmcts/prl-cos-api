package uk.gov.hmcts.reform.prl.services.sendandreply.messagehandler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.mapper.dynamiclistelement.LegalAdviserDynamicListElementBiConverter;
import uk.gov.hmcts.reform.prl.mapper.dynamiclistelement.LegalAdviserIdamId;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation.AssignRoleRequest;
import uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation.LegalAdviserRoleAllocator;

@Service
@RequiredArgsConstructor
@Slf4j
public class LegalAdviserMessageHandler implements MessageHandler{

    private final LegalAdviserRoleAllocator legalAdviserRoleAllocator;
    private final LegalAdviserDynamicListElementBiConverter legalAdviserDynamicListElementBiConverter;

    @Override
    public boolean canHandle(MessageRequest messageRequest) {
        return isMessageToLegalAdviser(messageRequest) && isMessageToSelectedLegalAdviser(messageRequest);
    }

    private boolean isMessageToLegalAdviser(MessageRequest messageRequest) {
        return InternalMessageWhoToSendToEnum.LEGAL_ADVISER.equals(messageRequest.getMessage().getInternalMessageWhoToSendTo());
    }

    private boolean isMessageToSelectedLegalAdviser(MessageRequest request) {
        return getSelectedLegalAdviserIdamId(request.getCaseData()) != null;
    }

    @Override
    public void handle(MessageRequest messageRequest) {
        AssignRoleRequest assignRoleRequest = createAssignRoleRequest(
            messageRequest, getSelectedLegalAdviserIdamId(messageRequest.getCaseData()));
        legalAdviserRoleAllocator.handleRequest(assignRoleRequest);

        Message message = messageRequest.getMessage();
        DynamicListElement selectedElement = getLegalAdviserSelection(messageRequest.getCaseData());
        LegalAdviserIdamId selectedLegalAdviser = legalAdviserDynamicListElementBiConverter
            .convertFromDynamicListElement(selectedElement);
        message.setLegalAdviserEmail(selectedLegalAdviser.getEmail());
        message.setLegalAdviserName(selectedLegalAdviser.getFullName());
    }

    private AssignRoleRequest createAssignRoleRequest(MessageRequest messageRequest, String idamId) {
        return AssignRoleRequest.builder()
            .idamId(idamId)
            .caseData(messageRequest.getCaseData())
            .caseDataMap(messageRequest.getCaseDataMap())
            .message(messageRequest.getMessage())
            .build();
    }

    private String getSelectedLegalAdviserIdamId(CaseData caseData) {
        DynamicListElement selectedElement = getLegalAdviserSelection(caseData);
        return selectedElement != null ? selectedElement.getCode() : null;
    }

    private DynamicListElement getLegalAdviserSelection(CaseData caseData) {
        SendOrReplyMessage sendOrReplyMessage = caseData.getSendOrReplyMessage();

        DynamicList legalAdviserList = switch (caseData.getChooseSendOrReply()) {
            case SEND -> sendOrReplyMessage.getSendMessageObject().getLegalAdviserList();
            case REPLY -> sendOrReplyMessage.getReplyMessageObject().getLegalAdviserList();
        };

        return legalAdviserList != null ? legalAdviserList.getValue() : null;
    }
}
