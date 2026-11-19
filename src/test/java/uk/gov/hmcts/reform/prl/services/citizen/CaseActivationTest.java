package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.exception.CaseActivationNotAllowedException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.DUPLICATE;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.INVALID;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.LINKED;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.NOT_READY;
import static uk.gov.hmcts.reform.prl.services.citizen.LinkCitizenCaseService.VALID;

@RunWith(MockitoJUnitRunner.class)
public class CaseActivationTest {
    private static final String CASE_ID = "1234567890123456";
    private static final String ACCESS_CODE = "ABCD1234";
    private static final String USER_TOKEN = "citizen-token";
    private static final String SYSTEM_TOKEN = "system-user-token";
    private static final String PARTY_ID = "00000000-0000-0000-0000-000000000001";

    @InjectMocks
    private LinkCitizenCaseService service;
    @Mock
    private AllTabServiceImpl allTabService;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private CcdCoreCaseDataService ccdCoreCaseDataService;
    @Mock
    private IdamClient idamClient;
    @Mock
    private CaseAccessApi caseAccessApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private LaunchDarklyClient launchDarklyClient;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new ParameterNamesModule()).registerModule(new JavaTimeModule());

    @Before
    public void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_TOKEN);
        lenient().when(idamClient.getUserInfo(USER_TOKEN)).thenReturn(UserInfo.builder().uid("citizen-id").build());
    }

    @Test
    public void checksUnlinkedHearingCaseUsingSystemAccountWithoutGrantingAccess() {
        mockCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(), "C100", "Yes", Map.of());

        assertEquals(VALID, service.checkCaseActivation(CASE_ID, ACCESS_CODE, USER_TOKEN).status());
        verify(ccdCoreCaseDataService).findCaseById(SYSTEM_TOKEN, CASE_ID);
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    public void permitsRespondentAndEnabledFl401ActivationAtHearing() {
        mockCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(), "FL401", "No", Map.of());
        when(launchDarklyClient.isFeatureEnabled(PrlAppsConstants.CITIZEN_ALLOW_DA_JOURNEY)).thenReturn(true);

        assertEquals(VALID, service.checkCaseActivation(CASE_ID, ACCESS_CODE, USER_TOKEN).status());
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    public void refusesAllOtherStatesWithoutChangingTheCase() {
        for (State state : State.values()) {
            if (state != State.PREPARE_FOR_HEARING_CONDUCT_HEARING) {
                mockCase(state.getValue(), "C100", "Yes", Map.of());
                assertEquals(state.getValue(), NOT_READY,
                             service.checkCaseActivation(CASE_ID, ACCESS_CODE, USER_TOKEN).status());
            }
        }
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    public void refusesMissingOrUnknownState() {
        for (String state : new String[]{null, "", "UNKNOWN"}) {
            mockCase(state, "C100", "Yes", Map.of());
            assertEquals(NOT_READY, service.checkCaseActivation(CASE_ID, ACCESS_CODE, USER_TOKEN).status());
        }
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    public void invalidCodeDoesNotDiscloseWhetherTheCaseIsReady() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of());

        assertEquals(INVALID, service.checkCaseActivation(CASE_ID, "BADCODE1", USER_TOKEN).status());
        verifyNoInteractions(caseAccessApi, allTabService, idamClient);
    }

    @Test
    public void preservesLinkedAndDuplicateResultsBeforeCheckingState() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of("hasLinked", "Yes"));
        assertEquals(LINKED, service.checkCaseActivation(CASE_ID, ACCESS_CODE, USER_TOKEN).status());

        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes",
                 Map.of("hasLinked", "Yes", "invitedUserId", "citizen-id"));
        assertEquals(DUPLICATE, service.checkCaseActivation(CASE_ID, ACCESS_CODE, USER_TOKEN).status());
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    public void keepsLegacyAccessCodeValidationUnchanged() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of());

        assertEquals(VALID, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
    }

    @Test
    public void directLinkRequestCannotBypassTheStateCheck() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of());

        assertThrows(CaseActivationNotAllowedException.class,
                     () -> service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE));
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    public void refusesLinkingIfStateChangesAfterTheInitialRead() {
        CaseDetails details = mockCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class).toBuilder()
            .state(State.AWAITING_INFORMATION).build();
        mockEvent(eventData);

        assertThrows(CaseActivationNotAllowedException.class,
                     () -> service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE));
        verifyNoInteractions(caseAccessApi);
        assertNull(eventData.getCaseInvites().getFirst().getValue().getHasLinked());
    }

    @Test
    public void refusesLinkingIfTheEventSnapshotHasNoState() {
        CaseDetails details = mockCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class);
        mockEvent(eventData);

        assertThrows(CaseActivationNotAllowedException.class,
                     () -> service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE));
        verifyNoInteractions(caseAccessApi);
        assertNull(eventData.getCaseInvites().getFirst().getValue().getHasLinked());
    }

    @Test
    public void refusesLinkingIfTheCodeWasUsedAfterTheInitialRead() {
        CaseDetails details = mockCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class).toBuilder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();
        eventData.getCaseInvites().getFirst().getValue().setHasLinked("Yes");
        mockEvent(eventData);

        assertFalse(service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE).isPresent());
        verifyNoInteractions(caseAccessApi);
    }

    @Test
    public void linksAndUpdatesInvitationWhenTheEventSnapshotIsEligible() {
        CaseDetails details = mockCase(State.PREPARE_FOR_HEARING_CONDUCT_HEARING.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class).toBuilder()
            .state(State.PREPARE_FOR_HEARING_CONDUCT_HEARING).build();
        mockEvent(eventData);
        when(idamClient.getUserDetails(SYSTEM_TOKEN)).thenReturn(UserDetails.builder().id("system-id").build());
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(allTabService.submitAllTabsUpdate(eq(SYSTEM_TOKEN), eq(CASE_ID), any(), any(), anyMap())).thenReturn(details);

        assertTrue(service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE).isPresent());
        verify(caseAccessApi).grantAccessToCase(eq(SYSTEM_TOKEN), eq("service-token"), eq("system-id"),
                                              eq(PrlAppsConstants.JURISDICTION), eq(PrlAppsConstants.CASE_TYPE),
                                              eq(CASE_ID), any());
        assertEquals("Yes", eventData.getCaseInvites().getFirst().getValue().getHasLinked());
        assertEquals("citizen-id", eventData.getCaseInvites().getFirst().getValue().getInvitedUserId());
    }

    private void mockEvent(CaseData eventData) {
        when(idamClient.getUserDetails(USER_TOKEN)).thenReturn(UserDetails.builder()
                                                                  .id("citizen-id").email("citizen@example.com").build());
        when(allTabService.getStartUpdateForSpecificEvent(CASE_ID, CaseEvent.LINK_CITIZEN.getValue()))
            .thenReturn(new StartAllTabsUpdateDataContent(SYSTEM_TOKEN, EventRequestData.builder().build(),
                                                        StartEventResponse.builder().build(), Map.of(), eventData, null));
    }

    private CaseDetails mockCase(String state, String caseType, String isApplicant, Map<String, Object> inviteFields) {
        Map<String, Object> invite = new HashMap<>(Map.of("partyId", PARTY_ID, "isApplicant", isApplicant,
                                                        "accessCode", ACCESS_CODE));
        invite.putAll(inviteFields);
        CaseDetails details = CaseDetails.builder().id(123L).state(state)
            .data(Map.of("caseTypeOfApplication", caseType,
                         "applicants", List.of(Map.of("id", PARTY_ID, "value", Map.of("user", Map.of()))),
                         "caseInvites", List.of(Map.of("id", PARTY_ID, "value", invite))))
            .build();
        when(ccdCoreCaseDataService.findCaseById(SYSTEM_TOKEN, CASE_ID)).thenReturn(details);
        return details;
    }
}
