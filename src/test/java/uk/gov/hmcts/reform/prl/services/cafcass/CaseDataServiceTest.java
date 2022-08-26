package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.mockito.Mockito;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;


@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    CafcassCcdDataStoreService cafcassCcdDataStoreService;

    @Mock
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
        String expectedCafCassResponse = TestResourceUtil.readFileFrom("classpath:response/CafCaasResponse.json");
        SearchResult searchResult = objectMapper.readValue(expectedCafCassResponse,
                                                                    SearchResult.class);
        CafCassResponse cafCassResponse = objectMapper.readValue(expectedCafCassResponse, CafCassResponse.class);

        Mockito.doReturn(searchResult).when(cafcassCcdDataStoreService).searchCases(
            ArgumentMatchers.anyString(),  ArgumentMatchers.anyString(),  ArgumentMatchers.anyString(),  ArgumentMatchers.any()
        );
        Mockito.doNothing().when(cafCassFilter).filter(cafCassResponse);
        CafCassResponse realCafCassResponse = caseDataService.getCaseData("authorisation", "serviceAuthorisation",
                                                               "start", "end"
        );
        Assertions.assertEquals(objectMapper.writeValueAsString(cafCassResponse), objectMapper.writeValueAsString(realCafCassResponse));


    }
}

