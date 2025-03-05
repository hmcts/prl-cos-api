package uk.gov.hmcts.reform.prl.services.reopenclosedcases;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.closingcase.CaseClosingReasonEnum;
import uk.gov.hmcts.reform.prl.enums.reopenclosedcases.ValidReopenClosedCasesStatusEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.closingcase.CaseClosingReasonForChildren;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.closingcase.ClosingCaseService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FINAL_CASE_CLOSED_DATE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.services.reopenclosedcases.ReopenClosedCasesService.REOPEN_STATE_TO;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ReopenClosedCasesServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CaseSummaryTabService caseSummaryTab;

    @Mock
    private ClosingCaseService closingCaseService;

    @InjectMocks
    ReopenClosedCasesService reopenClosedCasesService;

    @Test
    public void testReopenClosedCasesForC100V3() {
        List<Element<ChildDetailsRevised>> newChildDetails = new ArrayList<>();
        ChildDetailsRevised childDetailsRevised1 = ChildDetailsRevised.builder()
            .firstName("Tom")
            .lastName("Doe")
            .build();

        newChildDetails.add(element(UUID.fromString(TEST_UUID), childDetailsRevised1));

        CaseData caseData = CaseData.builder()
            .newChildDetails(newChildDetails)
            .taskListVersion(TASK_LIST_VERSION_V3)
            .changeStatusOptions(ValidReopenClosedCasesStatusEnum.CASE_ISSUED)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = reopenClosedCasesService.reopenClosedCases(callbackRequest);
        assertTrue(response.containsKey(FINAL_CASE_CLOSED_DATE));
        assertNull(response.get(CASE_CLOSED));
        assertEquals(response.get(REOPEN_STATE_TO), ValidReopenClosedCasesStatusEnum.CASE_ISSUED.toString());
    }

    @Test
    public void testReopenClosedCasesForC100V2() {
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

        CaseData caseData = CaseData.builder()
            .children(children)
            .taskListVersion(TASK_LIST_VERSION_V2)
            .changeStatusOptions(ValidReopenClosedCasesStatusEnum.CASE_ISSUED)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = reopenClosedCasesService.reopenClosedCases(callbackRequest);
        assertTrue(response.containsKey(FINAL_CASE_CLOSED_DATE));
        assertNull(response.get(CASE_CLOSED));
        assertEquals(response.get(REOPEN_STATE_TO), ValidReopenClosedCasesStatusEnum.CASE_ISSUED.toString());
    }

    @Test
    public void testReopenClosedCasesForFL401() {
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
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .changeStatusOptions(ValidReopenClosedCasesStatusEnum.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .data(caseData.toMap(new ObjectMapper()))
                             .build())
            .build();
        Map<String, Object> response = reopenClosedCasesService.reopenClosedCases(callbackRequest);
        assertTrue(response.containsKey(FINAL_CASE_CLOSED_DATE));
        assertNull(response.get(CASE_CLOSED));
        assertEquals(response.get(REOPEN_STATE_TO), ValidReopenClosedCasesStatusEnum.PREPARE_FOR_HEARING_CONDUCT_HEARING.toString());

    }
}
