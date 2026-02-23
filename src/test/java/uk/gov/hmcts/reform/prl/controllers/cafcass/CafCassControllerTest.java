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
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.prl.exception.cafcass.exceptionhandlers.ApiError;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassResponse;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassCaseDataService;
import uk.gov.hmcts.reform.prl.utils.TestResourceUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static feign.Request.HttpMethod.GET;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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

    private static final String RESPONSE_JSON =
        "classpath:response/CafCaasResponse.json";

    private String startDate = "2022-08-22T10:54:43.49";

    private String endDate = "2022-08-22T11:00:43.49";

    @Test
    public void getCaseDataTest() throws IOException {
        ObjectMapper objectMapper = CcdObjectMapper.getObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        CafCassResponse expectedCafCassResponse = objectMapper.readValue(
            TestResourceUtil.readFileFrom(RESPONSE_JSON),
            CafCassResponse.class
        );

        when(authorisationService.authoriseService(any())).thenReturn(true);
        when(cafcassCaseDataService.getCaseData("authorisation", startDate, endDate))
                      .thenReturn(expectedCafCassResponse);
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
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
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.empty());
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
        when(cafcassCaseDataService.getCaseData(TEST_AUTHORIZATION, startDate,
                                                endDate
        )).thenThrow(feignException(HttpStatus.BAD_REQUEST.value(), "Not found"));
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
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
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(cafcassCaseDataService.getCaseData(TEST_AUTHORIZATION, startDate,
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
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
        when(cafcassCaseDataService.getCaseData(TEST_AUTHORIZATION, "startDate",
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
        when(authorisationService.authoriseUser(any())).thenReturn(Optional.of(userInfo));
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
    public void getCaseData_shouldNotFail_whenHearingTypesIsNull() throws Exception {
        // Arrange: the same resilient payload as above
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
        when(cafcassCaseDataService.getCaseData("authorisation", startDate, endDate))
            .thenReturn(cafCassResponse);

        // Act
        ResponseEntity<?> responseEntity = cafCassController.searcCasesByDates(
            "authorisation", "Bearer serviceAuthorisation", startDate, endDate
        );

        // Assert: still 200 and body present
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        CafCassResponse body = (CafCassResponse) responseEntity.getBody();
        assertEquals(1, body.getTotal());
        assertEquals(1, body.getCases().size());

        // And the problematic part didn’t crash the controller
        var caseOrder = body.getCases().get(0).getCaseData().getOrderCollection().get(0).getValue();
        assertNull(caseOrder.getHearingDetails());
        assertNull(caseOrder.getHearingId());
    }

    @Test
    public void getCaseData_shouldDeserializeNormally_withMultipleHearings() throws Exception {
        // Arrange: create a valid payload with 2 hearing details
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
        when(cafcassCaseDataService.getCaseData("authorisation", startDate, endDate))
            .thenReturn(cafCassResponse);

        // Act
        ResponseEntity<?> responseEntity = cafCassController.searcCasesByDates(
            "authorisation", "Bearer serviceAuthorisation", startDate, endDate
        );

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        CafCassResponse body = (CafCassResponse) responseEntity.getBody();
        assertEquals(1, body.getTotal());
        assertEquals(1, body.getCases().size());

        // Verify both hearings parsed correctly
        var order = body.getCases().get(0).getCaseData().getOrderCollection().get(0).getValue();
        assertNotNull(order.getHearingDetails());
        assertEquals("TYPE_A", order.getHearingDetails().getHearingType()); // first one taken
        assertEquals("First Hearing", order.getHearingDetails().getHearingTypeValue());
        assertEquals("123, 456", order.getHearingId());
        assertEquals(List.of(123L, 456L), order.getHearingIds());
    }

}
