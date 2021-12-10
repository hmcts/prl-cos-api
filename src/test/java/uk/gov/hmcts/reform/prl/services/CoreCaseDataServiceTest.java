package uk.gov.hmcts.reform.prl.services;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.prl.request.RequestData;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CoreCaseDataServiceTest {

    private final String JURISDICTION = "PRIVATELAW";
    private final String CASE_TYPE = "C100";
    private final Long CASE_ID = 1234567887654321L;
    private final String EVENT_NAME = "system-update";
    private final Map<String, Object> EVENT_DATA = new HashMap<>();
    private final String USERTOKEN = "testToken";
    private final String SERVICE_AUTH = "testServiceAuth";
    private final String SYSTEM_USER_ID = "systemUserID";

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private RequestData requestData;
    @Mock
    private SystemUserService systemUserService;

    @InjectMocks
    CoreCaseDataService coreCaseDataService;

    @Test
    public void triggerEventShouldCallCoreCaseDataApi() {

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            CASE_ID,
            EVENT_NAME,
            EVENT_DATA
        );

        verify(coreCaseDataApi).startEventForCaseWorker(
            eq(USERTOKEN),
            eq(SERVICE_AUTH),
            eq(SYSTEM_USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID.toString()),
            eq(EVENT_NAME)
        );


        CaseDataContent caseDataContent = CaseDataContent.builder().build();


        verify(coreCaseDataApi).submitEventForCaseWorker(
            eq(USERTOKEN),
            eq(SERVICE_AUTH),
            eq(SYSTEM_USER_ID),
            eq(JURISDICTION),
            eq(CASE_TYPE),
            eq(CASE_ID.toString()),
            eq(true),
            eq(caseDataContent)
        );

    }
}
