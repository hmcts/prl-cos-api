package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Assert;

import org.junit.Before;
import org.junit.Test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.Cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.Hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.Hearing.Hearings;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HearingServiceTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    private String authToken;
    private String s2sToken;

    private String caseReferenceNumber;

    @Mock
    HearingApiClient hearingApiClient;

    Hearings hearings = null;

    @InjectMocks
    private HearingService hearingService;

    @Before
    public void init() {
        MockitoAnnotations.openMocks(this);
        s2sToken = "s2sToken";
        authToken = "Authorization";
       // when(authTokenGenerator.generate()).thenReturn(s2sToken);

        caseReferenceNumber = "1234567890";

        List<CaseHearing> caseHearings = new ArrayList();

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setHmctsServiceCode("Authorization");
        hearings.setCaseHearings(caseHearings);
        //when(hearingApiClient.getHearingDetails(anyString(),anyString() ,anyString())).thenReturn(hearings);


    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestSuccess(){

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestException(){
        hearingApiClient = null;

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }




}
