package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
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
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.*;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.DocumentListForLa;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.tab.summary.CaseSummaryTabService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.MISSING_ADDRESS_WARNING_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.NO;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SERVED_PARTY_APPLICANT_SOLICITOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.YES;
import static uk.gov.hmcts.reform.prl.enums.State.CASE_ISSUED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum.unrepresentedApplicant;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.ADDRESS_MISSED_FOR_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.ADDRESS_MISSED_FOR_RESPONDENT_AND_OTHER_PARTIES;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.CA_ADDRESS_MISSED_FOR_RESPONDENT;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.CONFIDENTIALITY_CONFIRMATION_HEADER_PERSONAL;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.CONFIRMATION_HEADER_NON_PERSONAL;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DA_ADDRESS_MISSED_FOR_RESPONDENT;
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
    private FL401CaseInviteService fl401CaseInviteService;

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
    private UserService userService;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    private CaseSummaryTabService caseSummaryTabService;

    @Mock
    private C100CaseInviteService c100CaseInviteService;

    @Mock
    private AllTabServiceImpl allTabService;

    private final String authorization = "authToken";
    private final String testString = "test";
    private DynamicMultiSelectList dynamicMultiSelectList;
    private List<Element<PartyDetails>> parties;
    private final UUID testUuid = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final String template = "TEMPLATE";
    private CaseInvite caseInvite;
    private static final String SOA_FL415_FILENAME = "FL415.pdf";
    private static final String SOA_FL416_FILENAME = "FL416.pdf";
    public static final String TEST_AUTH = "test auth";

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

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn("");
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(), Mockito.any())).thenReturn(DynamicList.builder().build());
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
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
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
    public void testSendNotificationToApplicantSolicitorFl401() throws Exception {

        PartyDetails partyDetails = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .partyId(UUID.fromString(TEST_UUID))
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();
        Element<PartyDetails> respondent = element(partyDetails);
        Element<PartyDetails> applicant = element(partyDetails);

        DynamicMultiselectListElement dynamicMultiselectListElementApplicant = DynamicMultiselectListElement.builder()
            .code(TEST_UUID)
            .label(applicant.getValue().getRepresentativeFirstName() + " "
                       + applicant.getValue().getRepresentativeLastName())
            .build();
        DynamicMultiSelectList dynamicMultiSelectListApplicant = DynamicMultiSelectList.builder()
            .listItems(List.of(dynamicMultiselectListElementApplicant))
            .value(List.of(dynamicMultiselectListElementApplicant))
            .build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicantsFL401(partyDetails)
            .respondentsFL401(partyDetails)
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
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
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
            .applicants(parties)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().personalServiceBy("courtAdmin").build())
                                      .applicationServedYesNo(YesOrNo.Yes)
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        final ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.processConfidentialityCheck(
            authorization,
            callbackRequest
        );

        assertNotNull(response);

        final String confirmationBody = response.getBody().getConfirmationHeader();

        assertEquals(CONFIDENTIALITY_CONFIRMATION_HEADER_PERSONAL, confirmationBody);
    }

    @Test
    public void testConfidentialyCheckSuccessForNoPersonalService() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(parties)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason(
                                                                                    "pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.Yes)
                                      .soaServeToRespondentOptions(No)
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(12345L)
                             .data(caseDetails).build()).build();
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        final ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.processConfidentialityCheck(
            authorization,
            callbackRequest
        );

        assertNotNull(response);

        final String confirmationBody = response.getBody().getConfirmationHeader();

        assertEquals(CONFIRMATION_HEADER_NON_PERSONAL, confirmationBody);
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
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

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
            .applicants(parties)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .packDocument(List.of(element(Document.builder()
                                                                                                    .documentFileName("").build())))
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtAdmin.toString()).build())
                                      .unServedOthersPack(SoaPack.builder().build())
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
    public void testsendNotificationsForUnServedLaPack() {
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedLaPack(SoaPack.builder()
                                                          .partyIds(List.of(element("test12345")))
                                                          .personalServiceBy(SoaSolicitorServingRespondentsEnum.courtBailiff.toString())
                                                          .partyIds(List.of(element("")))
                                                          .packDocument(List.of(element(Document.builder().build())))
                                                          .build())
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
            .applicants(parties)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder().build())
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
    public void testgeneratePacksForConfidentialCheckPersonal() {
        DynamicList documentList = DynamicList.builder().value(DynamicListElement.builder().code(UUID.randomUUID()).build()).build();

        DocumentListForLa documentListForLa = DocumentListForLa.builder().documentsListForLa(documentList).build();

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
                                      .soaServeToRespondentOptions(YesOrNo.Yes)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
                                      .soaRecipientsOptions(dynamicMultiSelectList)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaLaEmailAddress("")
                                      .soaDocumentDynamicListForLa(List.of(element(documentListForLa)))
                                      .soaServeC8ToLocalAuthorityYesOrNo(Yes)
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
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
    public void testgeneratePacksForConfidentialCheckPersonalApplicantLegalRep() {
        DynamicList documentList = DynamicList.builder().value(DynamicListElement.builder().code(UUID.randomUUID()).build()).build();

        DocumentListForLa documentListForLa = DocumentListForLa.builder().documentsListForLa(documentList).build();

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
                                      .soaServeToRespondentOptions(YesOrNo.Yes)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaRecipientsOptions(dynamicMultiSelectList)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaLaEmailAddress("")
                                      .soaDocumentDynamicListForLa(List.of(element(documentListForLa)))
                                      .soaServeC8ToLocalAuthorityYesOrNo(Yes)
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .applicationServedYesNo(YesOrNo.No)
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
        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        String[] caseTypes = {"C100", "FL401"};
        for (String caseType : caseTypes) {

            CaseData caseData = CaseData.builder().id(12345L)
                .applicants(parties)
                .respondents(parties)
                .caseInvites(caseInviteList)
                .caseTypeOfApplication(caseType)
                .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
                .othersToNotify(parties)
                .serviceOfApplication(ServiceOfApplication.builder()
                                          .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                    .builder()
                                                                                    .confidentialityCheckRejectReason(
                                                                                        "pack contain confidential info")
                                                                                    .build()))
                                          .soaServeToRespondentOptions(YesOrNo.No)
                                          .soaCitizenServingRespondentsOptionsCA(unrepresentedApplicant)
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
        Element<PartyDetails> partyDetailsElement = element(partyDetails);
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
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        caseInvites.add(element(CaseInvite.builder().partyId(UUID.fromString(TEST_UUID)).build()));
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .caseInvites(caseInvites)
            .finalDocument(Document.builder().documentFileName("").build())
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
            .c1ADocument(Document.builder().documentFileName("Blank_C7.pdf").build())
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());
        when(c100CaseInviteService.generateCaseInvite(any(),any()))
            .thenReturn(CaseInvite.builder().partyId(UUID.randomUUID()).build());
        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            casedata
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

        DynamicList documentList = DynamicList.builder().value(DynamicListElement.builder().code(UUID.randomUUID()).build()).build();

        DocumentListForLa documentListForLa = DocumentListForLa.builder().documentsListForLa(documentList).build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .c8Document(Document.builder().build())
            .orderCollection(List.of(Element.<OrderDetails>builder().value(OrderDetails.builder()
                                                                               .orderTypeId("Blank order or directions (C21)")
                                                                               .build())
                                         .build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(Yes)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaServeC8ToLocalAuthorityYesOrNo(Yes)
                                      .soaOtherParties(DynamicMultiSelectList.builder()
                                                           .value(List.of(DynamicMultiselectListElement.EMPTY)).build())
                                      .soaDocumentDynamicListForLa(List.of(element(documentListForLa)))
                                      .soaLaEmailAddress("soala@test.com")
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
        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(documents), null);

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(sendAndReplyService.fetchDocumentIdFromUrl("documentURL")).thenReturn("test");
        when(coreCaseDataApi.getCategoriesAndDocuments(authorization, authTokenGenerator.generate(), "1"))
            .thenReturn(categoriesAndDocuments);
        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            casedata
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
        assertEquals("repFirstName repLastName", servedApplicationDetails.getWhoIsResponsible());

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
                                      .soaServeToRespondentOptions(Yes)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin)
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
            authorization,
            casedata
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

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyList)
            .respondents(partyList)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
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
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTypeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            casedata
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
            .respondentsFL401(partyDetails)
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
        List<Document> staticDocs = new ArrayList<>();
        staticDocs.add(Document.builder().documentBinaryUrl("testUrl").documentFileName("Blank.pdf").build());
        when(serviceOfApplicationPostService.getStaticDocs(anyString(),anyString()))
            .thenReturn(staticDocs);
        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            casedata
        );

        assertNotNull(servedApplicationDetails);
        assertEquals(SERVED_PARTY_APPLICANT_SOLICITOR, servedApplicationDetails.getWhoIsResponsible());

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
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertEquals("By email", servedApplicationDetails.getModeOfService());
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
            authorization,
            new HashMap<>()
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
            authorization,
            new HashMap<>()
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
            .applicants(parties)
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
            authorization,
            new HashMap<>()
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
            .applicants(parties)
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
            authorization,
            new HashMap<>()
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
            .applicantsFL401(otherPerson)
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
    public void testSoaCaseFieldsMapC100() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails partyDetails1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .user(User.builder()
                      .idamId("4f854707-91bf-4fa0-98ec-893ae0025cae").build())
            .contactPreferences(ContactPreferences.digital)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .contactPreferences(ContactPreferences.digital)
            .solicitorEmail("abc")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"))
            .value(partyDetails1)
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae"))
            .value(partyDetails2)
            .build();

        List<Element<PartyDetails>> partyElementList = new ArrayList<>();
        partyElementList.add(partyDetailsElement);
        partyElementList.add(partyDetailsElement1);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicants(partyElementList)
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondents(partyElementList)
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
            .canYouProvideEmailAddress(Yes)
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(otherPerson)
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
        dynamicListElements.add(DynamicListElement.builder().label("Confidential").build());
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());
        when(welshCourtEmail.populateCafcassCymruEmailInManageOrders(caseData)).thenReturn(cafcassCymruEmailAddress);
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());
        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
        assertEquals(DA_ADDRESS_MISSED_FOR_RESPONDENT, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
    }

    @Test
    public void testSoaCaseFieldsMap_scenario3() {

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
            .canYouProvideEmailAddress(Yes)
            .address(Address.builder().postCode("XXXX").build())
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondentsFL401(otherPerson)
            .applicantsFL401(otherPerson)
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
        assertEquals(DA_ADDRESS_MISSED_FOR_RESPONDENT, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
    }

    @Test
    public void testSoaCaseFieldsMap_scenario4() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails person = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .address(Address.builder().postCode("XXXX").build())
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicants(Collections.singletonList(element(person)))
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondents(Collections.singletonList(element(person)))
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
            .othersToNotify(Collections.singletonList(element(person)))
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
        assertEquals(ADDRESS_MISSED_FOR_RESPONDENT_AND_OTHER_PARTIES, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
    }

    @Test
    public void testSoaCaseFieldsMap_scenario5() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails person = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .address(Address.builder().postCode("XXXX").build())
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicants(Collections.singletonList(element(person)))
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .respondents(Collections.singletonList(element(person)))
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
        assertEquals(CA_ADDRESS_MISSED_FOR_RESPONDENT, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
    }

    @Test
    public void testSoaCaseFieldsMap_scenario6() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails person = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .isCurrentAddressKnown(No)
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicants(Collections.singletonList(element(person)))
            .respondents(Collections.singletonList(element(person)))
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
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
        assertEquals(CA_ADDRESS_MISSED_FOR_RESPONDENT, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
    }

    @Test
    public void testSoaCaseFieldsMap_scenario7() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails person = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .isCurrentAddressKnown(Yes)
            .address(Address.builder().addressLine1("aaa").postCode("xxx").build())
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicants(Collections.singletonList(element(person)))
            .respondents(Collections.singletonList(element(person)))
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
        assertEquals(BLANK_STRING, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
    }

    @Test
    public void testSoaCaseFieldsMap_scenario8() {

        final String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        Element caseInviteElement = element(caseInvite);
        caseInviteList.add(caseInviteElement);

        PartyDetails person = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .address(Address.builder().addressLine1("aaa").build())
            .email("ofl@test.com")
            .build();

        PartyDetails person1 = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .isAddressConfidential(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .canYouProvideEmailAddress(Yes)
            .address(Address.builder().postCode("XXXX").build())
            .email("ofl@test.com")
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .applicants(Collections.singletonList(element(person)))
            .respondents(Collections.singletonList(element(person)))
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
            .otherPartyInTheCaseRevised(Collections.singletonList(element(person1)))
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
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(dynamicListElements).build());

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(authorization, caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
        assertEquals(ADDRESS_MISSED_FOR_OTHER_PARTIES, soaCaseFieldsMap.get(MISSING_ADDRESS_WARNING_TEXT));
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application has been served", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testHandleSoaSubmittedConfidential() throws Exception {
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application will be reviewed for confidential details", response.getBody().getConfirmationHeader());
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
                                      .soaServeToRespondentOptions(Yes)
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application is ready to be personally served", response.getBody().getConfirmationHeader());
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

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
    public void testSendNotificationsWhenUnServedPackPresentAndContactPreferenceIsDigital() {

        PartyDetails partyDetails1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .user(User.builder()
                      .idamId("4f854707-91bf-4fa0-98ec-893ae0025cae").build())
            .contactPreferences(ContactPreferences.digital)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .contactPreferences(ContactPreferences.digital)
            .solicitorEmail("abc")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"))
            .value(partyDetails1)
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae"))
            .value(partyDetails2)
            .build();

        List<Element<PartyDetails>> partyElementList = new ArrayList<>();
        partyElementList.add(partyDetailsElement);
        partyElementList.add(partyDetailsElement1);

        List<Element<String>> partyIds = new ArrayList<>();
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0025cae"));
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0024cae"));
        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae")).build();
        CaseInvite caseInvite1 = CaseInvite.builder()
            .partyId(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae")).build();
        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        caseInviteList.add(element(UUID.randomUUID(),caseInvite));
        caseInviteList.add(element(UUID.randomUUID(),caseInvite1));

        CaseData caseData = CaseData.builder().id(12345L)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(partyElementList)
            .respondents(partyElementList)
            .caseInvites(caseInviteList)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .specialArrangementsLetter(Document.builder().build())
                                                .pd36qLetter(Document.builder().build())
                                                .additionalDocuments(Document.builder().build())
                                                .specialArrangementsLetter(Document.builder().build())
                                                .noticeOfSafetySupportLetter(Document.builder().build())
                                                .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder()
                                                                 .partyIds(partyIds).build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .partyIds(partyIds)
                                                                  .build())
                                      .applicationServedYesNo(No)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        CaseData updatedcaseData = serviceOfApplicationService
            .sendNotificationsForUnServedPacks(caseData, authorization);
        assertNotNull(updatedcaseData.getFinalServedApplicationDetailsList());
        assertEquals("solicitorResp test", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getServedBy());
        assertEquals("By email and post", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getModeOfService());
        assertEquals("Court", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getWhoIsResponsible());
    }

    @Test
    public void testSendNotificationsWhenUnServedPackPresentAndContactPreferenceIsPost() {

        PartyDetails partyDetails1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .user(User.builder()
                      .idamId("4f854707-91bf-4fa0-98ec-893ae0025cae").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"))
            .value(partyDetails1)
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae"))
            .value(partyDetails2)
            .build();

        List<Element<PartyDetails>> partyElementList = new ArrayList<>();
        partyElementList.add(partyDetailsElement);
        partyElementList.add(partyDetailsElement1);

        List<Element<String>> partyIds = new ArrayList<>();
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0025cae"));
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0024cae"));
        CaseInvite caseInvite = CaseInvite.builder()
            .partyId(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae")).build();
        CaseInvite caseInvite1 = CaseInvite.builder()
            .partyId(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae")).build();
        List<Element<CaseInvite>> caseInviteList = new ArrayList<>();
        caseInviteList.add(element(UUID.randomUUID(),caseInvite));
        caseInviteList.add(element(UUID.randomUUID(),caseInvite1));

        CaseData caseData = CaseData.builder().id(12345L)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(partyElementList)
            .respondents(partyElementList)
            .caseInvites(caseInviteList)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .specialArrangementsLetter(Document.builder().build())
                                                .pd36qLetter(Document.builder().build())
                                                .additionalDocuments(Document.builder().build())
                                                .specialArrangementsLetter(Document.builder().build())
                                                .noticeOfSafetySupportLetter(Document.builder().build())
                                                .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder()
                                                                 .partyIds(partyIds).build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .partyIds(partyIds)
                                                                  .build())
                                      .applicationServedYesNo(No)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        CaseData updatedcaseData = serviceOfApplicationService
            .sendNotificationsForUnServedPacks(caseData, authorization);
        assertNotNull(updatedcaseData.getFinalServedApplicationDetailsList());
        assertEquals("solicitorResp test", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getServedBy());
        assertEquals("By post", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getModeOfService());
        assertEquals("Court", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getWhoIsResponsible());
    }

    @Test
    public void testSendNotificationsWhenUnServedPackPresentAndNoCasInvitesPresent() {

        PartyDetails partyDetails1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .user(User.builder()
                      .idamId("4f854707-91bf-4fa0-98ec-893ae0025cae").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"))
            .value(partyDetails1)
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae"))
            .value(partyDetails2)
            .build();

        List<Element<PartyDetails>> partyElementList = new ArrayList<>();
        partyElementList.add(partyDetailsElement);
        partyElementList.add(partyDetailsElement1);

        List<Element<String>> partyIds = new ArrayList<>();
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0025cae"));
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0024cae"));

        CaseData caseData = CaseData.builder().id(12345L)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(partyElementList)
            .respondents(partyElementList)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .specialArrangementsLetter(Document.builder().build())
                                                .pd36qLetter(Document.builder().build())
                                                .additionalDocuments(Document.builder().build())
                                                .specialArrangementsLetter(Document.builder().build())
                                                .noticeOfSafetySupportLetter(Document.builder().build())
                                                .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder()
                                                                 .partyIds(partyIds).build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .partyIds(partyIds)
                                                                  .build())
                                      .applicationServedYesNo(No)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(c100CaseInviteService.generateCaseInvite(any(),any()))
            .thenReturn(CaseInvite.builder().partyId(UUID.randomUUID()).build());
        CaseData updatedcaseData = serviceOfApplicationService
            .sendNotificationsForUnServedPacks(caseData, authorization);
        assertNotNull(updatedcaseData.getFinalServedApplicationDetailsList());
        assertEquals("solicitorResp test", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getServedBy());
        assertEquals("By post", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getModeOfService());
        assertEquals("Court", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getWhoIsResponsible());
    }

    @Test
    public void testSendNotificationsWhenUnServedPackC100Personal() {

        PartyDetails partyDetails1 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .user(User.builder()
                      .idamId("4f854707-91bf-4fa0-98ec-893ae0025cae").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        PartyDetails partyDetails2 = PartyDetails.builder()
            .solicitorOrg(Organisation.builder().organisationName("test").build())
            .solicitorEmail("abc")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        Element<PartyDetails> partyDetailsElement = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0025cae"))
            .value(partyDetails1)
            .build();
        Element<PartyDetails> partyDetailsElement1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("4f854707-91bf-4fa0-98ec-893ae0024cae"))
            .value(partyDetails2)
            .build();

        List<Element<PartyDetails>> partyElementList = new ArrayList<>();
        partyElementList.add(partyDetailsElement);
        partyElementList.add(partyDetailsElement1);

        List<Element<String>> partyIds = new ArrayList<>();
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0025cae"));
        partyIds.add(element(UUID.randomUUID(),"4f854707-91bf-4fa0-98ec-893ae0024cae"));

        CaseData caseData = CaseData.builder().id(12345L)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(partyElementList)
            .respondents(partyElementList)
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder()
                                                .specialArrangementsLetter(Document.builder().build())
                                                .pd36qLetter(Document.builder().build())
                                                .additionalDocuments(Document.builder().build())
                                                .specialArrangementsLetter(Document.builder().build())
                                                .noticeOfSafetySupportLetter(Document.builder().build())
                                                .build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder()
                                                                 .partyIds(partyIds)
                                                                 .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                        .applicantLegalRepresentative.toString())
                                                                 .build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                 .partyIds(partyIds)
                                                                 .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                        .applicantLegalRepresentative.toString())
                                                                 .build())
                                      .applicationServedYesNo(No)
                                      .rejectionReason("pack contain confidential address")
                                      .build()).build();
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        when(c100CaseInviteService.generateCaseInvite(any(),any()))
            .thenReturn(CaseInvite.builder().partyId(UUID.randomUUID()).build());
        CaseData updatedcaseData = serviceOfApplicationService
            .sendNotificationsForUnServedPacks(caseData, authorization);
        assertNotNull(updatedcaseData.getFinalServedApplicationDetailsList());
        assertEquals("solicitorResp test", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getServedBy());
        assertEquals("By email", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getModeOfService());
        assertEquals("Applicant solicitor", updatedcaseData.getFinalServedApplicationDetailsList().get(0).getValue().getWhoIsResponsible());
    }

    @Test
    public void testHandleSoaSubmittedWithC8() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder()
                                 .partyId(UUID.randomUUID())
                                 .build())
            .respondentsFL401(PartyDetails.builder()
                                  .partyId(UUID.randomUUID())
                                  .build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .c8Document(Document.builder().build())
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum
                                                                          .applicantLegalRepresentative)
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application will be reviewed for confidential details", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testSendNotificationForSolicitorC100() throws Exception {
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
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetails).build()))
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
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
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testGetNotificationPacks() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .finalDocument(Document.builder().build())
            .serviceOfApplication(ServiceOfApplication.builder().build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        for (String i : new ArrayList<>(Arrays.asList("E", "F", "H", "I", "L", "M", "O", "P", "Z"))) {
            List<Document> documentPack = serviceOfApplicationService.getNotificationPack(caseData,i,List.of(Document.builder()
                                                                                   .documentFileName(SOA_FL415_FILENAME).build(),
                                                                               Document.builder()
                                                                                   .documentFileName(SOA_FL416_FILENAME).build()));
            assertFalse(documentPack.isEmpty());
        }
    }

    @Test
    public void testSendNotificationForSoaCitizenScenario1() throws Exception {
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
        UUID uuid = UUID.randomUUID();
        Element applicantElement = element(uuid, partyDetails);
        applicants.add(applicantElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element respondentElement = element(uuid, partyDetails);
        respondents.add(respondentElement);

        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code(uuid.toString())
                               .label("recipient1")
                               .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .respondents(respondents)
            .othersToNotify(respondents)
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
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(soaRecipientsOptions)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenScenario2() throws Exception {
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
            .contactPreferences(ContactPreferences.digital)
            .build();


        List<Element<PartyDetails>> applicants = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        Element applicantElement = element(uuid, partyDetails);
        applicants.add(applicantElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element respondentElement = element(uuid, partyDetails);
        respondents.add(respondentElement);

        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code(uuid.toString())
                               .label("recipient1")
                               .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .respondents(respondents)
            .othersToNotify(respondents)
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
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(soaRecipientsOptions)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenScenario3() throws Exception {
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
            .contactPreferences(ContactPreferences.digital)
            .user(User.builder().idamId("12334566").build())
            .build();


        List<Element<PartyDetails>> applicants = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        Element applicantElement = element(uuid, partyDetails);
        applicants.add(applicantElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element respondentElement = element(uuid, partyDetails);
        respondents.add(respondentElement);

        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code(uuid.toString())
                               .label("recipient1")
                               .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .respondents(respondents)
            .othersToNotify(respondents)
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
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(soaRecipientsOptions)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenScenario4() throws Exception {
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
            .user(User.builder().idamId("12334566").build())
            .build();


        List<Element<PartyDetails>> applicants = new ArrayList<>();
        UUID uuid = UUID.randomUUID();
        Element applicantElement = element(uuid, partyDetails);
        applicants.add(applicantElement);

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        Element respondentElement = element(uuid, partyDetails);
        respondents.add(respondentElement);

        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code(uuid.toString())
                               .label("recipient1")
                               .build()))
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .respondents(respondents)
            .othersToNotify(respondents)
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
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .soaOtherParties(soaRecipientsOptions)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenFL401Senario2() throws Exception {
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
                                      .soaCitizenServingRespondentsOptionsDA(unrepresentedApplicant)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testSendNotificationForSoaCitizenC100Scenario1() throws Exception {
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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();


        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        partyDetailsList.add(applicantElement);

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(partyDetailsList)
            .respondents(partyDetailsList)
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
                                      .soaCitizenServingRespondentsOptionsCA(SoaCitizenServingRespondentsEnum.unrepresentedApplicant)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
    }


    @Test
    public void checkC6AOrderExistenceForSoaParties_whenNoPeopleSelected() throws JsonProcessingException {

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().soaOtherParties(
            DynamicMultiSelectList.builder().build()).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .serviceOfApplication(serviceOfApplication)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = serviceOfApplicationService.soaValidation(
            callbackRequest
        );
        assertNull(response.getErrors());

    }

    @Test
    public void checkC6AOrderExistenceForSoaParties_whenOtherPeopleSelected() throws JsonProcessingException {

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder().code(
            "code").label("label").build();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().value(List.of(
            dynamicMultiselectListElement)).build();

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().soaOtherParties(
            dynamicMultiSelectList).build();

        OrderDetails orderDetails = OrderDetails.builder().orderTypeId(CreateSelectOrderOptionsEnum.noticeOfProceedingsNonParties.toString()).build();
        Element<OrderDetails> element = element(UUID.randomUUID(), orderDetails);

        DynamicMultiSelectList soaScreen1 = DynamicMultiSelectList.builder().value(List.of(dynamicMultiselectListElement)).build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .serviceOfApplication(serviceOfApplication)
            .orderCollection(List.of(element))
            .serviceOfApplicationScreen1(soaScreen1)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = serviceOfApplicationService.soaValidation(
            callbackRequest
        );

        assertEquals(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR, response.getErrors().get(0));
    }


    @Test
    public void checkC6AOrderExistenceForSoaParties_whenC6aNotevenPresent() throws JsonProcessingException, JsonProcessingException {

        DynamicMultiselectListElement dynamicMultiselectListElement = DynamicMultiselectListElement.builder().code(
            "code").label("label").build();

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().value(List.of(
            dynamicMultiselectListElement)).build();

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().soaOtherParties(
            dynamicMultiSelectList).build();

        OrderDetails orderDetails = OrderDetails.builder().orderTypeId(CreateSelectOrderOptionsEnum.appointmentOfGuardian.toString()).build();
        Element<OrderDetails> element = element(UUID.randomUUID(), orderDetails);

        DynamicMultiSelectList soaScreen1 = DynamicMultiSelectList.builder().value(List.of(dynamicMultiselectListElement)).build();


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .serviceOfApplication(serviceOfApplication)
            .orderCollection(List.of(element))
            .serviceOfApplicationScreen1(soaScreen1)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        AboutToStartOrSubmitCallbackResponse response = serviceOfApplicationService.soaValidation(
            callbackRequest
        );

        assertEquals(OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR, response.getErrors().get(0));
    }

    @Test
    public void testSendNotificationForSoaCitizenFL401SolicitorScenario2() throws Exception {
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
            .respondentsFL401(partyDetails)
            .caseCreatedBy(CaseCreatedBy.SOLICITOR)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.courtBailiff)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        when(userService.getUserDetails(authorization)).thenReturn(UserDetails.builder()
                                                                       .forename("first")
                                                                       .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            authorization,
            new HashMap<>()
        );
        assertNotNull(servedApplicationDetails);
    }

    @Test
    public void testsendNotificationsForUnServedResponsnetPacksFL401() {
        PartyDetails testParty = PartyDetails.builder()
            .firstName(testString).lastName(testString).representativeFirstName(testString)
            .response(Response.builder().citizenFlags(CitizenFlags.builder().build()).build())
            .build();
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(testParty)
            .respondentsFL401(testParty)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .packDocument(List.of(element(Document.builder()
                                                                                                    .documentFileName("").build())))
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .courtBailiff.toString()).build())
                                      .unServedOthersPack(SoaPack.builder().build())
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
    public void testHandleSoaSubmittedForConfidentialPersonalCaCb() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(PartyDetails.builder().partyId(UUID.randomUUID())
                                 .build())
            .respondentsFL401(PartyDetails.builder().partyId(UUID.randomUUID())
                                  .build())
            .c8Document(Document.builder().build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.courtAdmin)
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application will be reviewed for confidential details", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testHandleSoaSubmittedForC100ConfidentialPersonalCaCb() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicants(parties)
            .respondents(parties)
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(Yes)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.courtAdmin)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(caseData)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
            EventRequestData.builder().build(), StartEventResponse.builder().build(), dataMap, caseData, null);
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);

        ResponseEntity<SubmittedCallbackResponse> response = serviceOfApplicationService.handleSoaSubmitted(authorization, callBackRequest);
        assertEquals("# The application is ready to be personally served", response.getBody().getConfirmationHeader());
    }

    @Test
    public void testsendNotificationsForUnServedApplicantResponsnetPacksFL401() {
        PartyDetails testParty = PartyDetails.builder()
            .firstName(testString).lastName(testString).representativeFirstName(testString)
            .solicitorEmail(testString)
            .response(Response.builder().citizenFlags(CitizenFlags.builder().build()).build())
            .build();
        CaseData caseData = CaseData.builder().id(12345L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(testParty)
            .respondentsFL401(testParty)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .confidentialCheckFailed(wrapElements(ConfidentialCheckFailed
                                                                                .builder()
                                                                                .confidentialityCheckRejectReason("pack contain confidential info")
                                                                                .build()))
                                      .unServedApplicantPack(SoaPack.builder().build())
                                      .unServedRespondentPack(SoaPack.builder()
                                                                  .packDocument(List.of(element(Document.builder()
                                                                                                    .documentFileName("").build())))
                                                                  .personalServiceBy(SoaSolicitorServingRespondentsEnum
                                                                                         .applicantLegalRepresentative.toString()).build())
                                      .unServedOthersPack(SoaPack.builder().build())
                                      .applicationServedYesNo(No)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassCymruEmail("test@hmcts.net")
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
    public void testSendNotificationForLa() throws Exception {
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
        List<Element<ResponseDocuments>> c8Docs = List.of(Element.<ResponseDocuments>builder()
                                                              .value(ResponseDocuments.builder().build())
            .build());
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .applicantCaseName("Test Case 45678")
            .respondentC8Document(RespondentC8Document.builder()
                                      .respondentAc8Documents(c8Docs)
                                      .respondentBc8Documents(c8Docs)
                                      .respondentCc8Documents(c8Docs)
                                      .respondentDc8Documents(c8Docs)
                                      .respondentEc8Documents(c8Docs)
                                      .build())
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(No)
                                      .soaServeLocalAuthorityYesOrNo(Yes)
                                      .soaLaEmailAddress("cymruemail@test.com")
                                      .soaDocumentDynamicListForLa(List.of(element(DocumentListForLa.builder()
                                                                               .documentsListForLa(DynamicList.builder().build())
                                                                               .build())))
                                      .soaServeC8ToLocalAuthorityYesOrNo(Yes)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        uk.gov.hmcts.reform.ccd.client.model.Document document = new uk.gov.hmcts.reform.ccd.client.model.Document("documentURL",
                                                                                                                   "fileName",
                                                                                                                   "binaryUrl",
                                                                                                                   "attributePath",
                                                                                                                   LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);
        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));
        when(coreCaseDataApi.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(categoriesAndDocuments);
        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH,
            new HashMap<>()
        );
        assertEquals("By email", servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());
    }


    @Test
    public void testSendNotificationForLaWithException() throws Exception {
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
                                      .soaServeLocalAuthorityYesOrNo(No)
                                      .soaLaEmailAddress("cymruemail@test.com")
                                      .soaDocumentDynamicListForLa(List.of(element(DocumentListForLa.builder()
                                                                                       .documentsListForLa(DynamicList.builder().build())
                                                                                       .build())))
                                      .soaServeC8ToLocalAuthorityYesOrNo(Yes)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        uk.gov.hmcts.reform.ccd.client.model.Document document = new uk.gov.hmcts.reform.ccd.client.model.Document("documentURL",
                                                                                                                   "fileName",
                                                                                                                   "binaryUrl",
                                                                                                                   "attributePath",
                                                                                                                   LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);
        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));
        when(coreCaseDataApi.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(categoriesAndDocuments);
        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());
        when(serviceOfApplicationEmailService
                 .sendEmailNotificationToLocalAuthority(Mockito.anyString(),Mockito.any(),Mockito.anyString(),
                                                        Mockito.any(),Mockito.anyString())).thenThrow(IOException.class);
        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH,
            new HashMap<>()
        );
        assertNull(servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());
    }

    @Test
    public void testgetSelectedDocumentFromDynamicList() {
        uk.gov.hmcts.reform.ccd.client.model.Document document = new uk.gov.hmcts.reform.ccd.client.model.Document("documentURL",
                                                                                                                   "fileName",
                                                                                                                   "binaryUrl",
                                                                                                                   "attributePath",
                                                                                                                   LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);
        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));
        when(sendAndReplyService.fetchDocumentIdFromUrl(Mockito.anyString())).thenReturn("test");
        when(coreCaseDataApi.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(categoriesAndDocuments);
        uk.gov.hmcts.reform.ccd.client.model.Document fetchedDocument = serviceOfApplicationService.getSelectedDocumentFromDynamicList(
            TEST_AUTH,
            DynamicList.builder().value(DynamicListElement.builder().code("test->test->test").build()).build(), ""
        );
        assertNotNull(fetchedDocument);
    }

    @Test
    public void testgetSelectedDocumentUncategorisedDocs() {
        uk.gov.hmcts.reform.ccd.client.model.Document document = new uk.gov.hmcts.reform.ccd.client.model.Document("documentURL",
                                                                                                                   "fileName",
                                                                                                                   "binaryUrl",
                                                                                                                   "attributePath",
                                                                                                                   LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);
        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, Collections.emptyList(), List.of(document));
        when(sendAndReplyService.fetchDocumentIdFromUrl(Mockito.anyString())).thenReturn("test1");
        when(coreCaseDataApi.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(categoriesAndDocuments);
        uk.gov.hmcts.reform.ccd.client.model.Document fetchedDocument = serviceOfApplicationService.getSelectedDocumentFromDynamicList(
            TEST_AUTH,
            DynamicList.builder().value(DynamicListElement.builder().code("test1").build()).build(), ""
        );
        assertNotNull(fetchedDocument);
    }

    @Test
    public void testgetSelectedDocumentForSubCategoriesDocs() {
        uk.gov.hmcts.reform.ccd.client.model.Document document = new uk.gov.hmcts.reform.ccd.client.model.Document("documentURL",
                                                                                                                   "fileName",
                                                                                                                   "binaryUrl",
                                                                                                                   "attributePath",
                                                                                                                   LocalDateTime.now());
        Category category = new Category("categoryId", "categoryName", 2, List.of(document), null);
        Category category1 = new Category("categoryId", "categoryName", 2, Collections.emptyList(), List.of(category));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category1), List.of(document));
        when(sendAndReplyService.fetchDocumentIdFromUrl(Mockito.anyString())).thenReturn("test");
        when(coreCaseDataApi.getCategoriesAndDocuments(Mockito.anyString(),Mockito.anyString(),Mockito.anyString()))
            .thenReturn(categoriesAndDocuments);
        uk.gov.hmcts.reform.ccd.client.model.Document fetchedDocument = serviceOfApplicationService.getSelectedDocumentFromDynamicList(
            TEST_AUTH,
            DynamicList.builder().value(DynamicListElement.builder().code("test->test->test").build()).build(), ""
        );
        assertNotNull(fetchedDocument);
        assertEquals("fileName", fetchedDocument.getDocumentFilename());
    }

    @Test
    public void checkC100SoaWaFieldsWhenConfidentialDetailsPresentForNonPersonalService() {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        partyDetailsList.add(applicantElement);

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().isConfidential(Yes).soaServeToRespondentOptions(
            No).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .serviceOfApplication(serviceOfApplication)
            .applicants(partyDetailsList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c8Document(Document.builder().build())
            .build();
        Map<String, Object> resultMap = serviceOfApplicationService.setSoaOrConfidentialWaFields(
            caseData,
            Event.SOA.getId()
        );
        assertEquals("Yes", resultMap.get("isC8CheckNeeded"));
        assertNull(resultMap.get("responsibleForService"));
    }

    @Test
    public void checkC100SoaWaFieldsWhenConfidentialDetailsPresent() {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        List<Element<PartyDetails>> partyDetailsList = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        partyDetailsList.add(applicantElement);

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().isConfidential(Yes).soaServeToRespondentOptions(
            Yes).soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.courtAdmin).build();
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .serviceOfApplication(serviceOfApplication)
            .applicants(partyDetailsList)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .c8Document(Document.builder().build())
            .build();
        Map<String, Object> resultMap = serviceOfApplicationService.setSoaOrConfidentialWaFields(
            caseData,
            Event.SOA.getId()
        );
        assertEquals("Yes", resultMap.get("isC8CheckNeeded"));
        assertEquals(
            SoaSolicitorServingRespondentsEnum.courtAdmin.getId(),
            resultMap.get("responsibleForService")
        );
    }


    @Test
    public void checkFL401SoaWaFieldsWhenConfidentialDetailsPresent() {
        PartyDetails partyDetails = PartyDetails.builder().representativeFirstName("repFirstName")
            .representativeLastName("repLastName")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .address(Address.builder().addressLine1("line1").build())
            .build();

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder()
            .isConfidential(Yes).soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.courtAdmin).build();
        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder().orderType(Collections.singletonList(
                FL401OrderTypeEnum.occupationOrder)).build())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .c8Document(Document.builder().build())
            .serviceOfApplication(serviceOfApplication)
            .build();
        Map<String, Object> resultMap = serviceOfApplicationService.setSoaOrConfidentialWaFields(
            caseData,
            Event.SOA.getId()
        );
        assertEquals(YES, resultMap.get("isC8CheckNeeded"));
        assertEquals(
            SoaSolicitorServingRespondentsEnum.courtAdmin.getId(),
            resultMap.get("responsibleForService")
        );
        assertEquals(YES, resultMap.get("isOccupationOrderSelected"));
    }


    @Test
    public void checkFL401ConfidentialCheckWaFieldsWhenConfidentialDetailsPresent() {

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().applicationServedYesNo(Yes).unServedRespondentPack(
            SoaPack.builder().build()).build();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .serviceOfApplication(serviceOfApplication)
            .build();
        Map<String, Object> resultMap = serviceOfApplicationService.setSoaOrConfidentialWaFields(
            caseData,
            Event.CONFIDENTIAL_CHECK.getId()
        );
        assertEquals(YES, resultMap.get("isC8CheckApproved"));
    }


    @Test
    public void checkFL401ConfidentialCheckEventWaFieldsWhenConfidentialDetailsPresentAndOccupationOrderSelected() {

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().applicationServedYesNo(Yes).unServedRespondentPack(
            SoaPack.builder().build()).build();
        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder().orderType(Collections.singletonList(
                FL401OrderTypeEnum.occupationOrder)).build())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .serviceOfApplication(serviceOfApplication)
            .build();
        Map<String, Object> resultMap = serviceOfApplicationService.setSoaOrConfidentialWaFields(
            caseData,
            Event.CONFIDENTIAL_CHECK.getId()
        );
        assertEquals(YES, resultMap.get("isC8CheckApproved"));
        assertEquals(YES, resultMap.get("isOccupationOrderSelected"));
    }

    @Test
    public void checkFL401ConfidentialCheckEventWaFieldsWhenConfidentialDetailsPresentAndOccupationOrderNotSelected() {

        ServiceOfApplication serviceOfApplication = ServiceOfApplication.builder().applicationServedYesNo(Yes).unServedRespondentPack(
            SoaPack.builder().build()).build();
        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(TypeOfApplicationOrders.builder().orderType(Collections.singletonList(
                FL401OrderTypeEnum.nonMolestationOrder)).build())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .serviceOfApplication(serviceOfApplication)
            .build();
        Map<String, Object> resultMap = serviceOfApplicationService.setSoaOrConfidentialWaFields(
            caseData,
            Event.CONFIDENTIAL_CHECK.getId()
        );
        assertEquals(YES, resultMap.get("isC8CheckApproved"));
        assertEquals(NO, resultMap.get("isOccupationOrderSelected"));
    }

}
