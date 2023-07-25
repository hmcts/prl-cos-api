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
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenFlags;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfirmRecipients;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplicationUploadDocs;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.State.CASE_ISSUED;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.wrapElements;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfApplicationServiceTest {


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

    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private CaseInviteManager caseInviteManager;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private CoreCaseDataService coreCaseDataService;

    @Mock
    private UserService userService;

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
    public void testSendViaPost() throws Exception {
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
        serviceOfApplicationService.sendPost(caseDetails, "");
        verify(serviceOfApplicationPostService).sendDocs(Mockito.any(CaseData.class),Mockito.anyString());
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
}
