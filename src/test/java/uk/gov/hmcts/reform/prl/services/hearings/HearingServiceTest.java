package uk.gov.hmcts.reform.prl.services.hearings;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.clients.HearingApiClient;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import java.util.ArrayList;
import java.util.List;

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

        caseReferenceNumber = "1234567890";

        final List<CaseHearing> caseHearings = new ArrayList();

        hearings = new Hearings();
        hearings.setCaseRef("caseReference");
        hearings.setHmctsServiceCode("Authorization");
        hearings.setCaseHearings(caseHearings);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestSuccess() {

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }

    @Test
    @DisplayName("test case for HearingService.")
    public void getHearingsTestException() {
        hearingApiClient = null;

        Hearings response =
            hearingService.getHearings(authToken, caseReferenceNumber);

        Assert.assertEquals(null, response);
    }


}
