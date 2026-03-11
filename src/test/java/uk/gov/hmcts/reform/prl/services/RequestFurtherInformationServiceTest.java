package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.RequestFurtherInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestFurtherInformation;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.State.AWAITING_INFORMATION;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RequestFurtherInformationServiceTest {

    @InjectMocks
    private RequestFurtherInformationService requestFurtherInformationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CallbackRequest callbackRequest;
    private Map<String, Object> caseDataMap;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345678L);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state(AWAITING_INFORMATION.getLabel())
            .data(caseDataMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
    }

    @Test
    public void validateHandlesNullReviewDateWithoutError() {
        RequestFurtherInformation awaitingInfo = RequestFurtherInformation.builder()
            .reviewDate(null)
            .build();

        caseDataMap.put(PrlAppsConstants.REQUEST_FURTHER_INFORMATION_DETAILS, awaitingInfo);

        when(objectMapper.convertValue(awaitingInfo, RequestFurtherInformation.class))
            .thenReturn(awaitingInfo);

        List<String> errors = requestFurtherInformationService.validate(callbackRequest);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateReturnsEmptyListWhenAwaitingInformationIsNull() {
        caseDataMap.put(PrlAppsConstants.REQUEST_FURTHER_INFORMATION_DETAILS, null);

        when(objectMapper.convertValue(null, RequestFurtherInformation.class))
            .thenReturn(RequestFurtherInformation.builder().reviewDate(null).build());

        List<String> errors = requestFurtherInformationService.validate(callbackRequest);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void validateProcessesCallbackRequestCorrectly() {
        RequestFurtherInformation awaitingInfo = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.now().plusDays(7))
            .build();

        caseDataMap.put(PrlAppsConstants.REQUEST_FURTHER_INFORMATION_DETAILS, awaitingInfo);

        when(objectMapper.convertValue(awaitingInfo, RequestFurtherInformation.class))
            .thenReturn(awaitingInfo);

        List<String> errors = requestFurtherInformationService.validate(callbackRequest);

        assertTrue(errors.isEmpty());
        verify(featureToggleService, times(1)).isAwaitingInformationEnabled();
    }


    @Test
    public void buildEventWithDescription_HandlesNullReviewDate() {
        // Given
        RequestFurtherInformation requestFurtherInfo = RequestFurtherInformation.builder()
            .reviewDate(null)
            .requestFurtherInformationReasonEnum(Collections.singletonList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation
            ))
            .build();

        // When
        Event event = requestFurtherInformationService.buildEventWithDescription(requestFurtherInfo);

        // Then
        String expectedDescription = "Awaiting Information Reasons:\n"
            + "MIAM - further information required";

        assertEquals(expectedDescription, event.getDescription());
    }

    @Test
    public void buildEventWithDescription_HandlesNullDateAndNullReasons() {
        // Given
        RequestFurtherInformation requestFurtherInfo = RequestFurtherInformation.builder()
            .reviewDate(null)
            .requestFurtherInformationReasonEnum(null)
            .build();

        // When
        Event event = requestFurtherInformationService.buildEventWithDescription(requestFurtherInfo);

        // Then
        String expectedDescription = "";

        assertEquals(expectedDescription, event.getDescription());
    }

}
