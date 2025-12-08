package uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.sendmessages.InternalMessageWhoToSendToEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.AllocatedUserForSendAndReply;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.Roles.ALLOCATED_LEGAL_ADVISER;
import static uk.gov.hmcts.reform.prl.services.sendandreply.roleallocation.LegalAdviserRoleAllocator.ALLOCATED_USER_FOR_SEND_AND_REPLY;

class LegalAdviserRoleAllocatorTest {

    private static final String SELECTED_LEGAL_ADVISER_IDAM_ID = UUID.randomUUID().toString();
    private static final String CASE_ID = "2463764890085620";
    private static final String NEW_MESSAGE_ID = "new-message-identifier";

    @Test
    void givenUserAlreadyAllocatedRoleDueToPreviousMessage_whenHandleRequest_thenNoNewAllocation() {
        CaseData caseData = caseDataWithPreviousMessage(SELECTED_LEGAL_ADVISER_IDAM_ID);
        AssignRoleRequest assignRoleRequest = assignRoleRequest(caseData);

        RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);
        LegalAdviserRoleAllocator roleAllocator = new LegalAdviserRoleAllocator(roleAssignmentService);

        roleAllocator.handleRequest(assignRoleRequest);

        verifyNoInteractions(roleAssignmentService);
        assertThat(assignRoleRequest.getCaseDataMap()).isEmpty();
    }

    @Test
    void givenUserAlreadyAllocatedRole_whenHandleRequest_thenNoNewAllocation() {
        CaseData caseData = caseDataWithNoPreviousMessage();
        AssignRoleRequest assignRoleRequest = assignRoleRequest(caseData);

        RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);
        when(roleAssignmentService.isUserAllocatedRoleForCase(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, ALLOCATED_LEGAL_ADVISER.getValue()))
            .thenReturn(true);
        LegalAdviserRoleAllocator roleAllocator = new LegalAdviserRoleAllocator(roleAssignmentService);

        roleAllocator.handleRequest(assignRoleRequest);

        assertThat(assignRoleRequest.getCaseDataMap()).isEmpty();
    }

    @Test
    void givenUserNotAllocatedRole_whenHandleRequest_thenNewAllocationCreated() {
        CaseData caseData = caseDataWithPreviousMessage("random-idam-id");
        AssignRoleRequest assignRoleRequest = assignRoleRequest(caseData);

        RoleAssignmentService roleAssignmentService = mock(RoleAssignmentService.class);
        when(roleAssignmentService.isUserAllocatedRoleForCase(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, ALLOCATED_LEGAL_ADVISER.getValue()))
            .thenReturn(false);

        LegalAdviserRoleAllocator roleAllocator = new LegalAdviserRoleAllocator(roleAssignmentService);

        roleAllocator.handleRequest(assignRoleRequest);

        verify(roleAssignmentService).createRoleAssignment(CASE_ID, SELECTED_LEGAL_ADVISER_IDAM_ID, RoleCategory.LEGAL_OPERATIONS,
                                                           ALLOCATED_LEGAL_ADVISER.getValue(), false);

        Map<String, Object> caseDataMap = assignRoleRequest.getCaseDataMap();
        assertThat(caseDataMap).hasSize(1);
        var allocatedUsers = (List<Element<AllocatedUserForSendAndReply>>) caseDataMap.get(ALLOCATED_USER_FOR_SEND_AND_REPLY);
        assertThat(allocatedUsers).hasSize(2);

        verifyUserAllocatedUser(allocatedUsers.getFirst().getValue(), "random-idam-id", "random-message-id");
        verifyUserAllocatedUser(allocatedUsers.get(1).getValue(), SELECTED_LEGAL_ADVISER_IDAM_ID, NEW_MESSAGE_ID);
    }

    private void verifyUserAllocatedUser(AllocatedUserForSendAndReply allocatedUser, String idamId, String messageIdentifier) {
        assertThat(allocatedUser.getIdamId()).isEqualTo(idamId);
        assertThat(allocatedUser.getRoleName()).isEqualTo(ALLOCATED_LEGAL_ADVISER.getValue());
        assertThat(allocatedUser.getMessageIdentifier()).isEqualTo(messageIdentifier);
    }

    private CaseData caseDataWithPreviousMessage(String idamId) {
        CaseData caseData = caseDataWithNoPreviousMessage();

        AllocatedUserForSendAndReply allocatedUser = AllocatedUserForSendAndReply.builder()
            .idamId(idamId)
            .roleName(ALLOCATED_LEGAL_ADVISER.getValue())
            .messageIdentifier("random-message-id")
            .build();
        Element<AllocatedUserForSendAndReply> element = Element.<AllocatedUserForSendAndReply>builder()
            .value(allocatedUser)
            .build();
        List<Element<AllocatedUserForSendAndReply>> allocatedUserForSendAndReply = new ArrayList<>();
        allocatedUserForSendAndReply.add(element);

        SendOrReplyDto sendOrReplyDto = SendOrReplyDto.builder()
            .allocatedUserForSendAndReply(allocatedUserForSendAndReply)
            .build();

        caseData.setSendOrReplyDto(sendOrReplyDto);
        return caseData;
    }

    private CaseData caseDataWithNoPreviousMessage() {
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
