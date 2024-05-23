package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenRespondentAohElementsMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@RunWith(MockitoJUnitRunner.class)
public class CitizenPartyDetailsMapperTest {
    @InjectMocks
    private CitizenPartyDetailsMapper citizenPartyDetailsMapper;
    public static final String authToken = "Bearer TestAuthToken";
    private CaseData caseData;

    private CitizenUpdatedCaseData updateCaseData;
    private C100RebuildData c100RebuildData;
    PartyDetails partyDetails;
    @Mock
    C100RespondentSolicitorService c100RespondentSolicitorService;
    @Mock
    UpdatePartyDetailsService updatePartyDetailsService;
    @Mock
    NoticeOfChangePartiesService noticeOfChangePartiesService;
    @Mock
    CitizenRespondentAohElementsMapper citizenAllegationOfHarmMapper;

    @Mock
    ObjectMapper objectMapper;

    @Before
    public void setUpCA() throws IOException {
        c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .build();
        partyDetails = PartyDetails.builder()
            .representativeFirstName("testUser")
            .representativeLastName("test test")
            .response(Response.builder().build())
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .response(Response.builder().build())
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(Arrays.asList(element(applicant1)))

            .c100RebuildData(c100RebuildData)
            .build();

        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
    }

    public void setUpDa() throws IOException {
        c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .build();
        partyDetails = PartyDetails.builder()
            .representativeFirstName("testUser")
            .representativeLastName("test test")
            .response(Response.builder().citizenFlags(CitizenFlags.builder().build()).build())
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)

            .c100RebuildData(c100RebuildData)
            .build();

        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .response(Response.builder().citizenFlags(CitizenFlags.builder().build()).build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
    }

    @Test
    public void testMapUpdatedPartyDetailsEventConfirmDetails() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                       CaseEvent.CONFIRM_YOUR_DETAILS,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsDaRespondent() throws Exception {
        setUpDa();
        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.respondent)
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .response(Response.builder().build())
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(Arrays.asList(element(applicant1)))

            .c100RebuildData(c100RebuildData)
            .build();
        doNothing().when(c100RespondentSolicitorService).checkIfConfidentialDataPresent(any(), any());
        when(updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(any(),any())).thenReturn(true);
        Map<String, Object> updatedCaseData = new HashMap<>();
        Element<PartyDetails> respondent = null;
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                       CaseEvent.KEEP_DETAILS_PRIVATE,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsDa13() throws Exception {
        setUpDa();
        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.respondent)
            .build();

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .response(Response.builder().build())
            .user(User.builder()
                      .email("test@gmail.com")
                      .idamId("123")
                      .solicitorRepresented(YesOrNo.Yes)
                      .build())
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .respondentsFL401(applicant1)
            .c100RebuildData(c100RebuildData)
            .build();
        Map<String, Object> updatedCaseData = new HashMap<>();
        Element<PartyDetails> respondent = null;
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                       CaseEvent.KEEP_DETAILS_PRIVATE,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsEventDetailPrivate() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                       CaseEvent.KEEP_DETAILS_PRIVATE,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsEventConsentApplication() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                 CaseEvent.CONSENT_TO_APPLICATION,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsEventRespMiam() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                CaseEvent.EVENT_RESPONDENT_MIAM,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsEventLegalRep() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                       CaseEvent.LEGAL_REPRESENTATION,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsEventInternational() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                      CaseEvent.EVENT_INTERNATIONAL_ELEMENT,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }


    @Test
    public void testMapUpdatedPartyDetailsCaseEventRespSafetyConcern() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData,updateCaseData,
                                                                                                     CaseEvent.EVENT_RESPONDENT_AOH,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventRemoveLegalRep() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData,updateCaseData,
                                                                                                     CaseEvent.CITIZEN_REMOVE_LEGAL_REPRESENTATIVE,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventSupportCase() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData,updateCaseData,
                                                                                                    CaseEvent.SUPPORT_YOU_DURING_CASE,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventContactPref() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData,updateCaseData,
                                                                                                         CaseEvent.CITIZEN_CONTACT_PREFERENCE,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventCitizenInternalFlag() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                             CaseEvent.CITIZEN_INTERNAL_FLAG_UPDATES,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventConfirmDetails() throws IOException {
        setUpCA();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData,updateCaseData,
                                                                                                                       CaseEvent.CONFIRM_YOUR_DETAILS,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventLinkCitizen() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData, updateCaseData,
                                                                                                                       CaseEvent.LINK_CITIZEN,
                                                                                                                       authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsEventCurrentProceedings() throws IOException {
        setUpDa();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper
            .mapUpdatedPartyDetails(caseData, updateCaseData, CaseEvent.CITIZEN_CURRENT_OR_PREVIOUS_PROCCEDINGS, authToken);
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testBuildUpdatedCaseData() throws IOException {
        c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
            .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
            .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam.json"))
            .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
            .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
            .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();
        CaseData caseDataResult = citizenPartyDetailsMapper.buildUpdatedCaseData(caseData,c100RebuildData);
        assertNotNull(caseDataResult);
    }

    @Test
    public void testGetC100RebuildCaseDataMap() throws IOException {
        Map<String, Object> caseDataResult = citizenPartyDetailsMapper.getC100RebuildCaseDataMap(caseData);
        assertNotNull(caseDataResult);
    }

}

