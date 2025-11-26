package uk.gov.hmcts.reform.prl.services.caseinitiation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.CaseRole;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.caseinitiation.CaseInitiationService.COURT_LIST;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseInitiationServiceTest {

    public static final String AUTHORISATION = "Bearer token";
    public static final String CASE_ID = "1234567891234567";

    @InjectMocks
    private CaseInitiationService caseInitiationService;

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private EventService eventPublisher;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AssignCaseAccessService assignCaseAccessService;
    @Mock
    private LocationRefDataService locationRefDataService;
    @Mock
    private OrganisationService organisationService;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(Long.parseLong(CASE_ID))
            .state("SUBMITTED_PAID")
            .build();

        PartyDetails partyDetailsApplicant = PartyDetails.builder()
            .firstName("applicantFirst")
            .lastName("applicantLast")
            .email("applicant@example.com")
            .user(User.builder().email("applicant@example.com").idamId("applicant-idam-id").build())
            .build();

        PartyDetails partyDetailsRespondent = PartyDetails.builder()
            .firstName("respFirst")
            .lastName("respLast")
            .email("respondent@example.com")
            .user(User.builder().email("respondent@example.com").idamId("respondent-idam-id").build())
            // no solicitorEmail / solicitorOrg by default
            .build();

        caseDataMap.put("caseTypeOfApplication", C100_CASE_TYPE);
        caseData = CaseData.builder()
            .id(Long.parseLong(CASE_ID))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder()
                                     .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent)
                                     .build()))
            .build();
    }

    @Test
    public void testHandleCaseInitiation_basicPath() {
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.submitSupplementaryData(any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());

        caseInitiationService.handleCaseInitiation(
            AUTHORISATION,
            CallbackRequest.builder().caseDetails(caseDetails).build()
        );

        // applicant path invoked
        verify(assignCaseAccessService, times(1)).assignCaseAccess(CASE_ID, AUTHORISATION);
        // no respondent assign because solicitorEmail/solicitorOrg not set
        verify(assignCaseAccessService, never())
            .assignCaseAccessToUserWithRole(any(), any(), any(), any());
        // event published
        verify(eventPublisher, times(1)).publishEvent(any(CaseDataChanged.class));
    }

    @Test
    public void testHandleCaseInitiationAssignsRespondentSolicitorAccess() {
        // respondent with legal rep, solicitor email + org
        PartyDetails respondentWithSolicitor = PartyDetails.builder()
            .firstName("respFirst")
            .lastName("respLast")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorEmail("resp-solicitor@example.com")
            .solicitorOrg(Organisation.builder().organisationID("ORG123").build())
            .build();

        caseData = CaseData.builder()
            .id(Long.parseLong(CASE_ID))
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(List.of(
                Element.<PartyDetails>builder()
                    .id(UUID.fromString("00000000-0000-0000-0000-000000000000"))
                    .value(respondentWithSolicitor)
                    .build()
            ))
            .build();

        caseDataMap.put("caseTypeOfApplication", C100_CASE_TYPE);

        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(coreCaseDataApi.submitSupplementaryData(any(), any(), any(), any()))
            .thenReturn(CaseDetails.builder().build());

        // organisationService returns a user id for that email
        when(organisationService.findUserByEmail("resp-solicitor@example.com"))
            .thenReturn(java.util.Optional.of("respondent-solicitor-user-id"));

        caseInitiationService.handleCaseInitiation(
            AUTHORISATION,
            CallbackRequest.builder().caseDetails(caseDetails).build()
        );

        // applicant call still invoked (even though no explicit applicant in this case variant)
        verify(assignCaseAccessService, times(1)).assignCaseAccess(CASE_ID, AUTHORISATION);

        // respondent role is first in CaseRole.respondentSolicitors()
        CaseRole expectedRole = CaseRole.respondentSolicitors().get(0);

        verify(assignCaseAccessService, times(1))
            .assignCaseAccessToUserWithRole(CASE_ID,
                "respondent-solicitor-user-id",
                expectedRole.formattedName(),
                AUTHORISATION
            );
    }

    @Test
    public void testHandlePrePopulateCourtDetailsC100() {
        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getCourtLocations(Mockito.anyString()))
            .thenReturn(List.of(DynamicListElement.EMPTY));

        Assertions.assertTrue(
            caseInitiationService.prePopulateCourtDetails(AUTHORISATION, caseDataMap)
                .containsKey(COURT_LIST)
        );
    }

    @Test
    public void testHandlePrePopulateCourtDetailsFL401() {
        caseDataMap.put("caseTypeOfApplication", FL401_CASE_TYPE);
        caseData = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .build();
        List<DynamicListElement> courts = List.of(DynamicListElement.builder().label("123").build());

        when(locationRefDataService.getDaCourtLocations(AUTHORISATION))
            .thenReturn(courts);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);
        when(locationRefDataService.getDaCourtLocations(Mockito.anyString()))
            .thenReturn(courts);

        Assertions.assertTrue(
            caseInitiationService.prePopulateCourtDetails(AUTHORISATION, caseDataMap)
                .containsKey(COURT_LIST)
        );
    }
}
