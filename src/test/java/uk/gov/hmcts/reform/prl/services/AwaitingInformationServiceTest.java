package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AwaitingInformationServiceTest {

    @InjectMocks
    private AwaitingInformationService awaitingInformationService;

    private AwaitingInformation awaitingInformation;

    @Before
    public void setUp() {
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();
    }

    @Test
    public void testValidateAwaitingInformationWithValidFutureDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithTodaysDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now())
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

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
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

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
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithFarFutureDate() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusYears(2))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithManyDaysInFuture() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(365))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithManyDaysInPast() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(365))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertEquals(1, errors.size());
    }

    @Test
    public void testValidateAwaitingInformationReturnsListType() {
        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);
        // Then
        assertNotNull(errors);
    }

    @Test
    public void testValidateAwaitingInformationWithDifferentReasons() {
        // Test with different reason enums
        AwaitingInformationReasonEnum[] reasons = {
            AwaitingInformationReasonEnum.immediateRisk,
            AwaitingInformationReasonEnum.leaveTheJurisdiction,
            AwaitingInformationReasonEnum.other
        };

        for (AwaitingInformationReasonEnum reason : reasons) {
            // Given
            awaitingInformation = AwaitingInformation.builder()
                .reviewDate(LocalDate.now().plusDays(5))
                .awaitingInformationReasonEnum(reason)
                .build();

            // When
            List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

            // Then
            assertNotNull(errors);
            assertTrue(errors.isEmpty());
        }
    }

    @Test
    public void testValidateAwaitingInformationWithNullReasonEnum() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(null)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithAllNullFields() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(null)
            .awaitingInformationReasonEnum(null)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationErrorMessageContent() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertTrue(errors.getFirst().contains("date"));
        assertTrue(errors.getFirst().contains("future"));
    }

    @Test
    public void testValidateAwaitingInformationBoundaryDateMinusOne() {
        // Given - Date exactly 1 day before today
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertEquals(1, errors.size());
    }

    @Test
    public void testValidateAwaitingInformationBoundaryDatePlusOne() {
        // Given - Date exactly 1 day after today
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationMultipleCalls() {
        // Test that the service handles multiple calls correctly
        AwaitingInformation info1 = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        AwaitingInformation info2 = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantsCare)
            .build();

        // When
        List<String> errors1 = awaitingInformationService.validateAwaitingInformation(info1);
        List<String> errors2 = awaitingInformationService.validateAwaitingInformation(info2);

        // Then
        assertTrue(errors1.isEmpty());
        assertFalse(errors2.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithMaxDate() {
        // Given - A date far in the future
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.of(9999, 12, 31))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithMinDate() {
        // Given - A date far in the past
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.of(2000, 1, 1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertFalse(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationDoesNotModifyInput() {
        // Given
        LocalDate originalDate = LocalDate.now().plusDays(5);
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(originalDate)
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then - Verify that the input object hasn't been modified
        assertEquals(originalDate, awaitingInformation.getReviewDate());
        assertEquals(AwaitingInformationReasonEnum.immediateRisk, awaitingInformation.getAwaitingInformationReasonEnum());
    }

    @Test
    public void testValidateAwaitingInformationErrorListIsMutable() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().minusDays(1))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then - Verify that error list can be modified
        int initialSize = errors.size();
        errors.add("Additional error");
        assertEquals(initialSize + 1, errors.size());
    }

    @Test
    public void testConstantCaseNameExists() {
        // Verify that the CASE_NAME constant is defined
        assertNotNull(AwaitingInformationService.CASE_NAME);
        assertEquals("Case Name: ", AwaitingInformationService.CASE_NAME);
    }

    @Test
    public void testValidateAwaitingInformationWithReasonDocuments() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(7))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithReasonInformation() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(7))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.applicantsCare)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationWithReasonOther() {
        // Given
        awaitingInformation = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(7))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.other)
            .build();

        // When
        List<String> errors = awaitingInformationService.validateAwaitingInformation(awaitingInformation);

        // Then
        assertTrue(errors.isEmpty());
    }

    @Test
    public void testValidateAwaitingInformationLogicIsolation() {
        // Test that validation logic only checks review date, not other fields
        AwaitingInformation info1 = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.immediateRisk)
            .build();

        AwaitingInformation info2 = AwaitingInformation.builder()
            .reviewDate(LocalDate.now().plusDays(5))
            .awaitingInformationReasonEnum(AwaitingInformationReasonEnum.other)
            .build();

        // When
        List<String> errors1 = awaitingInformationService.validateAwaitingInformation(info1);
        List<String> errors2 = awaitingInformationService.validateAwaitingInformation(info2);

        // Then - Both should be empty since the date is the same
        assertTrue(errors1.isEmpty());
        assertTrue(errors2.isEmpty());
    }
}

