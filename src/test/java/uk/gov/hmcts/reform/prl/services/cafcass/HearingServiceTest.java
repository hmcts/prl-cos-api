package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.Cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.Hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.Hearing.Hearings;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private String authToken;
    private String userToken;

    private String caseReferenceNumber;

    @Mock
    HearingApiClient hearingApiClient;

    Hearings hearings = null;

    @InjectMocks
    private HearingService hearingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authToken = "Authorization";
        userToken = "ServiceAuthorization";
        caseReferenceNumber = "caseReference";

        List<CaseHearing> caseHearings = null;
        CaseHearing mock1 = Mockito.mock(CaseHearing.class);
        caseHearings.add(mock1);
        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setHmctsServiceCode("Authorization");
        hearings.setCaseHearings(caseHearings);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestSuccess(){
        when(authTokenGenerator.generate()).thenReturn(userToken);
        when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString())).thenReturn(hearings);

        //when the test runs it says there is no instance of the following vars authToken, userToken, caseReferenceNumber
        //I think it's not able to read the setUp method
        //hearings is also null
        Hearings response =
            hearingService.getHearings(authToken, userToken, caseReferenceNumber);

        Assert.assertEquals(hearings, response);
    }

}
