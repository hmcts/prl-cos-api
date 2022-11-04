package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.Cafcass.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.cafcass.Hearing.Hearings;

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

    HttpStatus ok = HttpStatus.OK;

    @InjectMocks
    private HearingService hearingService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authToken = "Authorization";
        userToken = "ServiceAuthorization";
        caseReferenceNumber = "caseReference";
        when(authTokenGenerator.generate()).thenReturn(userToken);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTest(){

        //final ResponseEntity<Resource> response = hearingService.getHearings(authToken,userToken,caseReferenceNumber);

        when(hearingService.getHearings(authToken,userToken,caseReferenceNumber)).thenReturn(new Hearings(ok));

    }

}
