package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ExitAwaitingInformation;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EXIT_AWAITING_INFORMATION_DETAILS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.STATE_FIELD;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExitAwaitingInformationServiceTest {

    @InjectMocks
    private ExitAwaitingInformationService exitAwaitingInformationService;

    @Mock
    private ObjectMapper objectMapper;

    private Map<String, Object> caseDataMap;
    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345678L);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(State.AWAITING_INFORMATION.getValue())
            .data(caseDataMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    @Test
    public void updateCaseShouldSetRequestedTargetState() {
        ExitAwaitingInformation exitAwaitingInformation = ExitAwaitingInformation.builder()
            .exitAwaitingInformationTargetState(State.CASE_ISSUED)
            .build();

        caseDataMap.put(EXIT_AWAITING_INFORMATION_DETAILS, exitAwaitingInformation);
        when(objectMapper.convertValue(exitAwaitingInformation, ExitAwaitingInformation.class))
            .thenReturn(exitAwaitingInformation);

        Map<String, Object> updatedCaseData = exitAwaitingInformationService.updateCase(callbackRequest);

        assertSame(caseDataMap, updatedCaseData);
        assertEquals(State.CASE_ISSUED.getValue(), updatedCaseData.get(STATE_FIELD));
        assertTrue(updatedCaseData.get(CASE_STATUS) instanceof CaseStatus);
        assertEquals(
            State.CASE_ISSUED.getLabel(),
            ((CaseStatus) updatedCaseData.get(CASE_STATUS)).getState()
        );
    }

}
