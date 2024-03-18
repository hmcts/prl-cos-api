/*
package uk.gov.hmcts.reform.prl.controllers.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Ignore;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.cafcass.HearingService;
import uk.gov.hmcts.reform.prl.services.cafcass.RefDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_END_DATE_PARAM;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_END_DATE_PARAM_VALUE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_START_DATE_PARAM;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CAFCASS_START_DATE_PARAM_VALUE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.CREATE_SERVICE_RESPONSE;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.SEARCH_CASE_ENDPOINT;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.SERVICE_AUTHORISATION_HEADER;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTH_TOKEN;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
@ContextConfiguration
public class CafCassControllerFunctionalTest {

    private static final String USER_TOKEN = "Bearer testToken";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private CoreCaseDataApi coreCaseDataApi;

    @MockBean
    private AuthorisationService authorisationService;

    @MockBean
    private AuthTokenGenerator authTokenGenerator;

    @MockBean
    SystemUserService systemUserService;

    @MockBean
    private HearingService hearingService;

    @MockBean
    private RefDataService refDataService;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Ignore
    @Test
    public void givenDatetimeWindowWhenGetRequestToSearchCasesByCafCassControllerThen200Response() throws Exception {
        String cafcassResponseStr = TestResourceUtil.readFileFrom(CREATE_SERVICE_RESPONSE);
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        Map<String, String> refDataMap = new HashMap<>();
        refDataMap.put("ABA5-APL","Appeal");

        SearchResult expectedSearchResult = objectMapper.readValue(cafcassResponseStr, SearchResult.class);
        Mockito.when(authorisationService.authoriseService(any())).thenReturn(true);
        Mockito.when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_AUTH_TOKEN);
        Mockito.when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(systemUserService.getSysUserToken()).thenReturn(USER_TOKEN);
        Mockito.when(hearingService.getHearings(
            anyString(),
            anyString()
        )).thenReturn(Hearings.hearingsWith().hmctsServiceCode("ABA5")
                          .caseHearings(List.of(
                              CaseHearing.caseHearingWith().hearingType("ABA5-APL").build()))
            .build());
        Mockito.when(refDataService.getRefDataCategoryValueMap("authorisation", authTokenGenerator.generate(), "ABA5", "HearingType")).thenReturn(
            refDataMap);
        Mockito.when(coreCaseDataApi.searchCases(anyString(), anyString(), anyString(), anyString())).thenReturn(expectedSearchResult);


        MvcResult mvcResult = mockMvc.perform(get(SEARCH_CASE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header(AUTHORISATION_HEADER, TEST_AUTH_TOKEN)
                        .header(SERVICE_AUTHORISATION_HEADER, TEST_SERVICE_AUTH_TOKEN)
                        .queryParam(CAFCASS_START_DATE_PARAM, CAFCASS_END_DATE_PARAM_VALUE)
                        .queryParam(CAFCASS_END_DATE_PARAM, CAFCASS_START_DATE_PARAM_VALUE)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();

        CafCassResponse actualCafcassResponse = objectMapper.readValue(contentAsString, CafCassResponse.class);

        assertEquals(expectedSearchResult.getTotal(), actualCafcassResponse.getTotal());
        assertEquals(expectedSearchResult.getCases().size(), actualCafcassResponse.getCases().size());
        assertEquals(expectedSearchResult.getTotal(), actualCafcassResponse.getCases().size());

        if (0 != actualCafcassResponse.getTotal()) {
            for (int responseCaseCnt = 0; responseCaseCnt < actualCafcassResponse.getTotal(); responseCaseCnt++) {
                assertNotNull(actualCafcassResponse.getCases().get(responseCaseCnt).getState());
                assertNotNull(actualCafcassResponse.getCases().get(responseCaseCnt).getCaseTypeId());
                assertNotNull(actualCafcassResponse.getCases()
                        .get(responseCaseCnt).getCaseTypeOfApplication());

                assertEquals(actualCafcassResponse.getCases().get(responseCaseCnt).getCaseTypeId(),
                        expectedSearchResult.getCases().get(responseCaseCnt).getCaseTypeId());
                assertEquals(CASE_TYPE, actualCafcassResponse.getCases().get(responseCaseCnt).getCaseTypeId());
                assertEquals(C100_CASE_TYPE, actualCafcassResponse.getCases().get(responseCaseCnt).getCaseTypeOfApplication());
            }
        }
    }
}
*/
