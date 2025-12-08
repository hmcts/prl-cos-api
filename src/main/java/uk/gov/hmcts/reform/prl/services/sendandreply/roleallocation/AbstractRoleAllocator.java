package uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedUserForSendAndReply;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RequiredArgsConstructor
public abstract class AbstractRoleAllocator {

    private final RoleAssignmentService roleAssignmentService;

    protected boolean isUserAllocatedToCaseFromPreviousMessage(CaseData caseData, String idamId, Roles role) {
        if (caseData.getSendOrReplyDto() == null) {
            return false;
        }

        List<Element<AllocatedUserForSendAndReply>> allocatedUserForSendAndReply = caseData.getSendOrReplyDto()
            .getAllocatedUserForSendAndReply();
        if (allocatedUserForSendAndReply != null) {
            Optional<AllocatedUserForSendAndReply> allocatedUserForSendAndReplyOptional = allocatedUserForSendAndReply
                .stream()
                .map(Element::getValue)
                .filter(i -> i.getIdamId().equals(idamId) && i.getRoleName().equals(role.getValue()))
                .findAny();
            return allocatedUserForSendAndReplyOptional.isPresent();
        }

        return false;
    }

    protected boolean isUserAllocatedToCase(CaseData caseData, String idamId, Roles role) {
        return roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), idamId, role.getValue());
    }

    protected void createRoleAssignment(String caseId, String idamId, RoleCategory roleCategory, Roles role) {
        roleAssignmentService.createRoleAssignment(caseId, idamId, roleCategory, role.getValue(), false);
    }

    protected List<Element<AllocatedUserForSendAndReply>> addUserToSendReplyDto(CaseData caseData, String idamId,
                                                                                Roles role, String messageId) {
        List<Element<AllocatedUserForSendAndReply>> allocatedUserForSendAndReply =
            Optional.ofNullable(caseData.getSendOrReplyDto())
                .map(SendOrReplyDto::getAllocatedUserForSendAndReply)
                .orElseGet(ArrayList::new);

        allocatedUserForSendAndReply.add(element(AllocatedUserForSendAndReply.builder()
                                                     .idamId(idamId)
                                                     .roleName(role.getValue())
                                                     .messageIdentifier(messageId)
                                                     .build()));
        return allocatedUserForSendAndReply;
    }
}
