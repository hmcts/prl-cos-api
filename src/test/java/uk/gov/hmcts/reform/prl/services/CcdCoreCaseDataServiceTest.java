package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.prl.clients.ccd.CcdCoreCaseDataService;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdCoreCaseDataServiceTest {

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
    private final Long caseId = 1234567887654321L;
    private final String eventName = "system-update";
    private final String userToken = "Bearer testToken";
    private final String serviceAuthToken = "Bearer testServiceAuth";
    private final String systemUserId = "systemUserID";
    private final String eventToken = "eventToken";
    private CaseDataContent caseDataContent;
    private StartEventResponse startEventResponse;

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    CcdCoreCaseDataService coreCaseDataService;

    @Before
    public void setUp() {
        startEventResponse = buildStartEventResponse(eventName, eventToken);
        Map<String, Object> eventData = Map.of("A", "B");
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
        when(coreCaseDataApi.startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), eventName))
            .thenReturn(buildStartEventResponse(eventName, eventToken));
        caseDataContent = buildCaseDataContent(eventName, eventToken, eventData);
    }

    @Test
    public void shouldStartEventWithEventData() {
        coreCaseDataService.startUpdate(userToken, EventRequestData
            .builder()
                .userId(systemUserId)
                .jurisdictionId(jurisdiction)
                .caseTypeId(caseType)
                .eventId(eventName)
            .build(), caseId.toString(), true);

        verify(coreCaseDataApi).startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, Long.toString(caseId), eventName);
    }

    @Test
    public void shouldStartEventForCitizen() {
        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.startUpdate(userToken, EventRequestData
            .builder()
            .userId(systemUserId)
            .jurisdictionId(jurisdiction)
            .caseTypeId(caseType)
            .eventId(eventName)
            .build(), caseId.toString(), false);

        verify(coreCaseDataApi).startEventForCitizen(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, Long.toString(caseId), eventName);
    }

    @Test
    public void shouldSubmitEventForCaseWorker() {
        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.submitUpdate(userToken,
                                         EventRequestData
            .builder()
            .userId(systemUserId)
            .jurisdictionId(jurisdiction)
            .caseTypeId(caseType)
            .eventId(eventName)
            .build(), caseDataContent, caseId.toString(), true);

        verify(coreCaseDataApi).submitEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), false, caseDataContent);
    }

    @Test
    public void shouldSubmitEventForCitizen() {
        Map<String, Object> eventData = Map.of("A", "B");
        coreCaseDataService.submitUpdate(userToken,
                                         EventRequestData
                                             .builder()
                                             .userId(systemUserId)
                                             .jurisdictionId(jurisdiction)
                                             .caseTypeId(caseType)
                                             .eventId(eventName)
                                             .build(),
                                         buildCaseDataContent(eventName, eventToken, eventData),
                                         caseId.toString(), false);

        verify(coreCaseDataApi).submitEventForCitizen(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                     caseType, Long.toString(caseId), false, caseDataContent);
    }

    @Test
    public void testEventRequest() {
        EventRequestData eventRequestData = coreCaseDataService.eventRequest(CaseEvent.DELETE_APPLICATION, systemUserId);
        assertEquals(eventRequestData.getUserId(), systemUserId);
    }

    @Test
    public void testCreateCaseDAtaContent() {
        CaseDataContent caseDataContentReturned = coreCaseDataService.createCaseDataContent(startEventResponse, "");
        assertEquals(caseDataContentReturned.getEventToken(), eventToken);
    }

    @Test
    public void testStartSubmitCreateForCaseWorker() {
        coreCaseDataService.startSubmitCreate(userToken,
                                              serviceAuthToken,
                                              EventRequestData
            .builder()
            .userId(systemUserId)
            .jurisdictionId(jurisdiction)
            .caseTypeId(caseType)
            .eventId(eventName)
            .build(), true);

        verify(coreCaseDataApi).startForCaseworker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, eventName);
    }

    @Test
    public void testStartSubmitCreateForCitizen() {
        coreCaseDataService.startSubmitCreate(userToken,
                                              serviceAuthToken,
                                              EventRequestData
                                                  .builder()
                                                  .userId(systemUserId)
                                                  .jurisdictionId(jurisdiction)
                                                  .caseTypeId(caseType)
                                                  .eventId(eventName)
                                                  .build(), false);

        verify(coreCaseDataApi).startForCitizen(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                   caseType, eventName);
    }

    @Test
    public void shouldSubmitCreateEventForCaseWorker() {
        coreCaseDataService.submitCreate(userToken,
                                         serviceAuthToken,
                                         systemUserId,
                                         caseDataContent, true);

        verify(coreCaseDataApi).submitForCaseworker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, true, caseDataContent);
    }

    @Test
    public void shouldSubmitStartEventForCitizen() {
        coreCaseDataService.submitCreate(userToken,
                                         serviceAuthToken,
                                         systemUserId,
                                         caseDataContent, false);

        verify(coreCaseDataApi).submitForCitizen(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                    caseType, false, caseDataContent);
    }

    @Test
    public void findCaseById() {
        coreCaseDataService.findCaseById(serviceAuthToken, String.valueOf(caseId));
        verify(coreCaseDataApi).getCase(serviceAuthToken, serviceAuthToken, String.valueOf(caseId));
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

