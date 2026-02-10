package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AwaitingInformation;

import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AwaitingInformationServiceTest {

    @InjectMocks
    private AwaitingInformationService awaitingInformationService;
    @Mock
    private FeatureToggleService featureToggleService;

    private AwaitingInformation awaitingInformation;

    @Before
    public void setUp() {
        when(featureToggleService.isAwaitingInformationEnabled()).thenReturn(true);
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
}

