package uk.gov.hmcts.reform.prl.controllers.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers.ApiError;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.util.Map;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTHORIZATION;

@RunWith(MockitoJUnitRunner.Silent.class)
@Ignore
public class CafCassControllerTest {

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
        when(caseDataService.getCaseData("authorisation", "startDate", "endDate"))
            .thenReturn(expectedCafCassResponse);
        ResponseEntity responseEntity = cafCassController.searcCasesByDates(
            "authorisation",
            "serviceAuthorisation",
            "startDate",
            "endDate"
        );

        CafCassResponse realCafCassResponse = (CafCassResponse) responseEntity.getBody();
        assertEquals(
            objectMapper.writeValueAsString(expectedCafCassResponse),
            objectMapper.writeValueAsString(realCafCassResponse)
        );
        assertEquals(4, realCafCassResponse.getTotal());
        assertEquals(4, realCafCassResponse.getCases().size());
    }

    @Test
    public void testInvalidServicAuth_401UnAuthorized() {
        when(authorisationService.authoriseService(any())).thenReturn(false);
        when(authorisationService.authoriseUser(any())).thenReturn(false);
        final ResponseEntity response = cafCassController.searcCasesByDates(
            "authorisation",
            "inValidServiceAuthorisation",
            "startDate",
            "endDate"
        );
        assertEquals(UNAUTHORIZED, response.getStatusCode());
        final ApiError body = (ApiError) response.getBody();
        assertEquals("401 UNAUTHORIZED", body.getMessage());

    }

    @Test
    public void testFeignExceptionBadRequest() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseDataService.getCaseData(TEST_AUTHORIZATION, "startDate",
                                         "endDate"
        )).thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));
        final ResponseEntity response = cafCassController.searcCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            "startDate",
            "endDate"
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testFeignExceptionUnAuthorised() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseDataService.getCaseData(TEST_AUTHORIZATION, "startDate",
                                         "endDate"
        )).thenThrow(feignException(UNAUTHORIZED.value(), "Unauthorised"));
        final ResponseEntity response = cafCassController.searcCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            "startDate",
            "endDate"
        );
        assertEquals(UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    public void testExceptionInternalServerError() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseDataService.getCaseData(TEST_AUTHORIZATION, "startDate",
                                         "endDate"
        )).thenThrow(new RuntimeException());
        final ResponseEntity response = cafCassController.searcCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            "startDate",
            "endDate"
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(message, Response.builder()
            .status(status)
            .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
            .build());
    }
}

