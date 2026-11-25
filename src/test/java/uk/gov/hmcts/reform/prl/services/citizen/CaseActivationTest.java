package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
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
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        when(systemUserService.getSysUserToken()).thenReturn(SYSTEM_TOKEN);
        lenient().when(idamClient.getUserInfo(USER_TOKEN)).thenReturn(UserInfo.builder().uid("citizen-id").build());
    }

    @ParameterizedTest
    @MethodSource("allowedActivationStates")
    void checksUnlinkedEligibleCaseUsingSystemAccountWithoutGrantingAccess(State state) {
        mockCase(state.getValue(), "C100", "Yes", Map.of());

        assertEquals(VALID, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
        verify(ccdCoreCaseDataService).findCaseById(SYSTEM_TOKEN, CASE_ID);
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @ParameterizedTest
    @MethodSource("allowedActivationStates")
    void permitsRespondentAndEnabledFl401ActivationInEligibleStates(State state) {
        mockCase(state.getValue(), "FL401", "No", Map.of());
        when(launchDarklyClient.isFeatureEnabled(PrlAppsConstants.CITIZEN_ALLOW_DA_JOURNEY)).thenReturn(true);

        assertEquals(VALID, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING_CONDUCT_HEARING", "DECISION_OUTCOME", "ALL_FINAL_ORDERS_ISSUED"
    }, mode = EnumSource.Mode.EXCLUDE)
    void refusesAllOtherStatesWithoutChangingTheCase(State state) {
        mockCase(state.getValue(), "C100", "Yes", Map.of());

        assertEquals(NOT_READY, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    void refusesMissingOrUnknownState() {
        for (String state : new String[]{null, "", "UNKNOWN"}) {
            mockCase(state, "C100", "Yes", Map.of());
            assertEquals(NOT_READY, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
        }
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    void invalidCodeDoesNotDiscloseWhetherTheCaseIsReady() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of());

        assertEquals(INVALID, service.validateAccessCode(CASE_ID, "BADCODE1", USER_TOKEN));
        verifyNoInteractions(caseAccessApi, allTabService, idamClient);
    }

    @Test
    void preservesLinkedAndDuplicateResultsBeforeCheckingState() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of("hasLinked", "Yes"));
        assertEquals(LINKED, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));

        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes",
                 Map.of("hasLinked", "Yes", "invitedUserId", "citizen-id"));
        assertEquals(DUPLICATE, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @Test
    void reportsNotReadyForValidCodeBeforeHearing() {
        mockCase(State.AWAITING_INFORMATION.getValue(), "C100", "Yes", Map.of());

        assertEquals(NOT_READY, service.validateAccessCode(CASE_ID, ACCESS_CODE, USER_TOKEN));
    }

    @ParameterizedTest
    @EnumSource(value = State.class, names = {
        "PREPARE_FOR_HEARING_CONDUCT_HEARING", "DECISION_OUTCOME", "ALL_FINAL_ORDERS_ISSUED"
    }, mode = EnumSource.Mode.EXCLUDE)
    void directLinkRequestCannotBypassTheStateCheck(State state) {
        mockCase(state.getValue(), "C100", "Yes", Map.of());

        assertThrows(CaseActivationNotAllowedException.class,
                     () -> service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE));
        verifyNoInteractions(caseAccessApi, allTabService);
    }

    @ParameterizedTest
    @MethodSource("allowedActivationStates")
    void refusesLinkingIfStateChangesAfterTheInitialRead(State state) {
        CaseDetails details = mockCase(state.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class).toBuilder()
            .state(State.AWAITING_INFORMATION).build();
        mockEvent(eventData);

        assertThrows(CaseActivationNotAllowedException.class,
                     () -> service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE));
        verifyNoInteractions(caseAccessApi);
        assertNull(eventData.getCaseInvites().getFirst().getValue().getHasLinked());
    }

    @ParameterizedTest
    @MethodSource("allowedActivationStates")
    void refusesLinkingIfTheEventSnapshotHasNoState(State state) {
        CaseDetails details = mockCase(state.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class);
        mockEvent(eventData);

        assertThrows(CaseActivationNotAllowedException.class,
                     () -> service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE));
        verifyNoInteractions(caseAccessApi);
        assertNull(eventData.getCaseInvites().getFirst().getValue().getHasLinked());
    }

    @ParameterizedTest
    @MethodSource("allowedActivationStates")
    void refusesLinkingIfTheCodeWasUsedAfterTheInitialRead(State state) {
        CaseDetails details = mockCase(state.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class).toBuilder()
            .state(state).build();
        eventData.getCaseInvites().getFirst().getValue().setHasLinked("Yes");
        mockEvent(eventData);

        assertFalse(service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE).isPresent());
        verifyNoInteractions(caseAccessApi);
    }

    @ParameterizedTest
    @MethodSource("allowedActivationStates")
    void linksAndUpdatesInvitationWhenTheEventSnapshotIsEligible(State state) {
        CaseDetails details = mockCase(state.getValue(), "C100", "Yes", Map.of());
        CaseData eventData = objectMapper.convertValue(details.getData(), CaseData.class).toBuilder()
            .state(state).build();
        mockEvent(eventData);
        when(idamClient.getUserDetails(SYSTEM_TOKEN)).thenReturn(UserDetails.builder().id("system-id").build());
        when(authTokenGenerator.generate()).thenReturn("service-token");
        when(allTabService.submitAllTabsUpdate(eq(SYSTEM_TOKEN), eq(CASE_ID), any(), any(), anyMap())).thenReturn(details);

        assertTrue(service.linkCitizenToCase(USER_TOKEN, CASE_ID, ACCESS_CODE).isPresent());
        verify(caseAccessApi).grantAccessToCase(eq(SYSTEM_TOKEN), eq("service-token"), eq("system-id"),
                                              eq(PrlAppsConstants.JURISDICTION), eq(PrlAppsConstants.CASE_TYPE),
                                              eq(CASE_ID), any());
        verify(allTabService).submitAllTabsUpdate(eq(SYSTEM_TOKEN), eq(CASE_ID), any(), any(), anyMap());
        assertEquals("Yes", eventData.getCaseInvites().getFirst().getValue().getHasLinked());
        assertEquals("citizen-id", eventData.getCaseInvites().getFirst().getValue().getInvitedUserId());
    }

    private static Stream<State> allowedActivationStates() {
        return Stream.of(State.PREPARE_FOR_HEARING_CONDUCT_HEARING, State.DECISION_OUTCOME, State.ALL_FINAL_ORDERS_ISSUED);
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
