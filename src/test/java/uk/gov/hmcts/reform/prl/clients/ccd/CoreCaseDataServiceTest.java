package uk.gov.hmcts.reform.prl.clients.ccd;

import org.junit.Assert;
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
import uk.gov.hmcts.reform.prl.enums.CaseEvent;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceTest {

    private final String jurisdiction = "PRIVATELAW";
    private final String caseType = "PRLAPPS";
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

    @InjectMocks
    CoreCaseDataService coreCaseDataService;

    @Before
    public void setUp() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);
    }

    @Test
    public void testStartUpdateWhenIsRepresentedIsTrue() {
        coreCaseDataService.startUpdate(userToken,
                                        buildEventRequestData(),caseId.toString(),true);


        verify(coreCaseDataApi).startEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, Long.toString(caseId), CaseEvent.LINK_CITIZEN.getValue());
    }

    @Test
    public void testStartUpdateWhenIsRepresentedIsFalse() {
        coreCaseDataService.startUpdate(userToken,
                                        buildEventRequestData(),caseId.toString(),false);


        verify(coreCaseDataApi).startEventForCitizen(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                        caseType, Long.toString(caseId), CaseEvent.LINK_CITIZEN.getValue());
    }

    @Test
    public void testSubmitUpdateWhenIsRepresentedIsTrue() {
        coreCaseDataService.submitUpdate(userToken,
                                        buildEventRequestData(),
                                         buildCaseDataContent(buildStartEventResponse(eventName, eventToken)),
                                         caseId.toString(),true);


        verify(coreCaseDataApi).submitEventForCaseWorker(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, Long.toString(caseId), true,
                                                         buildCaseDataContent(buildStartEventResponse(eventName, eventToken)));
    }

    @Test
    public void testSubmitUpdateWhenIsRepresentedIsFalse() {
        coreCaseDataService.submitUpdate(userToken,
                                         buildEventRequestData(),
                                         buildCaseDataContent(buildStartEventResponse(eventName, eventToken)),
                                         caseId.toString(),false);


        verify(coreCaseDataApi).submitEventForCitizen(userToken, serviceAuthToken, systemUserId, jurisdiction,
                                                         caseType, Long.toString(caseId), true,
                                                         buildCaseDataContent(buildStartEventResponse(eventName, eventToken)));
    }

    @Test
    public void testEventRequest() {
        Assert.assertEquals(buildEventRequestData(), coreCaseDataService.eventRequest(CaseEvent.LINK_CITIZEN,systemUserId));
    }

    @Test
    public void testCreateCaseDataContent() {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id","12345L");
        stringObjectMap.put("caseTypeOfApplication","C100");
        Assert.assertEquals(buildCaseDataContent(buildStartEventResponse(eventName, eventToken)),
                            coreCaseDataService.createCaseDataContent(buildStartEventResponse(eventName, eventToken),stringObjectMap));
    }


    @Test
    public void testStartSubmitCreateWhenIsRepresentedIsTrue() {
        coreCaseDataService.startSubmitCreate(serviceAuthToken,userToken,systemUserId,
                                         buildEventRequestData(),true);


        verify(coreCaseDataApi).startForCaseworker(serviceAuthToken,userToken,systemUserId,jurisdiction,
                                                   caseType,buildEventRequestData().getEventId());
    }

    @Test
    public void testStartSubmitCreateWhenIsRepresentedIsFalse() {
        coreCaseDataService.startSubmitCreate(serviceAuthToken,userToken,systemUserId,
                                              buildEventRequestData(),false);


        verify(coreCaseDataApi).startForCitizen(serviceAuthToken,userToken,systemUserId,jurisdiction,
                                                   caseType,buildEventRequestData().getEventId());
    }

    @Test
    public void testSubmitCreateWhenIsRepresentedIsTrue() {
        coreCaseDataService.submitCreate(serviceAuthToken,userToken,systemUserId,
                                              buildCaseDataContent(buildStartEventResponse(eventName, eventToken)),true);


        verify(coreCaseDataApi).submitForCaseworker(serviceAuthToken,userToken,systemUserId,jurisdiction,
                                                   caseType,true,buildCaseDataContent(buildStartEventResponse(eventName, eventToken)));
    }

    @Test
    public void testSubmitCreateWhenIsRepresentedIsFalse() {
        coreCaseDataService.submitCreate(serviceAuthToken,userToken,systemUserId,
                                         buildCaseDataContent(buildStartEventResponse(eventName, eventToken)),false);


        verify(coreCaseDataApi).submitForCitizen(serviceAuthToken,userToken,systemUserId,jurisdiction,
                                                    caseType,false,buildCaseDataContent(buildStartEventResponse(eventName, eventToken)));
    }


    private StartEventResponse buildStartEventResponse(String eventId, String eventToken) {
        return StartEventResponse.builder().eventId(eventId).token(eventToken).build();
    }

    private EventRequestData buildEventRequestData() {
        return EventRequestData.builder()
            .eventId(CaseEvent.LINK_CITIZEN.getValue())
            .caseTypeId(CASE_TYPE)
            .ignoreWarning(true)
            .jurisdictionId(JURISDICTION)
            .userId(systemUserId)
            .build();
    }

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse) {
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("id","12345L");
        stringObjectMap.put("caseTypeOfApplication","C100");
        return CaseDataContent.builder()
            .data(stringObjectMap)
            .eventToken(startEventResponse.getToken())
            .event(Event.builder()
                       .id(startEventResponse.getEventId())
                       .build())
            .build();
    }
}

