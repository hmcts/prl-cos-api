package uk.gov.hmcts.reform.prl.controllers.citizen;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.CreatePaymentRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.FeeResponseForCitizen;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentResponse;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentStatusResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class FeesAndPaymentCitizenControllerIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    AuthorisationService authorisationService;

    @MockBean
    FeeService feeService;

    @MockBean
    PaymentRequestService paymentRequestService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testGetC100ApplicationFees() throws Exception {
        String url = "/fees-and-payment-apis/getC100ApplicationFees";

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(feeService.fetchFeeDetails(any())).thenReturn(FeeResponse.builder()
                                                               .amount(BigDecimal.ONE)
                                                               .build());

        mockMvc.perform(
                get(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testCreatePaymentRequest() throws Exception {
        String url = "/fees-and-payment-apis/create-payment";
        CreatePaymentRequest createPaymentRequest = new CreatePaymentRequest();

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(paymentRequestService.createPayment(anyString(), any(CreatePaymentRequest.class)))
            .thenReturn(new PaymentResponse());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .content(ResourceLoader.objectToJson(createPaymentRequest))
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testRetrievePaymentStatus() throws Exception {
        String url = "/fees-and-payment-apis/retrievePaymentStatus/testPaymentReference/testCaseId";

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(paymentRequestService.fetchPaymentStatus(anyString(), anyString()))
            .thenReturn(new PaymentStatusResponse());

        mockMvc.perform(
                get(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testGetFeeCode() throws Exception {
        String url = "/fees-and-payment-apis/getFeeCode";
        FeeRequest feeRequest = new FeeRequest();

        when(authorisationService.authoriseUser(anyString())).thenReturn(true);
        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(feeService.fetchFeeCode(any(FeeRequest.class), anyString()))
            .thenReturn(FeeResponseForCitizen.builder()
                        .amount("100")
                        .feeType("C100")
                        .errorRetrievingResponse("No error")
                        .build());

        mockMvc.perform(
                post(url)
                    .header(HttpHeaders.AUTHORIZATION, "testAuthToken")
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .content(ResourceLoader.objectToJson(feeRequest))
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testFetchFee() throws Exception {
        String url = "/fees-and-payment-apis/getFee/C100_SUBMISSION_FEE";

        when(authorisationService.authoriseService(anyString())).thenReturn(true);
        when(feeService.fetchFee(anyString()))
            .thenReturn(FeeResponseForCitizen.builder()
                            .amount("255.00")
                            .feeType("C100_SUBMISSION_FEE")
                            .build());

        mockMvc.perform(
                get(url)
                    .header(PrlAppsConstants.SERVICE_AUTHORIZATION_HEADER, "testServiceAuthToken")
                    .accept(APPLICATION_JSON)
                    .contentType(APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }
}
