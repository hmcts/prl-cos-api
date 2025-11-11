package uk.gov.hmcts.reform.prl.services.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CaseAssignmentService;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.caseflags.PartyRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.TypeOfNocEventEnum;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.SolicitorUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.barrister.BarristerRemoveService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.BarristerHelper;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NoticeOfChangePartiesServiceTest {
    @InjectMocks
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    CaseData caseData;

    CaseData caseDataForDa;
    SolicitorRole role;

    SolicitorRole roleForDa;

    @Mock
    RespondentPolicyConverter policyConverter;

    @Mock
    NoticeOfChangePartiesConverter partiesConverter;

    Optional<Element<PartyDetails>> optionalParty;

    PartyDetails daParty;

    PartyDetails partyDetails;

    PartyDetails partyDetailsNoRep;

    Element<PartyDetails> wrappedRespondents;

    NoticeOfChangeParties noticeOfChangeParties = NoticeOfChangeParties.builder().build();

    OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().build();
    @Mock
    AssignCaseAccessClient assignCaseAccessClient;
    @Mock
    AuthTokenGenerator tokenGenerator;

    @Mock
    DynamicMultiSelectListService dynamicMultiSelectListService;
    @Mock
    ObjectMapper objectMapper;

    ObjectMapper realObjectMapper;

    @Mock
    AllTabServiceImpl tabService;
    @Mock
    UserService userService;
    @Mock
    EventService eventPublisher;
    @Mock
    CcdDataStoreService ccdDataStoreService;
    @Mock
    SystemUserService systemUserService;
    @Mock
    CaseInviteManager caseInviteManager;
    @Mock
    CcdCoreCaseDataService ccdCoreCaseDataService;
    @Mock
    OrganisationService organisationService;

    @Mock
    CaseEventService caseEventService;
    @Mock
    private Time time;
    @Mock
    PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    @Mock
    private CaseAssignmentService caseAssignmentService;
    @Mock
    private BarristerHelper barristerHelper;
    @Mock
    private BarristerRemoveService barristerRemoveService;

    @Captor
    private ArgumentCaptor<CaseData> caseDataArgumentCaptor;

    @Before
    public void setUp() {
        realObjectMapper = new ObjectMapper();
        partyDetails = PartyDetails.builder().representativeFirstName("Abc")
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
            .build();

        partyDetailsNoRep = PartyDetails.builder().representativeFirstName("Abc")
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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no).firstName("fn").lastName("ln").user(User.builder().build())
            .build();


        wrappedRespondents = Element.<PartyDetails>builder().value(partyDetails).build();
        optionalParty = Optional.of(wrappedRespondents);
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);
        caseData = CaseData.builder()
            .caseTypeOfApplication("c100")
            .id(123456789L)
            .respondents(respondentList)
            .build();


        caseDataForDa = CaseData.builder()
            .caseTypeOfApplication("fl401")
            .respondents(respondentList)
            .respondentsFL401(PartyDetails.builder().representativeFirstName("1Abc")
                                  .representativeLastName("1Xyz")
                                  .gender(Gender.male)
                                  .email("1abc@xyz.com")
                                  .phoneNumber("11234567890")
                                  .canYouProvideEmailAddress(Yes)
                                  .isEmailAddressConfidential(Yes)
                                  .isPhoneNumberConfidential(Yes)
                                  .solicitorOrg(Organisation.builder().organisationID("1ABC").organisationName("1XYZ").build())
                                  .solicitorAddress(Address.builder().addressLine1("1ABC").postCode("1AB1 2MN").build())
                                  .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln")
                                  .build())
            .build();


        role = SolicitorRole.C100RESPONDENTSOLICITOR1;

        roleForDa = SolicitorRole.FL401RESPONDENTSOLICITOR;

        daParty = PartyDetails.builder().build();
    }

    @Test
    public void testGenerate() {

        when(policyConverter.caGenerate(role, optionalParty))
            .thenReturn(organisationPolicy);
        when(policyConverter.daGenerate(role, daParty))
            .thenReturn(organisationPolicy);

        when(partiesConverter.generateCaForSubmission(wrappedRespondents))
            .thenReturn(noticeOfChangeParties);

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseData, role.getRepresenting());

        assertTrue(test.containsKey("caRespondent1Policy"));

    }

    @Test
    public void testGenerateForDa() {
        when(policyConverter.caGenerate(role, optionalParty))
            .thenReturn(organisationPolicy);
        when(policyConverter.daGenerate(roleForDa, daParty))
            .thenReturn(organisationPolicy);

        when(partiesConverter.generateDaForSubmission(partyDetails))
            .thenReturn(noticeOfChangeParties);

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseDataForDa, roleForDa.getRepresenting());

        assertTrue(test.containsKey("daRespondentPolicy"));

    }

    @Test
    public void testGenerateWithBlankStrategy() {

        NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy strategy = NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy.BLANK;

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseData, role.getRepresenting(), strategy);

        assertTrue(test.containsKey("caRespondent1Policy"));

    }

    @Test
    public void testGenerateWithBlankStrategyForDa() {

        NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy strategy = NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy.BLANK;

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseDataForDa, role.getRepresenting(), strategy);

        assertTrue(test.containsKey("caApplicant3Policy"));

    }

    @Test
    public void testApplyDecision() {
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(tokenGenerator.generate()).thenReturn("s2sToken");
        when(assignCaseAccessClient.applyDecision(
            anyString(), anyString(), any(
                DecisionRequest.class))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(new HashMap<>()).build()
        );

        noticeOfChangePartiesService.applyDecision(CallbackRequest.builder()
                                                       .caseDetails(CaseDetails.builder()
                                                                        .build())
                                                       .build(),
                                                   "testAuth");
        verify(assignCaseAccessClient, times(1)).applyDecision(
            anyString(), anyString(), any(
                DecisionRequest.class));
        verify(caseAssignmentService).removeAmBarristerIfPresent(any(CaseDetails.class));
    }

    @Test
    public void testNocRequestSubmittedForC100RespondentSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[C100RESPONDENTSOLICITOR1]")
            .label("Respondent solicitor A")
            .build();

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(partyDetails));
        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondents)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        List<SolicitorUser> userList = new ArrayList<>();
        userList.add(SolicitorUser.builder().email("test_solicitor@mailinator.com").build());

        ChangeOrganisationRequest changeOrganisationRequest = caseData.getChangeOrganisationRequestField();


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(organisationService.getOrganisationSolicitorDetails("test", changeOrganisationRequest
            .getOrganisationToAdd().getOrganisationID())).thenReturn(
            OrgSolicitors.builder().organisationIdentifier("test").users(userList).build());
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData,
            0,
            PartyRole.Representing.CARESPONDENTSOLICITOR,
            true
        )).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);
        verify(caseAssignmentService).removePartyBarristerIfPresent(caseData,
                                                                    changeOrganisationRequest);
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            anyString(),
            anyString(),
            any(StartEventResponse.class),
            any(EventRequestData.class),
            any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testNocRequestSubmittedForC100RespondentSolicitorWithResponse() {
        partyDetails = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .isRemoveLegalRepresentativeRequested(Yes)
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .response(Response.builder().c7ResponseSubmitted(Yes).build())
            .partyId(UUID.randomUUID())
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes).firstName("fn").lastName("ln").user(User.builder().build())
            .build();

        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[C100RESPONDENTSOLICITOR1]")
            .label("Respondent solicitor A")
            .build();

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(partyDetails));
        CaseData caseDataC100 = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondents)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataC100.toMap(realObjectMapper))
            .build();

        List<SolicitorUser> userList = new ArrayList<>();
        userList.add(SolicitorUser.builder().email("test_solicitor@mailinator.com").build());

        ChangeOrganisationRequest changeOrganisationRequest = caseDataC100.getChangeOrganisationRequestField();


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataC100);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(organisationService.getOrganisationSolicitorDetails("test", changeOrganisationRequest
            .getOrganisationToAdd().getOrganisationID())).thenReturn(
            OrgSolicitors.builder().organisationIdentifier("test").users(userList).build());
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseDataC100,
            0,
            PartyRole.Representing.CARESPONDENTSOLICITOR,
            true
        )).thenReturn(caseDataC100);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            anyString(),
            anyString(),
            any(StartEventResponse.class),
            any(EventRequestData.class),
            any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
    }


    @Test
    public void testNocRequestSubmittedForC100ApplicantSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[C100APPLICANTSOLICITOR1]")
            .label("Applicant solicitor A")
            .build();
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(element(partyDetails));

        CaseData caseDataC100 = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataC100.toMap(realObjectMapper))
            .build();

        List<SolicitorUser> userList = new ArrayList<>();
        userList.add(SolicitorUser.builder().email("test_solicitor@mailinator.com").build());

        ChangeOrganisationRequest changeOrganisationRequest = caseDataC100.getChangeOrganisationRequestField();


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataC100);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(organisationService.getOrganisationSolicitorDetails("test", changeOrganisationRequest
            .getOrganisationToAdd().getOrganisationID())).thenReturn(
            OrgSolicitors.builder().organisationIdentifier("test").users(userList).build());
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            any(),
            anyInt(),
            any(),
            anyBoolean()
        )).thenReturn(caseDataC100);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            anyString(),
            anyString(),
            any(StartEventResponse.class),
            any(EventRequestData.class),
            any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testNocRequestSubmittedForC100ApplicantSolicitorThrowsError() {
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(element(partyDetails));

        CaseData caseDataC100 = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataC100.toMap(realObjectMapper))
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataC100);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseDataC100,
            0,
            PartyRole.Representing.CARESPONDENTSOLICITOR,
            true
        )).thenReturn(caseDataC100);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);

        verify(tabService, times(1)).updatePartyDetailsForNoc(
            anyString(),
            anyString(),
            any(StartEventResponse.class),
            any(EventRequestData.class),
            any(CaseData.class)
        );
    }

    @Test
    public void testNocRequestSubmittedForFL401RespondentSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[FL401RESPONDENTSOLICITOR]")
            .label("Respondent solicitor A")
            .build();

        CaseData caseDataC100 = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .respondentsFL401(partyDetails)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataC100.toMap(realObjectMapper))
            .build();

        List<SolicitorUser> userList = new ArrayList<>();
        userList.add(SolicitorUser.builder().email("test_solicitor@mailinator.com").build());

        ChangeOrganisationRequest changeOrganisationRequest = caseDataC100.getChangeOrganisationRequestField();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataC100);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(organisationService.getOrganisationSolicitorDetails("test", changeOrganisationRequest
            .getOrganisationToAdd().getOrganisationID())).thenReturn(
            OrgSolicitors.builder().organisationIdentifier("test").users(userList).build());
        when(organisationService.getOrganisationDetails("test", changeOrganisationRequest
            .getOrganisationToAdd().getOrganisationID())).thenReturn(
            Organisations.builder().organisationIdentifier("test").name("test").build());
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            any(),
            anyInt(),
            any(),
            anyBoolean()
        )).thenReturn(caseDataC100);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            anyString(),
            anyString(),
            any(StartEventResponse.class),
            any(EventRequestData.class),
            any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testNocRequestSubmittedForFL401ApplicantSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[APPLICANTSOLICITOR]")
            .label("Applicant solicitor A")
            .build();

        CaseData caseDataC100 = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataC100.toMap(realObjectMapper))
            .build();

        List<SolicitorUser> userList = new ArrayList<>();
        userList.add(SolicitorUser.builder().email("test_solicitor@mailinator.com").build());

        ChangeOrganisationRequest changeOrganisationRequest = caseDataC100.getChangeOrganisationRequestField();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataC100);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(organisationService.getOrganisationSolicitorDetails("test", changeOrganisationRequest
            .getOrganisationToAdd().getOrganisationID())).thenReturn(
            OrgSolicitors.builder().organisationIdentifier("test").users(userList).build());
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            any(),
            anyInt(),
            any(),
            anyBoolean()
        )).thenReturn(caseDataC100);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest);
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            anyString(),
            anyString(),
            any(StartEventResponse.class),
            any(EventRequestData.class),
            any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testUpdateLegalRepresentation() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[C100RESPONDENTSOLICITOR1]")
            .label("Respondent solicitor A")
            .build();

        List<Element<PartyDetails>> respondentsNoRep = new ArrayList<>();
        respondentsNoRep.add(element(partyDetailsNoRep));

        List<Element<PartyDetails>> respondentRep = new ArrayList<>();
        respondentRep.add(element(partyDetails));

        CaseData oldRepresentedParty = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondentRep)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();

        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(oldRepresentedParty.toMap(realObjectMapper))
            .build();


        when(objectMapper.convertValue(caseDetailsBefore.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());

        // Return a small map from applyDecision that should be merged into the callbackRequest data
        Map<String, Object> returned = new HashMap<>();
        returned.put("caRespondent1Policy", organisationPolicy);
        when(assignCaseAccessClient.applyDecision(any(), any(), any()))
            .thenReturn(AboutToStartOrSubmitCallbackResponse.builder().data(returned).build());

        CaseData newRepresentedParty = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondentsNoRep)
            .changeOrganisationRequestField(ChangeOrganisationRequest.builder()
                                                .createdBy("test_solicitor@mailinator.com")
                                                .caseRoleId(DynamicList.builder()
                                                                .value(dynamicListElement)
                                                                .listItems(List.of(dynamicListElement))
                                                                .build())
                                                .organisationToAdd(Organisation.builder()
                                                                       .organisationID("EOILU2A")
                                                                       .organisationName("FPRL-test-organisation")
                                                                       .build())
                                                .build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(newRepresentedParty.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .eventId("amendRespondentsDetails")
            .build();

        noticeOfChangePartiesService.updateLegalRepresentation(callbackRequest, "testAuth", newRepresentedParty);
        verify(assignCaseAccessClient, times(1)).applyDecision(any(), any(), any());
        // Verify merged data contains our sentinel key
        assertThat(callbackRequest.getCaseDetails().getData()).containsKey("caRespondent1Policy");
    }

    @Test
    public void testAboutToSubmitStopRepresentingForCaApplicant() {
        List<Element<PartyDetails>> applicant = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        applicant.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();


        CaseData caseDataC100 = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicant)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseDataC100);
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(List.of(CaseUser.builder().caseId("12345678").caseRole(
            "[C100APPLICANTSOLICITOR1]").build()));
        when(ccdDataStoreService.findUserCaseRoles(anyString(), anyString()))
            .thenReturn(findUserCaseRolesResponse);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());
        when(time.now()).thenReturn(LocalDateTime.now());
        when(tokenGenerator.generate()).thenReturn("");
        when(systemUserService.getSysUserToken()).thenReturn("");
        when(assignCaseAccessClient.applyDecision(anyString(), anyString(), any(DecisionRequest.class))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseDataC100.toMap(realObjectMapper)).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseDataC100.toMap(realObjectMapper))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest);
        verify(assignCaseAccessClient, times(1)).applyDecision(any(), any(), any());
        verify(caseAssignmentService).removeAmBarristerCaseRole(isA(CaseData.class),
                                                                ArgumentMatchers.<Map<Optional<SolicitorRole>, Element<PartyDetails>>>any());
    }

    @Test
    public void testAboutToSubmitStopRepresentingForCaRespondent() {
        List<Element<PartyDetails>> respondent = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        respondent.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondent)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(List.of(CaseUser.builder().caseId("12345678").caseRole(
            "[C100RESPONDENTSOLICITOR1]").build()));
        when(ccdDataStoreService.findUserCaseRoles(anyString(), anyString()))
            .thenReturn(findUserCaseRolesResponse);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());
        when(time.now()).thenReturn(LocalDateTime.now());
        when(tokenGenerator.generate()).thenReturn("");
        when(systemUserService.getSysUserToken()).thenReturn("");
        when(assignCaseAccessClient.applyDecision(anyString(), anyString(), any(DecisionRequest.class))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(realObjectMapper)).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest);
        verify(assignCaseAccessClient, times(1)).applyDecision(any(), any(), any());
        verify(caseAssignmentService).removeAmBarristerCaseRole(isA(CaseData.class),
                                                                ArgumentMatchers.<Map<Optional<SolicitorRole>, Element<PartyDetails>>>any());
    }

    @Test
    public void testAboutToSubmitStopRepresentingForDaApplicant() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();


        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(List.of(CaseUser.builder().caseId("12345678").caseRole(
            "[APPLICANTSOLICITOR]").build()));
        when(ccdDataStoreService.findUserCaseRoles(anyString(), anyString()))
            .thenReturn(findUserCaseRolesResponse);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());
        when(time.now()).thenReturn(LocalDateTime.now());
        when(tokenGenerator.generate()).thenReturn("");
        when(systemUserService.getSysUserToken()).thenReturn("");
        when(assignCaseAccessClient.applyDecision(anyString(), anyString(), any(DecisionRequest.class))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(realObjectMapper)).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest);
        verify(assignCaseAccessClient, times(1)).applyDecision(any(), any(), any());
        verify(caseAssignmentService).removeAmBarristerCaseRole(isA(CaseData.class),
                                                                ArgumentMatchers.<Map<Optional<SolicitorRole>, Element<PartyDetails>>>any());
    }

    @Test
    public void testAboutToSubmitStopRepresentingForDaRespondent() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();


        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .respondentsFL401(partyDetails)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(List.of(CaseUser.builder().caseId("12345678").caseRole(
            "[FL401RESPONDENTSOLICITOR]").build()));
        when(ccdDataStoreService.findUserCaseRoles(anyString(), anyString()))
            .thenReturn(findUserCaseRolesResponse);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());
        when(time.now()).thenReturn(LocalDateTime.now());
        when(tokenGenerator.generate()).thenReturn("");
        when(systemUserService.getSysUserToken()).thenReturn("");
        when(assignCaseAccessClient.applyDecision(anyString(), anyString(), any(DecisionRequest.class))).thenReturn(
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(realObjectMapper)).build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest);
        verify(assignCaseAccessClient, times(1)).applyDecision(any(), any(), any());
        verify(caseAssignmentService).removeAmBarristerCaseRole(isA(CaseData.class),
                                                                ArgumentMatchers.<Map<Optional<SolicitorRole>, Element<PartyDetails>>>any());
    }

    @Test
    public void testPopulateAboutToStartStopRepresentationCaApplicant() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> applicant = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        applicant.add(partyDetailsElement);


        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(List.of(CaseUser.builder().caseId("12345678").caseRole(
            "[C100APPLICANTSOLICITOR1]").build()));

        when(dynamicMultiSelectListService
                 .getSolicitorRepresentedParties(applicant))
            .thenReturn(DynamicMultiSelectList
                            .builder().value(List.of(dynamicListElement)).listItems(List.of(dynamicListElement))
                            .build());

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicant)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(ccdDataStoreService.findUserCaseRoles(anyString(), anyString()))
            .thenReturn(findUserCaseRolesResponse);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        String authToken = "test";

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        Map<String, Object> caseDataUpdated = noticeOfChangePartiesService
            .populateAboutToStartStopRepresentation(authToken, callbackRequest, new ArrayList<>());
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testPopulateAboutToStartStopRepresentationCaRespondent() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> respondent = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        respondent.add(partyDetailsElement);


        FindUserCaseRolesResponse findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(List.of(CaseUser.builder().caseId("12345678").caseRole(
            "[C100RESPONDENTSOLICITOR1]").build()));

        when(dynamicMultiSelectListService
                 .getSolicitorRepresentedParties(respondent))
            .thenReturn(DynamicMultiSelectList
                            .builder().value(List.of(dynamicListElement)).listItems(List.of(dynamicListElement))
                            .build());

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondent)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(ccdDataStoreService.findUserCaseRoles(anyString(), anyString()))
            .thenReturn(findUserCaseRolesResponse);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        String authToken = "test";

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        Map<String, Object> caseDataUpdated = noticeOfChangePartiesService
            .populateAboutToStartStopRepresentation(authToken, callbackRequest, new ArrayList<>());
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testSubmittedStopRepresenting() {
        List<Element<PartyDetails>> applicant = new ArrayList<>();
        partyDetails.setBarrister(Barrister.builder()
                                      .barristerEmail("barrister@gmail.com")
                                      .barristerId(UUID.randomUUID().toString())
                                      .build());

        Element partyDetailsElement = element(partyDetails);
        applicant.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicant)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();


        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            any(),
            anyInt(),
            any(),
            anyBoolean()
        )).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.submittedStopRepresenting(callbackRequest);
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
        verify(tabService).updatePartyDetailsForNoc(anyString(),
                                                    anyString(),
                                                    isA(StartEventResponse.class),
                                                    isA(EventRequestData.class),
                                                    caseDataArgumentCaptor.capture());
        CaseData updatedCaseData = caseDataArgumentCaptor.getValue();
        PartyDetails party = updatedCaseData.getApplicants().getFirst().getValue();
        assertThat(party.getBarrister())
            .isNull();
        assertThat(party.getSolicitorReference())
            .isNull();
        assertThat(party.getSolicitorTelephone())
            .isNull();
        assertThat(party.getSolicitorOrg())
            .isEqualTo(Organisation.builder().build());
        verify(barristerHelper, times(2)).setAllocatedBarrister(isA(PartyDetails.class),
                                                                isA(CaseData.class),
                                                                isA(UUID.class));
        verify(barristerRemoveService).notifyBarrister(isA(CaseData.class));
        verify(partyLevelCaseFlagsService).updateCaseDataWithGeneratePartyCaseFlags(isA(CaseData.class),
                                                                                    any(Function.class));
    }

    @Test
    public void testPopulateAboutToStartAdminRemoveLegalRepresentative() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        List<Element<PartyDetails>> respondent = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        respondent.add(partyDetailsElement);

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondent)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(dynamicMultiSelectListService
                 .getRemoveLegalRepAndPartiesList(caseData))
            .thenReturn(DynamicMultiSelectList
                            .builder().value(List.of(dynamicListElement)).listItems(List.of(dynamicListElement))
                            .build());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData,
            0,
            PartyRole.Representing.CAAPPLICANTSOLICITOR,
            false
        )).thenReturn(caseData);

        Map<String, Object> caseDataUpdated = noticeOfChangePartiesService
            .populateAboutToStartAdminRemoveLegalRepresentative(callbackRequest, new ArrayList<>());
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testAboutToSubmitAdminRemoveLegalRepresentativeC100Respondent() {
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(element(partyDetailsNoRep));

        List<Element<PartyDetails>> respondent = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);

        respondent.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicants)
            .respondents(respondent)
            .removeLegalRepAndPartiesList(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());

        String authToken = "test";

        Map<String, Object> caseDataUpdated = noticeOfChangePartiesService
            .aboutToSubmitAdminRemoveLegalRepresentative(authToken,callbackRequest);
        verify(caseAssignmentService).removeAmBarristerCaseRole(isA(CaseData.class),
                                                                ArgumentMatchers.<Map<Optional<SolicitorRole>, Element<PartyDetails>>>any());
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testAboutToSubmitAdminRemoveLegalRepresentativeFL401Applicant() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(partyDetails)
            .removeLegalRepAndPartiesList(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());

        String authToken = "test";

        Map<String, Object> caseDataUpdated = noticeOfChangePartiesService
            .aboutToSubmitAdminRemoveLegalRepresentative(authToken,callbackRequest);
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testAboutToSubmitAdminRemoveLegalRepresentativeFL401Respondent() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsNoRep)
            .respondentsFL401(partyDetails)
            .removeLegalRepAndPartiesList(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder()
                                                                     .forename("solicitorResp")
                                                                     .surname("test")
                                                                     .email("test@hmcts.net").build());

        String authToken = "test";

        Map<String, Object> caseDataUpdated = noticeOfChangePartiesService
            .aboutToSubmitAdminRemoveLegalRepresentative(authToken,callbackRequest);
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testSubmittedAdminRemoveLegalRepresentative() {
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetails.getPartyId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsNoRep)
            .respondentsFL401(partyDetails)
            .removeLegalRepAndPartiesList(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );
        DynamicListElement dynamicListElementRole = DynamicListElement.builder()
            .code("[FL401RESPONDENTSOLICITOR]")
            .label("[FL401RESPONDENTSOLICITOR]")
            .build();
        ChangeOrganisationRequest changeOrganisationRequest = ChangeOrganisationRequest.builder()
            .createdBy("test_solicitor@mailinator.com")
            .caseRoleId(DynamicList.builder()
                            .value(dynamicListElementRole)
                            .listItems(List.of(dynamicListElementRole))
                            .build())
            .organisationToAdd(Organisation.builder()
                                   .organisationID("EOILU2A")
                                   .organisationName("FPRL-test-organisation")
                                   .build())
            .build();
        PartyDetails updPartyDetails =
            updatePartyDetails(null, changeOrganisationRequest, partyDetails,TypeOfNocEventEnum.removeLegalRepresentation);

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());
        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            caseData.toBuilder().respondentsFL401(updPartyDetails).build(),
            0,
            PartyRole.Representing.DARESPONDENTSOLICITOR,
            false
        )).thenReturn(caseData);
        SubmittedCallbackResponse submittedCallbackResponse = noticeOfChangePartiesService
            .submittedAdminRemoveLegalRepresentative(callbackRequest);
        assertNotNull(submittedCallbackResponse);
        verify(barristerHelper, times(2)).setAllocatedBarrister(isA(PartyDetails.class),
                                                                isA(CaseData.class),
                                                                isA(UUID.class));
        verify(barristerRemoveService).notifyBarrister(isA(CaseData.class));
        verify(partyLevelCaseFlagsService).updateCaseDataWithGeneratePartyCaseFlags(isA(CaseData.class),
                                                                                    any(Function.class));
    }

    private static PartyDetails updatePartyDetails(SolicitorUser legalRepresentativeSolicitorDetails,
                                                   ChangeOrganisationRequest changeOrganisationRequest,
                                                   PartyDetails partyDetails, TypeOfNocEventEnum typeOfNocEvent) {
        return partyDetails.toBuilder()
            .user(partyDetails.getUser().toBuilder()
                      .solicitorRepresented(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                                ? YesOrNo.Yes : YesOrNo.No)
                      .build())
            .doTheyHaveLegalRepresentation(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                               ? YesNoDontKnow.yes : YesNoDontKnow.no)
            .solicitorEmail(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                ? legalRepresentativeSolicitorDetails.getEmail() : null)
            .representativeFirstName(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                         ? legalRepresentativeSolicitorDetails.getFirstName() : null)
            .representativeLastName(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                                        ? legalRepresentativeSolicitorDetails.getLastName() : null)
            .solicitorOrg(TypeOfNocEventEnum.addLegalRepresentation.equals(typeOfNocEvent)
                              ? changeOrganisationRequest.getOrganisationToAdd() : Organisation.builder().build())
            .response(null != partyDetails.getResponse()
                          && YesOrNo.Yes.equals(partyDetails.getResponse().getC7ResponseSubmitted())
                          ? partyDetails.getResponse() : Response.builder().build())
            .isRemoveLegalRepresentativeRequested(TypeOfNocEventEnum.removeLegalRepresentation.equals(typeOfNocEvent)
                                                      && YesOrNo.Yes.equals(partyDetails.getIsRemoveLegalRepresentativeRequested())
                                                      ? YesOrNo.No : partyDetails.getIsRemoveLegalRepresentativeRequested())
            .build();
    }

    @Test
    public void testSubmittedStopRepresentingWithAccessCode() {
        List<Element<PartyDetails>> applicant = new ArrayList<>();
        Element partyDetailsElement = element(partyDetails);
        UUID uuid = partyDetailsElement.getId();
        applicant.add(partyDetailsElement);
        DynamicMultiselectListElement dynamicListElement = DynamicMultiselectListElement.builder()
            .code(partyDetailsElement.getId().toString())
            .label(partyDetails.getFirstName() + " " + partyDetails.getLastName())
            .build();
        CaseInvite caseInvite = CaseInvite.builder().partyId(uuid).accessCode("123").build();
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.eventRequest(CaseEvent.UPDATE_ALL_TABS, "test")).thenReturn(EventRequestData.builder().build());


        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(applicant)
            .caseInvites(List.of(element(caseInvite)))
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(new ObjectMapper()))
            .build();


        when(ccdCoreCaseDataService.startUpdate(anyString(), any(EventRequestData.class), anyString(), eq(true))).thenReturn(
            StartEventResponse.builder().caseDetails(caseDetails).build());
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(ccdCoreCaseDataService.findCaseById("test", "12345678")).thenReturn(caseDetails);
        when(partyLevelCaseFlagsService.generateIndividualPartySolicitorCaseFlags(
            any(),
            anyInt(),
            any(),
            anyBoolean()
        )).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();

        noticeOfChangePartiesService.submittedStopRepresenting(callbackRequest);
        verify(eventPublisher, times(1)).publishEvent(any(NoticeOfChangeEvent.class));
        verify(barristerHelper, times(2)).setAllocatedBarrister(isA(PartyDetails.class),
                                                                isA(CaseData.class),
                                                                isA(UUID.class));
        verify(barristerRemoveService).notifyBarrister(isA(CaseData.class));
        verify(partyLevelCaseFlagsService).updateCaseDataWithGeneratePartyCaseFlags(isA(CaseData.class),
                                                                                    any(Function.class));
    }


    @Test
    public void testSendEmailAndUpdateCaseData_VerifiesSendEmailOnRemovalOfLegalRepresentation() throws Exception {

        Map<Optional<SolicitorRole>, Element<PartyDetails>> selectedPartyDetailsMap = new HashMap<>();
        Optional<SolicitorRole> solicitorRole = Optional.of(SolicitorRole.C100RESPONDENTSOLICITOR1);

        PartyDetails oldPD = PartyDetails.builder()
            .representativeFirstName("Old")
            .representativeLastName("Solicitor")
            .solicitorEmail("old@sol.test")
            .build();
        Element<PartyDetails> oldElem = ElementUtils.element(UUID.randomUUID(), oldPD);

        PartyDetails newPD = PartyDetails.builder()
            .representativeFirstName("New")
            .representativeLastName("Solicitor")
            .solicitorEmail(null)
            .build();
        Element<PartyDetails> newElem = ElementUtils.element(oldElem.getId(), newPD);

        selectedPartyDetailsMap.put(solicitorRole, newElem);

        // build a real data‐map so the static util sees non-null .data
        Map<String,Object> rawData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(rawData)
            .build();
        StartEventResponse startEventResponse = StartEventResponse.builder()
            .caseDetails(caseDetails)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(systemUserService.getUserId("test")).thenReturn("test");
        when(ccdCoreCaseDataService.startUpdate(
            any(),
            any(),
            any(),
            eq(true)
        )).thenReturn(startEventResponse);
        when(ccdCoreCaseDataService.findCaseById(anyString(), anyString()))
            .thenReturn(caseDetails);

        Method m = NoticeOfChangePartiesService.class
            .getDeclaredMethod("sendEmailAndUpdateCaseData", Map.class, String.class);
        m.setAccessible(true);
        NoticeOfChangePartiesService spyService = spy(noticeOfChangePartiesService);

        m.invoke(spyService, selectedPartyDetailsMap, "123456789");

        verify(spyService).sendEmailOnRemovalOfLegalRepresentation(
            any(Element.class),
            eq(newElem),
            eq(solicitorRole),
            eq(caseData)
        );
        verify(barristerHelper).setAllocatedBarrister(isA(PartyDetails.class),
                                                      isA(CaseData.class),
                                                      isA(UUID.class));
        verify(barristerRemoveService).notifyBarrister(isA(CaseData.class));
    }

    @Test
    public void testUpdateLegalRepresentationReturnsEarlyWhenRespondentsNull() {
        CaseData before = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(null)
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(before.toMap(realObjectMapper))
            .build();

        // after: some non-null respondents list
        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(partyDetails));
        CaseData after = CaseData.builder()
            .id(12345678L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(respondents)
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(after.toMap(realObjectMapper))
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .eventId("amendRespondentsDetails")
            .build();

        when(objectMapper.convertValue(caseDetailsBefore.getData(), CaseData.class)).thenReturn(before);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(after);

        noticeOfChangePartiesService.updateLegalRepresentation(callbackRequest, "testAuth", after);

        verifyNoInteractions(assignCaseAccessClient, caseAssignmentService, tabService, eventPublisher);
    }

    @Test
    public void testApplyDecisionLogsWhenResponseHasErrors() {
        when(userService.getUserDetails("testAuth"))
            .thenReturn(UserDetails.builder().forename("solicitorResp").surname("test").build());
        when(tokenGenerator.generate()).thenReturn("s2sToken");

        AboutToStartOrSubmitCallbackResponse errorResponse =
            AboutToStartOrSubmitCallbackResponse.builder()
                .errors(List.of("some error"))
                .data(new HashMap<>())
                .build();

        when(assignCaseAccessClient.applyDecision(anyString(), anyString(), any(DecisionRequest.class)))
            .thenReturn(errorResponse);

        noticeOfChangePartiesService.applyDecision(
            CallbackRequest.builder().caseDetails(CaseDetails.builder().build()).build(),
            "testAuth"
        );

        verify(assignCaseAccessClient, times(1)).applyDecision(anyString(), anyString(), any(DecisionRequest.class));
    }

    // ---- helper to invoke the private method that contains the guard clause
    @SuppressWarnings("unchecked")
    // ---- helper to invoke the private method that contains the guard clause
    private CaseData invokeUpdateC100PartyDetails(
        int partyIndex,
        CaseData caseData,
        PartyRole.Representing representing,
        TypeOfNocEventEnum typeOfNocEvent
    ) {
        try {
            // find the first declared method named updateC100PartyDetails
            Method target = Arrays.stream(NoticeOfChangePartiesService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("updateC100PartyDetails"))
                .findFirst()
                .orElseThrow(() -> new NoSuchMethodException("updateC100PartyDetails(..) not found"));

            target.setAccessible(true);

            Class<?>[] pts = target.getParameterTypes();
            Object[] args = new Object[pts.length];

            for (int i = 0; i < pts.length; i++) {
                Class<?> pt = pts[i];

                if (pt == int.class) {
                    args[i] = partyIndex;
                } else if (CaseData.class.isAssignableFrom(pt)) {
                    args[i] = caseData;
                } else if (pt.getName().endsWith("PartyRole$Representing")) {
                    args[i] = representing;
                } else if (pt.getName().endsWith("TypeOfNocEventEnum")) {
                    args[i] = typeOfNocEvent;
                } else {
                    // Any other params (List<Element<PartyDetails>>, SolicitorUser, ChangeOrganisationRequest,
                    // Organisations, etc.) aren't needed for the guard clause we want to hit; pass null.
                    args[i] = null;
                }
            }

            Object out = target.invoke(noticeOfChangePartiesService, args);
            return (CaseData) out;
        } catch (Exception e) {
            // Helpful dump if it ever fails again
            String sigs = Arrays.stream(NoticeOfChangePartiesService.class.getDeclaredMethods())
                .filter(m -> m.getName().equals("updateC100PartyDetails"))
                .map(Method::toString)
                .reduce((a,b) -> a + "\n" + b)
                .orElse("<none>");
            throw new RuntimeException("Failed to invoke updateC100PartyDetails. Available overloads:\n" + sigs, e);
        }
    }

    @Test
    public void updateC100PartyDetails_returnsOriginal_whenPartiesNull() {
        CaseData original = CaseData.builder().id(1L).build(); // respondents == null
        CaseData result = invokeUpdateC100PartyDetails(
            0, original,
            PartyRole.Representing.CARESPONDENTSOLICITOR,
            TypeOfNocEventEnum.removeLegalRepresentation
        );
        assertThat(result).isSameAs(original);
    }

    @Test
    public void updateC100PartyDetails_returnsOriginal_whenIndexNegative() {
        CaseData cd = CaseData.builder()
            .id(2L)
            .caseTypeOfApplication("c100")
            .respondents(Collections.singletonList(element(PartyDetails.builder().build())))
            .build();

        CaseData result = invokeUpdateC100PartyDetails(
            -1, cd,
            PartyRole.Representing.CARESPONDENTSOLICITOR,
            TypeOfNocEventEnum.removeLegalRepresentation
        );
        assertThat(result).isSameAs(cd);
    }

    @Test
    public void updateC100PartyDetails_returnsOriginal_whenIndexTooLarge() {
        CaseData cd = CaseData.builder()
            .id(3L)
            .caseTypeOfApplication("c100")
            .respondents(Collections.singletonList(element(PartyDetails.builder().build())))
            .build();

        // size is 1 ⇒ valid index is 0; 1 is out-of-bounds
        CaseData result = invokeUpdateC100PartyDetails(
            1, cd,
            PartyRole.Representing.CARESPONDENTSOLICITOR,
            TypeOfNocEventEnum.removeLegalRepresentation
        );
        assertThat(result).isSameAs(cd);
    }
}
