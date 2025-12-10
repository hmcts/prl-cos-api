package uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import static uk.gov.hmcts.reform.prl.enums.RoleCategory.LEGAL_OPERATIONS;
import static uk.gov.hmcts.reform.prl.enums.Roles.ALLOCATED_LEGAL_ADVISER;

/**
 * Allocator for Legal Adviser role <code>allocated-legal-adviser</code> that is allocated when a message is sent to a Legal Adviser.
 */
@Component
@Slf4j
public class LegalAdviserRoleAllocator extends AbstractRoleAllocator {

    public LegalAdviserRoleAllocator(RoleAssignmentService roleAssignmentService) {
        super(roleAssignmentService);
    }

    /**
     * Handles the role assignment request for a Legal Adviser.
     *
     * @param request the request containing case and user details
     */
    public void handleRequest(AssignRoleRequest request) {
        String caseId = request.getCaseId();
        String idamId = request.getIdamId();

        if (isUserAllocatedToCase(request.getCaseData(), idamId, ALLOCATED_LEGAL_ADVISER)) {
            log.info("User {} is already allocated role {} on case {}", idamId, ALLOCATED_LEGAL_ADVISER.getValue(),
                     caseId);
        } else {
            createRoleAssignment(caseId, idamId, LEGAL_OPERATIONS, ALLOCATED_LEGAL_ADVISER);
            log.info("Legal Adviser {} has been allocated role {} on case {} ", idamId, ALLOCATED_LEGAL_ADVISER.getValue(),
                     caseId);
        }
    }
}
