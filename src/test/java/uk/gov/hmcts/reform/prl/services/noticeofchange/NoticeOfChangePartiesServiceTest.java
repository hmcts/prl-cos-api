package uk.gov.hmcts.reform.prl.services.noticeofchange;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.pin.CaseInviteManager;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.time.Time;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
@Ignore
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

    Element<PartyDetails> wrappedRespondents;

    NoticeOfChangeParties noticeOfChangeParties = NoticeOfChangeParties.builder().build();

    OrganisationPolicy organisationPolicy = OrganisationPolicy.builder().build();
    @Mock
    AssignCaseAccessClient assignCaseAccessClient;
    @Mock
    AuthTokenGenerator tokenGenerator;
    @Mock
    ObjectMapper objectMapper;
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
    private Time time;

    @Before
    public void setUp() {
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

        wrappedRespondents = Element.<PartyDetails>builder().value(partyDetails).build();
        optionalParty = Optional.of(wrappedRespondents);
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);


        caseData = CaseData.builder()
            .caseTypeOfApplication("c100")
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

        assertTrue(test.containsKey("daApplicantPolicy"));

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
    public void testApplyDecision() {
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        when(tokenGenerator.generate()).thenReturn("s2sToken");
        noticeOfChangePartiesService.applyDecision(CallbackRequest.builder().build(), "testAuth");
        verify(assignCaseAccessClient, times(1)).applyDecision(Mockito.anyString(), Mockito.anyString(), Mockito.any(
            DecisionRequest.class));
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
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
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
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest, "testAuth");
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            Mockito.any(),
            Mockito.anyList(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(Mockito.any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testNocRequestSubmittedForC100ApplicantSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[C100APPLICANTSOLICITOR1]")
            .label("Applicant solicitor A")
            .build();
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        applicants.add(element(partyDetails));

        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
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
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest, "testAuth");
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            Mockito.any(),
            Mockito.anyList(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(Mockito.any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testNocRequestSubmittedForFL401RespondentSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[FL401RESPONDENTSOLICITOR]")
            .label("Respondent solicitor A")
            .build();

        CaseData caseData = CaseData.builder()
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
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest, "testAuth");
        verify(eventPublisher, times(1)).publishEvent(Mockito.any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testNocRequestSubmittedForFL401ApplicantSolicitor() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[FL401APPLICANTSOLICITOR]")
            .label("Applicant solicitor A")
            .build();

        CaseData caseData = CaseData.builder()
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
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        noticeOfChangePartiesService.nocRequestSubmitted(callbackRequest, "testAuth");
        verify(tabService, times(1)).updatePartyDetailsForNoc(
            Mockito.any(),
            Mockito.anyList(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(CaseData.class)
        );
        verify(eventPublisher, times(1)).publishEvent(Mockito.any(NoticeOfChangeEvent.class));
    }

    @Test
    public void testUpdateLegalRepresentation() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[C100RESPONDENTSOLICITOR1]")
            .label("Respondent solicitor A")
            .build();

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(partyDetails));
        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
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
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails("testAuth")).thenReturn(UserDetails.builder()
                                                                    .forename("solicitorResp")
                                                                    .surname("test").build());
        noticeOfChangePartiesService.updateLegalRepresentation(callbackRequest, "testAuth", caseData);
        verify(assignCaseAccessClient, times(0)).applyDecision(Mockito.any(), Mockito.any(), Mockito.any());
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


        CaseData caseData = CaseData.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .applicants(applicant)
            .solStopRepChooseParties(DynamicMultiSelectList.builder().value(List.of(dynamicListElement)).listItems(List.of(
                dynamicListElement)).build())
            .build();

        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
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
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(new ObjectMapper())).build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest, new ArrayList<>());
        verify(assignCaseAccessClient, times(1)).applyDecision(Mockito.any(), Mockito.any(), Mockito.any());
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
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
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
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(new ObjectMapper())).build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest, new ArrayList<>());
        verify(assignCaseAccessClient, times(1)).applyDecision(Mockito.any(), Mockito.any(), Mockito.any());
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
            "[FL401APPLICANTSOLICITOR]").build()));
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
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(new ObjectMapper())).build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest, new ArrayList<>());
        verify(assignCaseAccessClient, times(1)).applyDecision(Mockito.any(), Mockito.any(), Mockito.any());
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
            AboutToStartOrSubmitCallbackResponse.builder().data(caseData.toMap(new ObjectMapper())).build());
        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS.getValue())
            .data(caseData.toMap(new ObjectMapper()))
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetails)
            .build();
        noticeOfChangePartiesService.aboutToSubmitStopRepresenting("testAuth", callbackRequest, new ArrayList<>());
        verify(assignCaseAccessClient, times(1)).applyDecision(Mockito.any(), Mockito.any(), Mockito.any());
    }

}
