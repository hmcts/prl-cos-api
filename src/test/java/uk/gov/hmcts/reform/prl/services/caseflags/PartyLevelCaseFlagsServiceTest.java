package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CARESPONDENTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DAAPPLICANTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DARESPONDENTSOLICITOR;

@RunWith(MockitoJUnitRunner.class)
public class PartyLevelCaseFlagsServiceTest {

    public static final String CASE_ID = "1234567891234567";
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private PartyLevelCaseFlagsGenerator partyLevelCaseFlagsGenerator;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CcdCoreCaseDataService coreCaseDataService;

    private CaseData caseData;
    private CaseData caseDataFl401;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;

    private static final String authorisation = "Bearer auth";
    private static final String systemUpdateUser = "system User";

    @InjectMocks
    private  PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    private CaseData caseDataSolicitorRepresent;
    private CaseData caseDataFl401SolicitorRepresent;

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(1234567891234567L)
            .state("SUBMITTED_PAID")
            .build();
        PartyDetails partyDetailsApplicant = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .representativeFirstName("John")
            .representativeLastName("Smith")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .representativeFirstName("John")
            .representativeLastName("Smith")
            .user(User.builder().email("").idamId("").build())
            .build();

        PartyDetails partyDetailsApplicantSolicitor = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .build();
        PartyDetails partyDetailsRespondentSolicitor = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .representativeFirstName("")
            .representativeLastName("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeLastName("last name")
            .representativeFirstName("full name")
            .build();

        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .build();

        caseDataFl401 = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicant)
            .respondentsFL401(partyDetailsRespondent)
            .build();

        caseDataSolicitorRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitor).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondentSolicitor).build()))
            .build();

        caseDataFl401SolicitorRepresent = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicantSolicitor)
            .respondentsFL401(partyDetailsRespondentSolicitor)
            .build();
        when(systemUserService.getSysUserToken()).thenReturn(authorisation);
        when(systemUserService.getUserId(authorisation)).thenReturn(systemUpdateUser);
    }

    @Test
    public void testGenerateAndStoreC100CaseFlagsForProvidedCaseIdWhenRepresentedByParty() {
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUser))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(authorisation, eventRequestData, CASE_ID,
                                             true)).thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(authorisation, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        CaseDetails caseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);
        Assert.assertNotNull(caseDetails);
        Assert.assertEquals(SUBMITTED_STATE, caseDetails.getState());
    }

    @Test
    public void testGenerateAndStoreFl401CaseFlagsForProvidedCaseIdWhenRepresentedBySolicitor() {
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUser))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(authorisation, eventRequestData, CASE_ID,
                                             true)).thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseDataFl401);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(authorisation, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        CaseDetails caseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);
        Assert.assertNotNull(caseDetails);
        Assert.assertEquals(SUBMITTED_STATE, caseDetails.getState());
    }

    @Test
    public void testGenerateAndStoreC100CaseFlagsForProvidedCaseIdWhenRepresentedBySolicitor() {
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUser))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(authorisation, eventRequestData, CASE_ID,
                                             true)).thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseDataSolicitorRepresent);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(authorisation, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        CaseDetails caseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);
        Assert.assertNotNull(caseDetails);
        Assert.assertEquals(SUBMITTED_STATE, caseDetails.getState());
    }

    @Test
    public void testGenerateAndStoreFL401CaseFlagsForProvidedCaseIdForSolicitorRepresentedCase() {
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, systemUpdateUser))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(authorisation, eventRequestData, CASE_ID,
                                             true)).thenReturn(startEventResponse);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseDataFl401SolicitorRepresent);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(authorisation, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        CaseDetails caseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);
        Assert.assertNotNull(caseDetails);
        Assert.assertEquals(SUBMITTED_STATE, caseDetails.getState());
    }

    @Test
    public void testIndividualCaseFlagForC100CaseWhenPartiesRepresent() {

        when(partyLevelCaseFlagsGenerator
                 .generatePartyFlags(any(),
                                     any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseData);
        CaseData caseData =  partyLevelCaseFlagsService
            .generateIndividualPartySolicitorCaseFlags(
                this.caseData, 0, CARESPONDENTSOLICITOR, false);

        Assert.assertNotNull(caseData);
        Assert.assertEquals(C100_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    public void testIndividualCaseFlagForFl401CaseWhenPartiesRepresent() {

        when(partyLevelCaseFlagsGenerator
                 .generatePartyFlags(any(),
                                     any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataFl401);
        CaseData caseData =  partyLevelCaseFlagsService
            .generateIndividualPartySolicitorCaseFlags(
                caseDataFl401, 0, DARESPONDENTSOLICITOR, false);
        Assert.assertNotNull(caseData);
        Assert.assertEquals(FL401_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    public void testIndividualCaseFlagForC100CaseWhenSolicitorRepresent() {

        when(partyLevelCaseFlagsGenerator
                 .generatePartyFlags(any(),
                                     any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataSolicitorRepresent);
        CaseData caseData =  partyLevelCaseFlagsService
            .generateIndividualPartySolicitorCaseFlags(
                caseDataSolicitorRepresent, 0, CAAPPLICANTSOLICITOR, true);

        Assert.assertNotNull(caseData);
        Assert.assertEquals(C100_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    public void testIndividualCaseFlagForFl401CaseWhenSolicitorRepresent() {

        when(partyLevelCaseFlagsGenerator
                 .generatePartyFlags(any(),
                                     any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataFl401SolicitorRepresent);
        CaseData caseData =  partyLevelCaseFlagsService
            .generateIndividualPartySolicitorCaseFlags(
                caseDataFl401SolicitorRepresent, 0, DAAPPLICANTSOLICITOR, true);
        Assert.assertNotNull(caseData);
        Assert.assertEquals(FL401_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    public void testGenerateC100AllPartyCaseFlags() {

        when(partyLevelCaseFlagsGenerator
                 .generatePartyFlags(any(),
                                     any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataSolicitorRepresent);
        CaseData caseData = CaseData.builder().build();
        caseData =  partyLevelCaseFlagsService
            .generateC100AllPartyCaseFlags(caseData, this.caseData);

        Assert.assertNotNull(caseData);
        Assert.assertEquals(C100_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    public void testGenerateC100AllPartyCaseFlagsForSolicitor() {

        when(partyLevelCaseFlagsGenerator
                 .generatePartyFlags(any(),
                                     any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataSolicitorRepresent);

        CaseData caseData = CaseData.builder().build();
        caseData =  partyLevelCaseFlagsService
            .generateC100AllPartyCaseFlags(this.caseDataSolicitorRepresent, this.caseDataSolicitorRepresent);

        Assert.assertNotNull(caseData);
        Assert.assertEquals(C100_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    public void testGetPartyCaseDataExternalField() {
        CaseData caseData1 = CaseData.builder().caseTypeOfApplication(C100_CASE_TYPE).build();
        partyLevelCaseFlagsService.getPartyCaseDataExternalField(C100_CASE_TYPE,PartyRole.Representing.CAAPPLICANTSOLICITOR,1);
        Assert.assertEquals(C100_CASE_TYPE, caseData1.getCaseTypeOfApplication());
    }
}
