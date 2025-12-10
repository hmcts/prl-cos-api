package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.RoleCategory;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.roleassignment.RoleAssignmentDto;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RoleAssignmentServiceTest {

    @InjectMocks
    RoleAssignmentService roleAssignmentService;

    @Mock
    UserService userService;

    @Mock
    SystemUserService systemUserService;

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
        Map<String, Object> dataMap = new HashMap<>();
        caseDetails = CaseDetails.builder().data(dataMap).id(123L).build();
    }

    @Test
    public void testCreateRoleAssignmentActorIdIsNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        DynamicList legalAdviserList = DynamicList.builder().value(DynamicListElement.builder()
            .code("test1(test1@test.com)").label("test1(test1@test.com)").build()).build();
        when(objectMapper.convertValue(legalAdviserList, DynamicList.class)).thenReturn(legalAdviserList);
        when(userService.getUserByEmailId(auth, "test1@test.com")).thenReturn(List.of(UserDetails.builder()
            .forename("first")
            .id("1234")
            .surname("test").build()));
        roleAssignmentService.createRoleAssignment(
            auth,
            caseDetails,
            RoleAssignmentDto.builder().legalAdviserList(legalAdviserList).build(),
            "TEST EVENT",
            true,
            "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeWithName() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviser", "judge");
        caseDetailsMap.put("judgeName", "test");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(userService.getUserByEmailId(auth, "test1@test.com")).thenReturn(List.of(UserDetails.builder()
            .id("1234")
            .forename("first")
            .surname("test").build()));

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(
            auth,
            caseDetails,
            RoleAssignmentDto.builder().judgeEmail("test1@test.com").build(),
            "TEST EVENT",
            true,
            "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeWithEmail() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviser", "judge");
        caseDetailsMap.put("judgeNameAndEmail", "test");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder()
                .judicialUser(JudicialUser.builder().build()).build(),
            "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeGatekeeping() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "test");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(
            DynamicList
                .builder()
                .value(DynamicListElement.builder().code("(test)").build())
                .build());
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder().build(),
                                                   "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeGatekeepingWithName() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "judge");
        caseDetailsMap.put("judgeNameAndEmail", "test");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder().build(),
                                                   "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeGatekeepingWithEmail() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "judge");
        caseDetailsMap.put("judgeName", "test");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder().build(),
                                                   "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentIsJudgeNotNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviser", "");
        caseDetailsMap.put("legalAdviserList", "");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(
            DynamicList
                .builder()
                .value(DynamicListElement.builder().code("(test)").build())
                .build());
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder().build(),
                                                   "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentNameOfJudgeToReviewOrderNotNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("nameOfJudgeToReviewOrder", "Test");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(
            DynamicList
                .builder()
                .value(DynamicListElement.builder().code("(test)").build())
                .build());
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder().build(),
                                                   "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentNameOfLaToReviewOrderNotNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "Yes");
        caseDetailsMap.put("legalAdviserList", "");
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(
            DynamicList
                .builder()
                .value(DynamicListElement.builder().code("(test)").build())
                .build());
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, RoleAssignmentDto.builder().build(),
                                                   "TEST EVENT", true, "Judge"
        );
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testValidateIfUserHasRightRole() {
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName(HEARING_JUDGE_ROLE);
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);

        when(roleAssignmentApi.getRoleAssignments(auth, authTokenGenerator.generate(), null, "123")).thenReturn(roleAssignmentServiceResponse);

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails.toBuilder().data(stringObjectMap).build()).build();
        boolean bool = roleAssignmentService.validateIfUserHasRightRoles(auth, callbackRequest);
        assertTrue(bool);
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
        assertFalse(bool);
    }

    @Test
    public void testFetchIdamAmRoles() {
        List<String> roles = new ArrayList<>();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        String emailId = "test@email.com";
        when(userService.getUserByEmailId(auth,emailId)).thenReturn(List.of(userDetails));
        List<RoleAssignmentResponse> listOfRoleAssignmentResponses = new ArrayList<>();
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName("test");
        roleAssignmentResponse.setRoleCategory("test");
        listOfRoleAssignmentResponses.add(roleAssignmentResponse);
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = new RoleAssignmentServiceResponse();
        roleAssignmentServiceResponse.setRoleAssignmentResponse(listOfRoleAssignmentResponses);

        when(roleAssignmentApi.getRoleAssignments(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(roleAssignmentServiceResponse);

        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        callbackRequest = CallbackRequest.builder().caseDetails(caseDetails.toBuilder().data(stringObjectMap).build()).build();
        Map<String, String> rolesResponse = roleAssignmentService.fetchIdamAmRoles(auth, emailId);
        assertFalse(rolesResponse.isEmpty());
    }

    @Test
    public void testRoleAssignmentForCase() {
        when(systemUserService.getSysUserToken())
            .thenReturn("auth");
        when(authTokenGenerator.generate())
            .thenReturn("test");
        when(roleAssignmentApi.queryRoleAssignments(anyString(), anyString(), any(), isA(
            RoleAssignmentQueryRequest.class)))
            .thenReturn(RoleAssignmentServiceResponse.builder().build());
        roleAssignmentService.getRoleAssignmentForCase("1234");
        verify(roleAssignmentApi).queryRoleAssignments(anyString(), anyString(), any(), isA(
            RoleAssignmentQueryRequest.class));
    }

    @Test
    public void testCreateRoleAssignment() {
        when(systemUserService.getSysUserToken()).thenReturn("auth");
        when(authTokenGenerator.generate()).thenReturn("test");

        roleAssignmentService.createRoleAssignment(
            "1234", "idamId-123", RoleCategory.JUDICIAL,
            Roles.JUDGE.getValue(), true
        );

        verify(roleAssignmentApi).updateRoleAssignment(
            anyString(),
            anyString(),
            eq(null),
            any(RoleAssignmentRequest.class)
        );
    }

    @Test
    public void givenUserNotAllocated_whenIsUserAllocatedRoleForCase_thenReturnFalse() {
        List<RoleAssignmentResponse> roleAssignments = List.of(
            roleAssignmentResponse("some-other-idam-id", "some-other-role"),
            roleAssignmentResponse("some-other-idam-id-2", "some-other-role-2")
        );
        RoleAssignmentServiceResponse response = RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(roleAssignments)
            .build();
        when(systemUserService.getSysUserToken()).thenReturn("some-token");
        when(authTokenGenerator.generate()).thenReturn("some-service-auth-token");
        when(roleAssignmentApi.queryRoleAssignments(
            eq("some-token"), eq("some-service-auth-token"), eq(null),
            any(RoleAssignmentQueryRequest.class)
        )).thenReturn(response);

        boolean result = roleAssignmentService.isUserAllocatedRoleForCase("1234", "user-123", "some-role");

        assertFalse(result);
    }

    @Test
    public void givenUserAlreadyAllocated_whenIsUserAllocatedRoleForCase_thenReturnTrue() {
        List<RoleAssignmentResponse> roleAssignments = List.of(
            roleAssignmentResponse("some-other-idam-id", "some-other-role"),
            roleAssignmentResponse("user-123", "some-role")
        );
        RoleAssignmentServiceResponse response = RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(roleAssignments)
            .build();
        when(systemUserService.getSysUserToken()).thenReturn("some-token");
        when(authTokenGenerator.generate()).thenReturn("some-service-auth-token");
        when(roleAssignmentApi.queryRoleAssignments(
            eq("some-token"), eq("some-service-auth-token"), eq(null),
            any(RoleAssignmentQueryRequest.class)
        )).thenReturn(response);

        boolean result = roleAssignmentService.isUserAllocatedRoleForCase("1234", "user-123", "some-role");

        assertTrue(result);
    }

    private RoleAssignmentResponse roleAssignmentResponse(String idamId, String roleName) {
        RoleAssignmentResponse response = new RoleAssignmentResponse();
        response.setActorId(idamId);
        response.setRoleName(roleName);
        return response;
    }
}
