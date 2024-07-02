package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.TestUtil;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.COURT;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PRL_COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.services.StmtOfServImplService.RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL;
import static uk.gov.hmcts.reform.prl.services.StmtOfServImplService.RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String eventId = "1234567891234567";

    public static final String accessCode = "123456";

    @InjectMocks
    private CaseService caseService;
    @Mock
    CaseRepository caseRepository;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    IdamClient idamClient;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CcdCoreCaseDataService coreCaseDataService;

    @Mock
    HearingService hearingService;

    @Mock
    CaseDataMapper caseDataMapper;

    @Mock
    private UserService userService;

    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;

    private CaseData caseData;
    private CaseData caseData2;

    private CaseData caseData3;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;
    private CitizenUpdatedCaseData citizenUpdatedCaseData;

    private final UUID testUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private ServedApplicationDetails servedApplicationDetails;

    private ServedApplicationDetails servedApplicationDetailsEmailOnly;

    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;

    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList1;

    @Before
    public void setup() {
        partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("test")
            .email("")
            .citizenSosObject(CitizenSos.builder().build())
            .user(User.builder().email("").idamId("").build())
            .build();
        caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(Yes)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData2 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(No)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();

        caseData3 = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .respondentsFL401(partyDetails)
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(No)
                                                                         .accessCode("123").build()).build()))
            .build();


        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        userDetails = UserDetails.builder().id("tesUserId").email("testEmail").build();
        citizenUpdatedCaseData = CitizenUpdatedCaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .partyDetails(PartyDetails.builder()
                              .firstName("Test")
                              .lastName("User")
                              .user(User.builder()
                                        .email("test@gmail.com")
                                        .idamId("123")
                                        .solicitorRepresented(Yes)
                                        .build())
                              .citizenSosObject(CitizenSos.builder()
                                                    .partiesServed("123,234,1234")
                                                    .build())
                              .build())
            .partyType(PartyEnum.applicant)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(caseRepository.getCase(Mockito.anyString(), Mockito.anyString())).thenReturn(caseDetails);
        when(caseRepository.updateCase(any(), any(), any(), any())).thenReturn(caseDetails);
        when(idamClient.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(coreCaseDataApi.getCase(any(), any(), any())).thenReturn(caseDetails);
        when(coreCaseDataService.startUpdate("", null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(coreCaseDataService.startUpdate(null, null, "", true)).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());


        CaseData caseData1 = CaseData.builder().id(12345L).serviceOfApplication(ServiceOfApplication.builder()
                                                                                    .unServedRespondentPack(SoaPack.builder().packDocument(
                                                                                        List.of(element(Document.builder().documentBinaryUrl(
                                                                                            "abc").documentFileName("ddd").build()))).build())
                                                                                    .build()).build();

        SoaPack unServedRespondentPack = caseData1.getServiceOfApplication().getUnServedRespondentPack();
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE));
        String formatter = DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS).format(zonedDateTime);
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        bulkPrintDetails.add(element(BulkPrintDetails.builder()
                                         .servedParty(PRL_COURT_ADMIN)
                                         .bulkPrintId(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST)
                                         .printedDocs(String.join(",", unServedRespondentPack
                                             .getPackDocument().stream()
                                             .map(Element::getValue)
                                             .map(Document::getDocumentFileName).toList()))
                                         .printDocs(unServedRespondentPack.getPackDocument())
                                         .partyIds("00000000-0000-0000-0000-000000000000")
                                         .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                        .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                         .build()));


        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        emailNotificationDetails.add(element(EmailNotificationDetails.builder()
                                                 .emailAddress(RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL)
                                                 .servedParty(PRL_COURT_ADMIN)
                                                 .docs(unServedRespondentPack.getPackDocument())
                                                 .partyIds("00000000-0000-0000-0000-000000000000")
                                                 .attachedDocs(String.join(",", unServedRespondentPack
                                                     .getPackDocument().stream()
                                                     .map(Element::getValue)
                                                     .map(Document::getDocumentFileName).toList()))
                                                 .timeStamp(DateTimeFormatter.ofPattern(DD_MMM_YYYY_HH_MM_SS)
                                                                .format(ZonedDateTime.now(ZoneId.of(EUROPE_LONDON_TIME_ZONE))))
                                                 .build()));

        servedApplicationDetails = ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy("FullName")
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, bulkPrintDetails))
            .whoIsResponsible(COURT)
            .bulkPrintDetails(bulkPrintDetails).build();
        servedApplicationDetailsEmailOnly = ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy("FullName")
            .emailNotificationDetails(emailNotificationDetails)
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, null))
            .whoIsResponsible(COURT)
            .build();

        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));
        finalServedApplicationDetailsList1 = List.of(element(servedApplicationDetailsEmailOnly));
    }

    @Test
    public void testGetCase() {
        assertNotNull(caseService.getCase("", ""));
    }

    @Test
    public void testRetrieveCases() {
        assertNotNull(caseService.retrieveCases("", ""));
    }

    @Test
    public void testRetrieveCasesTwoParams() {
        assertNotNull(caseService.retrieveCases("", ""));
    }

    @Test
    public void shouldCreateCase() {
        //Given
        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        userDetails = UserDetails
            .builder()
            .roles(List.of(COURT_ADMIN_ROLE))
            .email("test@gmail.com")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder().id(
            1234567891234567L).data(stringObjectMap).build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseRepository.createCase(authToken, caseData)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.createCase(caseData, authToken);

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void testGetPartyCaseFlags() {
        User user1 = User.builder().idamId("applicant-1").build();
        User user2 = User.builder().idamId("respondent-1").build();
        User user3 = User.builder().idamId("respondent-2").build();
        PartyDetails applicant = PartyDetails.builder().user(user1).email("testappl@hmcts.net").firstName(
            "Applicant 1 FN").lastName("Applicant 1 LN").build();
        PartyDetails respondent1 = PartyDetails.builder().user(user2).email("testresp1@hmcts.net").firstName(
            "Respondent 1 FN").lastName("Respondent 1 LN").build();
        PartyDetails respondent2 = PartyDetails.builder().user(user3).email("testresp2@hmcts.net").firstName(
            "Respondent 2 FN").lastName("Respondent 2 LN").build();

        FlagDetail flagDetailRequestForFillingForms = FlagDetail.builder()
            .name("Support filling in forms")
            .name_cy("Cymorth i lenwi ffurflenni")
            .hearingRelevant(No)
            .flagCode("RA0018")
            .status("Requested")
            .availableExternally(Yes)
            .build();
        FlagDetail flagDetailRequestForHearing = FlagDetail.builder()
            .name("Private waiting area")
            .name_cy("Ystafell aros breifat")
            .hearingRelevant(Yes)
            .flagCode("RA0033")
            .status("Requested")
            .availableExternally(Yes)
            .build();

        Flags applicant1PartyFlags = Flags.builder().roleOnCase("Applicant 1").partyName("Applicant 1 FN Applicant 1 LN").details(
            Collections.singletonList(element(flagDetailRequestForFillingForms))).build();
        Flags respondent1PartyFlags = Flags.builder().roleOnCase("Respondent 1").partyName(
            "Respondent 1 FN Respondent 1 LN").details(Collections.singletonList(element(flagDetailRequestForHearing))).build();
        Flags respondent2PartyFlags = Flags.builder().roleOnCase("Respondent 2").partyName(
            "Respondent 2 FN Respondent 2 LN").details(Arrays.asList(
            element(flagDetailRequestForFillingForms),
            element(flagDetailRequestForHearing)
        )).build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .caseTypeOfApplication("C100")
            .applicants(Collections.singletonList(element(applicant)))
            .respondents(Arrays.asList(element(respondent1), element(respondent2)))
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(applicant1PartyFlags).caRespondent1ExternalFlags(
                respondent1PartyFlags).caRespondent2ExternalFlags(respondent2PartyFlags).build())
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetailsCaseFlags = CaseDetails.builder()
            .id(1234567891234567L)
            .data(stringObjectMap)
            .build();
        when(caseService.getCase(authToken, caseId)).thenReturn(caseDetailsCaseFlags);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        // Unhappy path - when the request is valid, but party details is invalid.
        Flags invalidUserExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "applicant-2");
        Assert.assertNull(invalidUserExternalFlag);

        // Happy path 1 - when the request is valid and respondent party external flags is retrieved from the existing case data.
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CARESPONDENT,
            1
        )).thenReturn("caRespondent2ExternalFlags");
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(respondent2PartyFlags);
        Flags respondentExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "respondent-2");
        Assert.assertNotNull(respondentExternalFlag);
        Assert.assertEquals("Respondent 2 FN Respondent 2 LN", respondentExternalFlag.getPartyName());

        // Happy path 2 - when the request is valid and applicant party external flags is retrieved from the existing case data.
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            "C100",
            PartyRole.Representing.CAAPPLICANT,
            0
        )).thenReturn("caApplicant1ExternalFlags");
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(applicant1PartyFlags);
        Flags applicantExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "applicant-1");
        Assert.assertNotNull(applicantExternalFlag);
        Assert.assertEquals(applicant1PartyFlags, applicantExternalFlag);
    }

    @Test
    public void shouldUpdateCaseWithCaseName() throws IOException {

        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
            .build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .applicantCaseName("applicantLN1 V respLN1")
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(any())).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
                                                                CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseWithCaseNameButNoApplicantOrRespondentDetails() throws IOException {

        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .build();

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(any())).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When

        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
                                                                CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseWithCaseNameButNoC100RebuildData() throws IOException {

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(any())).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
                                                                CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void shouldUpdateCaseWithCaseNameButCaseNameExists() throws IOException {

        CaseData caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();
        UserDetails userDetails = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData.toBuilder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        when(caseDataMapper.buildUpdatedCaseData(any())).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData, CITIZEN_CASE_UPDATE)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData, authToken, caseId,
                                                                CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }

    @Test
    public void getCaseWithHearing() {
        when(coreCaseDataService.findCaseById(authToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(hearingService.getHearings(authToken, "123")).thenReturn(Hearings.hearingsWith().build());
        CaseDataWithHearingResponse caseDataWithHearingResponse = caseService.getCaseWithHearing(authToken, caseId, "yes");
        assertNotNull(caseDataWithHearingResponse.getHearings());
    }

    @Test
    public void getCaseWithHearingHearingNotNeeded() {
        when(coreCaseDataService.findCaseById(authToken, caseId)).thenReturn(caseDetails);
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(hearingService.getHearings(authToken, caseId)).thenReturn(Hearings.hearingsWith().build());
        CaseDataWithHearingResponse caseDataWithHearingResponse = caseService.getCaseWithHearing(authToken, caseId, "dud");
        assertNull(caseDataWithHearingResponse.getHearings());
    }

    @Test
    public void testGetCitizenDocuments() {
        //Given
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .miamCertificateDocument(Document.builder().build())
            .documentParty("applicant")
            .categoryId("miamCertificate")
            .uploadedBy("test")
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000000")
            .documentUploadedDate(LocalDateTime.now())
            .build();
        caseData = caseData.toBuilder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .legalProfUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .cafcassUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .courtStaffUploadDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .citizenUploadedDocListDocTab(List.of(element(quarantineLegalDoc)))
                                 .confidentialDocuments(List.of(element(quarantineLegalDoc)))
                                 .restrictedDocuments(List.of(element(quarantineLegalDoc)))
                                 .build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(List.of(element(quarantineLegalDoc)))
                                           .build())
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();
        Map<String, Object> map = new HashMap<>();
        map.put("miamCertificateDocument", quarantineLegalDoc);

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(map.get("miamCertificateDocument"), Document.class))
            .thenReturn(quarantineLegalDoc.getMiamCertificateDocument());

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenDocuments().isEmpty());
        assertEquals(7, citizenDocumentsManagement.getCitizenDocuments().size());
    }

    @Test
    public void testEmptyCitizenDocumentsWhenNoDocs() {
        //Given
        caseData = caseData.toBuilder().state(State.DECISION_OUTCOME).build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(citizenDocumentsManagement.getCitizenDocuments().isEmpty());
    }

    @Test
    public void testFilterNonAccessibleCitizenDocuments() {
        //Given
        QuarantineLegalDoc cafcassDoc = QuarantineLegalDoc.builder()
            .uploaderRole(CAFCASS)
            .build();
        QuarantineLegalDoc otherPartyDoc = QuarantineLegalDoc.builder()
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000001")
            .build();
        caseData = caseData.toBuilder()
            .reviewDocuments(ReviewDocuments.builder()
                                 .confidentialDocuments(List.of(element(otherPartyDoc)))
                                 .restrictedDocuments(List.of(element(cafcassDoc)))
                                 .build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(List.of(element(otherPartyDoc)))
                                           .build())
            .state(State.DECISION_OUTCOME)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue()))
            .build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(citizenDocumentsManagement.getCitizenDocuments().isEmpty());
    }

    @Test
    public void testGetCitizenApplicantOrdersC100() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .orderCollection(List.of(element(orderDetails)))
            .applicants(List.of(element(testUuid, partyDetails)))
            .state(State.DECISION_OUTCOME)
            .serviceOfApplication(ServiceOfApplication.builder().unServedRespondentPack(SoaPack.builder().packDocument(
                List.of(element(Document.builder().documentBinaryUrl(
                    "abc").documentFileName("ddd").build()))).build()).build())
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList1)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }

    @Test
    public void testGetCitizenRespondentOrdersC100() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("C100")
            .state(State.DECISION_OUTCOME)
            .orderCollection(List.of(element(orderDetails)))
            .respondents(List.of(element(testUuid, partyDetails)))
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList1)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }

    @Test
    public void testGetCitizenApplicantOrdersFL401() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .partyId(testUuid)
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .state(State.DECISION_OUTCOME)
            .orderCollection(List.of(element(orderDetails)))
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .serviceOfApplication(ServiceOfApplication.builder().unServedRespondentPack(SoaPack.builder().packDocument(
                List.of(element(Document.builder().documentBinaryUrl(
                    "abc").documentFileName("ddd").build()))).build()).build())
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }

    @Test
    public void testGetCitizenRespondentOrdersFL401() {
        //Given
        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        OrderDetails orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        partyDetails = partyDetails.toBuilder()
            .partyId(testUuid)
            .user(User.builder()
                      .idamId("00000000-0000-0000-0000-000000000000").build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication("FL401")
            .orderCollection(List.of(element(orderDetails)))
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(partyDetails)
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .build();
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
    }
}
