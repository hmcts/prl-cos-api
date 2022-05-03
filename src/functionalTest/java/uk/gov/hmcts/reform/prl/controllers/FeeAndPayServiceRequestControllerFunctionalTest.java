package uk.gov.hmcts.reform.prl.controllers;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.ResourceLoader;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.services.PaymentRequestService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class FeeAndPayServiceRequestControllerFunctionalTest {

//    private final String userToken = "Bearer testToken";
//
//    private static final String VALID_REQUEST_BODY = "controller/valid-request-body.json";
//
//    private final String targetInstance =
//        StringUtils.defaultIfBlank(
//            System.getenv("TEST_URL"),
//            "http://localhost:4044"
//        );
//
//    private final RequestSpecification request = RestAssured.given().relaxedHTTPSValidation().baseUri(targetInstance);

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
    public void givenMiamAttendance_whenPostRequestToMiamValidatation_then200ResponseAndNoErrors() throws Exception {
        String requestBody = ResourceLoader.loadJson(CREATE_SERVICE_REQUEST);

        PaymentRequestService paymentRequestService = new PaymentRequestService();

        when(paymentRequestService.createServiceRequest(any(CallbackRequest.class), any(String.class))).thenReturn()



                                                        mockMvc.perform(post("/create-payment-service-request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Authorization", "auth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
//            .andExpect(jsonPath("errors").isEmpty())
            .andReturn();

    }

//    @Test
//    public void givenNoRequestBodyReturn400() throws Exception {
//        String requestBody = ResourceLoader.loadJson(VALID_REQUEST_BODY);
//        request
//            .header("Authorization", userToken)
//            .when()
//            .contentType("application/json")
//            .post("/create-payment-service-request")
//            .then()
//            .assertThat().statusCode(400);
//    }
}
