package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CafcassDateTimeServiceTest {

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
    void shouldUpdateCafcassDateTimeWhenValidState() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("manageDocumentsNew")
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).state("DECISION_OUTCOME").build())
            .build();

        Map<String, Object> actual = cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
        assertNotNull(actual.get(PrlAppsConstants.CAFCASS_DATE_TIME));
    }

    @Test
    void shouldNotUpdateCafcassDateTimeWhenInValidState() {
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .eventId("manageDocumentsNew")
            .caseDetails(CaseDetails.builder().data(new HashMap<>()).state("JUDICIAL_REVIEW").build())
            .build();

        Map<String, Object> actual = cafcassDateTimeService.updateCafcassDateTime(callbackRequest);
        assertNull(actual.get(PrlAppsConstants.CAFCASS_DATE_TIME));
    }
}
