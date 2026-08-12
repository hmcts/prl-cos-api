package uk.gov.hmcts.reform.prl.services.citizen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CitizenUserCaseUpdateServiceTest {

    private static final String AUTHORISATION = "Bearer test";
    private static final String CASE_ID = "123";

    @InjectMocks
    private CitizenUserCaseUpdateService citizenUserCaseUpdateService;

    @Mock
    private AllTabServiceImpl allTabService;

    @Test
    public void shouldStartAndSubmitCaseUpdateUsingCitizenUserAuth() {
        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseData.builder().build();
        EventRequestData eventRequestData = EventRequestData.builder().build();
        StartEventResponse startEventResponse = StartEventResponse.builder().build();
        UserDetails userDetails = UserDetails.builder().id("citizen-user-id").build();
        CaseDetails caseDetails = CaseDetails.builder().id(123L).data(caseDataMap).build();
        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            AUTHORISATION,
            eventRequestData,
            startEventResponse,
            caseDataMap,
            caseData,
            userDetails
        );

        when(allTabService.getStartUpdateForSpecificUserEvent(
            CASE_ID,
            CaseEvent.CITIZEN_CASE_UPDATE.getValue(),
            AUTHORISATION
        )).thenReturn(startAllTabsUpdateDataContent);
        when(allTabService.submitUpdateForSpecificUserEvent(
            AUTHORISATION,
            CASE_ID,
            startEventResponse,
            eventRequestData,
            caseDataMap,
            userDetails
        )).thenReturn(caseDetails);

        CaseDetails actual = citizenUserCaseUpdateService.updateCaseUsingCitizenUserAuth(
            AUTHORISATION,
            CASE_ID,
            CaseEvent.CITIZEN_CASE_UPDATE,
            startUpdateDataContent -> startUpdateDataContent.caseDataMap().put("testKey", "testValue")
        );

        assertSame(caseDetails, actual);
        assertEquals("testValue", caseDataMap.get("testKey"));
        verify(allTabService).getStartUpdateForSpecificUserEvent(
            CASE_ID,
            CaseEvent.CITIZEN_CASE_UPDATE.getValue(),
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
