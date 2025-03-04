package uk.gov.hmcts.reform.prl.controllers.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
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
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_SERVICE_AUTHORIZATION;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CafCassControllerTest {

    @InjectMocks
    private CafCassController cafCassController;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    private static final String jsonInString =
        "classpath:response/CafCaasResponse.json";

    private String startDate = "2022-08-22T10:54:43.49";

    private String endDate = "2022-08-22T11:00:43.49";

    @Test
    public void getCaseDataTest() throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        CafCassResponse expectedCafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(jsonInString),
            CafCassResponse.class
        );

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseDataService.getCaseData("authorisation", startDate, endDate))
            .thenReturn(expectedCafCassResponse);
        ResponseEntity responseEntity = cafCassController.searcCasesByDates(
            "authorisation",
            "Bearer serviceAuthorisation",
            startDate,
            endDate
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
        when(caseDataService.getCaseData(TEST_AUTHORIZATION, startDate,
                                         endDate
        )).thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));
        final ResponseEntity response = cafCassController.searcCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            startDate,
            endDate
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void testFeignExceptionUnAuthorised() throws IOException {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(caseDataService.getCaseData(TEST_AUTHORIZATION, startDate,
                                         endDate
        )).thenThrow(feignException(UNAUTHORIZED.value(), "Unauthorised"));
        final ResponseEntity response = cafCassController.searcCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            startDate,
            endDate
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

    @Test
    public void testExceptionInternalServerErrorForDateTimeRange() {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        final ResponseEntity response = cafCassController.searcCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            "2022-08-22T10:54:43.49",
            "2022-08-22T11:54:43.49"
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Difference between end date and start date should not be more than 15 minutes",
                     ((ApiError)response.getBody()).getMessage());
    }

    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(message, Response.builder()
            .status(status)
            .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
            .build());
    }

    @Test
    public void shouldReturnCaseDataWhenFeatureEnabledAndUserAndServiceAuthorized() throws Exception {
        when(launchDarklyClient.isFeatureEnabled("cafcass-search")).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(caseDataService.getCaseData(any(), any(), any())).thenReturn(new CafCassResponse());

        ResponseEntity<Object> response = cafCassController.searcCasesByDates("auth", "Bearer s2s token", "2022-08-22T10:54:43.49", "2022-08-22T11:00:43.49");
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldReturnBadRequestWhenDateDifferenceMoreThan15Minutes() {
        when(launchDarklyClient.isFeatureEnabled("cafcass-search")).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.authoriseService(any())).thenReturn(true);

        ResponseEntity<Object> response = cafCassController.searcCasesByDates("auth", "Bearer s2s token", "2022-08-22T10:54:43.49", "2022-08-22T11:54:43.49");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void shouldReturnUnauthorizedWhenFeatureDisabled() {
        when(launchDarklyClient.isFeatureEnabled("cafcass-search")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> cafCassController.searcCasesByDates("auth", "Bearer s2s token", "2022-08-22T10:54:43.49", "2022-08-22T11:00:43.49"));
    }

    @Test
    public void shouldReturnUnauthorizedWhenUserNotAuthorized() {
        when(launchDarklyClient.isFeatureEnabled("cafcass-search")).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> cafCassController.searcCasesByDates("auth", "Bearer s2s token", "2022-08-22T10:54:43.49", "2022-08-22T11:00:43.49"));
    }

    @Test
    public void shouldReturnUnauthorizedWhenServiceNotAuthorized() {
        when(launchDarklyClient.isFeatureEnabled("cafcass-search")).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(true);
        when(authorisationService.authoriseService(any())).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> cafCassController.searcCasesByDates("auth", "Bearer s2s token", "2022-08-22T10:54:43.49", "2022-08-22T11:00:43.49"));
    }
}
