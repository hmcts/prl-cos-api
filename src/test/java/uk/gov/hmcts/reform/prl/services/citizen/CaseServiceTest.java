package uk.gov.hmcts.reform.prl.services.citizen;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseDetailsConverter;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    @InjectMocks
    CaseService caseService;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    CaseDetailsConverter caseDetailsConverter;

    @Mock
    CaseAccessApi caseAccessApi;

    @Mock
    IdamClient idamClient;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    SystemUserService systemUserService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private UserDetails userDetails;

    private CaseData caseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String servAuthToken = "Bearer TestServToken";

    @Before
    public void setUp() {

        userDetails = UserDetails.builder()
            .surname("Solicitor")
            .forename("solicitor@example.com")
            .id("123")
            .email("test@demo.com")
            .build();

        caseData = CaseData.builder()
            .id(1234567891234567L)
            .applicantCaseName("test")
            .courtName("test")
            .applicantName("testappl")
            .build();
    }

    @Ignore
    @Test
    public void testUpdateCase()  {

        String caseId = "1234567891234567";
        String eventId = "e3ceb507-0137-43a9-8bd3-85dd23720648";

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken("eventToken")
            .data(Map.of("applicantCaseName", "test"))
            .build();

        when(idamClient.getUserDetails(authToken)).thenReturn(userDetails);

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(coreCaseDataApi.startEventForCaseWorker(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(),
                                                     Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(StartEventResponse.builder().token("eventToken").build());;

        when(idamClient.getUserInfo(Mockito.any())).thenReturn(UserInfo.builder().uid(eventId).build());

        when(coreCaseDataApi.submitEventForCaseWorker(
                 authToken,
                 servAuthToken,
                 eventId,
                 PrlAppsConstants.JURISDICTION,
                 PrlAppsConstants.CASE_TYPE,
                 "1234567891234567",
                 true,
                 caseDataContent
             )).thenReturn(CaseDetails.builder().id(1234567891234567L)
                                       .data(Map.of("applicantCaseName", "test"))

            .build());;

        CaseDetails caseDetails = caseService.updateCase(caseData, authToken, servAuthToken, caseId, eventId);

        assertEquals("1234567891234567", caseDetails.getId());

        verify(coreCaseDataApi, times(1)).startEventForCaseWorker(
            "Bearer abc",
            "s2s token",
            "e3ceb507-0137-43a9-8bd3-85dd23720648",
            "PRIVATELAW",
            "PRLAPPS",
            "1234567891234567",
            "courtnav-document-upload"
        );

        verify(coreCaseDataApi, times(1)).submitEventForCaseWorker(
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.any(),
            Mockito.anyBoolean(),
            Mockito.any(CaseDataContent.class)
        );

    }


}
