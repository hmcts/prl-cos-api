package uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

@RequiredArgsConstructor
public abstract class AbstractRoleAllocator {

    private final RoleAssignmentService roleAssignmentService;

    protected boolean isUserAllocatedToCase(CaseData caseData, String idamId, Roles role) {
        return roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), idamId, role.getValue());
    }

    protected void createRoleAssignment(String caseId, String idamId, RoleCategory roleCategory, Roles role) {
        roleAssignmentService.createRoleAssignment(caseId, idamId, roleCategory, role.getValue(), false);
    }
}
