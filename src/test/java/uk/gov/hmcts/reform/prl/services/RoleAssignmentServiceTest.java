package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARING_JUDGE_ROLE;

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

        Map<String, Object> dataMap = new HashMap<>();
        caseDetails = CaseDetails.builder().data(dataMap).id(123L).build();
    }

    @Test
    public void testCreateRoleAssignmentActorIdIsNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeWithName() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviser", "judge");
        caseDetailsMap.put("judgeName", "test");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeWithEmail() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviser", "judge");
        caseDetailsMap.put("judgeNameAndEmail", "test");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeGatekeeping() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "test");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(DynamicList
            .builder()
            .value(DynamicListElement.builder().code("(test)").build())
            .build());
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeGatekeepingWithName() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "judge");
        caseDetailsMap.put("judgeNameAndEmail", "test");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentJudgeGatekeepingWithEmail() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "judge");
        caseDetailsMap.put("judgeName", "test");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);

        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentIsJudgeNotNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviser", "");
        caseDetailsMap.put("legalAdviserList", "");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(DynamicList
            .builder()
            .value(DynamicListElement.builder().code("(test)").build())
            .build());
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentNameOfJudgeToReviewOrderNotNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("nameOfJudgeToReviewOrder", "Test");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(DynamicList
            .builder()
            .value(DynamicListElement.builder().code("(test)").build())
            .build());
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
        assertEquals("1", userDetails.getId());
    }

    @Test
    public void testCreateRoleAssignmentNameOfLaToReviewOrderNotNull() {
        Map<String, Object> caseDetailsMap = new HashMap<>();
        caseDetailsMap.put("isJudgeOrLegalAdviserGatekeeping", "Yes");
        caseDetailsMap.put("legalAdviserList", "");
        List<String> roles = new ArrayList();
        roles.add("caseworker-privatelaw-judge");
        userDetails = UserDetails.builder().id("1").roles(roles).build();
        caseDetails.setData(caseDetailsMap);
        when(objectMapper.convertValue(caseDetailsMap.get("legalAdviserList"), DynamicList.class)).thenReturn(DynamicList
            .builder()
            .value(DynamicListElement.builder().code("(test)").build())
            .build());
        when(userService.getUserDetails(auth)).thenReturn(userDetails);
        when(userService.getUserByEmailId(auth, "test")).thenReturn(List.of(userDetails));
        when(authTokenGenerator.generate()).thenReturn("test");
        roleAssignmentService.createRoleAssignment(auth, caseDetails, true, "Judge");
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
