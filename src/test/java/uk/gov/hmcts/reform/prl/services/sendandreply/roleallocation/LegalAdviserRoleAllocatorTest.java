package uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Roles.ALLOCATED_LEGAL_ADVISER;

class LegalAdviserRoleAllocatorTest {

    private static final String SELECTED_LEGAL_ADVISER_IDAM_ID = UUID.randomUUID().toString();
    private static final String CASE_ID = "2463764890085620";
    private static final String NEW_MESSAGE_ID = "new-message-identifier";

    @Test
    void givenUserAlreadyAllocatedRole_whenHandleRequest_thenNoNewAllocation() {
        CaseData caseData = caseData();
        AssignRoleRequest assignRoleRequest = assignRoleRequest(caseData);

        RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);
        when(roleAssignmentService.isUserAllocatedRoleForCase(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, ALLOCATED_LEGAL_ADVISER.getValue()))
            .thenReturn(true);
        LegalAdviserRoleAllocator roleAllocator = new LegalAdviserRoleAllocator(roleAssignmentService);

        roleAllocator.handleRequest(assignRoleRequest);

        verify(roleAssignmentService).isUserAllocatedRoleForCase(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, ALLOCATED_LEGAL_ADVISER.getValue());
        verifyNoMoreInteractions(roleAssignmentService);
    }

    @Test
    void givenUserNotAllocatedRole_whenHandleRequest_thenNewAllocationCreated() {
        CaseData caseData = caseData();
        AssignRoleRequest assignRoleRequest = assignRoleRequest(caseData);

        RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);
        when(roleAssignmentService.isUserAllocatedRoleForCase(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, ALLOCATED_LEGAL_ADVISER.getValue()))
            .thenReturn(false);

        LegalAdviserRoleAllocator roleAllocator = new LegalAdviserRoleAllocator(roleAssignmentService);

        roleAllocator.handleRequest(assignRoleRequest);

        verify(roleAssignmentService).createRoleAssignment(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, RoleCategory.LEGAL_OPERATIONS,
                                                           ALLOCATED_LEGAL_ADVISER.getValue(), false);
    }

    private CaseData caseData() {
        return CaseData.builder()
            .id(Long.parseLong(CASE_ID))
            .build();
    }

    private AssignRoleRequest assignRoleRequest(CaseData caseData) {
        Message message = message(legalAdviserList());

        return AssignRoleRequest.builder()
            .caseData(caseData)
            .caseDataMap(new HashMap<>())
            .message(message)
            .idamId(SELECTED_LEGAL_ADVISER_IDAM_ID)
            .build();
    }

    private Message message(DynamicList legalAdviserList) {
        return Message.builder()
            .internalMessageWhoToSendTo(InternalMessageWhoToSendToEnum.LEGAL_ADVISER)
            .legalAdviserList(legalAdviserList)
            .messageIdentifier(NEW_MESSAGE_ID)
            .build();
    }

    private DynamicList legalAdviserList() {
        DynamicListElement value = DynamicListElement.builder()
            .code(SELECTED_LEGAL_ADVISER_IDAM_ID)
            .label("Legal Advisor Name (legaladviser@justice.gov.uk)")
            .build();
        return DynamicList.builder()
            .listItems(List.of(value))
            .value(value)
            .build();
    }
}
