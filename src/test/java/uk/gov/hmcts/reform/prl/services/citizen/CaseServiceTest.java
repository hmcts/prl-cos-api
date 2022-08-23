package uk.gov.hmcts.reform.prl.services.citizen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN_PRL_CREATE_EVENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@ExtendWith(MockitoExtension.class)
public class CaseServiceTest {

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "Bearer TestS2sToken";
    public static final String USER_ID = "IDAM_ID";

    @InjectMocks
    private CaseService caseService;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    IdamClient idamClient;

    @Test
    public void shouldCreateCase() {
        //Given
        CaseData caseData = mock(CaseData.class);
        CaseDetails caseDetails = mock(CaseDetails.class);
        StartEventResponse startEventResponse = StartEventResponse
                .builder().eventId("EVENT_ID").caseDetails(caseDetails).token("TOKEN").build();
        Event event = Event.builder().id(CITIZEN_PRL_CREATE_EVENT).build();
        CaseDataContent caseDataContent = CaseDataContent.builder()
                .data(caseData)
                .event(event)
                .eventToken("TOKEN")
                .build();
        UserDetails userDetails = UserDetails.builder().id(USER_ID).build();
        Mockito.when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);
        Mockito.when(coreCaseDataApi.startForCitizen(authToken,
                s2sToken, USER_ID, JURISDICTION, CASE_TYPE, CITIZEN_PRL_CREATE_EVENT)).thenReturn(startEventResponse);
        Mockito.when(coreCaseDataApi.submitForCitizen(authToken, s2sToken, USER_ID,
                JURISDICTION, CASE_TYPE, true, caseDataContent)).thenReturn(caseDetails);

        //When
        CaseDetails actualCaseDetails =  caseService.createCase(caseData, authToken, s2sToken);

        //Then
        assertThat(actualCaseDetails).isEqualTo(caseDetails);
    }
}
