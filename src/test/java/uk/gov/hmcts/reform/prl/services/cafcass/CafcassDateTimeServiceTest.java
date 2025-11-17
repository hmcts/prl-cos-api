package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafcassDateTimeServiceTest {

    @Mock
    private FeatureToggleService featureToggleService;

    @InjectMocks
    private CafcassDateTimeService cafcassDateTimeService;

    @BeforeEach
    void setUp() {
        List<String> caseStateList = List.of("DECISION_OUTCOME","PREPARE_FOR_HEARING_CONDUCT_HEARING","ALL_FINAL_ORDERS_ISSUED");
        ReflectionTestUtils.setField(cafcassDateTimeService, "caseStateList", caseStateList);

        List<String> caseTypeList = new ArrayList<>();
        ReflectionTestUtils.setField(cafcassDateTimeService, "caseTypeList", caseTypeList);

        List<String> excludedEventList = new ArrayList<>();
        ReflectionTestUtils.setField(cafcassDateTimeService, "excludedEventList", excludedEventList);

    }

    @Test
    void shouldUpdateCafcassDateTimeWhenValidStateAndFeatureFlagIsTrue() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("manageDocumentsNew")
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).state("DECISION_OUTCOME").build())
            .build();

        when(featureToggleService.isCafcassDateTimeFeatureEnabled()).thenReturn(true);

        Map<String, Object> actual = cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
        assertNotNull(actual.get(PrlAppsConstants.CAFCASS_DATE_TIME));
    }

    @Test
    void shouldNotUpdateCafcassDateTimeWhenValidStateAndFeatureFlagIsFalse() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.MANAGE_ORDERS.getId())
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).state(State.DECISION_OUTCOME.getValue()).build())
            .build();

        when(featureToggleService.isCafcassDateTimeFeatureEnabled()).thenReturn(false);

        Map<String, Object> actual = cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
        assertNull(actual.get(PrlAppsConstants.CAFCASS_DATE_TIME));
    }

    @Test
    void shouldNotUpdateCafcassDateTimeWhenInValidStateAndFeatureFlagIsTrue() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.MANAGE_ORDERS.getId())
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).state(State.JUDICIAL_REVIEW.getValue()).build())
            .build();

        when(featureToggleService.isCafcassDateTimeFeatureEnabled()).thenReturn(true);

        Map<String, Object> actual = cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
        assertNull(actual.get(PrlAppsConstants.CAFCASS_DATE_TIME));
    }

    @Test
    void shouldNotUpdateCafcassDateTimeWhenValidStateAndFeatureFlagIsTrueButExcludedEvent() {
        List<String> excludedEventList = List.of(Event.MANAGE_ORDERS.getId());

        ReflectionTestUtils.setField(cafcassDateTimeService, "excludedEventList", excludedEventList);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId(Event.MANAGE_ORDERS.getId())
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).state(State.DECISION_OUTCOME.getValue()).build())
            .build();

        when(featureToggleService.isCafcassDateTimeFeatureEnabled()).thenReturn(true);

        Map<String, Object> actual = cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
        assertNull(actual.get(PrlAppsConstants.CAFCASS_DATE_TIME));
    }
}
