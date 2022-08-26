package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.request.QueryParam;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @Autowired
    private CafCassFilter cafCassFilter;

    @InjectMocks
    private CaseDataService caseDataService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @org.junit.Test
    public void getCaseData() throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        String searchResultJson = TestResourceUtil.readFileFrom("classpath:response/CCDResponse.json");
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafCaasResponse.json");
        SearchResult searchResult = objectMapper.readValue(searchResultJson,
                                                                    SearchResult.class);
        CafCassResponse cafCassResponse = objectMapper.convertValue(searchResult, CafCassResponse.class);

        Mockito.doReturn(searchResult).when(cafcassCcdDataStoreService).searchCases(
            ArgumentMatchers.anyString(),  ArgumentMatchers.anyString(),  ArgumentMatchers.anyString(),  ArgumentMatchers.any()
        );
        Mockito.doNothing().when(cafCassFilter).filter(cafCassResponse);
        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation", "serviceAuthorisation",
                                                               "start", "end"
        );
        Assertions.assertEquals(expectedCafCassResponse, objectMapper.writeValueAsString(realCafCassResponse));


    }
}

