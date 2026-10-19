package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.GatekeepingTaskTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.JudgeOrLegalAdviserGatekeepingEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.WhoToSendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.common.staff.StaffUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.validators.LegalAdviserChecker;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GatekeepingDetailsServiceTest {

    @InjectMocks
    GatekeepingDetailsService gatekeepingDetailsService;

    @Mock
    ObjectMapper objectMapper;
    Object idamId;

    @Mock
    LegalAdviserChecker legalAdviserChecker;

    @Test
    public void testGatekeepingWhenLegalAdvisorDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        stringObjectMap.put("whoToSendTheCaseToForGatekeeping",
                            WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_LEGAL_ADVISER);
        stringObjectMap.put("legalAdviser", StaffUser.builder().idamId("test1(test1@test.com)").build());

        GatekeepingDetails expectedResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,null,null);
        assertEquals(SendToGatekeeperTypeEnum.legalAdviser, expectedResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(YesOrNo.Yes, expectedResponse.getIsSpecificGateKeeperNeeded());
        assertNotNull(expectedResponse.getLegalAdviser());
        assertEquals("test1(test1@test.com)", expectedResponse.getLegalAdviser().getIdamId());
    }

    @Test
    public void testGatekeepingWhenLegalAdvisorDetailsEmpty() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("whoToSendTheCaseToForGatekeeping",
                            WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_LEGAL_ADVISER);
        stringObjectMap.put("legalAdviser", null);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        GatekeepingDetails actualResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,null,null);
        assertNotNull(actualResponse);
        assertNull(actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertNull(actualResponse.getIsSpecificGateKeeperNeeded());
        assertNull(actualResponse.getLegalAdviser());
    }

    @Test
    public void testGatekeepingWhenLegalAdvisorDetailsProvidedButGatekeepingOptionWrong() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("whoToSendTheCaseToForGatekeeping", WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_JUDGE);
        stringObjectMap.put("legalAdviser", StaffUser.builder().idamId("test1(test1@test.com)").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        GatekeepingDetails actualResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,null,null);
        assertNotNull(actualResponse);
        assertNull(actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertNull(actualResponse.getIsSpecificGateKeeperNeeded());
        assertNull(actualResponse.getLegalAdviser());
    }

    @Test
    public void testGatekeepingWhenJudgeDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("whoToSendTheCaseToForGatekeeping", WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_JUDGE);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        stringObjectMap.put(JURISDICTION, JURISDICTION);
        stringObjectMap.put(CASE_TYPE, CASE_TYPE);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        GatekeepingDetails actualResponse = gatekeepingDetailsService
            .getGatekeepingDetails(stringObjectMap,null);
        assertNotNull(actualResponse);
        assertEquals(SendToGatekeeperTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificGateKeeperNeeded());
        assertEquals(new JudicialUser((String) (idamId = "123"), personalCodes[0]), actualResponse.getJudgeName());
    }

    @Test
    public void testGatekeepingWhenJudgeDetailsNotProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("whoToSendTheCaseToForGatekeeping", WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_JUDGE);
        stringObjectMap.put(JURISDICTION, JURISDICTION);
        stringObjectMap.put(CASE_TYPE, CASE_TYPE);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        GatekeepingDetails actualResponse = gatekeepingDetailsService
            .getGatekeepingDetails(stringObjectMap,null);
        assertNotNull(actualResponse);
        assertNull(actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertNull(actualResponse.getIsSpecificGateKeeperNeeded());
        assertNull(actualResponse.getJudgeName());
    }

    @Test
    public void testGatekeepingWhenJudgeDetailsEmpty() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("whoToSendTheCaseToForGatekeeping", WhoToSendToGatekeeperTypeEnum.SEND_TO_A_SPECIFIC_JUDGE);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId(null).personalCode(null).build());
        stringObjectMap.put(JURISDICTION, JURISDICTION);
        stringObjectMap.put(CASE_TYPE, CASE_TYPE);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        GatekeepingDetails actualResponse = gatekeepingDetailsService
            .getGatekeepingDetails(stringObjectMap,null);
        assertNotNull(actualResponse);
        assertEquals(SendToGatekeeperTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificGateKeeperNeeded());
        assertNull(actualResponse.getJudgeName());
    }

    @Test
    public void testIdentifyGatekeepingTaskTypeJudgeOrLegalAdviser() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .judgeOrLegalAdviserForGatekeeping(List.of(
                JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_JUDGE,
                JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_LEGAL_ADVISER
            )).build();

        GatekeepingTaskTypeEnum taskType = gatekeepingDetailsService.identifyGatekeepingTaskType(caseData);
        assertEquals(GatekeepingTaskTypeEnum.JUDGE_OR_LEGAL_ADVISER, taskType);
    }

    @Test
    public void testIdentifyGatekeepingTaskTypeJudge() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .judgeOrLegalAdviserForGatekeeping(List.of(JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_JUDGE))
            .build();

        GatekeepingTaskTypeEnum taskType = gatekeepingDetailsService.identifyGatekeepingTaskType(caseData);
        assertEquals(GatekeepingTaskTypeEnum.JUDGE, taskType);
    }

    @Test
    public void testIdentifyGatekeepingTaskTypeLegalAdviser() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .judgeOrLegalAdviserForGatekeeping(List.of(JudgeOrLegalAdviserGatekeepingEnum.SEND_TO_A_LEGAL_ADVISER))
            .build();

        GatekeepingTaskTypeEnum taskType = gatekeepingDetailsService.identifyGatekeepingTaskType(caseData);
        assertEquals(GatekeepingTaskTypeEnum.LEGAL_ADVISER, taskType);
    }
}
