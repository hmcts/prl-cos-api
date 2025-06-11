package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentResponse;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleAssignmentServiceForSystemUserTest {
    @InjectMocks
    RoleAssignmentServiceForSystemUser roleAssignmentServiceForSystemUser;

    @Mock
    SystemUserService systemUserService;

    @Mock
    RoleAssignmentApi roleAssignmentApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Test
    public void testAssignHearingRoleToSysUser() {
        when(systemUserService.getSysUserToken()).thenReturn("systemUserToken");
        when(systemUserService.getUserId(any(String.class))).thenReturn("systemUserIdamID");
        when(roleAssignmentApi.updateRoleAssignment(any(), any(), any(), any(RoleAssignmentRequest.class)))
            .thenReturn(RoleAssignmentResponse.builder().build());
        when(authTokenGenerator.generate()).thenReturn("authToken");
        roleAssignmentServiceForSystemUser.assignHearingRoleToSysUser();
        verify(roleAssignmentApi, times(1)).updateRoleAssignment(any(), any(), any(), any());
    }
}
