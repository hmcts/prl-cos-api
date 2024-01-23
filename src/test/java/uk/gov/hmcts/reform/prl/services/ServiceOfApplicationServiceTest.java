package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplicationUploadDocs;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.DocumentListForLa;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.State.CASE_ISSUED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationServiceTest {


    @InjectMocks
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private DgsService dgsService;

    @Mock
    private SendAndReplyService sendAndReplyService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    WelshCourtEmail welshCourtEmail;

    @Mock
    private Time dateTime;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private CaseInviteManager caseInviteManager;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UserService userService;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseSummaryTabService caseSummaryTabService;

    private final String authorization = "authToken";
    private final String testString = "test";
    private DynamicMultiSelectList dynamicMultiSelectList;
    private List<Element<PartyDetails>> parties;
    private final UUID testUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final String template = "TEMPLATE";
    private CaseInvite caseInvite;

    @Before
    public void setup() throws Exception {
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(UserDetails.builder()
                                                                             .forename("solicitorResp")
                                                                             .surname("test").build());
        PartyDetails testParty = PartyDetails.builder()
            .firstName(testString).lastName(testString).representativeFirstName(testString)
            .response(Response.builder().citizenFlags(CitizenFlags.builder().build()).build())
            .build();
        dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().code(testUuid.toString()).label(authorization).build()))
            .build();
        parties = List.of(Element.<PartyDetails>builder().id(testUuid).value(testParty).build());
        caseInvite = CaseInvite.builder().partyId(testUuid).isApplicant(YesOrNo.Yes).accessCode(testString)
            .caseInviteEmail(testString)
            .hasLinked(testString)
            .build();
        when(dgsService.generateDocument(Mockito.anyString(),Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(GeneratedDocumentInfo.builder().build());
    }

    @Test
    public void testListOfOrdersCreated() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        List<String> createdOrders = List.of("Blank order (FL404B)",
                                             "Standard directions order",
                                             "Blank order or directions (C21)",
                                             "Blank order or directions (C21) - to withdraw application",
                                             "Child arrangements, specific issue or prohibited steps order (C43)",
                                             "Parental responsibility order (C45A)",
                                             "Special guardianship order (C43A)",
                                             "Notice of proceedings (C6) (Notice to parties)",
                                             "Notice of proceedings (C6a) (Notice to non-parties)",
                                             "Transfer of case to another court (C49)",
                                             "Appointment of a guardian (C47A)",
                                             "Non-molestation order (FL404A)",
                                             "Occupation order (FL404)",
                                             "Power of arrest (FL406)",
                                             "Amended, discharged or varied order (FL404B)",
                                             "General form of undertaking (N117)",
                                             "Notice of proceedings (FL402)",
                                             "Blank order (FL404B)",
                                             "Other (upload an order)");
        Map<String, Object> responseMap = serviceOfApplicationService.getOrderSelectionsEnumValues(createdOrders, new HashMap<>());
        assertEquals(18,responseMap.values().size());
        assertEquals("1", responseMap.get("option1"));
    }

    @Test
    public void testCollapasableGettingPopulated() {

        String responseMap = serviceOfApplicationService.getCollapsableOfSentDocuments();

        assertNotNull(responseMap);

    }

    @Test
    public void testSendViaPostNotInvoked() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())

            .data(casedata)
            .build();
        //CaseData caseData1 = serviceOfApplicationService.sendPostToOtherPeopleInCase(caseDetails,"test auth");
        verifyNoInteractions(serviceOfApplicationPostService);
    }

    @Test
    public void testSendViaPostToOtherPeopleInCase() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());
        packN.add(Document.builder().documentFileName("Blank_C7.pdf").build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())

            .data(casedata)
            .build();
        //CaseData caseData1 = serviceOfApplicationService.sendPostToOtherPeopleInCase(caseDetails,"test auth");
        verifyNoInteractions(serviceOfApplicationPostService);
    }

    @Test
    public void testSendNotificationToApplicantSolicitor() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondent = element(partyDetails);
        Element<PartyDetails> applicant = element(partyDetails);

        DynamicMultiselectListElement dynamicMultiselectListElementApplicant = DynamicMultiselectListElement.builder()
            .code(applicant.getId().toString())
            .label(applicant.getValue().getRepresentativeFirstName() + " "
                       + applicant.getValue().getRepresentativeLastName())
            .build();
        DynamicMultiSelectList dynamicMultiSelectListApplicant = DynamicMultiSelectList.builder()
            .listItems(List.of(dynamicMultiselectListElementApplicant))
            .value(List.of(dynamicMultiselectListElementApplicant))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicants(List.of(applicant))
            .respondents(List.of(respondent))
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(stringObjectMap)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(),CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(caseData);
        EmailNotificationDetails emailNotificationDetails = EmailNotificationDetails.builder()
            .servedParty("ApplicantSolicitor")
            .build();
        when(serviceOfApplicationEmailService.sendEmailNotificationToApplicantSolicitor(Mockito.anyString(),Mockito.any(),
                                                                                        Mockito.any(),Mockito.any(),Mockito.any(),
                                                                                        Mockito.anyString()))
            .thenReturn(emailNotificationDetails);
        List<Element<EmailNotificationDetails>> elementList = serviceOfApplicationService
            .sendNotificationToApplicantSolicitor(caseData, authorization,
                                                  dynamicMultiSelectListApplicant.getValue(),
                                                  List.of(Document.builder().build()), "Applicant");
        assertEquals("ApplicantSolicitor",elementList.get(0).getValue().getServedParty());
    }

    @Test
    public void testConfidentialyCheckSuccess() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.Yes)
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        final ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.processConfidentialityCheck(
            authorization,
            callbackRequest
        );

        assertNotNull(response);

        final String confirmationBody = response.getBody().getConfirmationHeader();

        assertEquals("# Application served", confirmationBody);
    }

    @Test
    public void testConfidentialyCheckFailed() {
        CaseData caseData = CaseData.builder().id(12345L)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                           .builder()
                                                                           .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                                       .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);

        final ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.processConfidentialityCheck(
             authorization,
             callbackRequest
        );

        assertNotNull(response);
    }


    @Test
    public void testsendNotificationsForUnServedPacks() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().build())
                                      .unServedOthersPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
                                      .soaCafcassCymruServedOptions(YesOrNo.Yes)
                                      .soaCafcassCymruEmail("test@hmcts.net")
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        assertNotNull(serviceOfApplicationService.sendNotificationsForUnServedPacks(caseData, authorization));
    }

    @Test
    public void testsendNotificationsForUnServedLaPack() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                    .builder()
                    .confidentialityCheckRejectReason("pack contain confidential info")
                    .build()))
                .unServedLaPack(SoaPack.builder().build())
                .applicationServedYesNo(No)
                .soaCafcassCymruServedOptions(Yes)
                .soaCafcassCymruEmail("test@hmcts.net")
                .rejectionReason("pack contain confidential address")
                .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                .id(12345L)
                .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        assertNotNull(serviceOfApplicationService.sendNotificationsForUnServedPacks(caseData, authorization));
    }

    @Test
    public void testsendNotificationsForUnServedRespondentPacks() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        assertNotNull(serviceOfApplicationService.sendNotificationsForUnServedPacks(caseData, authorization));
    }

    @Test
    public void testgeneratePacksForConfidentialCheck() {
        CaseData caseData = CaseData.builder().id(12345L)
            .applicants(parties)
            .respondents(parties)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(parties)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .soaServeToRespondentOptions(YesOrNo.No)
                                      .soaRecipientsOptions(dynamicMultiSelectList)
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
                                      .soaOtherParties(dynamicMultiSelectList)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> dataMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(dataMap)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(caseData);

        assertNotNull(serviceOfApplicationService.generatePacksForConfidentialCheckC100(caseDetails,authorization));
    }

    @Test
    public void testgenerateAccessCodeLetter() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        Map<String, Object> dataMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(dataMap)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(caseData);

        assertNotNull(serviceOfApplicationService.generateAccessCodeLetter(authorization, caseData,parties.get(0),
                                                                           caseInvite, template));
    }

    @Test
    public void testgetCollapsableOfSentDocumentsFL401() {
        assertNotNull(serviceOfApplicationService.getCollapsableOfSentDocumentsFL401());
    }

    @Test
    public void testgetCafcassNo() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();
        assertEquals(YesOrNo.No, serviceOfApplicationService.getCafcass(caseData));
    }

    @Test
    public void testgetCafcassNoC100() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        assertEquals(YesOrNo.No, serviceOfApplicationService.getCafcass(caseData));
    }

    @Test
    public void testgetCafcassYesC100() {
        CaseData caseData = CaseData.builder().id(12345L)
            .isCafcass(YesOrNo.Yes)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        assertEquals(YesOrNo.Yes, serviceOfApplicationService.getCafcass(caseData));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {
        CaseData caseData = CaseData.builder().id(12345L)
            .applicants(parties)
            .respondents(parties)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(parties)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                        .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .soaServeToRespondentOptions(YesOrNo.No)
                                      .soaCitizenServingRespondentsOptionsCA(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                                      .soaRecipientsOptions(dynamicMultiSelectList)
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
                                      .soaOtherParties(dynamicMultiSelectList)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> dataMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(dataMap)
            .build();
        CallbackRequest callBackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(caseData);

        assertNotNull(serviceOfApplicationService.handleAboutToSubmit(callBackRequest));
    }

    @Test
    public void testSendNotificationForSoaServeToRespondentOptionsNoC100() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .address(Address.builder().addressLine1("line1").build())
            .solicitorEmail("solicitor@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> partyList = new ArrayList<>();
        Element applicantElement = element(UUID.fromString("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f"), partyDetails);
        partyList.add(applicantElement);


        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f")
                               .label("recipient1")
                               .build()))
            .build();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().code("Blank order or directions (C21) - to withdraw application")
                               .label("Blank order or directions (C21) - to withdraw application").build())).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().value(OrderDetails.builder()
                                                                               .orderTypeId("Blank order or directions (C21)")
                                                                               .build())
                                         .build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).build()).build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(otherParities)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .finalDocument(Document.builder().build())
            .c1ADocument(Document.builder().documentFileName("Blank_C7.pdf").build())
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());

    }

    @Test
    public void testSendNotificationForSoaServeToRespondentOptionsApplicantsDontMatch() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .address(Address.builder().addressLine1("line1").build())
            .solicitorEmail("solicitor@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> partyList = new ArrayList<>();
        Element applicantElement = element(UUID.fromString("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f"), partyDetails);
        partyList.add(applicantElement);

        List<Document> c100StaticDocs = new ArrayList<>();
        c100StaticDocs.add(Document.builder().documentFileName("Blank.pdf").build());
        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .label("recipient1")
                               .build()))
            .build();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().code("Blank order or directions (C21) - to withdraw application")
                               .label("Blank order or directions (C21) - to withdraw application").build())).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().value(OrderDetails.builder()
                                                                               .orderTypeId("Blank order or directions (C21)")
                                                                               .build())
                                         .build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).build()).build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(otherParities)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .finalDocument(Document.builder().build())
            .c1ADocument(Document.builder().documentFileName("C1A_Blank.pdf").build())
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        when(serviceOfApplicationPostService.getStaticDocs(authorization,PrlAppsConstants.C100_CASE_TYPE))
            .thenReturn(c100StaticDocs);
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());

    }

    @Test
    public void testSendNotificationForSoaWithNoRecipientsC100() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .address(Address.builder().addressLine1("line1").build())
            .solicitorEmail("solicitor@email.com")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> partyList = new ArrayList<>();
        Element applicantElement = element(UUID.fromString("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f"), partyDetails);
        partyList.add(applicantElement);


        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f")
                               .label("recipient1")
                               .build()))
            .build();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().code("Blank order or directions (C21) - to withdraw application")
                               .label("Blank order or directions (C21) - to withdraw application").build())).build();
        List<Element<DocumentListForLa>> documentsForLa = new ArrayList<>();
        documentsForLa.add(Element.<DocumentListForLa>builder().value(DocumentListForLa.builder()
                                                                          .documentsListForLa(DynamicList.builder().build())
                                                                          .build()).build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().value(OrderDetails.builder()
                                                                               .orderTypeId("Blank order or directions (C21)")
                                                                               .build())
                                         .build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaLaEmailAddress("la@gmail.com")
                                      .soaDocumentDynamicListForLa(documentsForLa)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaOtherParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).build()).build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(otherParities)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplicationScreen1(dynamicMultiSelectList)
            .finalDocument(Document.builder().build())
            .c1ADocument(Document.builder().build())
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());

    }

    @Test
    public void testSendNotificationForSoaServeToRespondentOptionsNoAndLegalRepYesC100() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .solicitorEmail("solicitor@email.com")
            .build();

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> partyList = new ArrayList<>();
        Element applicantElement = element(UUID.fromString("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f"), partyDetails);
        partyList.add(applicantElement);


        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f")
                               .label("recipient1")
                               .build()))
            .build();
        List<Element<DocumentListForLa>> documentsForLa = new ArrayList<>();
        documentsForLa.add(Element.<DocumentListForLa>builder().value(DocumentListForLa.builder()
                                                                          .documentsListForLa(DynamicList.builder().build())
                                                                          .build()).build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .c8Document(Document.builder().build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaLaEmailAddress("La@gmail.com")
                                      .soaDocumentDynamicListForLa(documentsForLa)
                                      .soaServeC8ToLocalAuthorityYesOrNo(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).build()).build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(otherParities)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());

    }

    @Test
    public void testSendNotificationForSoaFL401() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .solicitorEmail("solicitor@email.com")
            .build();
        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code("a496a3e5-f8f6-44ec-9e12-13f5ec214e0f")
                               .label("recipient1")
                               .build()))
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(partyDetails)
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","FL401");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("repFirstName repLastName", servedApplicationDetails.getWhoIsResponsible());

    }


    @Test
    public void testSendNotificationForSoaCitizenC100ServeOtherParties() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();


        List<Element<PartyDetails>> applicants = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        applicants.add(applicantElement);
        dynamicMultiSelectList = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder().code(testUuid.toString()).label(authorization).build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicantCaseName("Test Case 45678")
            .othersToNotify(applicants)
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                .soaOtherParties(dynamicMultiSelectList)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );
        assertEquals("By email", servedApplicationDetails.getModeOfService());
    }

    @Test
    public void testSendNotificationForSoaCitizenC100() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantsFL401(partyDetails)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                .soaServeToRespondentOptions(No)
                .soaCafcassCymruServedOptions(Yes)
                .soaCafcassServedOptions(Yes)
                .soaCafcassEmailId("cymruemail@test.com")
                .soaCafcassCymruEmail("cymruemail@test.com")
                .soaCitizenServingRespondentsOptionsDA(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
            .forename("first")
            .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );
        assertEquals("first test", servedApplicationDetails.getServedBy());
    }

    @Test
    public void testSendNotificationForSoaCitizenFL401() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();


        List<Element<PartyDetails>> applicants = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        applicants.add(applicantElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenFL401Solicitor() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantsFL401(partyDetails)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenC100Solicitor() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantsFL401(partyDetails)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail(null)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaOtherParties(null)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenC100SolicitorOtherPeopleNull() throws Exception {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantsFL401(partyDetails)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail(null)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaOtherParties(DynamicMultiSelectList.builder().value(Collections.emptyList()).build())
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSoaCaseFieldsMap() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondentsFL401(otherPerson)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .caseInvites(caseInviteList)
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();

        List<DynamicMultiselectListElement> otherPeopleList = List.of(DynamicMultiselectListElement.builder()
                                                                          .label("otherPeople")
                                                                          .code("otherPeople")
                                                                          .build());
        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData)).thenReturn(otherPeopleList);
        Map<String, Object> caseDatatMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDatatMap).build();
        when(objectMapper.convertValue(caseDatatMap,  CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData)).thenReturn(cafcassCymruEmailAddress);
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(DynamicListElement.builder().label("Confidential-").build());
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
    }


    @Test
    public void testSoaCaseFieldsMap_scenario2() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondentsFL401(otherPerson)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .caseInvites(caseInviteList)
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();

        List<DynamicMultiselectListElement> otherPeopleList = List.of(DynamicMultiselectListElement.builder()
                                                                          .label("otherPeople")
                                                                          .code("otherPeople")
                                                                          .build());

        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData)).thenReturn(otherPeopleList);

        Map<String, Object> caseDatatMap = caseData.toMap(new ObjectMapper());


        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDatatMap).build();

        when(objectMapper.convertValue(caseDatatMap,  CaseData.class)).thenReturn(caseData);


        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(DynamicListElement.builder().label("").build());
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData)).thenReturn(cafcassCymruEmailAddress);

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
    }

    @Test
    public void testValidateRespondentConfidentialDetailsCA() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondents(List.of(element(otherPerson)))
            .serviceOfApplication(ServiceOfApplication.builder()
                .soaServeToRespondentOptions(No)
                .soaCafcassCymruServedOptions(Yes)
                .soaCafcassServedOptions(Yes)
                .soaCafcassEmailId("cymruemail@test.com")
                .soaCafcassCymruEmail("cymruemail@test.com")
                .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .caseInvites(caseInviteList)
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();

        List<DynamicMultiselectListElement> otherPeopleList = List.of(DynamicMultiselectListElement.builder()
            .label("otherPeople")
            .code("otherPeople")
            .build());

        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData)).thenReturn(otherPeopleList);

        Map<String, Object> caseDatatMap = caseData.toMap(new ObjectMapper());


        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDatatMap).build();

        when(objectMapper.convertValue(caseDatatMap,  CaseData.class)).thenReturn(caseData);


        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(DynamicListElement.builder().label("").build());
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData)).thenReturn(cafcassCymruEmailAddress);

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
    }

    @Test
    public void testValidateRespondentConfidentialDetailsCaWithConfidentialDetails() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(No)
            .isEmailAddressConfidential(No)
            .isPhoneNumberConfidential(No)
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondents(List.of(element(otherPerson)))
            .serviceOfApplication(ServiceOfApplication.builder()
                .soaServeToRespondentOptions(No)
                .soaCafcassCymruServedOptions(Yes)
                .soaCafcassServedOptions(Yes)
                .soaCafcassEmailId("cymruemail@test.com")
                .soaCafcassCymruEmail("cymruemail@test.com")
                .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .caseInvites(caseInviteList)
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();

        List<DynamicMultiselectListElement> otherPeopleList = List.of(DynamicMultiselectListElement.builder()
            .label("otherPeople")
            .code("otherPeople")
            .build());

        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(caseData)).thenReturn(otherPeopleList);

        Map<String, Object> caseDatatMap = caseData.toMap(new ObjectMapper());


        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDatatMap).build();

        when(objectMapper.convertValue(caseDatatMap,  CaseData.class)).thenReturn(caseData);


        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        dynamicListElements.add(DynamicListElement.builder().label("").build());
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData)).thenReturn(cafcassCymruEmailAddress);

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
    }

    @Test
    public void testHandleSoaSubmitted() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder()
                                 .build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();
        Map<String, Object> dataMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(dataMap)
            .build();
        when(objectMapper.convertValue(dataMap,  CaseData.class)).thenReturn(caseData);


        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        CallbackRequest callBackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(caseSummaryTabService.updateTab(Mockito.any(CaseData.class))).thenReturn(dataMap);
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application is served", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testHandleSoaSubmittedForNonConfidential() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder()
                                 .build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();
        Map<String, Object> dataMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(dataMap)
            .build();
        when(objectMapper.convertValue(dataMap,  CaseData.class)).thenReturn(caseData);


        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        CallbackRequest callBackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(caseSummaryTabService.updateTab(Mockito.any(CaseData.class))).thenReturn(dataMap);
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application is served", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testHandleSoaSubmittedForConfidential() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder()
                                 .build())
            .c8Document(Document.builder().build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();
        Map<String, Object> dataMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(dataMap)
            .build();
        when(objectMapper.convertValue(dataMap,  CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);
        CallbackRequest callBackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(caseSummaryTabService.updateTab(Mockito.any(CaseData.class))).thenReturn(dataMap);
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application will be reviewed for confidential details", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testGetSelectedDocumentFromDynamicListReturnsNull() {
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(documents), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(sendAndReplyService.fetchDocumentIdFromUrl("documentURL")).thenReturn("test");
        when(coreCaseDataApi.getCategoriesAndDocuments(authorization, authTokenGenerator.generate(), "1"))
            .thenReturn(categoriesAndDocuments);
        DynamicList documentList = DynamicList.builder().value(DynamicListElement.builder().code(UUID.randomUUID()).build()).build();
        uk.gov.hmcts.reform.ccd.client.model.Document document = serviceOfApplicationService
            .getSelectedDocumentFromDynamicList(authorization, documentList, "1");

        assertNull(document);
    }

    @Test
    public void testGetSelectedDocumentFromDynamicListWithCategories() {
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category blankCategory = new Category("categoryId", "categoryName", 2, List.of(documents), null);
        Category category = new Category("categoryId", "categoryName", 2, null, List.of(blankCategory));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(sendAndReplyService.fetchDocumentIdFromUrl("documentURL")).thenReturn("5be65243-f199-4edb-8565-f837ba46f1e6");
        when(coreCaseDataApi.getCategoriesAndDocuments(authorization, authTokenGenerator.generate(), "1"))
            .thenReturn(categoriesAndDocuments);
        DynamicList documentList = DynamicList.builder().value(DynamicListElement
            .builder().code("5be65243-f199-4edb-8565-f837ba46f1e6").build()).build();
        uk.gov.hmcts.reform.ccd.client.model.Document document = serviceOfApplicationService
            .getSelectedDocumentFromDynamicList(authorization, documentList, "1");

        assertNotNull(document);
        assertEquals("documentURL", document.getDocumentURL());
    }

    @Test
    public void testGetSelectedDocumentFromDynamicList() {
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(documents), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(sendAndReplyService.fetchDocumentIdFromUrl("documentURL")).thenReturn("5be65243-f199-4edb-8565-f837ba46f1e6");
        when(coreCaseDataApi.getCategoriesAndDocuments(authorization, authTokenGenerator.generate(), "1"))
            .thenReturn(categoriesAndDocuments);
        DynamicList documentList = DynamicList.builder().value(DynamicListElement
            .builder().code("5be65243-f199-4edb-8565-f837ba46f1e6").build()).build();
        uk.gov.hmcts.reform.ccd.client.model.Document document = serviceOfApplicationService
            .getSelectedDocumentFromDynamicList(authorization, documentList, "1");

        assertNotNull(document);
        assertEquals("documentURL", document.getDocumentURL());
    }

    @Test
    public void testGetNotificationPack() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "A", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackB() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "B", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackC() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "C", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackD() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "D", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackE() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "E", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackF() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "F", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackG() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "G", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackH() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "H", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackI() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "I", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackJ() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "J", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackK() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "K", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackL() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "L", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackM() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "M", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackN() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "N", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackO() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "O", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackP() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "P", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackQ() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "Q", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackR() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "R", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackS() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "S", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackHI() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "HI", packN);
        assertNotNull(listOfDocuments);
    }

    @Test
    public void testGetNotificationPackZ() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();

        List<Document> packN = new ArrayList<>();
        packN.add(Document.builder().documentFileName("C1A_Blank.pdf").build());

        List<Document> listOfDocuments = serviceOfApplicationService.getNotificationPack(caseData, "Z", packN);
        assertNotNull(listOfDocuments);
    }
}
