package uk.gov.hmcts.reform.prl.controllers.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.clients.ccd.records.CitizenUpdatePartyDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenPartyDetailsMapper;
import uk.gov.hmcts.reform.prl.mapper.citizen.CitizenRespondentAohElementsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.services.C8ArchiveService;
import uk.gov.hmcts.reform.prl.services.CaseNameService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityC8RefugeService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;


@ExtendWith(MockitoExtension.class)
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
    ConfidentialityC8RefugeService confidentialityC8RefugeService;
    @Mock
    ConfidentialityTabService confidentialityTabService;
    @Mock
    DocumentGenService documentGenService;
    @Mock
    C8ArchiveService c8ArchiveService;
    @Mock
    CaseNameService caseNameService;
    @Mock
    ObjectMapper objectMapper;

    @BeforeEach
    public void setUpCA() throws IOException {
        c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .build();
        partyDetails = PartyDetails.builder()
            .partyId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
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
            .partyId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
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
                              .partyId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                              .firstName("Test")
                              .lastName("User")
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed(List.of("123", "234", "1234"))
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
                                                    .partiesServed(List.of("123", "234", "1234"))
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
                                                    .partiesServed(List.of("123", "234", "1234"))
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
        doNothing().when(c100RespondentSolicitorService).populateConfidentialAndMiscDataMap(any(), any(),
                                                                                            anyString());
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
                                                    .partiesServed(List.of("123", "234", "1234"))
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

    @ParameterizedTest
    @CsvSource({
        "KEEP_DETAILS_PRIVATE",
        "CONSENT_TO_APPLICATION",
        "EVENT_RESPONDENT_MIAM",
        "LEGAL_REPRESENTATION",
        "EVENT_INTERNATIONAL_ELEMENT",
        "EVENT_RESPONDENT_AOH",
        "CITIZEN_REMOVE_LEGAL_REPRESENTATIVE",
        "SUPPORT_YOU_DURING_CASE",
        "CITIZEN_CONTACT_PREFERENCE",
        "CITIZEN_INTERNAL_FLAG_UPDATES",
        "LINK_CITIZEN"
    })
    void testMapUpdatedPartyDetailsWithVariousEvents(String caseEventName) throws Exception {
        setUpDa();
        CaseEvent caseEvent = CaseEvent.valueOf(caseEventName);
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(
            caseData, updateCaseData, caseEvent, authToken
        );
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
    public void testMapUpdatedPartyDetailsCaseEventConfirmDetailsAddressIsYes() throws Exception {
        setUpCA();

        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .partyId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                              .firstName("Test")
                              .lastName("User")
                              .isAtAddressLessThan5Years(YesOrNo.Yes)
                              .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed(List.of("123,234,1234"))
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)

            .build();

        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(
            caseData,
            updateCaseData,
            CaseEvent.CONFIRM_YOUR_DETAILS,
            authToken
        );
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventConfirmDetailsAddressIsNo() throws IOException {
        setUpCA();
        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .partyId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                .firstName("Test")
                .lastName("User")
                .isAtAddressLessThan5Years(YesOrNo.No)
                .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.no)
                .response(Response.builder().build())
                .user(User.builder()
                    .email("test@gmail.com")
                    .idamId("123")
                    .solicitorRepresented(YesOrNo.Yes)
                    .build())
                .citizenSosObject(CitizenSos.builder()
                    .partiesServed(List.of("123,234,1234"))
                    .build())
                .build())
            .partyType(PartyEnum.applicant)
            .build();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(caseData,updateCaseData,
            CaseEvent.CONFIRM_YOUR_DETAILS,
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

    @ParameterizedTest
    @CsvSource({
        "resp.json",
        "resp2.json"
    })
    void testBuildUpdatedCaseDataWithVariousRespondents(String respondentDetailsFile) throws IOException {
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
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/" + respondentDetailsFile))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .applicantPcqId("123")
            .c100RebuildHelpWithFeesDetails(TestUtil.readFileFrom("classpath:c100-rebuild/hwf.json"))
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();
        CaseData caseDataResult = citizenPartyDetailsMapper.buildUpdatedCaseData(caseData,c100RebuildData);
        assertNotNull(caseDataResult);
        verify(caseNameService).getCaseNameForCA(anyString(), anyString());
    }

    @Test
    public void testBuildUpdatedCaseDataContainsCaseAccessCategory() throws IOException {
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
            .applicantPcqId("123")
            .c100RebuildHelpWithFeesDetails(TestUtil.readFileFrom("classpath:c100-rebuild/hwf.json"))
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();
        CaseData caseDataResult = citizenPartyDetailsMapper.buildUpdatedCaseData(caseData,c100RebuildData);
        assertNotNull(caseDataResult.getCaseAccessCategory());
        assertEquals(caseData.getCaseTypeOfApplication(), caseDataResult.getCaseAccessCategory());
        verify(caseNameService).getCaseNameForCA(anyString(), anyString());
    }


    @Test
    public void testBuildUpdatedCaseDataWhereAddressIsDontKnow() throws IOException {
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
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp2.json"))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .applicantPcqId("123")
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();
        CaseData caseDataResult = citizenPartyDetailsMapper.buildUpdatedCaseData(caseData,c100RebuildData);
        assertNotNull(caseDataResult);
        verify(caseNameService).getCaseNameForCA(anyString(), anyString());
    }

    @Test
    public void testGetC100RebuildCaseDataMap() throws IOException {
        Map<String, Object> caseDataResult = citizenPartyDetailsMapper.getC100RebuildCaseDataMap(caseData);
        assertNotNull(caseDataResult);
    }

    @Test
    public void testBuildUpdatedCaseDataForMiam() throws IOException {
        c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
            .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
            .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam1.json"))
            .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
            .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
            .c100RebuildOtherPersonsDetails(TestUtil.readFileFrom("classpath:c100-rebuild/oprs.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp1.json"))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .build();
        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();
        CaseData caseDataResult = citizenPartyDetailsMapper.buildUpdatedCaseData(caseData,c100RebuildData);
        assertNotNull(caseDataResult);
        verify(caseNameService).getCaseNameForCA(anyString(), anyString());
    }

    @Test
    public void testMapUpdatedPartyDetailsDaRespondentForV3() throws Exception {
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
                                                    .partiesServed(List.of("123", "234", "1234"))
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
            .taskListVersion(TASK_LIST_VERSION_V3)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(Arrays.asList(element(applicant1)))
            .c100RebuildData(c100RebuildData)
            .build();
        doNothing().when(c100RespondentSolicitorService).populateConfidentialAndMiscDataMap(any(), any(),
                                                                                            anyString()
        );
        when(updatePartyDetailsService.checkIfConfidentialityDetailsChangedRespondent(any(), any())).thenReturn(true);
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(
            caseData,
            updateCaseData,
            CaseEvent.KEEP_DETAILS_PRIVATE,
            authToken
        );
        assertNotNull(citizenUpdatePartyDataContent);
    }

    @ParameterizedTest(name = "{index}: {2}")
    @CsvSource({
        "Yes,Yes,Other Person Lives In Refuge",
        "No,No,Other Person Address Unknown"
    })
    void testBuildUpdatedCaseDataOtherPersonConfidentiality(String isAddressConfidentialStr,
                                                            String liveInRefugeStr, String testName) throws IOException {
        YesOrNo isAddressConfidential = YesOrNo.valueOf(isAddressConfidentialStr);
        YesOrNo liveInRefuge = YesOrNo.valueOf(liveInRefugeStr);

        c100RebuildData = C100RebuildData.builder()
            .c100RebuildInternationalElements(TestUtil.readFileFrom("classpath:c100-rebuild/ie.json"))
            .c100RebuildHearingWithoutNotice(TestUtil.readFileFrom("classpath:c100-rebuild/hwn.json"))
            .c100RebuildTypeOfOrder(TestUtil.readFileFrom("classpath:c100-rebuild/too.json"))
            .c100RebuildOtherProceedings(TestUtil.readFileFrom("classpath:c100-rebuild/op.json"))
            .c100RebuildMaim(TestUtil.readFileFrom("classpath:c100-rebuild/miam1.json"))
            .c100RebuildHearingUrgency(TestUtil.readFileFrom("classpath:c100-rebuild/hu.json"))
            .c100RebuildChildDetails(TestUtil.readFileFrom("classpath:c100-rebuild/cd.json"))
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildOtherChildrenDetails(TestUtil.readFileFrom("classpath:c100-rebuild/ocd.json"))
            .c100RebuildReasonableAdjustments(TestUtil.readFileFrom("classpath:c100-rebuild/ra.json"))
            .c100RebuildOtherPersonsDetails(
                isAddressConfidential == YesOrNo.Yes ? TestUtil.readFileFrom("classpath:c100-rebuild/oprs1.json")
                    : TestUtil.readFileFrom("classpath:c100-rebuild/oprs2.json")
            )
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp1.json"))
            .c100RebuildConsentOrderDetails(TestUtil.readFileFrom("classpath:c100-rebuild/co.json"))
            .applicantPcqId("123")
            .build();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c100RebuildData(c100RebuildData)
            .build();

        Element<PartyDetails> otherPartyElement = element(
            PartyDetails.builder()
                .firstName("c1")
                .lastName("c1")
                .liveInRefuge(liveInRefuge)
                .refugeConfidentialityC8Form(
                    Document.builder()
                        .documentUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/79e841a4-f232-4f2e-9e86-e4fc8f70fcac")
                        .documentBinaryUrl("http://dm-store-aat.service.core-compute-aat.internal/documents/79e841a4-f232-4f2e-9e86-e4fc8f70fcac/binary")
                        .documentFileName("Sample_doc_2.pdf")
                        .documentCreatedOn(Date.from(ZonedDateTime.parse("2024-05-14T14:13:44Z").toInstant()))
                        .build()
                )
                .address(
                    Address.builder()
                        .addressLine1("add1")
                        .addressLine2("add2")
                        .addressLine3("add3")
                        .postTown("")
                        .county("thames")
                        .country("uk")
                        .postCode("tw22tr8")
                        .build()
                )
                .isAddressConfidential(isAddressConfidential)
                .liveInRefuge(liveInRefuge)
                .build()
        );

        when(confidentialityTabService.updateOtherPeopleConfidentiality(any(), any()))
            .thenReturn(Collections.singletonList(otherPartyElement));

        CaseData caseDataResult = citizenPartyDetailsMapper.buildUpdatedCaseData(caseData, c100RebuildData);
        assertNotNull(caseDataResult);
        assertEquals(isAddressConfidential, caseDataResult.getOtherPartyInTheCaseRevised().get(0).getValue().getIsAddressConfidential());
        assertEquals(liveInRefuge, caseDataResult.getOtherPartyInTheCaseRevised().get(0).getValue().getLiveInRefuge());
        verify(caseNameService).getCaseNameForCA(anyString(), anyString());
    }

    @Test
    public void testUpdatedPartyDetailsBasedOnEvent() {
        PartyDetails partyDetails1 = partyDetails.toBuilder().response(null).build();
        PartyDetails partyDetails2 = partyDetails.toBuilder()
            .response(Response.builder().responseToAllegationsOfHarm(ResponseToAllegationsOfHarm.builder().build()).build())
            .build();
        PartyDetails updatedPartyDetailsBasedOnEvent = citizenPartyDetailsMapper.getUpdatedPartyDetailsBasedOnEvent(partyDetails2, partyDetails1,
                                                                     CaseEvent.CITIZEN_RESPONSE_TO_AOH,
                                                                     List.of(element(ChildDetailsRevised.builder().build())));
        assertNotNull(updatedPartyDetailsBasedOnEvent);

    }

    @Test
    public void testMapUpdatedPartyDetailsCaseEventConfirmDetailsWithSafeTimeToCall() throws IOException {
        setUpDa();
        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .isAtAddressLessThan5Years(YesOrNo.Yes)
                              .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .response(Response.builder().safeToCallOption("7pm").build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed(List.of("123,234,1234"))
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(
            caseData,
            updateCaseData,
            CaseEvent.CONFIRM_YOUR_DETAILS,
            authToken
        );
        assertNotNull(citizenUpdatePartyDataContent);
        assertEquals("7pm", citizenUpdatePartyDataContent.updatedCaseDataMap().get("daApplicantContactInstructions"));
        assertEquals("7pm", citizenUpdatePartyDataContent.updatedCaseData().getApplicantsFL401().getResponse().getSafeToCallOption());
    }

    @Test
    public void testMapUpdatedPartyDetailsWithSafeTimeToCallEmptyString() throws IOException {
        setUpDa();
        updateCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .isAtAddressLessThan5Years(YesOrNo.Yes)
                              .isAtAddressLessThan5YearsWithDontKnow(YesNoDontKnow.yes)
                              .response(Response.builder().build())
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(YesOrNo.Yes)
                                        .build())
                              .response(Response.builder().safeToCallOption("").build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed(List.of("123,234,1234"))
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
        CitizenUpdatePartyDataContent citizenUpdatePartyDataContent = citizenPartyDetailsMapper.mapUpdatedPartyDetails(
            caseData,
            updateCaseData,
            CaseEvent.CONFIRM_YOUR_DETAILS,
            authToken
        );
        assertNotNull(citizenUpdatePartyDataContent);
        assertNull(citizenUpdatePartyDataContent.updatedCaseDataMap().get("daApplicantContactInstructions"));
        assertEquals("", citizenUpdatePartyDataContent.updatedCaseData().getApplicantsFL401().getResponse().getSafeToCallOption());
    }

    @Test
    void testGetConfidentialFieldReturnsValueWhenConfidentialFields() {
        assertEquals("test", citizenPartyDetailsMapper.getConfidentialField(YesOrNo.Yes, "test"));
    }

    @Test
    void testGetConfidentialFieldReturnsNullWhenNotConfidentialFields() {
        assertEquals(null, citizenPartyDetailsMapper.getConfidentialField(YesOrNo.No, "test"));
    }

    @Test
    void testCreateApplicantConfidentialDetailsForCaseData() {
        Address address = Address.builder()
            .addressLine1("address line 1")
            .postTown("town")
            .postCode("postcode")
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .address(address)
            .email("email@test.com")
            .phoneNumber("123456789")
            .isAddressConfidential(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();


        List<Element<ApplicantConfidentialityDetails>> result =
            citizenPartyDetailsMapper.createApplicantConfidentialDetailsForCaseData(partyDetails);

        org.junit.jupiter.api.Assertions.assertNotNull(result);
        org.junit.jupiter.api.Assertions.assertEquals(1, result.size());
        uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails details = result.get(0).getValue();
        org.junit.jupiter.api.Assertions.assertEquals(address, details.getAddress());
        org.junit.jupiter.api.Assertions.assertNull(details.getEmail());
        org.junit.jupiter.api.Assertions.assertEquals("123456789", details.getPhoneNumber());
    }

    @Test
    void testAddUpdatedApplicantConfidentialFieldsToCaseDataFL401() throws IOException {
        setUpDa();

        PartyDetails updatedPartyDetails = PartyDetails.builder()
            .address(partyDetails.getAddress())
            .email(partyDetails.getEmail())
            .phoneNumber(partyDetails.getPhoneNumber())
            .isAddressConfidential(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .partyDetails(updatedPartyDetails)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();

        CaseData result = citizenPartyDetailsMapper.addUpdatedApplicantConfidentialFieldsToCaseDataFL401(caseData, citizenUpdatedCaseData);

        assertNotNull(result);
        assertEquals(YesOrNo.Yes, result.getApplicantsFL401().getIsAddressConfidential());
        assertEquals(YesOrNo.No, result.getApplicantsFL401().getIsEmailAddressConfidential());
        assertEquals(YesOrNo.Yes, result.getApplicantsFL401().getIsPhoneNumberConfidential());

        List<Element<ApplicantConfidentialityDetails>> confDetails = result.getApplicantsConfidentialDetails();
        assertNotNull(confDetails);
        assertEquals(1, confDetails.size());
        ApplicantConfidentialityDetails details = confDetails.get(0).getValue();
        assertEquals(updatedPartyDetails.getAddress(), details.getAddress());
        assertNull(details.getEmail());
        assertEquals(updatedPartyDetails.getPhoneNumber(), details.getPhoneNumber());
    }

    @Test
    void testAddUpdatedApplicantConfidentialFieldsToCaseDataC100() throws IOException {
        setUpCA();

        PartyDetails updatedPartyDetails = partyDetails.toBuilder()
            .isAddressConfidential(YesOrNo.Yes)
            .isEmailAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();

        CitizenUpdatedCaseData citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .partyDetails(updatedPartyDetails)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();

        CaseData result = citizenPartyDetailsMapper.addUpdatedApplicantConfidentialFieldsToCaseDataC100(caseData, citizenUpdatedCaseData);

        assertNotNull(result);
        List<Element<ApplicantConfidentialityDetails>> confDetails = result.getApplicantsConfidentialDetails();
        assertNotNull(confDetails);
        assertEquals(1, confDetails.size());
        ApplicantConfidentialityDetails details = confDetails.get(0).getValue();
        assertEquals(updatedPartyDetails.getAddress(), details.getAddress());
        assertNull(details.getEmail());
        assertEquals(updatedPartyDetails.getPhoneNumber(), details.getPhoneNumber());
    }

    @ParameterizedTest
    @MethodSource("confidentialityTestScenarios")
    @DisplayName("Update applicant confidentiality fields for C100 cases")
    void testAddUpdatedApplicantConfidentialFieldsToCaseDataC100(String testName,
                                                                 UUID targetPartyId,
                                                                 YesOrNo updatedAddressConf,
                                                                 YesOrNo updatedEmailConf,
                                                                 YesOrNo updatedPhoneConf,
                                                                 List<Element<PartyDetails>> existingApplicants,
                                                                 int expectedApplicantCount) throws IOException {
        setUpCA();

        PartyDetails updatedPartyDetails = createPartyDetailsWithConfidentiality(
            targetPartyId, "Updated", "User", updatedAddressConf, updatedEmailConf, updatedPhoneConf);

        CitizenUpdatedCaseData citizenUpdatedCaseData = createCitizenUpdatedCaseData(updatedPartyDetails);
        CaseData caseData = createCaseDataWithApplicants(existingApplicants);

        CaseData result = citizenPartyDetailsMapper.addUpdatedApplicantConfidentialFieldsToCaseDataC100(
            caseData, citizenUpdatedCaseData);

        assertNotNull(result);
        if (existingApplicants != null) {
            assertNotNull(result.getApplicants());
            assertEquals(expectedApplicantCount, result.getApplicants().size());

            PartyDetails updatedApplicant = result.getApplicants().stream()
                .map(Element::getValue)
                .filter(p -> targetPartyId.equals(p.getPartyId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Updated applicant not found"));

            assertEquals(updatedAddressConf, updatedApplicant.getIsAddressConfidential());
            assertEquals(updatedEmailConf, updatedApplicant.getIsEmailAddressConfidential());
            assertEquals(updatedPhoneConf, updatedApplicant.getIsPhoneNumberConfidential());
        } else {
            assertNotNull(result.getApplicantsConfidentialDetails());
            assertEquals(0, result.getApplicantsConfidentialDetails().size());
        }
    }

    private static final UUID APPLICANT_1_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID APPLICANT_2_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ELEMENT_1_UUID = UUID.fromString("aaaaaaaa-1111-1111-1111-111111111111");
    private static final UUID ELEMENT_2_UUID = UUID.fromString("bbbbbbbb-2222-2222-2222-222222222222");

    public static PartyDetails createPartyDetailsWithConfidentiality(UUID partyId,
                                                                         String firstName,
                                                                         String lastName,
                                                                         YesOrNo addressConfidential,
                                                                         YesOrNo emailConfidential,
                                                                         YesOrNo phoneConfidential) {
        return PartyDetails.builder()
            .partyId(partyId)
            .firstName(firstName)
            .lastName(lastName)
            .isAddressConfidential(addressConfidential)
            .isEmailAddressConfidential(emailConfidential)
            .isPhoneNumberConfidential(phoneConfidential)
            .build();
    }

    public static CitizenUpdatedCaseData createCitizenUpdatedCaseData(PartyDetails partyDetails) {
        return CitizenUpdatedCaseData.builder()
            .partyDetails(partyDetails)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
    }

    public static CaseData createCaseDataWithApplicants(List<Element<PartyDetails>> applicants) {
        return CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .build();
    }

    static Stream<Arguments> confidentialityTestScenarios() {
        return Stream.of(
            Arguments.of(
                "Single applicant - update confidentiality",
                APPLICANT_1_UUID,
                YesOrNo.Yes, YesOrNo.No, YesOrNo.Yes,
                List.of(element(ELEMENT_1_UUID, createPartyDetailsWithConfidentiality(
                    APPLICANT_1_UUID, "John", "Doe", YesOrNo.Yes, YesOrNo.No, YesOrNo.Yes))),
                1
            ),
            Arguments.of(
                "Multiple applicants - update second applicant",
                APPLICANT_2_UUID,
                YesOrNo.Yes, YesOrNo.Yes, YesOrNo.No,
                Arrays.asList(
                    element(ELEMENT_1_UUID, createPartyDetailsWithConfidentiality(
                        APPLICANT_1_UUID, "John", "Doe", YesOrNo.No, YesOrNo.No, YesOrNo.No)),
                    element(ELEMENT_2_UUID, createPartyDetailsWithConfidentiality(
                        APPLICANT_2_UUID, "Jane", "Smith", YesOrNo.Yes, YesOrNo.Yes, YesOrNo.No))
                ),
                2
            ),
            Arguments.of(
                "Null applicants list",
                APPLICANT_1_UUID,
                YesOrNo.Yes, YesOrNo.No, YesOrNo.Yes,
                null,
                0
            )
        );
    }
}


