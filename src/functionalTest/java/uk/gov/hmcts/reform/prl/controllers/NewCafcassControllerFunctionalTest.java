package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = { Application.class })
public class NewCafcassControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CaseDataService caseDataService;


    private static final String CREATE_SERVICE_REQUEST = "classpath:requests/cafcasssearchrequest.json";
    private static final String CREATE_SERVICE_RESPONSE = "classpath:requests/cafcasssearchresponse.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenC100Case_whenPostRequestToCreateServiceRequest_then200ResponseAndNoErrors() throws Exception {
        String requestBody = TestResourceUtil.readFileFrom(CREATE_SERVICE_REQUEST);
        String cafcassResponseStr = TestResourceUtil.readFileFrom(CREATE_SERVICE_RESPONSE);

        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();

        CafCassResponse cafCassResponse = objectMapper.readValue(cafcassResponseStr, CafCassResponse.class);

        when(caseDataService.getCaseData(any(String.class), any(String.class), eq("2022-08-22T10:44:43.49"), eq("2022-08-26T11:00:54.055")))
            .thenReturn(cafCassResponse);

        mockMvc.perform(get("/searchCases")
                            .contentType(MediaType.APPLICATION_JSON)
                        .header("authorization", "auth")
                        .header("serviceAuthorization", "serviceauth")
                            .content(requestBody)
                            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

    }
}
