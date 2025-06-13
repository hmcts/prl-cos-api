package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.judicial.Appointment;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.IS_SPECIFIC_JUDGE_OR_LA_NEEDED;

@ExtendWith(MockitoExtension.class)
class AllocatedJudgeServiceTest {

    @InjectMocks
    AllocatedJudgeService allocatedJudgeService;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    ObjectMapper objectMapper;

    @Test
    void testWhenTierOfJudiciaryWithCircuitJudgeOptionProvided() {
        CaseData caseData = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("tierOfJudiciary","circuitJudge");
        stringObjectMap.put(IS_SPECIFIC_JUDGE_OR_LA_NEEDED,"No");
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,null);

        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.circuitJudge.getDisplayedValue());
    }

    @Test
    void testWhenTierOfJudiciaryDetailsWithDistrictJudgeOptionProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("tierOfJudiciary","districtJudge");
        stringObjectMap.put(IS_SPECIFIC_JUDGE_OR_LA_NEEDED,"No");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,null);
        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.districtJudge.getDisplayedValue());

    }

    @Test
    void testWhenTierOfJudiciaryDetailsWithHighCourtJudgeOptionProvided() {
        CaseData caseData = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("tierOfJudiciary","highCourtJudge");
        stringObjectMap.put(IS_SPECIFIC_JUDGE_OR_LA_NEEDED,"No");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,null);

        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.highCourtJudge.getDisplayedValue());
    }

    @Test
    void testWhenTierOfJudiciaryDetailsWithMagistratesOptionProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("tierOfJudiciary","magistrates");
        stringObjectMap.put(IS_SPECIFIC_JUDGE_OR_LA_NEEDED,"No");
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,null);

        assertEquals(expectedResponse.getTierOfJudiciary().getDisplayedValue(),TierOfJudiciaryEnum.magistrates.getDisplayedValue());
    }

    @Test
    void testWhenLegalAdvisorDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviser", AllocatedJudgeTypeEnum.legalAdviser);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        DynamicList legalAdviserList = DynamicList.builder().value(DynamicListElement.builder()
            .code("test1(test1@test.com)").label("test1(test1@test.com)").build()).build();
        AllocatedJudge expectedResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,legalAdviserList,null);

        assertEquals(AllocatedJudgeTypeEnum.legalAdviser,expectedResponse.getIsJudgeOrLegalAdviser());
        assertNotNull(expectedResponse.getLegalAdviserList());
    }

    @Test
    void testWhenJudgeDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname")
                                .appointments(List.of(Appointment.builder().appointment("Circuit Judge").build()))
                                .build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviser", AllocatedJudgeTypeEnum.judge);
        stringObjectMap.put("judgeNameAndEmail", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
            .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        AllocatedJudge actualResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,refDataUserService);

        assertNotNull(actualResponse);
        assertEquals(AllocatedJudgeTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviser());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificJudgeOrLegalAdviserNeeded());
        assertEquals("test@Email.com",actualResponse.getJudgeEmail());
        assertEquals("testSurname",actualResponse.getJudgeName());
        assertEquals("Circuit Judge", actualResponse.getTierOfJudge());
    }

    @Test
    void testWhenJudgeTierNotPresent() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname")
                                .build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviser", AllocatedJudgeTypeEnum.judge);
        stringObjectMap.put("judgeNameAndEmail", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
                                                              .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        AllocatedJudge actualResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,refDataUserService);

        assertNotNull(actualResponse);
        assertEquals(AllocatedJudgeTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviser());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificJudgeOrLegalAdviserNeeded());
        assertEquals("test@Email.com",actualResponse.getJudgeEmail());
        assertEquals("testSurname",actualResponse.getJudgeName());
        assertNull(actualResponse.getTierOfJudge());
    }

    @Test
    void testWhenJudgeDetailsProvidedError() {
        CaseData caseData = CaseData.builder().caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviser", AllocatedJudgeTypeEnum.judge);
        stringObjectMap.put("judgeNameAndEmail", JudicialUser.builder().idamId("123").personalCode("123456").build());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().build())).thenReturn(apiResponseList);

        AllocatedJudge actualResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null,refDataUserService);

        assertNotNull(actualResponse);
        assertEquals(AllocatedJudgeTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviser());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificJudgeOrLegalAdviserNeeded());
    }

    @Test
    void testWhenJudgeDetailsIsNull() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("isJudgeOrLegalAdviser", AllocatedJudgeTypeEnum.judge);
        stringObjectMap.put("judgeNameAndEmail", "");
        when(refDataUserService.getAllJudicialUserDetails(Mockito.any())).thenReturn(null);
        AllocatedJudge actualResponse = allocatedJudgeService.getAllocatedJudgeDetails(stringObjectMap,null, refDataUserService);

        assertNotNull(actualResponse);
        assertEquals(AllocatedJudgeTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviser());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificJudgeOrLegalAdviserNeeded());

    }
}
