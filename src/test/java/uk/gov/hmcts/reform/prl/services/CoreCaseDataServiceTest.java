package uk.gov.hmcts.reform.prl.services;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;

import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CoreCaseDataServiceTest {

    private final String JURISDICTION = "PRIVATELAW";
    private final String CASE_TYPE = "C100";
    private final Long CASE_ID = 1234567887654321L;
    private final String EVENT_NAME = "system-update";
    private final String USERTOKEN = "Bearer testToken";
    private final String SERVICE_AUTH_TOKEN = "Bearer testServiceAuth";
    private final String SYSTEM_USER_ID = "systemUserID";
    private final String EVENT_TOKEN = "eventToken";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    CoreCaseDataService coreCaseDataService;

    @BeforeEach
    void setup() {
        when(authTokenGenerator.generate()).thenReturn(SERVICE_AUTH_TOKEN);
    }

    @BeforeEach
    void setUp() {
        when(systemUserService.getUserId(USERTOKEN)).thenReturn(SYSTEM_USER_ID);
        when(systemUserService.getSysUserToken()).thenReturn(USERTOKEN);

        when(coreCaseDataApi.startEventForCaseWorker(USERTOKEN, SERVICE_AUTH_TOKEN, SYSTEM_USER_ID, JURISDICTION,
                                                     CASE_TYPE, Long.toString(CASE_ID), EVENT_NAME))
            .thenReturn(buildStartEventResponse(EVENT_NAME, EVENT_TOKEN));
    }

    @Test
    void shouldStartAndSubmitEventWithEventData() {
        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.triggerEvent(JURISDICTION, CASE_TYPE, CASE_ID, EVENT_NAME, eventData);

        verify(coreCaseDataApi).startEventForCaseWorker(USERTOKEN, SERVICE_AUTH_TOKEN, SYSTEM_USER_ID,
                                                        JURISDICTION, CASE_TYPE, Long.toString(CASE_ID), EVENT_NAME);
        verify(coreCaseDataApi).submitEventForCaseWorker(USERTOKEN, SERVICE_AUTH_TOKEN, SYSTEM_USER_ID, JURISDICTION,
                                                         CASE_TYPE, Long.toString(CASE_ID), true,
                                                         buildCaseDataContent(EVENT_NAME, EVENT_TOKEN, eventData));
    }

    private CaseDataContent buildCaseDataContent(String eventId, String eventToken, Object eventData) {
        return CaseDataContent.builder()
            .eventToken(eventToken)
            .event(Event.builder()
                       .id(eventId)
                       .build())
            .data(eventData)
            .build();
    }

    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
    }

}

