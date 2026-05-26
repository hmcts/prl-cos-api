package uk.gov.hmcts.reform.prl.controllers.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import feign.FeignException;
import feign.Request;
import feign.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers.ApiError;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private UserInfo userInfo;

    @Mock
    private CafcassCaseDataService cafcassCaseDataService;

    private static final String RESPONSE_JSON = "classpath:response/CafCaasResponse.json";
    private static final String CAFCASS_USER_ROLE = "caseworker-privatelaw-cafcass";
    private final String startDate = "2022-08-22T10:54:43.49";
    private final String endDate = "2022-08-22T11:00:43.49";

    @Test
    public void getCaseDataTest() throws Exception {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        CafCassResponse expectedCafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(RESPONSE_JSON),
            CafCassResponse.class
        );

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        when(cafcassCaseDataService.getCaseData("authorisation", startDate, endDate))
            .thenReturn(expectedCafCassResponse);

        ResponseEntity<Object> responseEntity = cafCassController.searchCasesByDates(
            "authorisation", "Bearer serviceAuthorisation", startDate, endDate
        );

        CafCassResponse realCafCassResponse = (CafCassResponse) responseEntity.getBody();
        assertEquals(
            objectMapper.writeValueAsString(expectedCafCassResponse),
            objectMapper.writeValueAsString(realCafCassResponse)
        );
        Assert.assertNotNull(realCafCassResponse);
        assertEquals(4, realCafCassResponse.getTotal());
        assertEquals(4, realCafCassResponse.getCases().size());
    }

    @Test
    public void getCaseData_shouldNotFail_whenHearingTypesIsNull() throws Exception {
        String json = """
                    {
                       "cases": [
                         {
                           "id": 1234567890123456,
                           "case_data": {
                             "orderCollection": [
                               {
                                 "id": "00000000-0000-0000-0000-000000000000",
                                 "value": {
                                   "orderType": "SomeOrder",
                                   "manageOrderHearingDetails": [
                                     {
                                       "id": "11111111-1111-1111-1111-111111111111",
                                       "value": {
                                         "hearingTypes": null,
                                         "confirmedHearingDates": null
                                       }
                                     }
                                   ]
                                 }
                               }
                             ]
                           }
                         }
                       ],
                       "total": 1
                     }
            """;
        ObjectMapper mapper = CcdObjectMapper.getObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        CafCassResponse cafCassResponse = mapper.readValue(json, CafCassResponse.class);

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        when(cafcassCaseDataService.getCaseData("authorisation", startDate, endDate)).thenReturn(cafCassResponse);

        ResponseEntity<Object> responseEntity = cafCassController.searchCasesByDates(
            "authorisation",
            "Bearer serviceAuthorisation",
            startDate,
            endDate
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void getCaseData_shouldDeserializeNormally_withMultipleHearings() throws Exception {
        String json = """
            {
              "cases": [
                {
                  "id": 9876543210123456,
                  "case_data": {
                    "orderCollection": [
                      {
                        "id": "00000000-0000-0000-0000-000000000000",
                        "value": {
                          "orderType": "StandardOrder",
                          "manageOrderHearingDetails": [
                            {
                              "id": "11111111-1111-1111-1111-111111111111",
                              "value": {
                                "hearingTypes": {
                                  "value": {"code": "TYPE_A", "label": "First Hearing"}
                                },
                                "confirmedHearingDates": {
                                  "value": {"code": "123", "label": "2022-01-01T10:00:00"}
                                }
                              }
                            },
                            {
                              "id": "22222222-2222-2222-2222-222222222222",
                              "value": {
                                "hearingTypes": {
                                  "value": {"code": "TYPE_B", "label": "Second Hearing"}
                                },
                                "confirmedHearingDates": {
                                  "value": {"code": "456", "label": "2022-01-01T11:00:00"}
                                }
                              }
                            }
                          ]
                        }
                      }
                    ]
                  }
                }
              ],
              "total": 1
            }
            """;
        ObjectMapper mapper = CcdObjectMapper.getObjectMapper();
        mapper.registerModule(new ParameterNamesModule());
        CafCassResponse cafCassResponse = mapper.readValue(json, CafCassResponse.class);

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        when(cafcassCaseDataService.getCaseData("authorisation", startDate, endDate)).thenReturn(cafCassResponse);

        ResponseEntity<Object> responseEntity = cafCassController.searchCasesByDates(
            "authorisation",
            "Bearer serviceAuthorisation",
            startDate,
            endDate
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testInvalidServiceAuth_401UnAuthorized() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(false);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            cafCassController.searchCasesByDates("authorisation",
                                                 "inValidServiceAuthorisation",
                                                 "startDate", "endDate");
        });
        assertEquals(UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    public void testExceptionInternalServerErrorForDateTimeRange() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        final ResponseEntity<Object> response = cafCassController.searchCasesByDates(
            TEST_AUTHORIZATION,
            TEST_SERVICE_AUTHORIZATION,
            "2022-08-22T10:54:43.49",
            "2022-08-22T11:54:43.49"
        );
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Difference between end date and start date should not be more than 15 minutes",
                     ((ApiError)response.getBody()).getMessage());
    }

    @Test
    public void testExceptionUnAuthorisedForInvalidCaseRole() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of("invalid-case-role"));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            cafCassController.searchCasesByDates("authorisation", "Bearer serviceAuthorisation", startDate, endDate);
        });
        assertEquals(UNAUTHORIZED, exception.getStatusCode());
    }

    // Refactored exception tests (verifying Resilience4j)
    @Test
    public void testFeignExceptionBadRequest() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        when(cafcassCaseDataService.getCaseData(TEST_AUTHORIZATION, startDate, endDate))
            .thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));

        // Assert that the controller now propagates the FeignException directly
        assertThrows(
            FeignException.class, () -> {
                cafCassController.searchCasesByDates(
                    TEST_AUTHORIZATION,
                    TEST_SERVICE_AUTHORIZATION,
                    startDate,
                    endDate
                );
            }
        );
    }

    @Test
    public void testFeignExceptionUnAuthorised() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        when(cafcassCaseDataService.getCaseData(TEST_AUTHORIZATION, startDate, endDate))
            .thenThrow(feignException(UNAUTHORIZED.value(), "Unauthorised"));

        // Assert that the controller propagates the FeignException directly
        assertThrows(
            FeignException.class, () -> {
                cafCassController.searchCasesByDates(
                    TEST_AUTHORIZATION,
                    TEST_SERVICE_AUTHORIZATION,
                    startDate,
                    endDate
                );
            }
        );
    }

    @Test
    public void testExceptionInternalServerError() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(userInfo.getRoles()).thenReturn(List.of(CAFCASS_USER_ROLE));
        when(cafcassCaseDataService.getCaseData(TEST_AUTHORIZATION, "startDate", "endDate"))
            .thenThrow(new RuntimeException("Something broke inside"));

        // Assert that the controller propagates the raw Exception directly
        assertThrows(
            RuntimeException.class, () -> {
                cafCassController.searchCasesByDates(
                    TEST_AUTHORIZATION,
                    TEST_SERVICE_AUTHORIZATION,
                    "startDate",
                    "endDate"
                );
            }
        );
    }

    // New tests: verifying the fallback mapping methods directly
    @Test
    public void testFallback_HandlesFeignExceptionCorrectly() {
        FeignException ex = feignException(HttpStatus.BAD_REQUEST.value(), "Bad Request payload");

        ResponseEntity<ApiError> response = cafCassController.searchCasesFallback(
            TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION,
            startDate, endDate, ex
        );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        ApiError body = (ApiError) response.getBody();
        assertNotNull(body);
        assertEquals(ex.getMessage(), body.getMessage());
    }

    @Test
    public void testFallback_HandlesGenericExceptionCorrectly() {
        Exception ex = new RuntimeException("Unexpected core system error");

        ResponseEntity<ApiError> response = cafCassController.searchCasesFallback(
            TEST_AUTHORIZATION, TEST_SERVICE_AUTHORIZATION,
            startDate, endDate, ex
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ApiError body = (ApiError) response.getBody();
        assertNotNull(body);
        assertEquals(ex.getMessage(), body.getMessage());
    }

    // Helper method
    public static FeignException feignException(int status, String message) {
        return FeignException.errorStatus(
            message, Response.builder()
                .status(status)
                .request(Request.create(GET, EMPTY, Map.of(), new byte[]{}, UTF_8, null))
                .build()
        );
    }
}
