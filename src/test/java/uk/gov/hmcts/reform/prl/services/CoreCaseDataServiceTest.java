package uk.gov.hmcts.reform.prl.services;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "C100";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "system-update";
    private final String userToken = "Bearer testToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";

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
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
    }

    @BeforeEach
    void setUp() {
        when(systemUserService.getUserId(userToken)).thenReturn(systemUserId);
        when(systemUserService.getSysUserToken()).thenReturn(userToken);

        when(coreCaseDataApi.startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName))
            .thenReturn(buildStartEventResponse(eventName, eventToken));
    }

    @Test
    void shouldStartAndSubmitEventWithEventData() {
        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.triggerEvent(jurisdiction, caseType, caseId, eventName, eventData);

        verify(coreCaseDataApi).startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, Long.toString(caseId), eventName);
        verify(coreCaseDataApi).submitEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, Long.toString(caseId), true,
                                                         buildCaseDataContent(eventName, eventToken, eventData));
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

