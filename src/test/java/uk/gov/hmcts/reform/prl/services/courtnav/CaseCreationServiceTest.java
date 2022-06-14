package uk.gov.hmcts.reform.prl.services.courtnav;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CaseCreationServiceTest {

    private final String authToken = "Bearer abc";
    private final String s2sToken = "s2s token";
    private final String randomUserId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamClient idamClient;

    @InjectMocks
    CaseCreationService caseCreationService;


    private CaseData caseData;

    @Before
    public void setup() {
        caseData = CaseData.builder().id(1234567891234567L).applicantCaseName("xyz").build();
        when(idamClient.getUserInfo(any())).thenReturn(UserInfo.builder().uid(randomUserId).build());
        when(coreCaseDataApi.startForCaseworker(any(), any(), any(), any(), any(), any())
        ).thenReturn(StartEventResponse.builder().eventId("courtnav-case-creation").token("eventToken").build());
    }

    @Test
    public void shouldStartAndSubmitEventWithEventData() {
        caseCreationService.createCourtNavCase("Bearer abc", caseData, "s2s token");
        verify(coreCaseDataApi).startForCaseworker(authToken, s2sToken,
                                                   randomUserId, PrlAppsConstants.JURISDICTION,
                                                   PrlAppsConstants.CASE_TYPE, "courtnav-case-creation"
        );

        verify(coreCaseDataApi).submitForCaseworker(authToken, s2sToken, randomUserId, PrlAppsConstants.JURISDICTION,
                                                    PrlAppsConstants.CASE_TYPE,
                                                    true,
                                                    CaseDataContent.builder()
                                                        .eventToken("eventToken")
                                                        .event(Event.builder()
                                                                   .id("courtnav-case-creation")
                                                                   .build())
                                                        .data(caseData)
                                                        .build()
        );

    }
}
