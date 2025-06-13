package uk.gov.hmcts.reform.prl.controllers.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_DRAFT_C1A;

@ExtendWith(MockitoExtension.class)
class MigrationControllerTest {

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


    @BeforeEach
    void setup() {
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
    void handleSubmitted() {
        migrationController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService, times(1)).updateAllTabsIncludingConfTab(Mockito.anyString());
    }

    @Test
    void handleAboutSubmit() {
        migrationController.handleAboutToSubmit("testAuth", callbackRequest);
        verify(partyLevelCaseFlagsService, times(1)).generatePartyCaseFlags(caseData);
    }

    @Test
    void handleSubmittedToRemoveDoc() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = migrationController
            .handleSubmittedToRemoveDoc(authToken, serviceAuth, callbackRequest);
        assertNull(response.getData().get(DOCUMENT_FIELD_C1A));
        assertNull(response.getData().get(DOCUMENT_FIELD_DRAFT_C1A));
    }

    @Test
    void handleSubmittedToRemoveDoc_1() {
        when(authorisationService.isAuthorized(any(), any())).thenReturn(false);
        Mockito.when(authorisationService.isAuthorized(authToken, serviceAuth)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> {
                migrationController.handleSubmittedToRemoveDoc(authToken, serviceAuth, callbackRequest);
            }
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
