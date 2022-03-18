package uk.gov.hmcts.reform.prl.services;

import feign.FeignException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.clients.FeesRegisterApi;
import uk.gov.hmcts.reform.prl.config.FeesConfig;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeeServiceTest {

    @InjectMocks
    private FeeService feeService;

    @Mock
    private FeesRegisterApi feesRegisterApi;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private FeesConfig feesConfig;

    @Mock
    private FeesConfig.FeeParameters feeParameters;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
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
            .keyword("ChildArrangements")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        assertEquals(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE),feeParameters);

        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        assertEquals(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE),feeResponse);
        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertEquals(BigDecimal.valueOf(232.00), actualResult);

    }

    @Test
    public void testToCheckFeeAmountWithWrongCode() throws Exception {

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        BigDecimal actualResult = feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE).getAmount();

        assertNotEquals(BigDecimal.valueOf(65.00), actualResult);
    }

    @Test(expected = FeignException.class)
    public void whenFeeDetailsNotFetchedThrowError() throws Exception {

        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ChildArrangements")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeParameters);
        when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenThrow(FeignException.class);

    }
}
