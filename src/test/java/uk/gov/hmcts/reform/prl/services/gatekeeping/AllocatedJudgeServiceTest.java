package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    public void testWhenTierOfJudiciaryWithCircuitJudgeOptionProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(AllocatedJudge.builder()
            .tierOfJudiciary(TierOfJudiciaryEnum.CIRCUIT_JUDGE).isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No).build(),null);
        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.CIRCUIT_JUDGE.getDisplayedValue());
    }

    @Test
    public void testWhenTierOfJudiciaryDetailsWithDistrictJudgeOptionProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(AllocatedJudge.builder()
            .tierOfJudiciary(TierOfJudiciaryEnum.DISTRICT_JUDGE).isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No).build(),null);
        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.DISTRICT_JUDGE.getDisplayedValue());

    }

    @Test
    public void testWhenTierOfJudiciaryDetailsWithHighCourtJudgeOptionProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(AllocatedJudge.builder()
            .tierOfJudiciary(TierOfJudiciaryEnum.HIGHCOURT_JUDGE).isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No).build(),null);
        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.HIGHCOURT_JUDGE.getDisplayedValue());

    }

    @Test
    public void testWhenTierOfJudiciaryDetailsWithMagistratesOptionProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(AllocatedJudge.builder()
            .tierOfJudiciary(TierOfJudiciaryEnum.MAGISTRATES).isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.No).build(),null);
        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.MAGISTRATES.getDisplayedValue());
        assertEquals(YesOrNo.No,expectedResponse.getIsSpecificJudgeOrLegalAdviserNeeded());

    }

    @Test
    public void testWhenLegalAdvisorDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        DynamicList legalAdviserList = DynamicList.builder().value(DynamicListElement.builder()
            .code("test1(test1@test.com)").label("test1(test1@test.com)").build()).build();
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(AllocatedJudge.builder()
            .isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.LEGAL_ADVISER).legalAdviserList(legalAdviserList).build(),null);
        assertEquals(AllocatedJudgeTypeEnum.LEGAL_ADVISER,expectedResponse.getIsJudgeOrLegalAdviser());
        assertNotNull(expectedResponse.getLegalAdviserList());
    }

    @Test
    public void testWhenJudgeDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
            .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        AllocatedJudge actualResponse = allocatedJudgeService.getAllocatedJudgeDetails(AllocatedJudge.builder()
            .isSpecificJudgeOrLegalAdviserNeeded(YesOrNo.Yes)
            .isJudgeOrLegalAdviser(AllocatedJudgeTypeEnum.JUDGE).judgeDetails(JudicialUser.builder()
                .personalCode("123456").build()).build(), refDataUserService);
        assertNotNull(actualResponse);
        assertEquals(AllocatedJudgeTypeEnum.JUDGE, actualResponse.getIsJudgeOrLegalAdviser());
        assertEquals(YesOrNo.Yes, actualResponse.getIsSpecificJudgeOrLegalAdviserNeeded());
        assertEquals("test@Email.com", actualResponse.getJudgeEmail());
        assertEquals("testSurname", actualResponse.getJudgeName());

    }
}