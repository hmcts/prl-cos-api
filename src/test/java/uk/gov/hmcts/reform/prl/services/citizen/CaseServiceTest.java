package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.config.citizen.DashboardNotificationsConfig;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper;
import uk.gov.hmcts.reform.prl.models.CitizenUpdatedCaseData;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.ServeOrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.caseflags.request.CitizenPartyFlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagDetailRequest;
import uk.gov.hmcts.reform.prl.models.caseflags.request.FlagsRequest;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.citizen.CaseDataWithHearingResponse;
import uk.gov.hmcts.reform.prl.models.citizen.NotificationNames;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401Proceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.ServedParties;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.C2DocumentBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.FM5ReminderNotificationDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.citizen.CitizenDocumentsManagement;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.CitizenSos;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;
import uk.gov.hmcts.reform.prl.models.serviceofdocuments.ServiceOfDocuments;
import uk.gov.hmcts.reform.prl.repositories.CaseRepository;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWAITING_HEARING_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EUROPE_LONDON_TIME_ZONE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_EMAIL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_EMAIL_AND_POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_BY_POST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOS_COMPLETED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.C100_REQUEST_SUPPORT;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_BAILIFF;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PERSONAL_SERVICE_SERVED_BY_CA;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.PRL_COURT_ADMIN;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.UNREPRESENTED_APPLICANT;
import static uk.gov.hmcts.reform.prl.services.StmtOfServImplService.RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_EMAIL;
import static uk.gov.hmcts.reform.prl.services.StmtOfServImplService.RESPONDENT_WILL_BE_SERVED_PERSONALLY_BY_POST;
import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.DATE_FORMATTER_D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS;
import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.OCCUPATION_ORDER;
import static uk.gov.hmcts.reform.prl.services.citizen.CaseService.YYYY_MM_DD;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestAuthToken";
    public static final String caseId = "1234567891234567";
    public static final String eventId = "1234567891234567";

    public static final String accessCode = "123456";

    public static final String CA_SOA_PERSONAL_APPLICANT = "CAN7_SOA_PERSONAL_APPLICANT,CAN9_SOA_PERSONAL_APPLICANT";
    public static final String DA_SOA_PERSONAL_APPLICANT = "DN2_SOA_PERSONAL_APPLICANT";
    public static final String CA_SOA_APPLICANT = "CAN4_SOA_PERSONAL_NON_PERSONAL_APPLICANT";
    public static final String CA_SOA_RESPONDENT = "CAN5_SOA_RESPONDENT";
    public static final String DA_SOA_APPLICANT = "DN1_SOA_PERSONAL_NON_PERSONAL_APPLICANT";
    public static final String DA_SOA_RESPONDENT = "DN3_SOA_RESPONDENT";
    public static final String CA_SOA_SOS_CA_CB_APPLICANT = "CAN8_SOA_SOS_PERSONAL_APPLICANT";
    public static final String DA_SOA_SOS_CA_CB_APPLICANT = "DN5_SOA_SOS_PERSONAL_APPLICANT";
    public static final String ORDER_APPLICANT_RESPONDENT = "CRNF2_APPLICANT_RESPONDENT";
    public static final String ORDER_PERSONAL_APPLICANT = "CRNF3_PERSONAL_SERV_APPLICANT";
    public static final String CA_ORDER_SOS_CA_CB_APPLICANT = "CAN3_ORDER_SOS_CA_CB";
    public static final String DA_ORDER_SOS_CA_CB_APPLICANT = "DN6_ORDER_SOS_CA_CB";
    public static final String DN3_SOA_RESPONDENT = "DN3_SOA_RESPONDENT";

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

    @Mock
    private DashboardNotificationsConfig notificationsConfig;

    @Mock
    private RoleAssignmentService roleAssignmentService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private UserDetails userDetails;
    private Map<String, Object> caseDataMap;
    private PartyDetails partyDetails;
    private CitizenUpdatedCaseData citizenUpdatedCaseData;

    private final UUID testUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private ServedApplicationDetails servedApplicationDetails;

    private ServedApplicationDetails servedApplicationDetailsEmailOnly;

    private ServedApplicationDetails servedApplicationDetailsPostOnly;

    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;

    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList1;
    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsListPostOnly;

    private CaseData citizenCaseData;
    private QuarantineLegalDoc quarantineLegalDoc;
    private QuarantineLegalDoc fm5QuarantineLegalDoc;

    private OrderDetails orderDetails;

    @Before
    public void setup() {
        partyDetails = PartyDetails.builder()
            .firstName("")
            .lastName("test")
            .email("")
            .citizenSosObject(CitizenSos.builder().build())
            .user(User.builder().email("").idamId(TEST_UUID).build())
            .build();
        caseData = CaseData.builder()
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                "00000000-0000-0000-0000-000000000000")).value(partyDetails).build()))
            .caseInvites(List.of(Element.<CaseInvite>builder().value(CaseInvite.builder().isApplicant(Yes)
                                                                         .partyId(testUuid)
                                                                         .accessCode("123").build()).build()))
            .build();
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(123L)
            .state("SUBMITTED_PAID")
            .build();
        userDetails = UserDetails.builder().id("tesUserId").email("testEmail").build();
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
            .whoIsResponsible(PERSONAL_SERVICE_SERVED_BY_CA)
            .bulkPrintDetails(bulkPrintDetails).build();
        servedApplicationDetailsEmailOnly = ServedApplicationDetails.builder().emailNotificationDetails(emailNotificationDetails)
            .servedBy("FullName")
            .emailNotificationDetails(emailNotificationDetails)
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(emailNotificationDetails, null))
            .whoIsResponsible(PERSONAL_SERVICE_SERVED_BY_CA)
            .build();
        servedApplicationDetailsPostOnly = ServedApplicationDetails.builder()
            .servedBy("FullName")
            .servedAt(formatter)
            .modeOfService(CaseUtils.getModeOfService(null, bulkPrintDetails))
            .whoIsResponsible(PERSONAL_SERVICE_SERVED_BY_BAILIFF)
            .bulkPrintDetails(bulkPrintDetails).build();

        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));
        finalServedApplicationDetailsList1 = List.of(element(servedApplicationDetailsEmailOnly));
        finalServedApplicationDetailsListPostOnly = List.of(element(servedApplicationDetailsPostOnly));

        Document document = Document.builder().documentFileName("test").build();
        quarantineLegalDoc = QuarantineLegalDoc.builder()
            .miamCertificateDocument(Document.builder().build())
            .documentParty("applicant")
            .categoryId("miamCertificate")
            .uploadedBy("test")
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000000")
            .documentUploadedDate(LocalDateTime.now())
            .build();
        fm5QuarantineLegalDoc = QuarantineLegalDoc.builder()
            .fm5StatementsDocument(Document.builder().build())
            .documentParty("applicant")
            .categoryId("fm5Statements")
            .uploadedBy("test")
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000000")
            .documentUploadedDate(LocalDateTime.now())
            .build();

        citizenCaseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                    .value(partyDetails).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                                     .value(partyDetails).build()))
            .finalDocument(document)
            .finalWelshDocument(document)
            .c1ADocument(document)
            .c1AWelshDocument(document)
            .dateSubmitted(LocalDateTime.now().format(DateTimeFormatter.ofPattern(YYYY_MM_DD)))
            .existingProceedings(List.of(element(ProceedingDetails.builder().uploadRelevantOrder(document).build())))
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

        //Given
        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();
        Map<String, Object> map = new HashMap<>();
        map.put("miamCertificateDocument", quarantineLegalDoc);
        map.put("fm5StatementsDocument",fm5QuarantineLegalDoc);

        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(fm5QuarantineLegalDoc, Map.class)).thenReturn(map);

        when(objectMapper.convertValue(map.get("miamCertificateDocument"), Document.class))
            .thenReturn(quarantineLegalDoc.getMiamCertificateDocument());
        when(objectMapper.convertValue(map.get("fm5StatementsDocument"), Document.class))
            .thenReturn(fm5QuarantineLegalDoc.getFm5StatementsDocument());

        Map<NotificationNames, String> canMap = Map.of(NotificationNames.SOA_PERSONAL_APPLICANT, CA_SOA_PERSONAL_APPLICANT,
                                                       NotificationNames.SOA_APPLICANT, CA_SOA_APPLICANT,
                                                       NotificationNames.SOA_RESPONDENT, CA_SOA_RESPONDENT,
                                                       NotificationNames.SOA_SOS_CA_CB_APPLICANT, CA_SOA_SOS_CA_CB_APPLICANT,
                                                       NotificationNames.ORDER_PERSONAL_APPLICANT, ORDER_PERSONAL_APPLICANT,
                                                       NotificationNames.ORDER_APPLICANT_RESPONDENT, ORDER_APPLICANT_RESPONDENT,
                                                       NotificationNames.ORDER_SOS_CA_CB_APPLICANT, CA_ORDER_SOS_CA_CB_APPLICANT);
        Map<NotificationNames, String> dnMap = Map.of(NotificationNames.SOA_PERSONAL_APPLICANT, DA_SOA_PERSONAL_APPLICANT,
                                                      NotificationNames.SOA_APPLICANT, DA_SOA_APPLICANT,
                                                      NotificationNames.SOA_RESPONDENT, DA_SOA_RESPONDENT,
                                                      NotificationNames.SOA_SOS_CA_CB_APPLICANT, DA_SOA_SOS_CA_CB_APPLICANT,
                                                      NotificationNames.ORDER_PERSONAL_APPLICANT, ORDER_PERSONAL_APPLICANT,
                                                      NotificationNames.ORDER_APPLICANT_RESPONDENT, ORDER_APPLICANT_RESPONDENT,
                                                      NotificationNames.ORDER_SOS_CA_CB_APPLICANT, DA_ORDER_SOS_CA_CB_APPLICANT);
        when(notificationsConfig.getNotifications())
            .thenReturn(Map.of(C100_CASE_TYPE, canMap, FL401_CASE_TYPE, dnMap));

        userDetails = UserDetails.builder()
            .id("00000000-0000-0000-0000-000000000000")
            .roles(List.of(Roles.CITIZEN.getValue())).build();
        //When
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        ServedParties servedParties = ServedParties.builder()
            .partyId("00000000-0000-0000-0000-000000000000")
            .build();
        orderDetails = OrderDetails.builder()
            .orderDocument(Document.builder().build())
            .orderDocumentWelsh(Document.builder().build())
            .serveOrderDetails(ServeOrderDetails.builder()
                                   .servedParties(List.of(element(servedParties)))
                                   .build())
            .otherDetails(OtherOrderDetails.builder().createdBy("test").build())
            .build();
        caseData = caseData.toBuilder()
            .orderCollection(List.of(element(orderDetails)))
            .build();
        partyDetails = partyDetails.toBuilder()
            .partyId(testUuid)
            .user(User.builder()
                      .idamId(testUuid.toString()).build())
            .build();

        when(hearingService.getHearings(anyString(), anyString()))
            .thenReturn(Hearings.hearingsWith().caseHearings(List.of(
                CaseHearing.caseHearingWith().hmcStatus(AWAITING_HEARING_DETAILS).build(),
                CaseHearing.caseHearingWith().hmcStatus(LISTED).build()
            )).build());
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
            .caseTypeOfApplication(C100_CASE_TYPE)
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
            C100_CASE_TYPE,
            PartyRole.Representing.CARESPONDENT,
            1
        )).thenReturn("caRespondent2ExternalFlags");
        when(objectMapper.convertValue(Mockito.any(), Mockito.eq(Flags.class))).thenReturn(respondent2PartyFlags);
        Flags respondentExternalFlag = caseService.getPartyCaseFlags(authToken, caseId, "respondent-2");
        Assert.assertNotNull(respondentExternalFlag);
        Assert.assertEquals("Respondent 2 FN Respondent 2 LN", respondentExternalFlag.getPartyName());

        // Happy path 2 - when the request is valid and applicant party external flags is retrieved from the existing case data.
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(
            C100_CASE_TYPE,
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
    public void shouldUpdateCaseWhenNonCitizenEventId() throws IOException {

        C100RebuildData c100RebuildData = C100RebuildData.builder()
            .c100RebuildApplicantDetails(TestUtil.readFileFrom("classpath:c100-rebuild/appl.json"))
            .c100RebuildRespondentDetails(TestUtil.readFileFrom("classpath:c100-rebuild/resp.json"))
            .build();

        CaseData caseData1 = CaseData.builder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .build();
        UserDetails userDetails1 = UserDetails
            .builder()
            .email("test@gmail.com")
            .build();

        CaseDetails caseDetails1 = mock(CaseDetails.class);

        CaseData updatedCaseData = caseData1.toBuilder()
            .id(1234567891234567L)
            .c100RebuildData(c100RebuildData)
            .applicantCaseName("applicantLN1 V respLN1")
            .build();
        when(caseDataMapper.buildUpdatedCaseData(any())).thenReturn(updatedCaseData);
        when(caseRepository.updateCase(authToken, caseId, updatedCaseData,
                                       C100_REQUEST_SUPPORT)).thenReturn(caseDetails1);

        //When
        CaseDetails actualCaseDetails =  caseService.updateCase(caseData1, authToken, caseId,
                                                                CITIZEN_CASE_UPDATE.getValue());

        //Then
        assertNotNull(actualCaseDetails);
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
        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, citizenCaseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getApplicantDocuments().isEmpty());
        assertFalse(citizenDocumentsManagement.getCitizenOtherDocuments().isEmpty());
        assertEquals(11, citizenDocumentsManagement.getApplicantDocuments().size());
    }

    @Test
    public void testGetCitizenDocumentsWithFm5Reminders() {
        CaseData citizenCaseData1 = citizenCaseData.toBuilder().fm5ReminderNotificationDetails(
                FM5ReminderNotificationDetails.builder().fm5RemindersSent("YES").build())
            .documentManagementDetails(DocumentManagementDetails.builder().citizenQuarantineDocsList(List.of(
                element(fm5QuarantineLegalDoc))).build()).build();
        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, citizenCaseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getApplicantDocuments().isEmpty());
        assertFalse(citizenDocumentsManagement.getCitizenOtherDocuments().isEmpty());
        assertEquals(10, citizenDocumentsManagement.getApplicantDocuments().size());
    }

    @Test
    public void testGetCitizenDocumentsWithFm5RemindersSetToNO() {
        CaseData citizenCaseData1 = citizenCaseData.toBuilder().fm5ReminderNotificationDetails(
                FM5ReminderNotificationDetails.builder().fm5RemindersSent("NO").build())
            .documentManagementDetails(DocumentManagementDetails.builder().citizenQuarantineDocsList(List.of(
                element(fm5QuarantineLegalDoc))).build()).build();
        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, citizenCaseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getApplicantDocuments().isEmpty());
        assertFalse(citizenDocumentsManagement.getCitizenOtherDocuments().isEmpty());
        assertEquals(10, citizenDocumentsManagement.getApplicantDocuments().size());
    }

    @Test
    public void testGetCitizenDocumentsWithFm5RemindersWhenPartyIdIsDiffer() {

        QuarantineLegalDoc quarantineLegalDoc1 = QuarantineLegalDoc.builder()
            .fm5StatementsDocument(Document.builder().build())
             .categoryId("fm5Statements")
            .uploadedBy("test")
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-00")
            .documentUploadedDate(LocalDateTime.now())
            .citizenQuarantineDocument(Document.builder().documentUrl("url").build())
            .url(Document.builder().documentUrl("url").build())
            .build();
        CaseData citizenCaseData1 = citizenCaseData.toBuilder().fm5ReminderNotificationDetails(
                FM5ReminderNotificationDetails.builder().fm5RemindersSent("YES").build())
            .documentManagementDetails(DocumentManagementDetails.builder().citizenQuarantineDocsList(List.of(
                element(quarantineLegalDoc1))).build()).build();
        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, citizenCaseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertFalse(citizenDocumentsManagement.getApplicantDocuments().isEmpty());
        assertFalse(citizenDocumentsManagement.getCitizenOtherDocuments().isEmpty());
        assertEquals(10, citizenDocumentsManagement.getApplicantDocuments().size());
    }


    @Test
    public void testEmptyCitizenDocumentsWhenNoDocs() {
        //Given
        citizenCaseData = CaseData.builder().state(State.DECISION_OUTCOME).build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, citizenCaseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(citizenDocumentsManagement.getApplicantDocuments().isEmpty());
        assertTrue(citizenDocumentsManagement.getRespondentDocuments().isEmpty());
        assertTrue(citizenDocumentsManagement.getCitizenOtherDocuments().isEmpty());
    }

    @Test
    public void testFilterNonAccessibleCitizenDocuments() {
        //Given
        QuarantineLegalDoc otherPartyDoc = QuarantineLegalDoc.builder()
            .uploaderRole(CITIZEN)
            .uploadedByIdamId("00000000-0000-0000-0000-000000000001")
            .build();
        citizenCaseData = citizenCaseData.toBuilder()
            .reviewDocuments(citizenCaseData.getReviewDocuments().toBuilder()
                                 .confidentialDocuments(List.of(element(otherPartyDoc)))
                                 .build())
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .citizenQuarantineDocsList(List.of(element(otherPartyDoc)))
                                           .build())
            .state(State.DECISION_OUTCOME)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, citizenCaseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(citizenDocumentsManagement.getRespondentDocuments().isEmpty());
    }

    @Test
    public void testGetCitizenApplicantOrdersC100() {
        //Given
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(element(testUuid, partyDetails)))
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList1)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);
        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks()));
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks().get(0).getApplicantSoaPack()));
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(CA_SOA_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testGetCitizenRespondentOrdersC100() {
        //Given
        orderDetails = orderDetails.toBuilder()
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .whoIsResponsibleToServe(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.getId())
                                   .build())
            .otherDetails(OtherOrderDetails.builder().orderCreatedDate("12 Jan 2021").orderMadeDate("12 Jan 2021").build())
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .orderCollection(List.of(element(orderDetails)))
            .applicants(List.of(element(testUuid, partyDetails)))
            .respondents(List.of(element(testUuid, partyDetails)))
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList1)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);
        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertNotNull(citizenDocumentsManagement.getCitizenApplicationPacks());
        assertFalse(citizenDocumentsManagement.getCitizenOrders().isEmpty());
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_PERSONAL_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(CA_SOA_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testGetCitizenApplicantOrdersFL401() {
        //Given
        finalServedApplicationDetailsList.get(0).getValue().getBulkPrintDetails().get(0).getValue().setPartyIds(testUuid.toString());
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(DA_SOA_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testGetCitizenRespondentOrdersFL401() {
        //Given
        orderDetails = orderDetails.toBuilder()
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .whoIsResponsibleToServe(SoaCitizenServingRespondentsEnum.unrepresentedApplicant.getId())
                                   .build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .orderCollection(List.of(element(orderDetails)))
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_PERSONAL_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(DA_SOA_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testCitizenOrdersSosCompletedC100() {
        //Given
        orderDetails = orderDetails.toBuilder()
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .whoIsResponsibleToServe(SoaCitizenServingRespondentsEnum.courtAdmin.getId())
                                   .build())
            .sosStatus(SOS_COMPLETED)
            .build();

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .orderCollection(List.of(element(orderDetails)))
            .applicants(List.of(element(testUuid, partyDetails)))
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);
        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertNotNull(citizenDocumentsManagement.getCitizenApplicationPacks());
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(CA_ORDER_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }

    @Test
    public void testCitizenOrdersSosCompletedFL401() {
        //Given
        orderDetails = orderDetails.toBuilder()
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .whoIsResponsibleToServe(SoaCitizenServingRespondentsEnum.courtAdmin.getId())
                                   .build())
            .sosStatus(SOS_COMPLETED)
            .fl404CustomFields(FL404.builder().fl404bIsPowerOfArrest1(YES).fl404bIsPowerOfArrest2(YES)
                                   .fl404bIsPowerOfArrest3(YES).fl404bIsPowerOfArrest4(YES)
                                   .fl404bIsPowerOfArrest5(YES).fl404bIsPowerOfArrest6(NO)
                                   .build())
            .build();
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .orderCollection(List.of(element(orderDetails)))
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle
                .builder()
                .uploadedDateTime("04-Sep-2024 01:38:33 PM")
                .partyType(PartyEnum.applicant)
                .selectedParties(List.of(element(ServedParties.builder().partyId(testUuid.toString()).build())))
                .c2DocumentBundle(C2DocumentBundle
                    .builder()
                    .supportingEvidenceBundle(List.of(element(SupportingEvidenceBundle
                        .builder()
                        .build())))
                    .build())
                .build())))
            .state(State.DECISION_OUTCOME)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(DA_ORDER_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }

    @Test
    public void testCitizenOrdersSosCompletedFL401ForOccupationOrder() {

        //Given
        OrderDetails orderDetails1 = orderDetails.toBuilder()
            .otherDetails(OtherOrderDetails.builder().build())
            .dateCreated(LocalDateTime.now())
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .cafcassServed(No)
                                   .cafcassCymruServed(No)
                                   .multipleOrdersServed(Yes)
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .whoIsResponsibleToServe(SoaCitizenServingRespondentsEnum.courtAdmin.getId())
                                   .servedParties(List.of(Element.<ServedParties>builder()
                                                              .value(ServedParties.builder()
                                                                         .partyId("00000000-0000-0000-0000-000000000000")
                                                                         .build()).build()))
                                   .build())
            .fl404CustomFields(FL404.builder().fl404bIsPowerOfArrest1(YES).fl404bIsPowerOfArrest2(YES)
                                   .fl404bIsPowerOfArrest3(YES).fl404bIsPowerOfArrest4(YES)
                                   .fl404bIsPowerOfArrest5(YES).fl404bIsPowerOfArrest6(YES)
                                   .build())
            .orderType(OCCUPATION_ORDER)
            .sosStatus(SOS_COMPLETED)
            .build();
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().partyId(testUuid)
                                 .user(User.builder().idamId(userDetails.getId()).build()).build())
            .respondentsFL401(partyDetails)
            .orderCollection(List.of(element(orderDetails1)))
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle
                                                              .builder()
                                                              .uploadedDateTime("04-Sep-2024 01:38:33 PM")
                                                              .partyType(PartyEnum.applicant)
                                                              .selectedParties(List.of(element(ServedParties.builder().partyId(
                                                                  testUuid.toString()).build())))
                                                              .c2DocumentBundle(C2DocumentBundle
                                                                                    .builder()
                                                                                    .supportingEvidenceBundle(List.of(
                                                                                        element(SupportingEvidenceBundle
                                                                                                    .builder()
                                                                                                    .build())))
                                                                                    .build())
                                                              .build())))
            .state(State.DECISION_OUTCOME)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(DA_ORDER_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }

    @Test
    public void testCitizenOrdersSosCompletedFL401CustomFields1() {

        //Given
        OrderDetails orderDetails1 = orderDetails.toBuilder()
            .otherDetails(OtherOrderDetails.builder().orderCreatedDate(LocalDateTime.now().format(DATE_FORMATTER_D_MMM_YYYY)).build())
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .multipleOrdersServed(Yes)
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .servedParties(List.of(Element.<ServedParties>builder()
                                                              .value(ServedParties.builder()
                                                                         .partyId("00000000-0000-0000-0000-000000000000")
                                                                         .build()).build()))
                                   .build())
            .fl404CustomFields(FL404.builder().fl404bIsPowerOfArrest1(YES).fl404bIsPowerOfArrest2(YES)
                                   .fl404bIsPowerOfArrest3(YES).fl404bIsPowerOfArrest4(YES)
                                   .fl404bIsPowerOfArrest5(YES).fl404bIsPowerOfArrest6(NO)
                                   .build())
            .orderType(OCCUPATION_ORDER)
            .sosStatus(SOS_COMPLETED)
            .build();
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().partyId(testUuid)
                                 .user(User.builder().idamId(userDetails.getId()).build()).build())
            .respondentsFL401(partyDetails)
            .orderCollection(List.of(element(orderDetails1)))
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle
                                                              .builder()
                                                              .uploadedDateTime("04-Sep-2024 01:38:33 PM")
                                                              .partyType(PartyEnum.applicant)
                                                              .selectedParties(List.of(element(ServedParties.builder().partyId(
                                                                  testUuid.toString()).build())))
                                                              .c2DocumentBundle(C2DocumentBundle
                                                                                    .builder()
                                                                                    .supportingEvidenceBundle(List.of(
                                                                                        element(SupportingEvidenceBundle
                                                                                                    .builder()
                                                                                                    .build())))
                                                                                    .build())
                                                              .build())))
            .state(State.DECISION_OUTCOME)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(DA_ORDER_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }

    @Test
    public void testCitizenOrdersSosCompletedFL401ForOccupationOrderWithNoPowerOfArrest() {
        //Given
        OrderDetails orderDetails1 = orderDetails.toBuilder()
            .dateCreated(LocalDateTime.now())
            .serveOrderDetails(orderDetails.getServeOrderDetails().toBuilder()
                                   .serveOnRespondent(YesNoNotApplicable.Yes)
                                   .whoIsResponsibleToServe(SoaCitizenServingRespondentsEnum.courtAdmin.getId())
                                   .cafcassServed(Yes)
                                   .cafcassCymruServed(Yes)
                                   .servedParties(List.of(Element.<ServedParties>builder()
                                                              .value(ServedParties.builder().partyId(
                                                                  "00000000-0000-0000-0000-000000000000").build()).build()))
                                   .build())
            .fl404CustomFields(FL404.builder().fl404bIsPowerOfArrest1(NO).fl404bIsPowerOfArrest2(NO)
                                   .fl404bIsPowerOfArrest3(NO).fl404bIsPowerOfArrest4(NO)
                                   .fl404bIsPowerOfArrest5(NO).fl404bIsPowerOfArrest6(NO)
                                   .build())
            .orderType(OCCUPATION_ORDER)
            .sosStatus(SOS_COMPLETED)
            .build();

        servedApplicationDetails = servedApplicationDetails.toBuilder()
            .whoIsResponsible(PERSONAL_SERVICE_SERVED_BY_BAILIFF)
            .build();
        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));

        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().partyId(testUuid)
                                 .user(User.builder().idamId(userDetails.getId()).build()).build())
            .respondentsFL401(partyDetails)
            .orderCollection(List.of(element(orderDetails1)))
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .additionalApplicationsBundle(List.of(element(AdditionalApplicationsBundle
                                                              .builder()
                                                              .uploadedDateTime("04-Sep-2024 01:38:33 PM")
                                                              .partyType(PartyEnum.applicant)
                                                              .selectedParties(List.of(element(ServedParties.builder().partyId(
                                                                  testUuid.toString()).build())))
                                                              .c2DocumentBundle(C2DocumentBundle
                                                                                    .builder()
                                                                                    .supportingEvidenceBundle(List.of(
                                                                                        element(SupportingEvidenceBundle
                                                                                                    .builder()
                                                                                                    .build())))
                                                                                    .build())
                                                              .build())))
            .state(State.DECISION_OUTCOME)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(DA_ORDER_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }


    @Test
    public void testCitizenApplicantSoaPersonalServiceC100() {
        //Given
        servedApplicationDetails = servedApplicationDetails.toBuilder()
            .whoIsResponsible(UNREPRESENTED_APPLICANT)
            .build();
        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .applicants(List.of(element(testUuid, partyDetails)))
            .serviceOfApplication(ServiceOfApplication.builder().unservedCitizenRespondentPack(
                SoaPack.builder().packDocument(List.of(element(Document.builder().documentBinaryUrl(
                    "abc").documentFileName("ddd").build()))).build()).build())
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);
        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks()));
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks().get(0).getApplicantSoaPack()));
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks().get(0).getRespondentSoaPack()));
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals("CAN9_SOA_PERSONAL_APPLICANT", citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
        assertEquals("CAN7_SOA_PERSONAL_APPLICANT", citizenDocumentsManagement.getCitizenNotifications().get(2).getId());
    }

    @Test
    public void testCitizenApplicantSoaPersonalServiceFL401() {
        //Given
        servedApplicationDetails = servedApplicationDetails.toBuilder()
            .whoIsResponsible(UNREPRESENTED_APPLICANT)
            .build();
        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .serviceOfApplication(ServiceOfApplication.builder().unservedCitizenRespondentPack(
                SoaPack.builder().packDocument(List.of(element(Document.builder().documentBinaryUrl(
                    "abc").documentFileName("ddd").build()))).build()).build())
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .state(State.DECISION_OUTCOME)
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks()));
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks().get(0).getApplicantSoaPack()));
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks().get(0).getRespondentSoaPack()));
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(DA_SOA_PERSONAL_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testCitizenApplicantSosCompletedC100() {
        //Given
        servedApplicationDetails = servedApplicationDetails.toBuilder()
            .whoIsResponsible(PERSONAL_SERVICE_SERVED_BY_CA)
            .build();
        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.DECISION_OUTCOME)
            .applicants(List.of(element(testUuid, partyDetails)))
            .respondents(List.of(element(testUuid, partyDetails)))
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);
        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertNotNull(citizenDocumentsManagement.getCitizenApplicationPacks());
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenApplicationPacks().get(0).getApplicantSoaPack()));
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(CA_SOA_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testCitizenApplicantSosCompletedFL401() {
        //Given
        servedApplicationDetails = servedApplicationDetails.toBuilder()
            .whoIsResponsible(PERSONAL_SERVICE_SERVED_BY_CA)
            .build();
        finalServedApplicationDetailsList = List.of(element(servedApplicationDetails));

        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsList)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(DA_SOA_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testCitizenApplicantSosCompletedPostOnly() {
        //Given
        caseData = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(DA_SOA_SOS_CA_CB_APPLICANT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testCitizenApplicantSosCompletedPostOnlyWhenCaseStatePrepareForHearingConductHearing() {
        OrderDetails orderDetails1 = orderDetails.toBuilder()
            .dateCreated(LocalDateTime.now())
            .fl404CustomFields(FL404.builder().fl404bIsPowerOfArrest1(NO).fl404bIsPowerOfArrest2(YES)
                                   .fl404bIsPowerOfArrest3(YES).fl404bIsPowerOfArrest4(YES)
                                   .fl404bIsPowerOfArrest5(YES).fl404bIsPowerOfArrest6(YES)
                                   .build())
            .orderType(OCCUPATION_ORDER)
            .sosStatus(SOS_COMPLETED)
            .build();
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(partyDetails)
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .orderCollection(List.of(ElementUtils.element(orderDetails1)))
            .reviewDocuments(ReviewDocuments.builder()
                                 .reviewDoc(Document.builder().categoryId("16aRiskAssessment").build())
                                 .build())

            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
        assertEquals(DN3_SOA_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(1).getId());
    }

    @Test
    public void testCitizenApplicantSosCompletedPostOnlyWhenBothPartiesIdamIdIsNull() {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(PartyDetails.builder().build())
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenOrders()));
        //Assert notifications
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }

    @Test
    public void testCitizenApplicantSosCompletedPostOnlyWhenCaseStateIsJudicialReview() {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().user(User.builder().build()).build())
            .respondentsFL401(PartyDetails.builder()
                                  .user(User.builder().idamId("00000000-0000-0000-0000-000000000000").build()).build())
            .state(State.JUDICIAL_REVIEW)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertEquals(0, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }

    @Test
    public void testRespondentCitizenDocOrders() {
        List<Element<FL401Proceedings>> fl401Proceedings = new ArrayList<>();
        fl401Proceedings.add(ElementUtils.element(FL401Proceedings.builder().uploadRelevantOrder(Document.builder().build()).build()));
        List<Element<RespondentProceedingDetails>> respondentProceedingDetails = new ArrayList<>();
        respondentProceedingDetails.add(Element.<RespondentProceedingDetails>builder().value(
            RespondentProceedingDetails.builder().uploadRelevantOrder(Document.builder().build()).build()).build());
        PartyDetails partyDetails1 = partyDetails.toBuilder().partyId(UUID.randomUUID())
            .response(Response.builder().respondentExistingProceedings(respondentProceedingDetails).build()).build();
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().partyId(UUID.randomUUID()).build())
            .respondentsFL401(partyDetails1)
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .fl401OtherProceedingDetails(FL401OtherProceedingDetails.builder().fl401OtherProceedings(fl401Proceedings).build())
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }

    @Test
    public void testCitizenDocOrdersWhenRespondentsFl401IdamIdIsNull() {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(PartyDetails.builder().build())
            .respondentsFL401(PartyDetails.builder().user(User.builder().build()).build())
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenOrders()));
        //Assert notifications
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }


    @Test
    public void testCitizenDocOrdersWhenC100IsCaseType() {
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }


    @Test
    public void testCitizenDocOrdersWhenRespondentPartyIdamIdIsNull() {
        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(Element.<PartyDetails>builder()
                                 .value(PartyDetails.builder()
                                            .user(User.builder().build()).build()).build());
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .applicants(partyDetailsList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(PartyDetails.builder()
                                  .user(User.builder().idamId("00000000-0000-0000-0000-000000000000").build())
                                  .partyId(UUID.randomUUID()).build())
            .manageOrders(ManageOrders.builder().build())
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        assertEquals(1, citizenDocumentsManagement.getCitizenOrders().size());
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
        assertEquals(ORDER_APPLICANT_RESPONDENT, citizenDocumentsManagement.getCitizenNotifications().get(0).getId());
    }

    @Test
    public void testCitizenDocOrdersWhenApplicantAndRespondentPartyIdamIdIsNull() {
        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(Element.<PartyDetails>builder()
                                 .value(PartyDetails.builder()
                                            .user(User.builder().build()).build()).build());
        //Given
        CaseData caseData1 = caseData.toBuilder()
            .applicants(partyDetailsList)
            .respondents(partyDetailsList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(PartyDetails.builder()
                                  .user(User.builder().idamId("00000000-0000-0000-0000-000000000000").build())
                                  .partyId(UUID.randomUUID()).build())
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenOrders()));
        //Assert notifications
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }

    @Test
    public void testCitizenDocOrdersWhenApplicantAndRespondentPartyIdamIdIsNotSameAsUser() {

        List<Element<RespondentProceedingDetails>> respondentProceedingDetails = new ArrayList<>();
        respondentProceedingDetails.add(ElementUtils.element(RespondentProceedingDetails.builder().uploadRelevantOrder(
            Document.builder().build()).build()));
        Response response = Response.builder().respondentExistingProceedings(respondentProceedingDetails).build();
        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(Element.<PartyDetails>builder().id(testUuid)
                                 .value(PartyDetails.builder()
                                            .user(User.builder().idamId("0000").build())
                                            .response(response).partyId(UUID.randomUUID()).build()).build());
        partyDetailsList.add(Element.<PartyDetails>builder().id(testUuid)
                                 .value(PartyDetails.builder().partyId(UUID.randomUUID())
                                            .build()).build());

        //Given
        CaseData caseData1 = caseData.toBuilder()
            .applicants(partyDetailsList)
            .respondents(partyDetailsList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(PartyDetails.builder()
                                  .user(User.builder().idamId("00000000-0000-0000-0000-000000000000").build())
                                  .partyId(UUID.randomUUID()).build())
            .state(State.DECISION_OUTCOME)
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenOrders()));
        //Assert notifications
        assertTrue(CollectionUtils.isEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }

    @Test
    public void testCitizenDocOrdersWhenApplicationServedBy() {

        List<Element<RespondentProceedingDetails>> respondentProceedingDetails = new ArrayList<>();
        respondentProceedingDetails.add(ElementUtils.element(RespondentProceedingDetails.builder().uploadRelevantOrder(
            Document.builder().categoryId("ordersFromOtherProceedings").build()).build()));
        Response response = Response.builder().respondentExistingProceedings(respondentProceedingDetails).build();
        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        partyDetailsList.add(Element.<PartyDetails>builder().id(testUuid)
                                 .value(PartyDetails.builder().partyId(testUuid)
                                            .user(User.builder().idamId(userDetails.getId()).build())
                                            .response(response).partyId(testUuid).build()).build());
        partyDetailsList.add(Element.<PartyDetails>builder()
                                 .value(PartyDetails.builder().partyId(testUuid)
                                            .build()).build());
        List<Element<Document>> docs = new ArrayList<>();
        List<Element<EmailNotificationDetails>> emailNotificationDetails = new ArrayList<>();
        emailNotificationDetails.add(element(EmailNotificationDetails.builder().partyIds("00000000-0000-0000-0000-000000000000").docs(
            docs).servedParty(SERVED_PARTY_APPLICANT).timeStamp(LocalDateTime.now().format(DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS)).build()));
        List<Element<BulkPrintDetails>> bulkPrintDetails = new ArrayList<>();
        bulkPrintDetails.add(element(BulkPrintDetails.builder().partyIds("00000000-0000-0000-0000-000000000000").printDocs(List.of(
            element(Document.builder().build()))).timeStamp(LocalDateTime.now().format(DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS)).build()));
        List<Element<ServedApplicationDetails>> servedApplicationDetails1 = new ArrayList<>();
        servedApplicationDetails1.add(element(ServedApplicationDetails.builder().emailNotificationDetails(
                emailNotificationDetails).servedBy("courtAdmin").servedAt(LocalDateTime.now().format(
                DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS)).whoIsResponsible("courtAdmin")
                                                 .modeOfService(SOA_BY_EMAIL_AND_POST).build()));
        servedApplicationDetails1.add(element(ServedApplicationDetails.builder().servedAt(LocalDateTime.now().format(
                DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS)).modeOfService(SOA_BY_EMAIL).bulkPrintDetails(bulkPrintDetails)
                                                 .servedBy("courtAdmin").whoIsResponsible("courtAdmin").build()));
        servedApplicationDetails1.add(element(ServedApplicationDetails.builder().servedAt(LocalDateTime.now().format(
                DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS)).modeOfService(SOA_BY_POST).bulkPrintDetails(bulkPrintDetails)
                                                 .servedBy("courtAdmin").whoIsResponsible("courtAdmin").build()));
        servedApplicationDetails1.add(element(ServedApplicationDetails.builder().servedAt(LocalDateTime.now().format(
                DATE_TIME_FORMATTER_DD_MMM_YYYY_HH_MM_SS)).modeOfService("SOA").bulkPrintDetails(bulkPrintDetails)
                                                 .servedBy("courtAdmin").whoIsResponsible("courtAdmin").build()));

        //Given
        CaseData caseData1 = caseData.toBuilder()
            .applicants(partyDetailsList)
            .respondents(partyDetailsList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .respondentsFL401(PartyDetails.builder()
                                  .user(User.builder().idamId("00000000-0000-0000-0000-000000000000").build())
                                  .partyId(UUID.randomUUID()).build())
            .state(State.DECISION_OUTCOME)
            .serviceOfDocuments(ServiceOfDocuments.builder().servedDocumentsDetailsList(servedApplicationDetails1).build())
            .finalServedApplicationDetailsList(finalServedApplicationDetailsListPostOnly)
            .statementOfService(StatementOfService.builder()
                                    .stmtOfServiceForApplication(List.of(element(StmtOfServiceAddRecipient.builder()
                                                                                     .selectedPartyId(testUuid.toString())
                                                                                     .build())))
                                    .build())
            .build();

        //Action
        CitizenDocumentsManagement citizenDocumentsManagement = caseService.getAllCitizenDocumentsOrders(authToken, caseData1);

        //Assert
        assertNotNull(citizenDocumentsManagement);
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenOrders()));
        //Assert notifications
        assertTrue(CollectionUtils.isNotEmpty(citizenDocumentsManagement.getCitizenNotifications()));
    }



    @Test
    public void testUpdateCitizenRaFlagsWithNullPartyDetailsMeta() {
        //Given
        CitizenPartyFlagsRequest citizenPartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .partyExternalFlags(FlagsRequest.builder().build())
            .partyIdamId("test")
            .build();
        when(idamClient.getUserInfo(Mockito.anyString())).thenReturn(UserInfo.builder().uid("test").build());
        when(coreCaseDataService.startUpdate(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(StartEventResponse.builder()
                            .caseDetails(caseDetails)
                            .build());
        when(coreCaseDataService.eventRequest(Mockito.any(), Mockito.anyString())).thenReturn(EventRequestData.builder()
                                                                                                  .build());
        //Action
        ResponseEntity<Object> response = caseService.updateCitizenRAflags("test", "citizenAwpCreate", authToken,
                                                                           citizenPartyFlagsRequest);

        //Assert
        assertNotNull(response);
    }

    @Test
    public void testUpdateCitizenRaFlagsBadRequest() {
        //Given
        CitizenPartyFlagsRequest citizenPartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .partyExternalFlags(FlagsRequest.builder().build())
            .build();
        //Action
        ResponseEntity<Object> response = caseService.updateCitizenRAflags("test", "citizenAwpCreate", authToken,
                                                                           citizenPartyFlagsRequest);

        //Assert
        assertEquals("bad request", response.getBody());
    }

    @Test
    public void testUpdateCitizenRaFlagsWithPartyDetailsMetaEmptyExtCaseFlag() {
        //Given
        caseData = caseData.toBuilder().caseTypeOfApplication(C100_CASE_TYPE).build();
        CitizenPartyFlagsRequest citizenPartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .partyExternalFlags(FlagsRequest.builder().build())
            .partyIdamId(TEST_UUID)
            .build();
        when(idamClient.getUserInfo(Mockito.anyString())).thenReturn(UserInfo.builder().uid("test").build());
        when(coreCaseDataService.startUpdate(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(StartEventResponse.builder()
                            .caseDetails(caseDetails)
                            .build());
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        when(coreCaseDataService.eventRequest(Mockito.any(), Mockito.anyString())).thenReturn(EventRequestData.builder()
                                                                                                  .build());
        //Action
        ResponseEntity<Object> response = caseService.updateCitizenRAflags("test", "citizenAwpCreate", authToken,
                                                                           citizenPartyFlagsRequest);

        //Assert
        assertEquals("party external flag details not found", response.getBody());
    }

    @Test
    public void testUpdateCitizenRaFlagsWithPartyDetailsMetaWithExtCaseFlag() {
        //Given
        caseData = caseData.toBuilder().caseTypeOfApplication(C100_CASE_TYPE).build();
        when(idamClient.getUserInfo(Mockito.anyString())).thenReturn(UserInfo.builder().uid("test").build());
        Map<String, Object> updatedCaseMap = caseDetails.getData();
        updatedCaseMap.put("caApplicant1ExternalFlags", Flags.builder().build());
        caseDetails = caseDetails.toBuilder().data(updatedCaseMap).build();
        when(coreCaseDataService.startUpdate(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.anyBoolean()))
            .thenReturn(StartEventResponse.builder()
                            .caseDetails(caseDetails)
                            .build());
        when(partyLevelCaseFlagsService.getPartyCaseDataExternalField(Mockito.anyString(), Mockito.any(), Mockito.anyInt()))
            .thenReturn("caApplicant1ExternalFlags");
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(updatedCaseMap.get("caApplicant1ExternalFlags"), Flags.class))
            .thenReturn(Flags.builder().build());
        when(coreCaseDataService.eventRequest(Mockito.any(), Mockito.anyString())).thenReturn(EventRequestData.builder()
                                                                                                  .build());
        CitizenPartyFlagsRequest citizenPartyFlagsRequest = CitizenPartyFlagsRequest.builder()
            .partyExternalFlags(FlagsRequest.builder().details(List.of(element(FlagDetailRequest.builder().build()))).build())
            .partyIdamId(TEST_UUID)
            .build();
        //Action
        ResponseEntity<Object> response = caseService.updateCitizenRAflags("test", "citizenAwpCreate", authToken,
                                                                           citizenPartyFlagsRequest);

        //Assert
        assertEquals("party flags updated", response.getBody());
    }

    @Test
    public void testFetchIdamRoles() {
        when(roleAssignmentService.fetchIdamAmRoles(Mockito.anyString(), Mockito.anyString())).thenReturn(Map.of("test", "role"));
        Map<String, String> roles = caseService.fetchIdamAmRoles(authToken, "test");
        assertEquals("role", roles.get("test"));
    }
}
