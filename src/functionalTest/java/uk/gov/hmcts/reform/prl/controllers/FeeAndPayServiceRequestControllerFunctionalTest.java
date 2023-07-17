package uk.gov.hmcts.reform.prl.controllers;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.payment.PaymentServiceResponse;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;


/*
   These test cases will be enabled once we have merged and integrated with Fee and Pay on Demo environment.
*/
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
@Ignore
public class FeeAndPayServiceRequestControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    PaymentRequestService paymentRequestService;

    private static final String CREATE_SERVICE_REQUEST = "requests/call-back-controller-about-to-submit-case-creation.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenC100Case_whenPostRequestToCreateServiceRequest_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_SERVICE_REQUEST);

        PaymentServiceResponse paymentServiceResponse = PaymentServiceResponse.builder().serviceRequestReference("C100Test")
            .build();

        when(paymentRequestService.createServiceRequest(any(CallbackRequest.class), any(String.class), any(FeeResponse.class)))
            .thenReturn(paymentServiceResponse);


        mockMvc.perform(post("/create-payment-service-request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.paymentServiceRequestReferenceNumber").value("C100Test"))
            .andReturn();

    }
}
