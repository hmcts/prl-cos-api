package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Mock
    ObjectMapper objectMapper;

    String auth = "auth-token";

    CaseDetails caseDetails;
    UserDetails userDetails;

    CallbackRequest callbackRequest;

    @Before
    public void init() {
        caseDetails = CaseDetails.builder().id(123L).build();
    }

    @Ignore
    @Test
    public void testCreateRoleAssignment() {
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testValidateIfUserHasRightRole() {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("allocate-judge");
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);

        when(roleAssignmentApi.getRoleAssignments(auth, authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails.toBuilder().data(stringObjectMap).build()).build();
        boolean bool = roleAssignmentService.validateIfUserHasRightRoles(auth, callbackRequest);
        assertEquals(true, bool);
    }

    @Test
    public void testValidateIfUserDoesNotHaveRightRole() {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("test");
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);

        when(roleAssignmentApi.getRoleAssignments(auth, authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails.toBuilder().data(stringObjectMap).build()).build();
        boolean bool = roleAssignmentService.validateIfUserHasRightRoles(auth, callbackRequest);
        assertEquals(false, bool);
    }
}
