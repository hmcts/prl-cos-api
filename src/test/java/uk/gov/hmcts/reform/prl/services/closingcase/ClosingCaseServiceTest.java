package uk.gov.hmcts.reform.prl.services.closingcase;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.RoleAssignmentApi;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.closingcase.CaseClosingReasonEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.CaseClosingReasonForChildren;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.DateFinalDecisionWasMade;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.closingcases.ClosingCaseOptions;
import uk.gov.hmcts.reform.prl.models.roleassignment.addroleassignment.RoleAssignmentQueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.deleteroleassignment.RoleAssignmentDeleteQueryRequest;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabServiceHelper;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_DETAILS_REVISED_TABLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_CASE_CLOSED_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService.CHILD_OPTIONS_FOR_FINAL_DECISION;
import static uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService.FINAL_OUTCOME_FOR_CHILDREN;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ClosingCaseServiceTest {

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ApplicationsTabService applicationsTabService;
    @Mock
    private ApplicationsTabServiceHelper applicationsTabServiceHelper;
    @Mock
    private CaseSummaryTabService caseSummaryTab;
    @Mock
    private RoleAssignmentApi roleAssignmentApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    ClosingCaseService closingCaseService;

    @Test
    public void testPrePopulateChildDataForC100() {
        List<Element<ChildDetailsRevised>> newChildDetails = new ArrayList<>();
        ChildDetailsRevised childDetailsRevised1 = ChildDetailsRevised.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();
        ChildDetailsRevised childDetailsRevised2 = ChildDetailsRevised.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        newChildDetails.add(element(childDetailsRevised1));
        newChildDetails.add(element(childDetailsRevised2));
        CaseData caseData = CaseData.builder()
            .taskListVersion(TASK_LIST_VERSION_V3)
            .newChildDetails(newChildDetails)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.prePopulateChildData(callbackRequest);
        assertTrue(response.containsKey(CHILD_OPTIONS_FOR_FINAL_DECISION));
        DynamicMultiSelectList dynamicMultiSelectList = (DynamicMultiSelectList) response.get(CHILD_OPTIONS_FOR_FINAL_DECISION);
        assertEquals(dynamicMultiSelectList.getListItems().size(), 2);
    }

    @Test
    public void testPrePopulateChildDataForFL401() {
        List<Element<ApplicantChild>> applicantChildDetail = new ArrayList<>();
        ApplicantChild child1 = ApplicantChild.builder()
            .fullName("Tom")
            .build();
        ApplicantChild child2 = ApplicantChild.builder()
            .fullName("Jerry")
            .build();
        applicantChildDetail.add(element(child1));
        applicantChildDetail.add(element(child2));
        CaseData caseData = CaseData.builder()
            .applicantChildDetails(applicantChildDetail)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.prePopulateChildData(callbackRequest);
        assertTrue(response.containsKey(CHILD_OPTIONS_FOR_FINAL_DECISION));
        DynamicMultiSelectList dynamicMultiSelectList = (DynamicMultiSelectList) response.get(CHILD_OPTIONS_FOR_FINAL_DECISION);
        assertEquals(dynamicMultiSelectList.getListItems().size(), 2);
    }

    @Test
    public void testPrePopulateChildDataForAlreadyClosedChildren() {
        List<Element<ChildDetailsRevised>> newChildDetails = new ArrayList<>();
        ChildDetailsRevised childDetailsRevised1 = ChildDetailsRevised.builder()
            .firstName("Tom")
            .lastName("Doe")
            .finalDecisionResolutionReason("test")
            .build();
        ChildDetailsRevised childDetailsRevised2 = ChildDetailsRevised.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        newChildDetails.add(element(childDetailsRevised1));
        newChildDetails.add(element(childDetailsRevised2));
        CaseData caseData = CaseData.builder()
            .taskListVersion(TASK_LIST_VERSION_V3)
            .newChildDetails(newChildDetails)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.prePopulateChildData(callbackRequest);
        assertTrue(response.containsKey(CHILD_OPTIONS_FOR_FINAL_DECISION));
        DynamicMultiSelectList dynamicMultiSelectList = (DynamicMultiSelectList) response.get(CHILD_OPTIONS_FOR_FINAL_DECISION);
        assertEquals(dynamicMultiSelectList.getListItems().size(), 1);
    }

    @Test
    public void testPopulateSelectedChildWithFinalOutcomeForC100V3() {

        List<Element<ChildDetailsRevised>> newChildDetails = new ArrayList<>();
        ChildDetailsRevised childDetailsRevised1 = ChildDetailsRevised.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();
        ChildDetailsRevised childDetailsRevised2 = ChildDetailsRevised.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        newChildDetails.add(element(childDetailsRevised1));
        newChildDetails.add(element(childDetailsRevised2));
        CaseData caseData = CaseData.builder()
            .newChildDetails(newChildDetails)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .isTheDecisionAboutAllChildren(YesOrNo.Yes)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.populateSelectedChildWithFinalOutcome(callbackRequest);
        assertTrue(response.containsKey(FINAL_OUTCOME_FOR_CHILDREN));
        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren =
            (List<Element<CaseClosingReasonForChildren>>) response.get(FINAL_OUTCOME_FOR_CHILDREN);
        assertEquals(finalOutcomeForChildren.size(), 2);
    }

    @Test
    public void testPopulateSelectedChildWithFinalOutcomeForC100V2() {

        List<Element<Child>> children = new ArrayList<>();
        Child child1 = Child.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();
        Child child2 = Child.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        children.add(element(child1));
        children.add(element(child2));
        CaseData caseData = CaseData.builder()
            .children(children)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .isTheDecisionAboutAllChildren(YesOrNo.Yes)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.populateSelectedChildWithFinalOutcome(callbackRequest);
        assertTrue(response.containsKey(FINAL_OUTCOME_FOR_CHILDREN));
        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren =
            (List<Element<CaseClosingReasonForChildren>>) response.get(FINAL_OUTCOME_FOR_CHILDREN);
        assertEquals(finalOutcomeForChildren.size(), 2);
    }

    @Test
    public void testPopulateSelectedChildWithFinalOutcomeForFL401() {

        List<Element<ApplicantChild>> applicantChildDetail = new ArrayList<>();
        ApplicantChild child1 = ApplicantChild.builder()
            .fullName("Tom")
            .build();
        ApplicantChild child2 = ApplicantChild.builder()
            .fullName("Jerry")
            .build();
        applicantChildDetail.add(element(UUID.fromString(TEST_UUID), child1));
        applicantChildDetail.add(element(child2));

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        CaseData caseData = CaseData.builder()
            .applicantChildDetails(applicantChildDetail)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .isTheDecisionAboutAllChildren(YesOrNo.No)
                                    .childOptionsForFinalDecision(dynamicMultiSelectList)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.populateSelectedChildWithFinalOutcome(callbackRequest);
        assertTrue(response.containsKey(FINAL_OUTCOME_FOR_CHILDREN));
        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren =
            (List<Element<CaseClosingReasonForChildren>>) response.get(FINAL_OUTCOME_FOR_CHILDREN);
        assertEquals(finalOutcomeForChildren.size(), 1);
    }

    @Test
    public void testValidateChildDetailsForAllChildren() {
        List<Element<Child>> children = new ArrayList<>();
        Child child1 = Child.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();
        Child child2 = Child.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        children.add(element(child1));
        children.add(element(child2));

        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        CaseClosingReasonForChildren caseClosingReasonForChildren1 = CaseClosingReasonForChildren.builder().build();
        CaseClosingReasonForChildren caseClosingReasonForChildren2 = CaseClosingReasonForChildren.builder().build();
        finalOutcomeForChildren.add(element(caseClosingReasonForChildren1));
        finalOutcomeForChildren.add(element(caseClosingReasonForChildren2));
        CaseData caseData = CaseData.builder()
            .taskListVersion(TASK_LIST_VERSION_V2)
            .children(children)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .finalOutcomeForChildren(finalOutcomeForChildren)
                                    .isTheDecisionAboutAllChildren(YesOrNo.Yes)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        List<String> errorList = closingCaseService.validateChildDetails(callbackRequest);
        assertTrue(errorList.isEmpty());
    }

    @Test
    public void testValidateChildDetails() {
        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label("test")
            .build();
        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().listItems(List.of(dynamicMultiselectListElement))
            .value(List.of(dynamicMultiselectListElement))
            .build();

        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        CaseClosingReasonForChildren caseClosingReasonForChildren1 = CaseClosingReasonForChildren.builder().build();
        CaseClosingReasonForChildren caseClosingReasonForChildren2 = CaseClosingReasonForChildren.builder().build();
        finalOutcomeForChildren.add(element(caseClosingReasonForChildren1));
        finalOutcomeForChildren.add(element(caseClosingReasonForChildren2));
        CaseData caseData = CaseData.builder()
            .taskListVersion(TASK_LIST_VERSION_V3)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .finalOutcomeForChildren(finalOutcomeForChildren)
                                    .isTheDecisionAboutAllChildren(YesOrNo.No)
                                    .childOptionsForFinalDecision(dynamicMultiSelectList)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        List<String> errorList = closingCaseService.validateChildDetails(callbackRequest);
        assertFalse(errorList.isEmpty());
        assertEquals(errorList.getFirst(), "Children details are altered");
    }

    @Test
    public void testClosingCaseForChildrenForC100V3() {
        List<Element<ChildDetailsRevised>> newChildDetails = new ArrayList<>();
        ChildDetailsRevised childDetailsRevised1 = ChildDetailsRevised.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();

        newChildDetails.add(element(UUID.fromString(TEST_UUID), childDetailsRevised1));

        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        CaseClosingReasonForChildren caseClosingReasonForChildren1 = CaseClosingReasonForChildren.builder()
            .caseClosingReason(CaseClosingReasonEnum.consolidation)
            .build();
        finalOutcomeForChildren.add(element(UUID.fromString(TEST_UUID), caseClosingReasonForChildren1));
        CaseData caseData = CaseData.builder()
            .newChildDetails(newChildDetails)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .dateFinalDecisionWasMade(DateFinalDecisionWasMade.builder()
                                                                  .build())
                                    .finalOutcomeForChildren(finalOutcomeForChildren)
                                    .isTheDecisionAboutAllChildren(YesOrNo.Yes)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.closingCaseForChildren(callbackRequest);
        assertTrue(response.containsKey(FINAL_CASE_CLOSED_DATE));
        assertEquals(response.get(CASE_CLOSED), YesOrNo.Yes);
    }

    @Test
    public void testClosingCaseForChildrenForC100V2() {
        List<Element<Child>> children = new ArrayList<>();
        Child child1 = Child.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();
        Child child2 = Child.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        children.add(element(UUID.fromString(TEST_UUID), child1));
        children.add(element(child2));

        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        CaseClosingReasonForChildren caseClosingReasonForChildren1 = CaseClosingReasonForChildren.builder()
            .caseClosingReason(CaseClosingReasonEnum.consolidation)
            .build();
        finalOutcomeForChildren.add(element(UUID.fromString(TEST_UUID), caseClosingReasonForChildren1));
        CaseData caseData = CaseData.builder()
            .children(children)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .dateFinalDecisionWasMade(DateFinalDecisionWasMade.builder()
                                                                  .build())
                                    .finalOutcomeForChildren(finalOutcomeForChildren)
                                    .isTheDecisionAboutAllChildren(YesOrNo.Yes)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.closingCaseForChildren(callbackRequest);
        assertTrue(response.containsKey(FINAL_CASE_CLOSED_DATE));
        assertEquals(response.get(CASE_CLOSED), YesOrNo.Yes);
    }

    @Test
    public void testClosingCaseForChildrenForFL401() {
        List<Element<ApplicantChild>> applicantChildDetail = new ArrayList<>();
        ApplicantChild child1 = ApplicantChild.builder()
            .fullName("Tom")
            .build();

        applicantChildDetail.add(element(UUID.fromString(TEST_UUID), child1));

        List<Element<CaseClosingReasonForChildren>> finalOutcomeForChildren = new ArrayList<>();
        CaseClosingReasonForChildren caseClosingReasonForChildren1 = CaseClosingReasonForChildren.builder()
            .caseClosingReason(CaseClosingReasonEnum.consolidation)
            .build();
        finalOutcomeForChildren.add(element(UUID.fromString(TEST_UUID), caseClosingReasonForChildren1));
        CaseData caseData = CaseData.builder()
            .applicantChildDetails(applicantChildDetail)
            .closingCaseOptions(ClosingCaseOptions.builder()
                                    .dateFinalDecisionWasMade(DateFinalDecisionWasMade.builder()
                                                                  .build())
                                    .finalOutcomeForChildren(finalOutcomeForChildren)
                                    .isTheDecisionAboutAllChildren(YesOrNo.Yes)
                                    .build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = closingCaseService.closingCaseForChildren(callbackRequest);
        assertTrue(response.containsKey(FINAL_CASE_CLOSED_DATE));
        assertEquals(response.get(CASE_CLOSED), YesOrNo.Yes);
    }

    @Test
    public void testUpdateChildDetailsInTab() {
        List<Element<ChildDetailsRevised>> newChildDetails = new ArrayList<>();
        ChildDetailsRevised childDetailsRevised1 = ChildDetailsRevised.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();
        ChildDetailsRevised childDetailsRevised2 = ChildDetailsRevised.builder()
            .firstName("Jerry")
            .lastName("Doe")
            .build();
        newChildDetails.add(element(childDetailsRevised1));
        newChildDetails.add(element(childDetailsRevised2));
        CaseData caseData = CaseData.builder()
            .taskListVersion(TASK_LIST_VERSION_V3)
            .caseTypeOfApplication("C100")
            .newChildDetails(newChildDetails)
            .build();

        Map<String, Object> caseDataUpdated =  caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        closingCaseService.updateChildDetailsInTab(caseDataUpdated, caseData);
        assertTrue(caseDataUpdated.containsKey(CHILD_DETAILS_REVISED_TABLE));
    }

    @Test
    public void testUnAllocateCourtStaffs() {
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(authTokenGenerator.generate()).thenReturn("test");

        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleCategory("PROFESSIONAL");
        roleAssignmentResponse.setActorId("d4c3ec30-cc11-4503-89d1-46b6875b0b8a");
        RoleAssignmentResponse roleAssignmentResponse1 = new RoleAssignmentResponse();
        roleAssignmentResponse1.setRoleCategory("LEGAL_OPERATIONS");
        roleAssignmentResponse1.setActorId("d4c3ec30-cc11-4503-89d1-46b6875b0b8b");
        RoleAssignmentResponse roleAssignmentResponse2 = new RoleAssignmentResponse();
        roleAssignmentResponse2.setRoleCategory("JUDICIAL");
        roleAssignmentResponse2.setActorId("d4c3ec30-cc11-4503-89d1-46b6875b0b8c");
        RoleAssignmentServiceResponse roleAssignmentServiceResponse = RoleAssignmentServiceResponse.builder()
            .roleAssignmentResponse(List.of(roleAssignmentResponse, roleAssignmentResponse1, roleAssignmentResponse2))
            .build();
        when(roleAssignmentApi.queryRoleAssignments(anyString(), anyString(), any(), any(
            RoleAssignmentQueryRequest.class))).thenReturn(roleAssignmentServiceResponse);

        when(roleAssignmentApi.deleteQueryRoleAssignments(anyString(), anyString(), any(),
            any(RoleAssignmentDeleteQueryRequest.class))).thenReturn(ResponseEntity.status(OK).body(OK));

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .legalAdviserList(DynamicList.builder().value(DynamicListElement.EMPTY).build())
            .build();

        Map<String, Object> caseDataUpdated = new HashMap<>();

        closingCaseService.unAllocateCourtStaffs(caseData, caseDataUpdated);
        assertTrue(caseDataUpdated.containsKey("allocatedJudge"));
        assertTrue(caseDataUpdated.containsKey("legalAdviserList"));
    }

}
