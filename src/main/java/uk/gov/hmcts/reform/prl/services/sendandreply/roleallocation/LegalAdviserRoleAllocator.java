package uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedUserForSendAndReply;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.RoleCategory.LEGAL_OPERATIONS;
import static uk.gov.hmcts.reform.prl.enums.Roles.ALLOCATED_LEGAL_ADVISER;

/**
 * Allocator for Legal Adviser role <code>allocated-legal-adviser</code>.
 * This role is allocated to a user for a case based on whether the message is to a specific legal adviser.
 */
@Component
@Slf4j
public class LegalAdviserRoleAllocator extends AbstractRoleAllocator {

    static final String ALLOCATED_USER_FOR_SEND_AND_REPLY = "allocatedUserForSendAndReply";

    public LegalAdviserRoleAllocator(RoleAssignmentService roleAssignmentService) {
        super(roleAssignmentService);
    }

    /**
     * Handles the role assignment request for a Legal Adviser.
     * @param request the request containing case and user details
     */
    public void handleRequest(AssignRoleRequest request) {
        String caseId = request.getCaseId();
        String idamId = request.getIdamId();
        if (isUserAllocatedToCase(request.getCaseData(), idamId)) {
            log.info("User {} is already allocated role {} on case {}", idamId, ALLOCATED_LEGAL_ADVISER.getValue(), caseId);
            return;
        }

        createRoleAssignment(caseId, idamId, LEGAL_OPERATIONS, ALLOCATED_LEGAL_ADVISER);
        log.info("Legal Adviser {} has been allocated role {} on case {} ", idamId, ALLOCATED_LEGAL_ADVISER.getValue(), caseId);

        List<Element<AllocatedUserForSendAndReply>> allocatedUserForSendAndReply = addUserToSendReplyDto(
            request.getCaseData(), idamId, ALLOCATED_LEGAL_ADVISER, request.getMessage().getMessageIdentifier());

        request.getCaseDataMap().put(ALLOCATED_USER_FOR_SEND_AND_REPLY, allocatedUserForSendAndReply);
    }

    private boolean isUserAllocatedToCase(CaseData caseData, String idamId) {
        if (isUserAllocatedToCaseFromPreviousMessage(caseData, idamId, ALLOCATED_LEGAL_ADVISER)) {
            return true;
        } else {
            return isUserAllocatedToCase(caseData, idamId, ALLOCATED_LEGAL_ADVISER);
        }
    }
}
