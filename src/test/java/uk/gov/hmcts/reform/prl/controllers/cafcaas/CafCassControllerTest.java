package uk.gov.hmcts.reform.prl.controllers.cafcaas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public  class CafCassControllerTest {

    @InjectMocks
    private CafCassController cafCassController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CaseDataService caseDataService;

    private static final String jsonInString =
        "classpath:response/CafCaasResponse.json";

    @Test
    public void getCaseDataTest() throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        CafCassResponse expectedCafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(jsonInString),
            CafCassResponse.class
        );

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseDataService.getCaseData("authorisation", "serviceAuthorisation", "startDate", "endDate"))
            .thenReturn(expectedCafCassResponse);
        ResponseEntity responseEntity = cafCassController.searcCasesByDates(
            "authorisation",
            "serviceAuthorisation",
            "startDate",
            "endDate"
        );
        CafCassResponse realCafCassResponse = (CafCassResponse) responseEntity.getBody();
        assertEquals(objectMapper.writeValueAsString(expectedCafCassResponse),
                     objectMapper.writeValueAsString(realCafCassResponse));
        assertEquals(realCafCassResponse.getTotal(), 2);
        assertEquals(realCafCassResponse.getCases().size(), 2);
    }

    @Test
    public void testExpectedException() {
        when(authorisationService.authoriseService(any())).thenReturn(false);
        when(authorisationService.authoriseUser(any())).thenReturn(false);
        Assertions.assertThrows(ResponseStatusException.class, () -> {
            cafCassController.searcCasesByDates(
                 "authorisation",
                 "serviceAuthorisation",
                 "startDate",
                 "endDate"
            );
        });

    }
}

