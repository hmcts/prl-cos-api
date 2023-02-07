package uk.gov.hmcts.reform.prl.services.gatekeeping;


import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AllocatedJudgeServiceTest {

    @InjectMocks
    AllocatedJudgeService allocatedJudgeService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    ObjectMapper objectMapper;

    @Test
    public void testWhenTierOfJudiciaryDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("tierOfJudiciary","circuitJudge");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,null);
        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.CIRCUIT_JUDGE.getDisplayedValue());

    }
    @Test
    public void testWhenLegalAdvisorDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviser", AllocatedJudgeTypeEnum.LEGAL_ADVISER);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        DynamicList legalAdviserList =DynamicList.builder().value(DynamicListElement.builder().code("test1(test1@test.com)").label("test1(test1@test.com)").build()).build();
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,legalAdviserList,null);
        assertEquals(AllocatedJudgeTypeEnum.LEGAL_ADVISER,expectedResponse.getIsJudgeOrLegalAdviser());
        assertNotNull(expectedResponse.getLegalAdviserList());
    }
}
