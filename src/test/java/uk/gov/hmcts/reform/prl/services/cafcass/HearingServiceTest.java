package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
//import org.junit.Test;
//import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authToken = "Authorization";
        userToken = "ServiceAuthorization";
        caseReferenceNumber = "caseReference";

        List<CaseHearing> caseHearings = new ArrayList();
        //CaseHearing mock1 = Mockito.mock(CaseHearing.class);
        //caseHearings.add(mock1);
        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setHmctsServiceCode("Authorization");
        hearings.setCaseHearings(caseHearings);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestSuccess(){
//        authToken = "Authorization";
//        userToken = "ServiceAuthorization";
//        caseReferenceNumber = "caseReference";
//
//        List<CaseHearing> caseHearings = new ArrayList();
//        CaseHearing mock1 = Mockito.mock(CaseHearing.class);
//        caseHearings.add(mock1);
//        hearings = new Hearings();
//        hearings.setCaseRef("caseReference");
//        hearings.setHmctsServiceCode("Authorization");
//        hearings.setCaseHearings(caseHearings);
        when(authTokenGenerator.generate()).thenReturn(userToken);
        //when(hearingApiClient.getHearingDetails(anyString(), anyString(), anyString())).thenReturn(hearings);
        when(hearingApiClient.getHearingDetails(userToken, authTokenGenerator.generate(), caseReferenceNumber)).thenReturn(hearings);


        Hearings response =
            hearingService.getHearings(authToken, userToken, caseReferenceNumber);

        Assert.assertEquals(hearings, response);
    }

//    @Test
//    public void shouldThrowNullPointerException() throws Exception {
//        //from PaymentRequestServiceTest
//        hearings = Hearings.hearingsWith().build();
//        assertThrows(NullPointerException.class, () -> {
//            Hearings hearings = hearingService.getHearings(authToken, "", caseReferenceNumber);
//        });
//    }

    @Test(expected = NullPointerException.class)
    public void senLetterServiceWithInValidInput() {
        assertEquals(hearingService.getHearings(null, null, null), hearings);

    }

}
