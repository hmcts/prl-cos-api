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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
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

    @Test
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
        assertNotNull(when(feeService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenThrow(FeignException.class));

    }

    @Test
    public void testGetFeesDataForAdditionalApplications() throws Exception {

        List<FeeType> applicationsFeeTypes = List.of(FeeType.C2_WITH_NOTICE, FeeType.CHILD_ARRANGEMENTS_ORDER);



        FeesConfig.FeeParameters feeParameters = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("GAOnNotice")
            .build();

        FeesConfig.FeeParameters feeParameters1 = FeesConfig.FeeParameters
            .builder()
            .channel("default")
            .event("miscellaneous")
            .service("private law")
            .jurisdiction1("family")
            .jurisdiction2("family court")
            .keyword("ParentalResponsibility")
            .build();

        when(feesConfig.getFeeParametersByFeeType(FeeType.C2_WITH_NOTICE)).thenReturn(feeParameters);
        when(feesConfig.getFeeParametersByFeeType(FeeType.CHILD_ARRANGEMENTS_ORDER)).thenReturn(feeParameters1);

        FeeResponse feeResponse1 = FeeResponse.builder()
            .code("FEE0324")
            .amount(BigDecimal.valueOf(167.00))
            .build();
        when(feeService.fetchFeeDetails(FeeType.C2_WITH_NOTICE)).thenReturn(feeResponse1);
        when(feeService.fetchFeeDetails(FeeType.CHILD_ARRANGEMENTS_ORDER)).thenReturn(feeResponse);
        FeeResponse response = feeService.getFeesDataForAdditionalApplications(applicationsFeeTypes);

        assertEquals(feeResponse, response);


    }
}
