package uk.gov.hmcts.reform.prl.controllers.citizen;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

public class FeesAndPaymentCitizenControllerTest {

    @InjectMocks
    private FeesAndPaymentCitizenController feesAndPaymentCitizenController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private FeeService feeService;

    @Mock
    private FeeResponse feeResponse;

    private FeeResponseForCitizen feeResponseForCitizen;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "TestS2sToken";

    @Before
    public void setUp() {

        MockitoAnnotations.openMocks(this);

        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(232.00))
            .build();


    }

    @Test
    public void fetchFeeDetailsSuccessfully() throws Exception {
        feeResponseForCitizen = FeeResponseForCitizen.builder()
            .amount(feeResponse.getAmount().toString());

        when(authorisationService.authorise(s2sToken)).thenReturn(Boolean.TRUE);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        feesAndPaymentCitizenController.fetchFeesAmount(authToken, s2sToken);
        Assert.assertEquals(feeResponseForCitizen.getAmount(), feeResponse.getAmount());
    }

}
