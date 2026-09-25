package uk.gov.hmcts.reform.prl.services.citizen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.CaseEvent.CITIZEN_CASE_UPDATE;

@ExtendWith(MockitoExtension.class)
class CitizenUserCaseUpdateServiceTest {

    private static final String AUTHORISATION = "Bearer citizen-token";
    private static final String CASE_ID = "1234567890123456";

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private CitizenCoreCaseDataService citizenCoreCaseDataService;

    @InjectMocks
    private CitizenUserCaseUpdateService citizenUserCaseUpdateService;

    @Test
    void shouldReturnNotFoundWhenCitizenDoesNotHaveCaseAccess() {
        when(citizenCoreCaseDataService.hasCitizenAccess(AUTHORISATION, CASE_ID)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> citizenUserCaseUpdateService.validateCitizenCaseAccess(AUTHORISATION, CASE_ID)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldStartUpdateSubmitAndReturnUsingCitizenAuthorisation() {
        Map<String, Object> caseDataMap = new HashMap<>();
        EventRequestData eventRequestData = EventRequestData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().build();
        UserDetails userDetails = UserDetails.builder().id("citizen-id").build();
        StartAllTabsUpdateDataContent updateData = new StartAllTabsUpdateDataContent(
            AUTHORISATION,
            eventRequestData,
            startEventResponse,
            caseDataMap,
            CaseData.builder().build(),
            userDetails
        );

        when(allTabService.getStartUpdateForSpecificUserEvent(
            CASE_ID,
            CITIZEN_CASE_UPDATE.getValue(),
            AUTHORISATION
        )).thenReturn(updateData);
        when(allTabService.submitUpdateForSpecificUserEvent(
            AUTHORISATION,
            CASE_ID,
            startEventResponse,
            eventRequestData,
            caseDataMap,
            userDetails
        )).thenReturn(CaseDetails.builder().build());

        String result = citizenUserCaseUpdateService.updateCaseUsingCitizenUserAuthAndReturn(
            AUTHORISATION,
            CASE_ID,
            CITIZEN_CASE_UPDATE,
            startUpdateData -> {
                startUpdateData.caseDataMap().put("paymentReferenceNumber", "RC-reference");
                return "payment-response";
            }
        );

        assertEquals("payment-response", result);
        assertEquals("RC-reference", caseDataMap.get("paymentReferenceNumber"));
        verify(allTabService).getStartUpdateForSpecificUserEvent(
            CASE_ID,
            CITIZEN_CASE_UPDATE.getValue(),
            AUTHORISATION
        );
        verify(allTabService).submitUpdateForSpecificUserEvent(
            AUTHORISATION,
            CASE_ID,
            startEventResponse,
            eventRequestData,
            caseDataMap,
            userDetails
        );
    }
}
