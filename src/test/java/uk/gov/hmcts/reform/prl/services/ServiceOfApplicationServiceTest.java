package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfirmRecipients;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.bulkprint.BulkPrintDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplicationUploadDocs;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WelshCourtEmail;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.C100CaseInviteService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.pin.FL401CaseInviteService;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.State.CASE_ISSUED;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationServiceTest {


    public static final String TEST_AUTH = "test auth";
    @InjectMocks
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

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
    LaunchDarklyClient launchDarklyClient;

    @Mock
    private UserService userService;

    @Mock
    private WelshCourtEmail welshCourtEmail;

    @Mock
    private C100CaseInviteService c100CaseInviteService;

    @Mock
    private FL401CaseInviteService fl401CaseInviteService;

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
    public void testSendDocs() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
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
        final List<GeneratedDocumentInfo> documentInfos = serviceOfApplicationPostService.sendDocs(Mockito.any(
            CaseData.class), Mockito.anyString());
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

        List<Document> packN = List.of(Document.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("FL401")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaOtherParties(DynamicMultiSelectList.builder()
                                                           .value(List.of(dynamicListElement))
                                                           .build()).build())
            .othersToNotify(otherParities)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(serviceOfApplicationPostService.sendPostNotificationToParty(caseData,
                                                                         TEST_AUTH, partyDetails, packN, "servedParty"))
            .thenReturn(BulkPrintDetails.builder().bulkPrintId("1234").recipientsName("recipientName").build());

        final GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().docName("testDocName").createdOn("today").build();

        when(serviceOfApplicationPostService.getCoverLetterGeneratedDocInfo(caseData, TEST_AUTH, Address.builder().addressLine1("line1")
            .build(),"testName"))
            .thenReturn(generatedDocumentInfo);

        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())

            .data(casedata)
            .build();

        List<Element<BulkPrintDetails>> bulkPrintDetails = serviceOfApplicationService.sendPostToOtherPeopleInCase(caseData,
                                                                                                                   TEST_AUTH, packN, "servedParty");
        assertNotNull(bulkPrintDetails);
    }

    @Test
    public void testSendViaEmailC100() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(caseInviteManager.generatePinAndSendNotificationEmail(Mockito.any(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(casedata)
            .build();
        when(launchDarklyClient.isFeatureEnabled("send-res-email-notification")).thenReturn(true);
        CaseData caseData1 = serviceOfApplicationService.sendEmail(caseDetails);
        //verify(serviceOfApplicationEmailService).sendEmailC100(Mockito.any(CaseDetails.class));
    }

    @Test
    public void testSendViaEmailFl401() throws Exception {
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
        when(caseInviteManager.generatePinAndSendNotificationEmail(Mockito.any(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(casedata)
            .build();
        CaseData caseData1 = serviceOfApplicationService.sendEmail(caseDetails);
        //verify(serviceOfApplicationEmailService).sendEmailFL401(Mockito.any(CaseDetails.class));
    }

    @Test
    public void skipSolicitorEmailForCaseCreatedByCitizen() throws Exception {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .caseTypeOfApplication("C100")
            .applicantCaseName("Test Case 45678")
            .fl401FamilymanCaseNumber("familyman12345")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .caseCreatedBy(CaseCreatedBy.CITIZEN)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(caseInviteManager.generatePinAndSendNotificationEmail(Mockito.any(CaseData.class))).thenReturn(caseData);
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(casedata)
            .build();
        CaseData caseData1 = serviceOfApplicationService.sendEmail(caseDetails);
        //verify(serviceOfApplicationEmailService, never()).sendEmailC100(Mockito.any(CaseDetails.class));
    }

    @Test
    public void testSendNotificationToApplicantSolicitor() throws Exception {
        String authorization = "authToken";

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

        ConfirmRecipients confirmRecipients = ConfirmRecipients.builder()
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



        // Map<String,Object> casedata = new HashMap<>();
        //casedata.put("caseTyoeOfApplication","C100");


        //when(caseInviteManager.generatePinAndSendNotificationEmail(Mockito.any(CaseData.class))).thenReturn(caseData);
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CaseDetails caseDetails = CaseDetails
            .builder()
            .id(123L)
            .state(CASE_ISSUED.getValue())
            .data(stringObjectMap)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(),CaseData.class)).thenReturn(caseData);
        when(CaseUtils.getCaseData(caseDetails, objectMapper)).thenReturn(caseData);

        //CaseData caseData1 = serviceOfApplicationService.sendNotificationToApplicantSolicitor(caseDetails, authorization);
        //verify(serviceOfApplicationEmailService, never()).sendEmailC100(Mockito.any(CaseDetails.class));
    }

    @Test
    public void testSendNotificationForSoaServeToRespondentOptionsYesC100() throws Exception {

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

        List<Element<PartyDetails>> otherParities = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        otherParities.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> applicants = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        applicants.add(applicantElement);

        List<Document> packN = List.of(Document.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicants(applicants)
            .applicantCaseName("Test Case 45678")
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServeToRespondentOptions(Yes)
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaOtherParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).build()).build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .othersToNotify(otherParities)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                    .forename("first")
                                                                    .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("By email and post", servedApplicationDetails.getModeOfService());
        assertEquals("repFirstName repLastName", servedApplicationDetails.getWhoIsResponsible());

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
            .c1ADocument(Document.builder().build())
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                    .forename("first")
                                                                    .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH
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
        casedata.put("caseTyoeOfApplication","C100");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                    .forename("first")
                                                                    .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH
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


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(partyDetails)
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                                           .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();
        Map<String,Object> casedata = new HashMap<>();
        casedata.put("caseTyoeOfApplication","FL401");
        when(objectMapper.convertValue(casedata, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                    .forename("first")
                                                                    .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH
        );

        assertNotNull(servedApplicationDetails);
        assertEquals("repFirstName repLastName", servedApplicationDetails.getWhoIsResponsible());

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

        when(userService.getUserDetails(TEST_AUTH)).thenReturn(UserDetails.builder()
                                                                   .forename("first")
                                                                   .surname("test").build());

        final ServedApplicationDetails servedApplicationDetails = serviceOfApplicationService.sendNotificationForServiceOfApplication(
            caseData,
            TEST_AUTH
        );
        assertEquals("By email", servedApplicationDetails.getModeOfService());
        assertEquals("Court", servedApplicationDetails.getWhoIsResponsible());
    }


    @Test
    public void testSoaCaseFieldsMap() {

        String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        CaseData caseData = CaseData.builder()
            .id(12345L)
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

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(No, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
    }

    @Test
    public void testSoaCaseFieldsMapC100() {

        String cafcassCymruEmailAddress = "cafcassCymruEmailAddress@email.com";

        CaseData caseData = CaseData.builder()
            .id(12345L)
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
            .isCafcass(Yes)
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

        final Map<String, Object> soaCaseFieldsMap = serviceOfApplicationService.getSoaCaseFieldsMap(caseDetails);

        assertNotNull(soaCaseFieldsMap);

        assertEquals(Yes, soaCaseFieldsMap.get("soaOtherPeoplePresentInCaseFlag"));
        assertEquals(Yes, soaCaseFieldsMap.get("isCafcass"));
        assertEquals("cafcassCymruEmailAddress@email.com", soaCaseFieldsMap.get("soaCafcassCymruEmail"));
    }

    @Test
    public void testSendAndReturnCaseInvitesSoaServeToRespondentOptionsNo() {

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

        List<Element<PartyDetails>> partyList = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        partyList.add(applicantElement);

        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code(applicantElement.getId().toString())
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
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        when(c100CaseInviteService.generateAndSendCaseInviteEmailForCaApplicant(caseData, applicantElement)).thenReturn(List.of(
            CaseInvite.builder()
                .caseInviteEmail("inviteemail@test.com")
                .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
                .isApplicant(Yes)
                .build()));

        final List<Element<CaseInvite>> caseInvites = serviceOfApplicationService.sendAndReturnCaseInvites(caseData);

        assertNotNull(caseInvites);
        assertEquals(1, caseInvites.size());

    }

    @Test
    public void testSendAndReturnCaseInvitesSoaServeToRespondentOptionsYes() {

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

        List<Element<PartyDetails>> partyList = new ArrayList<>();
        Element applicantElement = element(partyDetails);
        partyList.add(applicantElement);

        DynamicMultiSelectList soaRecipientsOptions = DynamicMultiSelectList.builder()
            .value(List.of(DynamicMultiselectListElement.builder()
                               .code(applicantElement.getId().toString())
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
                                      .soaServeToRespondentOptions(Yes)
                                      .soaCafcassCymruServedOptions(Yes)
                                      .soaCafcassServedOptions(Yes)
                                      .soaCafcassEmailId("cymruemail@test.com")
                                      .soaCafcassCymruEmail("cymruemail@test.com")
                                      .soaServingRespondentsOptionsCA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .soaRecipientsOptions(soaRecipientsOptions)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        when(c100CaseInviteService.generateAndSendCaseInviteForAllC100AppAndResp(caseData))
            .thenReturn(wrapElements(List.of(caseInvite)));

        final List<Element<CaseInvite>> caseInvites = serviceOfApplicationService.sendAndReturnCaseInvites(caseData);

        assertNotNull(caseInvites);
        assertEquals(1, caseInvites.size());

    }


    @Test
    public void testSendAndReturnCaseInvitesSoaFL401() throws Exception {

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


        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
            .applicantsFL401(partyDetails)
            .orderCollection(List.of(Element.<OrderDetails>builder().build()))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaServingRespondentsOptionsDA(SoaSolicitorServingRespondentsEnum.applicantLegalRepresentative)
                                      .build())
            .serviceOfApplicationUploadDocs(ServiceOfApplicationUploadDocs.builder().build())
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .build();

        final CaseInvite caseInvite = CaseInvite.builder()
            .caseInviteEmail("inviteemail@test.com")
            .partyId(UUID.fromString("ecc87361-d2bb-4400-a910-e5754888385b"))
            .isApplicant(Yes)
            .build();

        when(fl401CaseInviteService.generateAndSendCaseInviteForDaApplicant(caseData, caseData.getApplicantsFL401()))
                .thenReturn(wrapElements(List.of(caseInvite)));

        when(fl401CaseInviteService.generateAndSendCaseInviteForDaRespondent(caseData, caseData.getRespondentsFL401()))
            .thenReturn(wrapElements(List.of(caseInvite)));

        final List<Element<CaseInvite>> caseInvites = serviceOfApplicationService.sendAndReturnCaseInvites(caseData);

        assertNotNull(caseInvites);
        assertEquals(2, caseInvites.size());
    }

    @Test
    public void testSendViaPost() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
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
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        Map<String, Object> caseDatatMap = caseData.toMap(new ObjectMapper());


        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDatatMap).build();

        when(objectMapper.convertValue(caseDatatMap,  CaseData.class)).thenReturn(caseData);

        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);

        final GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder().docName("testDocName").createdOn("today").build();


        when(serviceOfApplicationPostService.sendDocs(Mockito.any(CaseData.class), Mockito.anyString()))
            .thenReturn(List.of(generatedDocumentInfo));

        final CaseData caseData1 = serviceOfApplicationService.sendPost(caseDetails, TEST_AUTH);

        assertNotNull(caseData1);
    }

    @Test
    public void testCleanUpSoaSelections() {
        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("Test Case 45678")
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
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .build();

        Map<String, Object> caseDatatMap = caseData.toMap(new ObjectMapper());


        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(caseDatatMap).build();

        when(objectMapper.convertValue(caseDatatMap,  CaseData.class)).thenReturn(caseData);

        when(CaseUtils.getCaseData(
            caseDetails,
            objectMapper
        )).thenReturn(caseData);


        final Map<String, Object> caseDataUpdated = serviceOfApplicationService.cleanUpSoaSelections(caseDatatMap);

        assertNull(caseDataUpdated.get("soaCafcassCymruEmail"));
    }
}
