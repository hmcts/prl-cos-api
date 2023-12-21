package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RoleAssignmentServiceTest {

    @InjectMocks
    RoleAssignmentService roleAssignmentService;

    @Mock
    UserService userService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    RoleAssignmentApi roleAssignmentApi;

    String auth = "auth-token";

    CaseDetails caseDetails;
    UserDetails userDetails;

    @Before
    public void init() {
        caseDetails = CaseDetails.builder().id(123L).build();
    }

    @Test
    public void testCreateRoleAssignment() {
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "test", "Judge");
        assertEquals("1", userDetails.getId());
    }
}
