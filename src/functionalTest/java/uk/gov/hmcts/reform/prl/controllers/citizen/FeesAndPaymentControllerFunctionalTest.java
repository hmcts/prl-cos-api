package uk.gov.hmcts.reform.prl.controllers.citizen;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
@Ignore
public class FeesAndPaymentControllerFunctionalTest {

    private MockMvc mockMvc;
    @Autowired
    private WebApplicationContext webApplicationContext;
    @Mock
    protected AuthorisationService authorisationService;
    @MockBean
    private FeeService feeService;
    @MockBean
    private PaymentRequestService paymentRequestService;

    private static final String CREATE_PAYMENT_INPUT = "requests/create-payment-input.json";


    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    /*
    These test cases will be enabled once we have merged and integrated with Fee and Pay on Demo environment.
     */
    @Ignore
    @Test
    public void givenRequestBody_whenGetC100ApplicationFees_then200Response() throws Exception {
        mockMvc.perform(get("/fees-and-payment-apis/getC100ApplicationFees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .header("ServiceAuthorization", "auth")
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();
    }
}
