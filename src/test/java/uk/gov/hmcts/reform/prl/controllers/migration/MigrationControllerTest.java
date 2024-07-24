package uk.gov.hmcts.reform.prl.controllers.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagMigrationService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MigrationControllerTest {

    @Mock
    AllTabServiceImpl tabService;

    @InjectMocks
    MigrationController migrationController;

    @Mock
    CaseFlagMigrationService caseFlagMigrationService;

    @Mock
    PartyLevelCaseFlagsService partyLevelCaseFlagsService;


    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

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
        Map<String,Object> caseFlags = new HashMap<String, Object>();
        caseFlags.put("caApplicant1InternalFlags", Flags.builder().build());
        when(partyLevelCaseFlagsService.generatePartyCaseFlags(any(CaseData.class))).thenReturn(caseFlags);
        when(caseFlagMigrationService.migrateCaseForCaseFlags(any(Map.class))).thenReturn(caseFlags);


        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    @Test
    public void handleSubmitted() {
        migrationController.handleSubmitted(callbackRequest, "testAuth");
        verify(tabService,times(1)).updateAllTabsIncludingConfTab(Mockito.anyString());
    }

    @Test
    public void handleAboutSubmit() {
        migrationController.handleAboutToSubmit("testAuth",callbackRequest);
        verify(partyLevelCaseFlagsService,times(1)).generatePartyCaseFlags(caseData);
    }
}
