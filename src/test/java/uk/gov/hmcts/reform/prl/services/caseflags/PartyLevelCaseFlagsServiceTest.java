package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.caseflags.PartyLevelCaseFlagsGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Map.entry;
import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBMITTED_STATE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.AMEND_APPLICANTS_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.AMEND_RESPONDENTS_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANTBARRISTER;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CAAPPLICANTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.CARESPONDENTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DAAPPLICANTBARRISTER;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DAAPPLICANTSOLICITOR;
import static uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole.Representing.DARESPONDENTSOLICITOR;

@ExtendWith(MockitoExtension.class)
class PartyLevelCaseFlagsServiceTest {

    private ObjectMapper objectMapper;
    @Mock
    private PartyLevelCaseFlagsGenerator partyLevelCaseFlagsGenerator;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CcdCoreCaseDataService coreCaseDataService;
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    private static final String AUTHORISATION = "Bearer auth";
    private static final String SYSTEM_UPDATE_USER = "system User";
    private static final String CASE_ID = "1234567891234567";

    @BeforeEach
    void setup() {
        objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());

        partyLevelCaseFlagsService = new PartyLevelCaseFlagsService(
            objectMapper, partyLevelCaseFlagsGenerator,
            systemUserService, coreCaseDataService
        );
    }

    @Test
    void testGenerateAndStoreC100CaseFlagsForProvidedCaseIdWhenRepresentedByParty() {
        CaseDetails caseDetails = createC100CaseDetails();
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, SYSTEM_UPDATE_USER))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(AUTHORISATION, eventRequestData, CASE_ID, true))
            .thenReturn(startEventResponse);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService.submitUpdate(AUTHORISATION, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        when(systemUserService.getUserId(AUTHORISATION)).thenReturn(SYSTEM_UPDATE_USER);

        CaseDetails updatedCaseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);

        assertNotNull(updatedCaseDetails);
        assertEquals(SUBMITTED_STATE, updatedCaseDetails.getState());
    }

    @Test
    void testGenerateAndStoreFl401CaseFlagsForProvidedCaseIdWhenRepresentedBySolicitor() {
        CaseDetails caseDetails = createFl401CaseDetailsWithSolicitorRepresentative();
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, SYSTEM_UPDATE_USER))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(
            AUTHORISATION, eventRequestData, CASE_ID,
            true
        )).thenReturn(startEventResponse);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(AUTHORISATION, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        when(systemUserService.getUserId(AUTHORISATION)).thenReturn(SYSTEM_UPDATE_USER);

        CaseDetails updatedCaseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);

        assertNotNull(updatedCaseDetails);
        assertEquals(SUBMITTED_STATE, updatedCaseDetails.getState());
    }

    @Test
    void testGenerateAndStoreC100CaseFlagsForProvidedCaseIdWhenRepresentedBySolicitor() {
        CaseDetails caseDetails = createC100CaseDetailsWithSolicitorRepresentative();
        EventRequestData eventRequestData = EventRequestData.builder().build();
        when(coreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, SYSTEM_UPDATE_USER))
            .thenReturn(eventRequestData);
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails).build();
        when(coreCaseDataService.startUpdate(AUTHORISATION, eventRequestData, CASE_ID, true))
            .thenReturn(startEventResponse);
        CaseDataContent caseDataContent = CaseDataContent.builder().build();
        when(coreCaseDataService.createCaseDataContent(any(), any()))
            .thenReturn(caseDataContent);
        when(coreCaseDataService
                 .submitUpdate(AUTHORISATION, eventRequestData, caseDataContent, CASE_ID, true))
            .thenReturn(caseDetails);
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        when(systemUserService.getUserId(AUTHORISATION)).thenReturn(SYSTEM_UPDATE_USER);

        CaseDetails updatedCaseDetails = partyLevelCaseFlagsService.generateAndStoreCaseFlags(CASE_ID);

        assertNotNull(updatedCaseDetails);
        assertEquals(SUBMITTED_STATE, updatedCaseDetails.getState());
    }

    @Test
    void testIndividualCaseFlagForC100CaseWhenPartiesRepresent() {
        CaseData caseData = createC100CaseDataWithSolicitorRepresentative();
        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseData);

        CaseData updatedCaseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData, 0, CARESPONDENTSOLICITOR, false);

        assertEquals(C100_CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
    }

    @Test
    void testIndividualCaseFlagForFl401CaseWhenPartiesRepresent() {
        CaseData caseData = createFl401CaseData();

        CaseData updatedCaseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData, 0, DARESPONDENTSOLICITOR, false);

        assertEquals(FL401_CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
    }

    @Test
    void testIndividualCaseFlagForC100CaseWhenSolicitorRepresent() {
        CaseData caseData = createC100CaseDataWithSolicitorRepresentative();
        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseData);

        CaseData updatedCaseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData, 0, CAAPPLICANTSOLICITOR, true);

        assertEquals(C100_CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
    }

    @Test
    void testIndividualCaseFlagForC100CaseWhenSolicitorRepresentAndBarrister() {
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .barrister(Barrister.builder().barristerId("")
                           .barristerFirstName("BarrFN").barristerLastName("BarrLN").build())
            .build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitorBarrister).build()))
            .build();

        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataSolicitorBarristerRepresent);

        CaseData caseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseDataSolicitorBarristerRepresent, 0, CAAPPLICANTBARRISTER, true);

        assertEquals(C100_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    void testPartyCaseFlagForC100CaseWhenSolicitorRepresentAndBarrister() {
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .barrister(Barrister.builder().barristerId("1")
                           .barristerFirstName("BarrFN").barristerLastName("BarrLN").build())
            .build();

        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .barrister(Barrister.builder().barristerId("2")
                           .barristerFirstName("BarrRespFN").barristerLastName("BarrRespLN").build())
            .build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitorBarrister).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetailsRespondentSolicitorBarrister).build()))
            .build();

        when(partyLevelCaseFlagsGenerator.generateExternalPartyFlags(any(), any(), any()))
            .thenReturn(Flags.builder().partyName("ext").build());
        when(partyLevelCaseFlagsGenerator.generateInternalPartyFlags(any(), any(), any()))
            .thenReturn(Flags.builder().partyName("int").build());

        Map<String, Object> caseData = partyLevelCaseFlagsService.generatePartyCaseFlags(
            caseDataSolicitorBarristerRepresent);

        assertNotNull(caseData);
        assertNotNull(caseData.get("caApplicantSolicitor1ExternalFlags"));
        assertNotNull(caseData.get("caApplicantBarrister1ExternalFlags"));
        assertNotNull(caseData.get("caRespondentSolicitor1ExternalFlags"));
        assertNotNull(caseData.get("caRespondentBarrister1ExternalFlags"));
        assertNotNull(caseData.get("caApplicantSolicitor1InternalFlags"));
        assertNotNull(caseData.get("caApplicantBarrister1InternalFlags"));
        assertNotNull(caseData.get("caRespondentSolicitor1InternalFlags"));
        assertNotNull(caseData.get("caRespondentBarrister1InternalFlags"));
        assertEquals("ext", ((Flags) (caseData.get("caApplicantSolicitor1ExternalFlags"))).getPartyName());
        assertEquals("ext", ((Flags) (caseData.get("caApplicantBarrister1ExternalFlags"))).getPartyName());
        assertEquals("ext", ((Flags) (caseData.get("caRespondentSolicitor1ExternalFlags"))).getPartyName());
        assertEquals("ext", ((Flags) (caseData.get("caRespondentBarrister1ExternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("caApplicantSolicitor1InternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("caApplicantBarrister1InternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("caRespondentSolicitor1InternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("caRespondentBarrister1InternalFlags"))).getPartyName());
    }

    @Test
    void testPartyCaseFlagForFL401CaseWhenSolicitorRepresentAndBarrister() {
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("AppFN")
            .lastName("AppLN")
            .email("app@email.com")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .barrister(Barrister.builder().barristerId("1")
                           .barristerFirstName("BarrFN").barristerLastName("BarrLN").build())
            .build();

        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("RespFN")
            .lastName("RespLN")
            .email("resp@email.com")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("respSolFN")
            .lastName("respSolLN")
            .barrister(Barrister.builder().barristerId("2")
                           .barristerFirstName("BarrRespFN").barristerLastName("BarrRespLN").build())
            .build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicantSolicitorBarrister)
            .respondentsFL401(partyDetailsRespondentSolicitorBarrister)
            .build();

        when(partyLevelCaseFlagsGenerator.generateExternalPartyFlags(any(), any(), any()))
            .thenReturn(Flags.builder().partyName("ext").build());
        when(partyLevelCaseFlagsGenerator.generateInternalPartyFlags(any(), any(), any()))
            .thenReturn(Flags.builder().partyName("int").build());

        Map<String, Object> caseData = partyLevelCaseFlagsService.generatePartyCaseFlags(
            caseDataSolicitorBarristerRepresent);

        assertNotNull(caseData);
        assertNull(caseData.get("caApplicantSolicitor1ExternalFlags"));
        assertNull(caseData.get("caRespondentSolicitor1ExternalFlags"));
        assertNull(caseData.get("caRespondentBarrister1ExternalFlags"));
        assertEquals("ext", ((Flags) (caseData.get("daApplicantSolicitorExternalFlags"))).getPartyName());
        assertEquals("ext", ((Flags) (caseData.get("daRespondentSolicitorExternalFlags"))).getPartyName());
        assertEquals("ext", ((Flags) (caseData.get("daRespondentBarristerExternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("daApplicantSolicitorInternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("daRespondentSolicitorInternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("daRespondentBarristerInternalFlags"))).getPartyName());
    }

    @Test
    void testPartyCaseFlagForC100CaseWhenRepresentAndBarristerOnlyAdd() {
        UUID appPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0111");
        UUID respPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0222");
        Barrister applicantBarrister = Barrister.builder().barristerId("UUID3")
            .barristerFirstName("BarrFN").barristerLastName("BarrLN").build();
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .barrister(applicantBarrister)
            .build();

        Barrister respondentBarrister = Barrister.builder().barristerId("UUID4")
            .barristerFirstName("RespBarrFN").barristerLastName("RespBarrLN").build();
        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .barrister(respondentBarrister)
            .build();

        FlagDetail flagDetail = FlagDetail.builder().flagCode("test").flagComment("test comment").name("test flag").build();
        Flags caApplicantSolicitor1ExternalFlags = generateCaseFlag("ApplicantSolicitor1", "caApplicant1", flagDetail);
        AllPartyFlags allPartyFlags = AllPartyFlags.builder().caApplicantSolicitor1ExternalFlags(
            caApplicantSolicitor1ExternalFlags).build();
        DynamicListElement abp = DynamicListElement.builder().code(appPartyUuid.toString()).label(appPartyUuid.toString()).build();
        DynamicList abpl = DynamicList.builder().value(abp).listItems(List.of(abp)).build();
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder().partyList(abpl).build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitorBarrister)
                                    .id(appPartyUuid).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetailsRespondentSolicitorBarrister)
                                     .id(respPartyUuid).build()))
            .allPartyFlags(allPartyFlags)
            .allocatedBarrister(allocatedBarrister)
            .build();

        PartyLevelCaseFlagsService localPartyLevelCaseFlagsService = new PartyLevelCaseFlagsService(
            objectMapper,
            new PartyLevelCaseFlagsGenerator(),
            systemUserService,
            coreCaseDataService
        );

        Map<String, Object> caseData = localPartyLevelCaseFlagsService.generatePartyCaseFlagsForBarristerOnly(
            caseDataSolicitorBarristerRepresent);

        assertNotNull(caseData);
        Flags externalFlag = Flags.builder()
            .partyName("BarrFN BarrLN")
            .roleOnCase("Applicant barrister 1")
            .groupId("caApplicantBarrister1")
            .visibility("External")
            .details(List.of())
            .build();
        Flags internalFlag = Flags.builder()
            .partyName("BarrFN BarrLN")
            .roleOnCase("Applicant barrister 1")
            .groupId("caApplicantBarrister1")
            .visibility("Internal")
            .details(List.of())
            .build();

        assertNotNull(caseData);
        assertThat(caseData)
            .contains(
                entry("caApplicantBarrister1ExternalFlags", externalFlag),
                entry("caApplicantBarrister1InternalFlags", internalFlag)
            );

        assertThat(caseData)
            .doesNotContainKeys(
                "caApplicantSolicitor1ExternalFlags",
                "caRespondentSolicitor1ExternalFlags",
                "caApplicantSolicitor1InternalFlags",
                "caRespondentSolicitor1InternalFlags",
                "caRespondentBarrister1InternalFlags",
                "caRespondentBarrister1ExternalFlags"
            );
    }

    @Test
    void testPartyCaseFlagForC100CaseWhenBarristerNotRepresented() {
        UUID appPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0111");
        UUID respPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0222");

        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .build();

        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .build();

        FlagDetail flagDetail = FlagDetail.builder().flagCode("test").flagComment("test comment").name("test flag").build();
        Flags caApplicantSolicitor1ExternalFlags = generateCaseFlag("ApplicantSolicitor1", "caApplicant1", flagDetail);
        AllPartyFlags allPartyFlags = AllPartyFlags.builder().caApplicantSolicitor1ExternalFlags(
            caApplicantSolicitor1ExternalFlags).build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitorBarrister)
                                    .id(appPartyUuid).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetailsRespondentSolicitorBarrister)
                                     .id(respPartyUuid).build()))
            .allPartyFlags(allPartyFlags)
            .build();

        PartyLevelCaseFlagsService localPartyLevelCaseFlagsService = new PartyLevelCaseFlagsService(
            objectMapper,
            new PartyLevelCaseFlagsGenerator(),
            systemUserService,
            coreCaseDataService
        );
        Map<String, Object> localCaseData = localPartyLevelCaseFlagsService
            .generatePartyCaseFlagsForBarristerOnly(caseDataSolicitorBarristerRepresent);

        assertNotNull(localCaseData);

        assertThat(localCaseData)
            .doesNotContainKeys(
                "caApplicantBarrister1ExternalFlags",
                "caApplicantBarrister1InternalFlags",
                "caApplicantSolicitor1ExternalFlags",
                "caRespondentSolicitor1ExternalFlags",
                "caApplicantSolicitor1InternalFlags",
                "caRespondentSolicitor1InternalFlags",
                "caRespondentBarrister1InternalFlags",
                "caRespondentBarrister1ExternalFlags"
            );
    }

    @Test
    void testCaseDataPartyCaseFlagForC100CaseWhenRepresentAndBarristerOnlyAdd() {
        UUID appPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0111");
        UUID respPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0222");
        Barrister applicantBarrister = Barrister.builder().barristerId("UUID3")
            .barristerFirstName("BarrFN").barristerLastName("BarrLN").build();
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .barrister(applicantBarrister)
            .build();

        Barrister respondentBarrister = Barrister.builder().barristerId("UUID4")
            .barristerFirstName("RespBarrFN").barristerLastName("RespBarrLN").build();
        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .barrister(respondentBarrister)
            .build();

        FlagDetail flagDetail = FlagDetail.builder().flagCode("test").flagComment("test comment").name("test flag").build();
        Flags caApplicantSolicitor1ExternalFlags = generateCaseFlag("ApplicantSolicitor1", "caApplicant1", flagDetail);
        AllPartyFlags allPartyFlags = AllPartyFlags.builder().caApplicantSolicitor1ExternalFlags(caApplicantSolicitor1ExternalFlags).build();
        DynamicListElement abp = DynamicListElement.builder().code(appPartyUuid.toString()).label(appPartyUuid.toString()).build();
        DynamicList abpl = DynamicList.builder().value(abp).listItems(Arrays.asList(abp)).build();
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder().partyList(abpl).build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitorBarrister)
                                    .id(appPartyUuid).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetailsRespondentSolicitorBarrister)
                                     .id(respPartyUuid).build()))
            .allPartyFlags(allPartyFlags)
            .allocatedBarrister(allocatedBarrister)
            .build();
        ObjectMapper mapper = new ObjectMapper();
        mapper.findAndRegisterModules();

        PartyLevelCaseFlagsService localPartyLevelCaseFlagsService = new PartyLevelCaseFlagsService(
            mapper,
            new PartyLevelCaseFlagsGenerator(),
            systemUserService,
            coreCaseDataService
        );
        localPartyLevelCaseFlagsService
            .updateCaseDataWithGeneratePartyCaseFlags(caseDataSolicitorBarristerRepresent,
                                                      localPartyLevelCaseFlagsService::generatePartyCaseFlagsForBarristerOnly);

        Flags externalFlag = Flags.builder()
            .partyName("BarrFN BarrLN")
            .roleOnCase("Applicant barrister 1")
            .groupId("caApplicantBarrister1")
            .visibility("External")
            .details(List.of())
            .build();
        Flags internalFlag = Flags.builder()
            .partyName("BarrFN BarrLN")
            .roleOnCase("Applicant barrister 1")
            .groupId("caApplicantBarrister1")
            .visibility("Internal")
            .details(List.of())
            .build();
        AllPartyFlags updatedPartyFlags = caseDataSolicitorBarristerRepresent.getAllPartyFlags();
        assertThat(updatedPartyFlags.getCaApplicantBarrister1InternalFlags())
            .isEqualTo(internalFlag);
        assertThat(updatedPartyFlags.getCaApplicantBarrister1ExternalFlags())
            .isEqualTo(externalFlag);
    }

    @Test
    void testPartyCaseFlagForC100CaseWhenRepresentAndBarristerOnlyRemove() {
        UUID appPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0111");
        UUID respPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0222");

        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .build();

        Barrister respondentBarrister = Barrister.builder().barristerId("UUID4")
            .barristerFirstName("RespBarrFN").barristerLastName("RespBarrLN").build();
        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .barrister(respondentBarrister)
            .build();

        FlagDetail flagDetail = FlagDetail.builder().flagCode("test").flagComment("test comment").name("test flag").build();
        Flags caApplicantSolicitor1ExternalFlags = generateCaseFlag("ApplicantSolicitor1", "caApplicant1", flagDetail);
        AllPartyFlags allPartyFlags = AllPartyFlags.builder().caApplicantSolicitor1ExternalFlags(
            caApplicantSolicitor1ExternalFlags).build();
        DynamicListElement abp = DynamicListElement.builder().code(appPartyUuid.toString()).label(appPartyUuid.toString()).build();
        DynamicList abpl = DynamicList.builder().value(abp).listItems(List.of(abp)).build();
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder().partyList(abpl).build();

        CaseData caseDataSolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicantSolicitorBarrister)
                                    .id(appPartyUuid).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetailsRespondentSolicitorBarrister)
                                     .id(respPartyUuid).build()))
            .allPartyFlags(allPartyFlags)
            .allocatedBarrister(allocatedBarrister)
            .build();

        PartyLevelCaseFlagsService localPartyLevelCaseFlagsService = new PartyLevelCaseFlagsService(
            objectMapper,
            new PartyLevelCaseFlagsGenerator(),
            systemUserService,
            coreCaseDataService
        );

        Map<String, Object> localCaseData = localPartyLevelCaseFlagsService
            .generatePartyCaseFlagsForBarristerOnly(caseDataSolicitorBarristerRepresent);

        assertNotNull(localCaseData);
        assertThat(localCaseData)
            .contains(
                entry("caApplicantBarrister1ExternalFlags", empty()),
                entry("caApplicantBarrister1InternalFlags", empty())
            );

        assertThat(localCaseData)
            .doesNotContainKeys(
                "caApplicantSolicitor1ExternalFlags",
                "caRespondentSolicitor1ExternalFlags",
                "caApplicantSolicitor1InternalFlags",
                "caRespondentSolicitor1InternalFlags",
                "caRespondentBarrister1InternalFlags",
                "caRespondentBarrister1ExternalFlags"
            );
    }

    @Test
    void testIndividualCaseFlagForFl401CaseWhenSolicitorRepresent() {
        CaseData caseData = createFl401CaseDataWithSolicitorRepresentative();
        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseData);
        CaseData updatedCaseData = partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData, 0, DAAPPLICANTSOLICITOR, true);

        assertEquals(FL401_CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
    }

    @Test
    void testIndividualCaseFlagForFl401CaseWhenSolicitorBarristerRepresent() {
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .barrister(Barrister.builder().barristerId("")
                           .barristerFirstName("BarrFN").barristerLastName("BarrLN").build())
            .build();
        CaseData caseDataFl401SolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicantSolicitorBarrister)
            .build();

        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseDataFl401SolicitorBarristerRepresent);

        CaseData caseData = partyLevelCaseFlagsService
            .generateIndividualPartySolicitorCaseFlags(
                caseDataFl401SolicitorBarristerRepresent, 0, DAAPPLICANTBARRISTER, true);
        assertNotNull(caseData);
        assertEquals(FL401_CASE_TYPE, caseData.getCaseTypeOfApplication());
    }

    @Test
    void testPartyCaseFlagForFl401CaseWhenSolicitorBarristerRepresent() {
        UUID appPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0111");
        UUID respPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0222");
        Barrister applicantBarrister = Barrister.builder().barristerId("UUID3")
            .barristerFirstName("BarrFN").barristerLastName("BarrLN").build();
        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .partyId(appPartyUuid)
            .barrister(applicantBarrister)
            .build();

        Barrister respondentBarrister = Barrister.builder().barristerId("UUID4")
            .barristerFirstName("RespBarrFN").barristerLastName("RespBarrLN").build();
        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .partyId(respPartyUuid)
            .barrister(respondentBarrister)
            .build();

        DynamicListElement abp = DynamicListElement.builder().code(respPartyUuid.toString()).label(respPartyUuid.toString()).build();
        DynamicList abpl = DynamicList.builder().value(abp).listItems(List.of(abp)).build();
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder().partyList(abpl).build();
        CaseData caseDataFl401SolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicantSolicitorBarrister)
            .respondentsFL401(partyDetailsRespondentSolicitorBarrister)
            .allocatedBarrister(allocatedBarrister)
            .build();

        when(partyLevelCaseFlagsGenerator.generateExternalPartyFlags(any(), any(), any()))
            .thenReturn(Flags.builder().partyName("ext").build());
        when(partyLevelCaseFlagsGenerator.generateInternalPartyFlags(any(), any(), any()))
            .thenReturn(Flags.builder().partyName("int").build());

        Map<String, Object> caseData = partyLevelCaseFlagsService
            .generatePartyCaseFlagsForBarristerOnly(caseDataFl401SolicitorBarristerRepresent);
        assertNotNull(caseData);
        assertNull((caseData.get("daApplicantBarristerExternalFlags")));
        assertNull((caseData.get("daApplicantBarristerInternalFlags")));
        assertEquals("ext", ((Flags) (caseData.get("daRespondentBarristerExternalFlags"))).getPartyName());
        assertEquals("int", ((Flags) (caseData.get("daRespondentBarristerInternalFlags"))).getPartyName());
    }

    @Test
    void testPartyCaseFlagForFl401CaseWhenBarristerNotRepresented() {
        UUID appPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0111");
        UUID respPartyUuid = UUID.fromString("fbd63138-6396-4879-ac62-7f1c915f0222");

        PartyDetails partyDetailsApplicantSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("first name")
            .lastName("last name")
            .partyId(appPartyUuid)
            .build();

        PartyDetails partyDetailsRespondentSolicitorBarrister = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("resp first name")
            .lastName("resp last name")
            .partyId(respPartyUuid)
            .build();

        CaseData caseDataFl401SolicitorBarristerRepresent = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicantSolicitorBarrister)
            .respondentsFL401(partyDetailsRespondentSolicitorBarrister)
            .build();

        Map<String, Object> caseData = partyLevelCaseFlagsService
            .generatePartyCaseFlagsForBarristerOnly(caseDataFl401SolicitorBarristerRepresent);
        assertNotNull(caseData);
        assertThat(caseData)
            .doesNotContainKeys(
                "daApplicantBarristerExternalFlags",
                "daApplicantBarristerInternalFlags"
            );
    }

    @Test
    void testGenerateC100AllPartyCaseFlags() {
        CaseData caseData = createC100CaseData();
        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseData);

        CaseData updatedCaseData = partyLevelCaseFlagsService.generateC100AllPartyCaseFlags(caseData, caseData);

        assertEquals(C100_CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
    }

    @Test
    void testGenerateC100AllPartyCaseFlagsForSolicitor() {
        CaseData caseData = createC100CaseDataWithSolicitorRepresentative();
        when(partyLevelCaseFlagsGenerator.generatePartyFlags(any(), any(), any(), any(), Mockito.anyBoolean(), any()))
            .thenReturn(caseData);

        CaseData updatedCaseData = partyLevelCaseFlagsService.generateC100AllPartyCaseFlags(caseData, caseData);

        assertEquals(C100_CASE_TYPE, updatedCaseData.getCaseTypeOfApplication());
    }

    @Test
    void testGetPartyCaseDataExternalField() {
        CaseData caseData1 = CaseData.builder().caseTypeOfApplication(C100_CASE_TYPE).build();
        partyLevelCaseFlagsService.getPartyCaseDataExternalField(C100_CASE_TYPE, CAAPPLICANTSOLICITOR, 1);
        assertEquals(C100_CASE_TYPE, caseData1.getCaseTypeOfApplication());
    }

    @Test
    void testAmendApplicantDetails() {
        PartyDetails partyDetailsApplicant = createUnrepresentedParty("test", "test");

        PartyDetails partyDetailsApplicant2 = PartyDetails.builder()
            .firstName("test2")
            .lastName("test2")
            .email("")
            .representativeFirstName("John")
            .representativeLastName("Smith")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .user(User.builder().email("").idamId("").build())
            .build();

        PartyDetails partyDetailsApplicant3 = PartyDetails.builder()
            .firstName("test3")
            .lastName("test3")
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

        CaseData caseDataBefore = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002")).value(
                    partyDetailsApplicant2).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .allPartyFlags(createAllPartyFlags())
            .build();

        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .allPartyFlags(createAllPartyFlags())
            .build();

        Map<String, Object> caseDataMapBefore = objectMapper.convertValue(
            caseDataBefore, new TypeReference<>() {
            }
        );
        Map<String, Object> caseDataMap1 = objectMapper.convertValue(
            caseData1, new TypeReference<>() {
            }
        );
        partyLevelCaseFlagsService.amendCaseFlags(caseDataMapBefore, caseDataMap1, AMEND_APPLICANTS_DETAILS.getValue());
        assertNotNull(caseDataMap1);
    }

    @Test
    void testAmendApplicantDetailsWithCaseFlags() {
        PartyDetails partyDetailsApplicant = createUnrepresentedParty("test", "test");
        PartyDetails partyDetailsApplicant2 = createRepresentedParty("test2", "test2", "John", "Smith");
        PartyDetails partyDetailsApplicant3 = createUnrepresentedParty("test3", "test3");
        PartyDetails partyDetailsRespondent = createRepresentedParty("test4", "test4", "John", "Smith");

        CaseData caseDataBefore = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002")).value(
                    partyDetailsApplicant2).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .allPartyFlags(createAllPartyFlags())
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .build();

        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .allPartyFlags(createAllPartyFlags())
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .build();
        Map<String, Object> caseDataMapBefore = objectMapper.convertValue(
            caseDataBefore, new TypeReference<>() {
            }
        );
        Map<String, Object> caseDataMapLatest = objectMapper.convertValue(
            caseData1, new TypeReference<>() {
            }
        );

        partyLevelCaseFlagsService.amendCaseFlags(
            caseDataMapBefore,
            caseDataMapLatest,
            AMEND_APPLICANTS_DETAILS.getValue()
        );
        assertNotNull(caseDataMapLatest);
    }

    private Flags generateCaseFlag(String roleOnCase, String caApplicant3, FlagDetail flagDetail) {
        return Flags
            .builder().partyName("test test").roleOnCase(roleOnCase).groupId(caApplicant3).details(List.of(
                ElementUtils.element(flagDetail))).build();
    }

    @Test
    void testAmendApplicantDetailsWithCaseFlagsRespondents() {
        PartyDetails partyDetailsApplicant = createUnrepresentedParty("test", "test");
        PartyDetails partyDetailsApplicant2 = createUnrepresentedParty("test2", "test2");
        PartyDetails partyDetailsApplicant3 = createUnrepresentedParty("test3", "test3");
        PartyDetails partyDetailsRespondent = createUnrepresentedParty("test4", "test4");

        CaseData caseDataBefore = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .respondents(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .allPartyFlags(createAllPartyFlags())
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetailsRespondent).build()))
            .build();

        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .respondents(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000002")).value(
                    partyDetailsApplicant2).build()
            ))
            .allPartyFlags(createAllPartyFlags())
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetailsRespondent).build()))
            .build();
        Map<String, Object> caseDataMapBefore = objectMapper.convertValue(
            caseDataBefore, new TypeReference<>() {
            }
        );
        Map<String, Object> caseDataMapLatest = objectMapper.convertValue(
            caseData1, new TypeReference<>() {
            }
        );

        partyLevelCaseFlagsService.amendCaseFlags(
            caseDataMapBefore,
            caseDataMapLatest,
            AMEND_RESPONDENTS_DETAILS.getValue()
        );
        assertNotNull(caseDataMapLatest);
    }

    @Test
    void testAmendApplicantDetailsWithCaseFlagsOtherPeople() {
        PartyDetails partyDetailsApplicant = createUnrepresentedParty("test", "test");
        PartyDetails partyDetailsApplicant3 = createUnrepresentedParty("test3", "test3");
        PartyDetails partyDetailsRespondent = createUnrepresentedParty("test4", "test4");

        FlagDetail flagDetail = FlagDetail.builder().flagCode("test").flagComment("test comment").name("test flag").build();
        Flags caOtherParty1ExternalFlags = generateCaseFlag("Applicant 1", "caApplicant1", flagDetail);
        Flags caOtherParty1InternalFlags = generateCaseFlag("Applicant 1", "caApplicant1", flagDetail);
        AllPartyFlags allPartyFlags = AllPartyFlags.builder().caRespondent1ExternalFlags(caOtherParty1ExternalFlags).caRespondent1ExternalFlags(
                caOtherParty1InternalFlags)
            .build();
        CaseData caseDataBefore = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .respondents(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .allPartyFlags(allPartyFlags)
            .otherPartyInTheCaseRevised(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build()
            ))
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetailsRespondent).build()))
            .build();

        CaseData caseData1 = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .respondents(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .allPartyFlags(allPartyFlags)
            .otherPartyInTheCaseRevised(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001")).value(
                    partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000003")).value(
                    partyDetailsApplicant3).build()
            ))
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetailsRespondent).build()))
            .build();
        Map<String, Object> caseDataMapBefore = objectMapper.convertValue(
            caseDataBefore, new TypeReference<>() {
            }
        );
        Map<String, Object> caseDataMapLatest = objectMapper.convertValue(
            caseData1, new TypeReference<>() {
            }
        );

        partyLevelCaseFlagsService.amendCaseFlags(
            caseDataMapBefore, caseDataMapLatest,
            AMEND_OTHER_PEOPLE_IN_THE_CASE_REVISED.getValue()
        );
        assertNotNull(caseDataMapLatest);
    }

    private CaseDetails createC100CaseDetails() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(Element.<PartyDetails>builder().value(createUnrepresentedParty(
                "John",
                "Smith"
            )).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(createUnrepresentedParty("Jane", "Smith")).build()))
            .build();

        return createCaseDetails(caseData);
    }

    private CaseData createFl401CaseData() {
        return CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(createUnrepresentedParty("John", "Smith"))
            .respondentsFL401(createUnrepresentedParty("Jane", "Smith"))
            .build();
    }

    private CaseData createC100CaseData() {
        return CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicants(List.of(Element.<PartyDetails>builder().value(createUnrepresentedParty(
                "John",
                "Smith"
            )).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(createUnrepresentedParty("Jane", "Smith")).build()))
            .build();
    }

    private CaseData createC100CaseDataWithSolicitorRepresentative() {
        return CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(
                Element.<PartyDetails>builder()
                    .value(createRepresentedParty("John", "Smith", "rep first name", "rep last name"))
                    .build()))
            .respondents(List.of(
                Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .value(createRepresentedParty("Jane", "Smith", "rep first name", "rep last name"))
                    .build()))
            .build();
    }

    private CaseData createFl401CaseDataWithSolicitorRepresentative() {
        return CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(createRepresentedParty("John", "Smith", "rep first name", "rep last name"))
            .respondentsFL401(createRepresentedParty("Jane", "Smith", "rep first name", "rep last name"))
            .build();
    }

    private CaseDetails createC100CaseDetailsWithSolicitorRepresentative() {
        CaseData caseData = createC100CaseDataWithSolicitorRepresentative();
        return createCaseDetails(caseData);
    }

    private CaseDetails createFl401CaseDetailsWithSolicitorRepresentative() {
        CaseData caseData = createFl401CaseDataWithSolicitorRepresentative();
        return createCaseDetails(caseData);
    }

    private CaseDetails createCaseDetails(CaseData caseData) {
        Map<String, Object> caseDataMap = objectMapper.convertValue(
            caseData, new TypeReference<>() {
            }
        );
        return CaseDetails.builder()
            .data(caseDataMap)
            .id(1234567891234567L)
            .state("SUBMITTED_PAID")
            .build();
    }

    private PartyDetails createRepresentedParty(String firstName, String lastName, String repFirstName, String repLastName) {
        return PartyDetails.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email("")
            .user(User.builder().email("").idamId("").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName(repFirstName)
            .representativeLastName(repLastName)
            .build();
    }

    private PartyDetails createUnrepresentedParty(String firstName, String lastName) {
        return PartyDetails.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
    }

    private AllPartyFlags createAllPartyFlags() {
        FlagDetail flagDetail = FlagDetail.builder().flagCode("test").flagComment("test comment").name("test flag").build();
        Flags caApplicant1ExternalFlags = generateCaseFlag("Applicant 1", "caApplicant1", flagDetail);
        Flags caApplicant1InternalFlags = generateCaseFlag("Applicant 1", "caApplicant1", flagDetail);
        Flags caApplicant2ExternalFlags = generateCaseFlag("Applicant 2", "caApplicant2", flagDetail);
        Flags caApplicant2InternalFlags = generateCaseFlag("Applicant 2", "caApplicant2", flagDetail);
        Flags caApplicant3ExternalFlags = generateCaseFlag("Applicant 3", "caApplicant3", flagDetail);
        Flags caApplicant3InternalFlags = generateCaseFlag("Applicant 3", "caApplicant3", flagDetail);
        Flags caRespondent1ExternalFlags = generateCaseFlag("Applicant 1", "caApplicant1", flagDetail);
        Flags caRespondent1InternalFlags = generateCaseFlag("Applicant 1", "caApplicant1", flagDetail);
        return AllPartyFlags.builder()
            .caApplicant1InternalFlags(caApplicant1InternalFlags)
            .caApplicant1ExternalFlags(caApplicant1ExternalFlags)
            .caApplicant2ExternalFlags(caApplicant2ExternalFlags)
            .caApplicant2InternalFlags(caApplicant2InternalFlags)
            .caApplicant3ExternalFlags(caApplicant3ExternalFlags)
            .caApplicant3InternalFlags(caApplicant3InternalFlags)
            .caRespondent1ExternalFlags(caRespondent1ExternalFlags)
            .caRespondent1InternalFlags(caRespondent1InternalFlags)

            .caApplicantSolicitor1ExternalFlags(Flags.builder().build())
            .caApplicantSolicitor1InternalFlags(Flags.builder().build())
            .caApplicantSolicitor2ExternalFlags(Flags.builder().build())
            .caApplicantSolicitor2InternalFlags(Flags.builder().build())
            .caApplicantSolicitor3ExternalFlags(Flags.builder().build())
            .caApplicantSolicitor3InternalFlags(Flags.builder().build())
            .caRespondentSolicitor1ExternalFlags(Flags.builder().build())
            .caRespondentSolicitor1InternalFlags(Flags.builder().build())

            .caApplicantBarrister1ExternalFlags(Flags.builder().build())
            .caApplicantBarrister1InternalFlags(Flags.builder().build())
            .caApplicantBarrister2ExternalFlags(Flags.builder().build())
            .caApplicantBarrister2InternalFlags(Flags.builder().build())
            .caApplicantBarrister3ExternalFlags(Flags.builder().build())
            .caApplicantBarrister3InternalFlags(Flags.builder().build())
            .caRespondentBarrister1ExternalFlags(Flags.builder().build())
            .caRespondentBarrister1InternalFlags(Flags.builder().build())
            .build();
    }
}
