package uk.gov.hmcts.reform.prl.controllers.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagMigrationService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_DRAFT_C1A;

@RunWith(MockitoJUnitRunner.class)
public class MigrationControllerTest {

    @Mock
    AllTabServiceImpl tabService;

    @InjectMocks
    MigrationController migrationController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    CaseFlagMigrationService caseFlagMigrationService;

    @Mock
    PartyLevelCaseFlagsService partyLevelCaseFlagsService;


    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;

    String authToken = "Bearer TestAuthToken";
    String serviceAuth = "serviceAuth";

    @Mock
    private ObjectMapper objectMapper;


    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseData = CaseData.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID)
                .build();
        caseDetails = CaseDetails.builder()
                .id(12345678L)
                .state(State.SUBMITTED_PAID.getValue())
                .data(caseDataMap)
                .build();
        callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();
        Map<String, Object> caseFlags = new HashMap<String, Object>();
        caseFlags.put("caApplicant1InternalFlags", Flags.builder().build());
        when(partyLevelCaseFlagsService.generatePartyCaseFlags(any(CaseData.class))).thenReturn(caseFlags);
        when(caseFlagMigrationService.migrateCaseForCaseFlags(any(Map.class))).thenReturn(caseFlags);


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    @Test
    public void handleSubmitted() {
        migrationController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService, times(1)).updateAllTabsIncludingConfTab(Mockito.anyString());
    }

    @Test
    public void handleAboutSubmit() {
        migrationController.handleAboutToSubmit("testAuth", callbackRequest);
        verify(partyLevelCaseFlagsService, times(1)).generatePartyCaseFlags(caseData);
    }

    @Test
    public void handleSubmittedToRemoveDoc() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = migrationController
                .handleSubmittedToRemoveDoc(authToken, serviceAuth, callbackRequest);
        assertNull(response.getData().get(DOCUMENT_FIELD_C1A));
        assertNull(response.getData().get(DOCUMENT_FIELD_DRAFT_C1A));
    }

    @Test
    public void handleSubmittedToRemoveDoc_1() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Mockito.when(authorisationService.isAuthorized(authToken, serviceAuth)).thenReturn(false);
        assertExpectedException(() -> {
            migrationController.handleSubmittedToRemoveDoc(authToken, serviceAuth, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
