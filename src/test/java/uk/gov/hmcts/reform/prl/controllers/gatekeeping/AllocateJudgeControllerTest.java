package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


@Slf4j
@ExtendWith(MockitoExtension.class)
class AllocateJudgeControllerTest {

    @InjectMocks
    private AllocateJudgeController allocateJudgeController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    AllocatedJudgeService allocatedJudgeService;

    @Mock
    @Qualifier("caseSummaryTab")
    CaseSummaryTabService caseSummaryTabService;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    RoleAssignmentService roleAssignmentService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    void shouldSeeLegalAdvisorDetails() {
        CaseData caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = allocateJudgeController.prePopulateLegalAdvisorDetails(
            AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        assertFalse(response.getData().containsKey("legalAdvisorList"));
    }


    @Test
    void shouldSeeAllocatedJudgeDetailsInSummaryTab() throws Exception {

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.districtJudge)
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .allocatedJudge(allocatedJudge)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(allocatedJudgeService.getAllocatedJudgeDetails(
            caseDataUpdated,
            caseData.getLegalAdviserList(),
            refDataUserService
        )).thenReturn(
            allocatedJudge);

        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        assertNotNull(allocateJudgeController.allocateJudge(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

    }

    @Test
    void shouldSeeAllocatedJudgeDetailsInSummaryTabIfSpecificJudgeSelected() throws Exception {

        DynamicList legalAdviserList = DynamicList.builder().value(DynamicListElement.builder()
                                                                       .code("test1(test1@test.com)").label(
                "test1(test1@test.com)").build()).build();

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes)
            .tierOfJudiciary(TierOfJudiciaryEnum.districtJudge)
            .judgeEmail("testEmail")
            .legalAdviserList(legalAdviserList)
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .allocatedJudge(allocatedJudge)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        when(allocatedJudgeService.getAllocatedJudgeDetails(
            caseDataUpdated,
            caseData.getLegalAdviserList(),
            refDataUserService
        )).thenReturn(
            allocatedJudge);

        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        assertNotNull(allocateJudgeController.allocateJudge(AUTH_TOKEN, S2S_TOKEN, callbackRequest));

    }

    @Test
    void testExceptionForPrePopulateLegalAdvisorDetails() {

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.districtJudge)
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .allocatedJudge(allocatedJudge)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () ->
                allocateJudgeController.prePopulateLegalAdvisorDetails(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForAllocatedJudge() {

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.districtJudge)
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .allocatedJudge(allocatedJudge)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> allocateJudgeController.allocateJudge(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
