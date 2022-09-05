package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.prl.controllers.cafcaas.CafCassController;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CafCassControllerTest {
    @Mock
    private CaseDataService caseDataService;

    @InjectMocks
    private CafCassController cafCassController;
    private static final String jsonInString =
        "classpath:response/CafCaasResponse.json";

    @org.junit.Test
    public void getCaseDataTest() throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        CafCassResponse expectedCafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(jsonInString),
            CafCassResponse.class
        );
        Mockito.when(caseDataService.getCaseData("authorisation", "serviceAuthorisation", "startDate", "endDate"))
            .thenReturn(expectedCafCassResponse);
        ResponseEntity responseEntity = cafCassController.searcCasesByDates(
            "authorisation",
            "serviceAuthorisation",
            "startDate",
            "endDate"
        );
        CafCassResponse realCafCassResponse = (CafCassResponse) responseEntity.getBody();
        Assertions.assertEquals(objectMapper.writeValueAsString(expectedCafCassResponse),
                                objectMapper.writeValueAsString(realCafCassResponse));
        Assertions.assertEquals(realCafCassResponse.getTotal(), 2);
        Assertions.assertEquals(realCafCassResponse.getCases().size(), 2);
    }
}
