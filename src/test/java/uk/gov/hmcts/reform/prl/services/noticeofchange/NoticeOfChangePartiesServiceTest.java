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
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.DecisionRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessClient;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class NoticeOfChangePartiesServiceTest {
    @InjectMocks
    NoticeOfChangePartiesService noticeOfChangePartiesService;

    CaseData caseData;
    SolicitorRole role;

    @Mock
    RespondentPolicyConverter policyConverter;

    @Mock
    NoticeOfChangePartiesConverter partiesConverter;

    Optional<Element<PartyDetails>> optionalParty;

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

    @Before
    public void setUp() {

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        optionalParty = Optional.of(wrappedRespondents);
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder()
            .caseTypeOfApplication("c100")
            .respondents(respondentList)
            .build();

        role = SolicitorRole.SOLICITORCARA;
    }

    @Test
    public void testGenerate() {

        when(policyConverter.caGenerate(role, optionalParty))
            .thenReturn(organisationPolicy);

        when(partiesConverter.generateCaForSubmission(wrappedRespondents))
            .thenReturn(noticeOfChangeParties);

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseData, role.getRepresenting());

        assertTrue(test.containsKey("caRespondent0Policy"));

    }

    @Test
    public void testGenerateWithBlankStrategy() {

        NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy strategy = NoticeOfChangePartiesService
            .NoticeOfChangeAnswersPopulationStrategy.BLANK;

        Map<String, Object> test = noticeOfChangePartiesService.generate(caseData, role.getRepresenting(), strategy);

        assertTrue(test.containsKey("caRespondent0Policy"));

    }

    @Test
    public void testApplyDecision() {
        when(tokenGenerator.generate()).thenReturn("s2sToken");
        noticeOfChangePartiesService.applyDecision(CallbackRequest.builder().build(), "testAuth");
        verify(assignCaseAccessClient, times(1)).applyDecision(Mockito.anyString(), Mockito.anyString(), Mockito.any(
            DecisionRequest.class));
    }

    @Test
    @Ignore
    public void testNocRequestSubmitted() {
        DynamicListElement dynamicListElement = DynamicListElement.builder()
            .code("[SOLICITORA]")
            .label("Respondent solicitor A")
            .build();

        List<Element<PartyDetails>> respondents = new ArrayList<>();
        respondents.add(element(PartyDetails.builder()
                                    .user(User.builder().build())
                                    .firstName("test")
                                    .lastName("test")
                                    .email("test@hmcts.net")
                                    .build()));
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
        verify(tabService, times(1)).updatePartyDetailsForNoc(Mockito.any(CaseData.class), Mockito.any());
        verify(eventPublisher, times(1)).publishEvent(Mockito.any(NoticeOfChangeEvent.class));
    }
}
