package uk.gov.hmcts.reform.prl.services.gatekeeping;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiRequest;
import uk.gov.hmcts.reform.prl.models.dto.judicial.JudicialUsersApiResponse;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;

import java.util.ArrayList;
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
    RefDataUserService refDataUserService;

    @Mock
    RoleAssignmentService roleAssignmentService;

    @Mock
    ObjectMapper objectMapper;
    Object idamId;

    @Test
    public void testGatekeepingWhenLegalAdvisorDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.legalAdviser);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        DynamicList legalAdviserList = DynamicList.builder().value(DynamicListElement.builder()
                                                                       .code("test1(test1@test.com)").label("test1(test1@test.com)").build()).build();
        GatekeepingDetails expectedResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,legalAdviserList,null);
        assertEquals(SendToGatekeeperTypeEnum.legalAdviser,expectedResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertNotNull(expectedResponse.getLegalAdviserList());
    }

    @Test
    public void testGatekeepingWhenJudgeDetailsProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.judge);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        stringObjectMap.put(JURISDICTION, JURISDICTION);
        stringObjectMap.put(CASE_TYPE, CASE_TYPE);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
                                                              .personalCode(personalCodes).build())).thenReturn(apiResponseList);
        GatekeepingDetails actualResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,null,refDataUserService);
        assertNotNull(actualResponse);
        assertEquals(SendToGatekeeperTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificGateKeeperNeeded());
        assertEquals(new JudicialUser((String) (idamId = "123"), personalCodes[0]), actualResponse.getJudgeName());
    }

    @Test
    public void testGatekeepingWhenJudgeDetailsNotProvided() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        List<JudicialUsersApiResponse> apiResponseList = new ArrayList<>();
        apiResponseList.add(JudicialUsersApiResponse.builder().personalCode("123456").emailId("test@Email.com").surname("testSurname").build());
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.judge);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        stringObjectMap.put(JURISDICTION, JURISDICTION);
        stringObjectMap.put(CASE_TYPE, CASE_TYPE);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
                                                              .personalCode(personalCodes).build())).thenReturn(null);
        GatekeepingDetails actualResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,null,refDataUserService);
        assertNotNull(actualResponse);
        assertEquals(SendToGatekeeperTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificGateKeeperNeeded());
        assertNull(actualResponse.getJudgeName());
    }

    @Test
    public void testGatekeepingWhenJudgeDetailsEmpty() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE).build();
        String[] personalCodes = new String[3];
        personalCodes[0] = "123456";
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        stringObjectMap.put("isJudgeOrLegalAdviserGatekeeping", SendToGatekeeperTypeEnum.judge);
        stringObjectMap.put("judgeName", JudicialUser.builder().idamId("123").personalCode("123456").build());
        stringObjectMap.put(JURISDICTION, JURISDICTION);
        stringObjectMap.put(CASE_TYPE, CASE_TYPE);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(refDataUserService.getAllJudicialUserDetails(JudicialUsersApiRequest.builder().ccdServiceName(null)
                                                              .personalCode(personalCodes).build())).thenReturn(new ArrayList<>());
        GatekeepingDetails actualResponse = gatekeepingDetailsService.getGatekeepingDetails(stringObjectMap,null,refDataUserService);
        assertNotNull(actualResponse);
        assertEquals(SendToGatekeeperTypeEnum.judge,actualResponse.getIsJudgeOrLegalAdviserGatekeeping());
        assertEquals(YesOrNo.Yes,actualResponse.getIsSpecificGateKeeperNeeded());
        assertNull(actualResponse.getJudgeName());
    }
}

