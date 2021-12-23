package uk.gov.hmcts.reform.prl.controllers;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;
import static org.springframework.http.RequestEntity.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@PropertySource(value = "classpath:application.yaml")
@RunWith(SpringRunner.class)
public class PrePopulateFeeAndSolicitorNameControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private UserService userService;

    @MockBean
    private FeeService feesService;

    private UserDetails userDetails;

    private FeeResponse feeResponse;

    public static final String authToken = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dy"
        + "SDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzb2xpY2l0b3JAZXhhbXBsZS5jb20iLCJhdXRoX2xldmVsIjowLCJhdW"
        + "RpdFRyYWNraW5nSWQiOiI0NWExZWMyOS1jYTU3LTQ2OWUtODg3Zi1kZGVjMmYyZThjNWIiLCJpc3MiOiJodHRwOi8vZnItYW06ODA4MC9v"
        + "cGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudE"
        + "lkIjoiYWJkY2M0MDMtZDljMi00NWY5LWI2ZTYtNzVmMDVmNTU3NjY1IiwiYXVkIjoieHVpX3dlYmFwcCIsIm5iZiI6MTYzOTQwMjY2Nywi"
        + "Z3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2"
        + "VyIiwibWFuYWdlLXVzZXIiXSwiYXV0aF90aW1lIjoxNjM5NDAyNjY2MDAwLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTYzOTQzMTQ2Nywi"
        + "aWF0IjoxNjM5NDAyNjY3LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiMmRmYWI5ODgtNTY5Zi00MTUwLThiNWQtNTNjOGU5NDZmNWM2In"
        + "0.DNroM7YxJXiF1GgR2d5T0vag7Iac4eh7xdSAURXnNfnjS573uFD2ghUYtFg-fBRiYX5VbcAwo1Lhui_re_6CdSJDUljWH8ftfbbMYWf0"
        + "jHnV_x6qfoobNTe2cm1mebQReJCw6wjlRTyGTIz8ctkls5oe7GKPPO6j5zR7qJmCDh22jbHqzBEwEq1qBrjUwUX4mET1eOGczOI8VAGAqu"
        + "9kJXcTdKv6s2FER40Auc0Av70WalPQZd8J56q2W56SdvWmOpIBqM1enGMLIhYivl8xjqJH99B6gNTHdD9ll1IYivAo-yAtqg4inKqjraTC"
        + "mBvd02ULSxYkLkYHJQ2VD3pB6A";

    @Before
    public void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        userDetails = UserDetails.builder()
            .forename("solicitor@example.com")
            .surname("Solicitor")
            .build();

        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(232.00))
            .build();
    }

    @Test
    public void shouldRetrieveSolicitorNameAndFeeWhenValidRequestAndReturn200() throws Exception {

        when(userDetails.getFullName()).thenReturn("solicitor@example.com Solicitor");
        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);
        MvcResult mvcResult = (MvcResult) mockMvc.perform(MockMvcRequestBuilders.post("/prePopulateSolicitorAndFees")
                                                 .header(HttpHeaders.AUTHORIZATION, authToken)
                                                 .accept(MediaType.APPLICATION_JSON)
                                                 .content("{ \"CaseDetails\": { \"caseId\": \"1639090820727541\",\"state\": "
                                                              + "\"AWAITING_SUBMISSION_TO_HMCTS\",\"CaseData\": { \"id\": null}}}"))
            .andExpect(status().isOk());

    }

    @Test
    public void shouldReturn400WhenInvalidRequestBody() throws Exception {

        String authToken = "";

        mockMvc.perform((RequestBuilder) post("/prePopulateSolicitorAndFees")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header(HttpHeaders.AUTHORIZATION,  authToken)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnWhenInValidRequestAndReturn404() throws Exception {

        String authToken = "";
        mockMvc.perform((RequestBuilder) post("/getSolicitorAndFeeDetails1")
                            .contentType(MediaType.APPLICATION_CBOR)
                            .header(HttpHeaders.AUTHORIZATION,  authToken));

    }

}
