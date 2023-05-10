package uk.gov.hmcts.reform.prl.controllers.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.gatekeeping.AllocatedJudgeService;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;


@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class AllocateJudgeControllerTest {

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

    @Test
    public void shouldSeeLegalAdvisorDetails() throws Exception {
        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
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
        when(refDataUserService.getLegalAdvisorList()).thenReturn(List.of(DynamicListElement.builder().build()));
        AboutToStartOrSubmitCallbackResponse response = allocateJudgeController.prePopulateLegalAdvisorDetails(
            "auth",callbackRequest);
        assertFalse(response.getData().containsKey("legalAdvisorList"));
    }


    @Test
    public void shouldSeeAllocatedJudgeDetailsInSummaryTab() throws Exception {

        AllocatedJudge allocatedJudge = AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No)
            .tierOfJudiciary(TierOfJudiciaryEnum.DISTRICT_JUDGE)
            .build();

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
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
        when(allocatedJudgeService.getAllocatedJudgeDetails(caseDataUpdated, caseData.getLegalAdviserList(), refDataUserService)).thenReturn(
            allocatedJudge);

        when(caseSummaryTabService.updateTab(caseData)).thenReturn(summaryTabFields);

        assertNotNull(allocateJudgeController.allocateJudge("auth",callbackRequest));

    }

}
