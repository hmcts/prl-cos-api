package uk.gov.hmcts.reform.prl.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RoleAssignmentServiceForSystemUserTest {

    @InjectMocks
    RoleAssignmentServiceForSystemUser roleAssignmentServiceForSystemUser;

    @Mock
    UserService userService;

    @Mock
    SystemUserService systemUserService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    RoleAssignmentApi roleAssignmentApi;


    @Test
    public void testAssignHearingRole() {
        roleAssignmentServiceForSystemUser.assignHearingRoleToSysUser();
        verify(roleAssignmentApi, times(1)).updateRoleAssignment(Mockito.any(), Mockito.any(),
                                                                 Mockito.any(), Mockito.any());
    }
}
