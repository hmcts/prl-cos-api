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
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.RequestFurtherInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestFurtherInformation;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REQUEST_FURTHER_INFORMATION_DETAILS;
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
    private CaseDetails caseDetails;

    @Before
    public void setUp() {
        caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345678L);

        caseDetails = CaseDetails.builder()
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
    public void validate_FutureDate_NoErrors() {
        RequestFurtherInformation awaitingInfo = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.now().plusDays(10))
            .build();

        caseDataMap.put(REQUEST_FURTHER_INFORMATION_DETAILS, awaitingInfo);
        when(objectMapper.convertValue(awaitingInfo, RequestFurtherInformation.class))
            .thenReturn(awaitingInfo);

        List<String> errors = requestFurtherInformationService.validate(callbackRequest);

        assertTrue(errors.isEmpty());
    }

    @Test
    public void validate_PastDate_ReturnsError() {
        RequestFurtherInformation awaitingInfo = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .build();

        caseDataMap.put(REQUEST_FURTHER_INFORMATION_DETAILS, awaitingInfo);
        when(objectMapper.convertValue(awaitingInfo, RequestFurtherInformation.class))
            .thenReturn(awaitingInfo);

        List<String> errors = requestFurtherInformationService.validate(callbackRequest);

        assertFalse(errors.isEmpty());
    }

    @Test
    public void buildEventWithDescription_WithSingleReason() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 3, 28))
            .requestFurtherInformationReasonEnum(Collections.singletonList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation
            ))
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        assertTrue(event.getDescription().contains("28 Mar 2026"));
        assertTrue(event.getDescription().contains("MIAM - further information required"));
    }

    @Test
    public void buildEventWithDescription_WithMultipleReasons() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 3, 28))
            .requestFurtherInformationReasonEnum(Arrays.asList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation,
                RequestFurtherInformationReasonEnum.dwpHmrcWhereaboutsUnknown
            ))
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        assertTrue(event.getDescription().contains("28 Mar 2026"));
        assertTrue(event.getDescription().contains("MIAM - further information required"));
        assertTrue(event.getDescription().contains("DWP/HMRC - whereabouts unknown"));
    }

    @Test
    public void buildEventWithDescription_DifferentMonths() {
        RequestFurtherInformation jan = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 1, 5))
            .requestFurtherInformationReasonEnum(Collections.singletonList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation
            ))
            .build();

        Event janEvent = requestFurtherInformationService.buildEventWithDescription(jan);
        assertTrue(janEvent.getDescription().contains("05 Jan 2026"));
    }

    @Test
    public void buildEventWithDescription_NullReviewDate() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(null)
            .requestFurtherInformationReasonEnum(Collections.singletonList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation
            ))
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        assertFalse(event.getDescription().contains("Review By Date:"));
        assertTrue(event.getDescription().contains("MIAM - further information required"));
    }

    @Test
    public void buildEventWithDescription_NullReasons() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 3, 28))
            .requestFurtherInformationReasonEnum(null)
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        assertTrue(event.getDescription().contains("28 Mar 2026"));
        assertFalse(event.getDescription().contains("Awaiting Information Reasons:"));
    }

    @Test
    public void buildEventWithDescription_EmptyReasons() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 3, 28))
            .requestFurtherInformationReasonEnum(Collections.emptyList())
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        assertTrue(event.getDescription().contains("28 Mar 2026"));
    }

    @Test
    public void buildEventWithDescription_SingleDigitDay() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 3, 5))
            .requestFurtherInformationReasonEnum(Collections.singletonList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation
            ))
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        assertTrue(event.getDescription().contains("05 Mar 2026"));
    }

    @Test
    public void buildEventWithDescription_MultipleReasonTypes() {
        RequestFurtherInformation info = RequestFurtherInformation.builder()
            .reviewDate(LocalDate.of(2026, 5, 20))
            .requestFurtherInformationReasonEnum(Arrays.asList(
                RequestFurtherInformationReasonEnum.miamFurtherInformation,
                RequestFurtherInformationReasonEnum.dwpHmrcWhereaboutsUnknown,
                RequestFurtherInformationReasonEnum.other
            ))
            .build();

        Event event = requestFurtherInformationService.buildEventWithDescription(info);

        String desc = event.getDescription();
        assertTrue(desc.contains("20 May 2026"));
        assertTrue(desc.contains("MIAM - further information required"));
        assertTrue(desc.contains("DWP/HMRC - whereabouts unknown"));
        assertTrue(desc.contains("Another reason that has not been listed"));
    }
}

