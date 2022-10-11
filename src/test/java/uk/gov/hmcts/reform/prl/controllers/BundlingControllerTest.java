package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.HashMap;
import java.util.Map;

@RunWith(MockitoJUnitRunner.Silent.class)
public class BundlingControllerTest {

    @InjectMocks
    private BundlingController bundlingController;
    private CaseDetails caseDetails;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {
        Map<String,Object> caseData = new HashMap<>();
        caseDetails = CaseDetails.builder().data(caseData).build();
    }

    @Test
    public void testCreateBundle() throws Exception {
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        bundlingController.createBundle("auth",callbackRequest);
    }
}
