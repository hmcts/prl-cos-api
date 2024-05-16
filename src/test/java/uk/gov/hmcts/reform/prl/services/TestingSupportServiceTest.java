package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.courtnav.mappers.FL401ApplicationMapper;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.DxAddress;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.supportyouneed.ReasonableAdjustmentsSupport;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService;
import uk.gov.hmcts.reform.prl.services.citizen.CaseService;
import uk.gov.hmcts.reform.prl.services.courtnav.CourtNavCaseService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_DATA_ID;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TESTING_SUPPORT_LD_FLAG_ENABLED;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_ADMIN_APPLICATION_NOC;
import static uk.gov.hmcts.reform.prl.enums.Event.TS_SOLICITOR_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class TestingSupportServiceTest {

    @InjectMocks
    TestingSupportService testingSupportService;

    @Mock
    private ObjectMapper objectMapper;

    private final String authorization = "authToken";

    @Mock
    C100RespondentSolicitorService respondentSolicitorService;
    @Mock
    private EventService eventPublisher;
    @Mock
    private AllTabServiceImpl tabService;
    @Mock
    private UserService userService;
    @Mock
    private DocumentGenService dgsService;

    @Mock
    private EventService eventService;

    @Mock
    private CaseUtils caseUtils;
    @Mock
    private CaseWorkerEmailService caseWorkerEmailService;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private RequestUpdateCallbackService requestUpdateCallbackService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CaseService caseService;
    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    @Mock
    private CaseInitiationService caseInitiationService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private FL401ApplicationMapper fl401ApplicationMapper;
    @Mock
    private CourtNavCaseService courtNavCaseService;

    @Mock
    private TaskListService taskListService;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    PartyDetails partyDetails;
    String auth = "authorisation";
    String s2sAuth = "s2sAuth";

    @Before
    public void setUp() throws Exception {
        List<ContactInformation> contactInformation = new ArrayList<>();
        List<DxAddress> dxAddress = new ArrayList<>();
        dxAddress.add(DxAddress.builder().dxNumber("dxNumber").build());
        contactInformation.add(ContactInformation.builder()
                .addressLine1("AddressLine1").dxAddress(dxAddress).build());

        User user = User.builder().email("respondent@example.net")
                .idamId("1234-5678").solicitorRepresented(Yes).build();

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        RespondentProceedingDetails proceedingDetails = RespondentProceedingDetails.builder()
                .caseNumber("122344")
                .nameAndOffice("testoffice")
                .nameOfCourt("testCourt")
                .uploadRelevantOrder(Document.builder().build())
                .build();

        Element<RespondentProceedingDetails> proceedingDetailsElement = Element.<RespondentProceedingDetails>builder()
                .value(proceedingDetails).build();
        List<Element<RespondentProceedingDetails>> proceedingsList = Collections.singletonList(proceedingDetailsElement);

        partyDetails = PartyDetails.builder()
                .user(user)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .response(Response.builder()
                        .citizenDetails(CitizenDetails.builder()
                                .firstName("test")
                                .lastName("test")
                                .build())
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                                .noConsentReason("test")
                                .build())
                        .c7ResponseSubmitted(No)
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())
                        .miam(Miam.builder().attendedMiam(No)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .respondentExistingProceedings(proceedingsList)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        /*      .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData
                                                             .builder()
                                                        .respChildAbductionInfo(RespondentChildAbduction
                                                                                         .builder()
                                                                                         .previousThreatsForChildAbduction(
                                                                                             Yes)
                                                                                         .previousThreatsForChildAbductionDetails(
                                                                                             "Test")
                                                                                         .reasonForChildAbductionBelief(
                                                                                             "Test")
                                                                                         .whereIsChild("Test")
                                                                                         .hasPassportOfficeNotified(
                                                                                             Yes)
                                                                                         .childrenHavePassport(Yes)
                                                                                         .childrenHaveMoreThanOnePassport(
                                                                                             Yes)
                                                                                         .whoHasChildPassportOther(
                                                                                             "father")
                                                                                         .anyOrgInvolvedInPreviousAbduction(
                                                                                             Yes)
                                                                                         .anyOrgInvolvedInPreviousAbductionDetails(
                                                                                             "Test")
                                                                                         .build())
                                                             .respOtherConcernsInfo(RespondentOtherConcerns
                                                                                        .builder()
                                                                                        .childHavingOtherFormOfContact(
                                                                                            Yes)
                                                                                        .childSpendingSupervisedTime(
                                                                                            Yes)
                                                                                        .ordersRespondentWantFromCourt(
                                                                                            "Test")
                                                                                        .childSpendingUnsupervisedTime(
                                                                                            Yes)
                                                                                        .build())
                                                             .respAllegationsOfHarmInfo(RespondentAllegationsOfHarm
                                                                                            .builder()
                                                                                            .respondentChildAbuse(Yes)
                                                                                            .isRespondentChildAbduction(
                                                                                                Yes)
                                                                                            .respondentNonMolestationOrder(
                                                                                                Yes)
                                                                                            .respondentOccupationOrder(
                                                                                                Yes)
                                                                                            .respondentForcedMarriageOrder(
                                                                                                Yes)
                                                                                            .respondentDrugOrAlcoholAbuse(
                                                                                                Yes)
                                                                                            .respondentOtherInjunctiveOrder(
                                                                                                Yes)
                                                                                            .respondentRestrainingOrder(
                                                                                                Yes)
                                                                                            .respondentDomesticAbuse(
                                                                                                Yes)
                                                                                            .respondentDrugOrAlcoholAbuseDetails(
                                                                                                "Test")
                                                                                            .respondentOtherSafetyConcerns(
                                                                                                Yes)
                                                                                            .respondentOtherSafetyConcernsDetails(
                                                                                                "Test")
                                                                                            .build())
                                                             .respAohYesOrNo(Yes)
                                                             .build())
                  */
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .build()

                )
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(Yes)
                .isPhoneNumberConfidential(Yes)
                .isAddressConfidential(Yes)
                .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorReference("test")
                .address(Address.builder().addressLine1("").build())
                .organisations(Organisations.builder().contactInformation(contactInformation).build())
                .build();

        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.TRUE);
        when(authorisationService.authoriseService(anyString())).thenReturn(Boolean.TRUE);
    }

    @Test
    public void testAboutToSubmitCaseCreationWithoutDummyData() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_SOLICITOR_APPLICATION.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitSolicitorCaseCreationWithDummyC100Data() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_SOLICITOR_APPLICATION.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);

        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testRespondentTaskListRequestSubmittedWithDummyC100Data() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_SOLICITOR_APPLICATION.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        eventService.publishEvent(caseDataChanged);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        testingSupportService.respondentTaskListRequestSubmitted(callbackRequest);
        verify(eventService).publishEvent(caseDataChanged);

    }

    @Test
    public void testAboutToSubmitSolicitorCaseCreationWithDummyFl401Data() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .fl401StmtOfTruth(StatementOfTruth.builder()
                        .fullname("test")
                        .signature("test sign")
                        .build())
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_SOLICITOR_APPLICATION.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }


    @Test
    public void testAboutToSubmitAdminCaseCreationWithDummyC100Data() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitAdminCaseCreationWithDummyFl401Data() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test
    public void testAboutToSubmitAdminCaseCreationWithDummyFl401DataDocumentCreationError() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        when(dgsService.generateDocumentsForTestingSupport(
                anyString(),
                any(CaseData.class)
        )).thenThrow(RuntimeException.class);
        Map<String, Object> stringObjectMap = testingSupportService.initiateCaseCreation(auth, callbackRequest);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testAboutToSubmitAdminCaseCreationInvalidClient() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.FALSE);
        testingSupportService.initiateCaseCreation(auth, callbackRequest);
    }

    @Test
    public void testSubmittedCaseCreation() {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse =
                AboutToStartOrSubmitCallbackResponse.builder().data(caseDataMap).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(taskListService.updateTaskList(callbackRequest, auth)).thenReturn(aboutToStartOrSubmitCallbackResponse);
        Map<String, Object> caseDetails = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(caseDetails, CaseData.class)).thenReturn(caseData);
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(authorization,
             EventRequestData.builder().build(), StartEventResponse.builder().build(), caseDetails, caseData, null);

        when(tabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        Map<String, Object> stringObjectMap = testingSupportService.submittedCaseCreation(callbackRequest, auth);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testSubmittedCaseCreationWithInvalidClient() {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(true);
        when(authorisationService.authoriseUser(anyString())).thenReturn(Boolean.FALSE);
        testingSupportService.submittedCaseCreation(callbackRequest, auth);
    }

    @Test
    public void testConfirmDummyPayment() {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.getCase(
                any(),
                any(),
                any()
        )).thenReturn(caseDetails);
        Map<String, Object> stringObjectMap = testingSupportService.confirmDummyPayment(callbackRequest, auth);
        Assert.assertTrue(!stringObjectMap.isEmpty());
    }

    @Test(expected = RuntimeException.class)
    public void testConfirmDummyPaymentWithInvalidClient() {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_ADMIN_APPLICATION_NOC.getId())
                .build();
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(false);
        testingSupportService.confirmDummyPayment(callbackRequest, auth);
    }

    @Test
    public void testCreateDummyLiPC100Case() throws Exception {
        caseData = CaseData.builder()
                .id(12345678L)
                .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .eventId(TS_SOLICITOR_APPLICATION.getId())
                .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(caseDetails);
        when(caseService.createCase(Mockito.any(), Mockito.anyString())).thenReturn(caseDetails);

        CaseData updatedCaseData = testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
        assertEquals(12345678L, updatedCaseData.getId());
    }

    @Test
    public void testCourtNavCreatedCase() throws Exception {
        caseData = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.SUBMITTED_PAID)
            .fl401StmtOfTruth(StatementOfTruth.builder().build())
            .build();
        caseDataMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.SUBMITTED_PAID.getValue())
            .caseTypeId("FL401")
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .eventId(TS_ADMIN_APPLICATION_NOC.getId())
            .build();
        when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(CourtNavFl401
                                                                                   .builder()
                                                                                   .build());
        when(systemUserService.getSysUserToken()).thenReturn(s2sAuth);
        when(fl401ApplicationMapper.mapCourtNavData(any(),any()))
            .thenReturn(caseData);
        when(courtNavCaseService.createCourtNavCase(any(),any()))
            .thenReturn(caseDetails);
        Map<String,Object> casDateMap =
            testingSupportService.initiateCaseCreationForCourtNav(auth,callbackRequest);
        assertEquals(12345678L,caseDataMap.get(CASE_DATA_ID));
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyCourtNavCase_InvalidClientLD_disabled() throws Exception{
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(false);
        testingSupportService.initiateCaseCreationForCourtNav(auth, CallbackRequest.builder().build());
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyLiPC100Case_InvalidClient_LdDisabled() throws Exception {
        when(launchDarklyClient.isFeatureEnabled(TESTING_SUPPORT_LD_FLAG_ENABLED)).thenReturn(false);
        testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyLiPC100Case_InvalidS2S() throws Exception {
        when(authorisationService.authoriseService(anyString())).thenReturn(false);
        testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateDummyLiPC100Case_InvalidAuthorisation() throws Exception {
        when(authorisationService.authoriseUser(anyString())).thenReturn(false);
        testingSupportService.createDummyLiPC100Case(auth, s2sAuth);
    }
}
