package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.services.cafcass.PostcodeLookupService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.cafcass.CafcassAppConstants.ENGLAND_POSTCODE_NATIONALCODE;

@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafCassControllerIntegrationTest {
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private PostcodeLookupService postcodeLookupService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    private static final String CREATE_SERVICE_RESPONSE = "classpath:response/cafcass-search-response.json";

    @Test
    public void givenValidDatetimeRangeSearchCasesByCafCassControllerReturnOkStatus() throws Exception {
        String cafcassResponseStr = TestResourceUtil.readFileFrom(CREATE_SERVICE_RESPONSE);
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();

        SearchResult searchResult = objectMapper.readValue(cafcassResponseStr, SearchResult.class);

        Mockito.when(postcodeLookupService.isValidNationalPostCode(anyString(), eq(ENGLAND_POSTCODE_NATIONALCODE))).thenReturn(true);
        Mockito.when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString())).thenReturn(searchResult);

        mockMvc.perform(
                        get("/searchCases")
                                .contentType(APPLICATION_JSON)
                                .header("authorisation", "Bearer Auth")
                                .header("serviceauthorisation", "serviceauth")
                                .queryParam("start_date", "2022-08-22T10:39:43.49")
                                .queryParam("end_date", "2022-08-26T10:44:54.055")
                                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
    }
}