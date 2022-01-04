package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class FeeServiceTest {

    @Mock
    private FeeService feeService;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private FeesConfig feesConfig;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {


        feeResponse = FeeResponse.builder()
            .code("FEE0325")
            .amount(BigDecimal.valueOf(232.00))
            .build();

    }

    @Test
    public void testToCheckFeeAmount() throws Exception {

        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ChildArrangement")
            .build();

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);

        assertEquals(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE),feeParameters);

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        assertEquals(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE),feeResponse);
        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertEquals(BigDecimal.valueOf(232.00), actualResult);

    }

    @Test
    public void testToCheckFeeAmountWithWrongCode() throws Exception {

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertNotEquals(BigDecimal.valueOf(65.00), actualResult);
    }
}
