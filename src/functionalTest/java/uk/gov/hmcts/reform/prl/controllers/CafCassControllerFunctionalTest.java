package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafCassControllerFunctionalTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CaseDataService caseDataService;

    private static final String CREATE_SERVICE_RESPONSE = "classpath:response/cafcass-search-response.json";

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void givenDatetimeWindow_whenGetRequestToSearchCasesByCafCassController_then200Response() throws Exception {
        String cafcassResponseStr = TestResourceUtil.readFileFrom(CREATE_SERVICE_RESPONSE);
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();

        CafCassResponse expectedCafcassResponse = objectMapper.readValue(cafcassResponseStr, CafCassResponse.class);

        Mockito.when(caseDataService.getCaseData(anyString(), anyString(), anyString(), anyString())).thenReturn(expectedCafcassResponse);

        MvcResult mvcResult = mockMvc.perform(get("/searchCases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("authorisation", "authorisation")
                        .header("serviceAuthorisation", "serviceauthorisation")
                        .queryParam("start_date", "2022-08-22T10:44:43.49")
                        .queryParam("end_date", "2022-08-26T11:00:54.055")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

        CafCassResponse actualCafcassResponse = objectMapper.readValue(contentAsString, CafCassResponse.class);

        assertEquals(expectedCafcassResponse.getTotal(), actualCafcassResponse.getTotal());
        assertEquals(expectedCafcassResponse.getCases().size(), actualCafcassResponse.getCases().size());
        assertEquals(expectedCafcassResponse.getTotal(), actualCafcassResponse.getCases().size());

        if (0 != expectedCafcassResponse.getTotal() && 0 != actualCafcassResponse.getTotal()) {
            assertNotNull(actualCafcassResponse.getCases().get(0).getState());
            assertNotNull(actualCafcassResponse.getCases().get(0).getCaseTypeId());
            assertNotNull(actualCafcassResponse.getCases().get(0).getCaseTypeOfApplication());

            assertEquals(actualCafcassResponse.getCases().get(0).getCaseTypeId(), expectedCafcassResponse.getCases().get(0).getCaseTypeId());
            assertEquals(CASE_TYPE, actualCafcassResponse.getCases().get(0).getCaseTypeId());
            assertEquals(C100_CASE_TYPE, actualCafcassResponse.getCases().get(0).getCaseTypeOfApplication());
        }
    }
}
