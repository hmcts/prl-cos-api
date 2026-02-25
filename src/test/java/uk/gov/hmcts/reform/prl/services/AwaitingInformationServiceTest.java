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
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_STATUS;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AwaitingInformationServiceTest {

    @InjectMocks
    private AwaitingInformationService awaitingInformationService;

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private ObjectMapper objectMapper;

    private AwaitingInformation awaitingInformation;
    private Map<String, Object> caseDataMap;
    private CallbackRequest callbackRequest;

    @Before
    public void setUp() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345678L);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(12345678L)
            .state("AWAITING_INFORMATION")
            .data(caseDataMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
    }

    // Tests for validate method
    @Test
    public void testValidateAwaitingInformationWithValidFutureDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        // When
        List<String> errors = awaitingInformationService.validate(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithTodaysDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now())
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        // When
        List<String> errors = awaitingInformationService.validate(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("The date must be in the future", errors.getFirst());
    }

    @Test
    public void testValidateAwaitingInformationWithPastDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        // When
        List<String> errors = awaitingInformationService.validate(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
        assertEquals("The date must be in the future", errors.getFirst());
    }

    @Test
    public void testValidateAwaitingInformationWithNullDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(null)
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantFurtherInformation)
            .build();

        // When
        List<String> errors = awaitingInformationService.validate(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithDifferentReasons() {
        // Test with different reason enums
        AwaitingInformationReasonEnum[] reasons = {
            AwaitingInformationReasonEnum.applicantFurtherInformation,
            AwaitingInformationReasonEnum.applicantClarifyConfidentialDetails,
            AwaitingInformationReasonEnum.other
        };

        for (AwaitingInformationReasonEnum reason : reasons) {
            // Given
            awaitingInformation = AwaitingInformation.builder()
                .reviewDate(LocalDate.now().plusDays(5))
                .awaitingInformationReasonEnum(reason)
                .build();

            // When
            List<String> errors = awaitingInformationService.validate(awaitingInformation);

            // Then
            assertNotNull(errors);
            assertTrue(errors.isEmpty());
        }
    }

    // Tests for addToCase method
    @Test
    public void testAddToCaseSuccessfully() {
        // Given
        when(objectMapper.convertValue(eq(caseDataMap), eq(AwaitingInformation.class)))
            .thenReturn(awaitingInformation);

        // When
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey(CASE_STATUS));
        Object caseStatusObj = result.get(CASE_STATUS);
        assertNotNull(caseStatusObj);
        assertTrue(caseStatusObj instanceof CaseStatus);
    }


    @Test
    public void testAddToCasePreservesExistingCaseData() {
        // Given
        caseDataMap.put("testKey", "testValue");
        caseDataMap.put("anotherKey", 12345);
        when(objectMapper.convertValue(eq(caseDataMap), eq(AwaitingInformation.class)))
            .thenReturn(awaitingInformation);

        // When
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        // Then
        assertTrue(result.containsKey("testKey"));
        assertEquals("testValue", result.get("testKey"));
        assertTrue(result.containsKey("anotherKey"));
        assertEquals(12345, result.get("anotherKey"));
    }


    @Test
    public void testAddToCaseWithMultipleCaseDataEntries() {
        // Given
        caseDataMap.put("applicantName", "John Doe");
        caseDataMap.put("respondentName", "Jane Doe");
        caseDataMap.put("caseType", "C100");
        caseDataMap.put("eventId", "123456");
        when(objectMapper.convertValue(eq(caseDataMap), eq(AwaitingInformation.class)))
            .thenReturn(awaitingInformation);

        // When
        Map<String, Object> result = awaitingInformationService.addToCase(callbackRequest);

        // Then
        assertNotNull(result);
        assertEquals("John Doe", result.get("applicantName"));
        assertEquals("Jane Doe", result.get("respondentName"));
        assertEquals("C100", result.get("caseType"));
        assertEquals("123456", result.get("eventId"));
        assertTrue(result.containsKey(CASE_STATUS));
    }


}

