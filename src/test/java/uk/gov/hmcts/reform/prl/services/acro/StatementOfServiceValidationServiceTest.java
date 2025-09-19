package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StmtOfServiceAddRecipient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
@DisplayName("Statement of Service Validation Service Tests")
class StatementOfServiceValidationServiceTest {

    @InjectMocks
    private StatementOfServiceValidationService validationService;

    private AcroCaseData caseData;
    private OrderDetails order;
    private PartyDetails respondent;

    @BeforeEach
    void setUp() {
        respondent = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .address(Address.builder()
                         .addressLine1("123 Test Street")
                         .postCode("AB1 2CD")
                         .build())
            .build();

        caseData = AcroCaseData.builder()
            .id(12345L)
            .respondent(respondent)
            .build();

        order = OrderDetails.builder()
            .orderType("FL404")
            .build();
    }

    @Nested
    @DisplayName("statementOfServiceHasServedSubmittedTime Tests")
    class StatementOfServiceHasServedSubmittedTimeTests {

        @Test
        @DisplayName("Should return true when servedDateTimeOption is populated")
        void shouldReturnTrueWhenServedDateTimeOptionIsPopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .build();

            boolean result = validationService.statementOfServiceHasServedSubmittedTime(sos);

            assertTrue(result, "Should return true when servedDateTimeOption is populated");
        }

        @Test
        @DisplayName("Should return true when submittedDateTime is populated")
        void shouldReturnTrueWhenSubmittedDateTimeIsPopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .submittedDateTime(LocalDateTime.now())
                .build();

            boolean result = validationService.statementOfServiceHasServedSubmittedTime(sos);

            assertTrue(result, "Should return true when submittedDateTime is populated");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "2024-01-15T10:30:00.000", // Valid datetime format
            "2024-01-15", // Valid date format
            "2024-12-31T23:59:59.999"
        })
        @DisplayName("Should return true when partiesServedDateTime has valid formats")
        void shouldReturnTrueWhenPartiesServedDateTimeHasValidFormats(String validDateTime) {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .partiesServedDateTime(validDateTime)
                .build();

            boolean result = validationService.statementOfServiceHasServedSubmittedTime(sos);

            assertTrue(result, "Should return true for valid datetime format: " + validDateTime);
        }

        @Test
        @DisplayName("Should return false when no date fields are populated")
        void shouldReturnFalseWhenNoDateFieldsArePopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John Doe")
                .build();

            boolean result = validationService.statementOfServiceHasServedSubmittedTime(sos);

            assertFalse(result, "Should return false when no date fields are populated");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {
            "   ", "\t", "\n",
            "invalid-date",
            "2024-13-01", // Invalid month
            "2024/01/15", // Wrong separator
            "2024-01-15 10:30:00" // Space instead of T
        })
        @DisplayName("Should return false when partiesServedDateTime has invalid formats")
        void shouldReturnFalseWhenPartiesServedDateTimeHasInvalidFormats(String invalidDateTime) {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .partiesServedDateTime(invalidDateTime)
                .build();

            boolean result = validationService.statementOfServiceHasServedSubmittedTime(sos);

            assertFalse(result, "Should return false for invalid format: " + invalidDateTime);
        }
    }

    @Nested
    @DisplayName("isRespondentIncludedInStatementOfService Tests")
    class IsRespondentIncludedInServiceTests {

        @ParameterizedTest(name = "{2}")
        @MethodSource("respondentIncludedInServiceTestCases")
        @DisplayName("Should correctly validate selectedPartyName scenarios")
        void shouldValidateSelectedPartyNameScenarios(String selectedPartyName, boolean expectedResult, String testDescription) {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName(selectedPartyName)
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertEquals(expectedResult, result, testDescription);
        }

        @Test
        @DisplayName("Should return false when case data has no respondent")
        void shouldReturnFalseWhenCaseDataHasNoRespondent() {
            AcroCaseData caseDataWithoutRespondent = AcroCaseData.builder()
                .id(12345L)
                .respondent(null)
                .build();

            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John Doe")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseDataWithoutRespondent);

            assertFalse(result, "Should return false when case data has no respondent");
        }

        static Stream<Arguments> respondentIncludedInServiceTestCases() {
            return Stream.of(
                // Valid matches - should return true
                arguments("John Doe", true, "Should return true when selectedPartyName contains both first and last name"),
                arguments("Doe, John", true, "Should return true when selectedPartyName contains names in different order"),
                arguments("Mr. John Doe (Respondent)", true, "Should return true when selectedPartyName contains names with extra text"),
                arguments("JOHN DOE", true, "Should be case insensitive"),
                arguments("john doe", true, "Should handle lowercase names"),
                arguments("Dr. John Doe", true, "Should handle titles"),
                arguments("John Doe Jr.", true, "Should handle suffixes"),
                arguments("  John Doe  ", true, "Should handle extra whitespace"),

                // Invalid matches - should return false
                arguments("John", false, "Should return false when selectedPartyName contains only first name"),
                arguments("Doe", false, "Should return false when selectedPartyName contains only last name"),
                arguments("Jane Smith", false, "Should return false when selectedPartyName contains different names"),
                arguments("Jane Doe", false, "Should return false when first name doesn't match"),
                arguments("John Smith", false, "Should return false when last name doesn't match"),
                arguments("Jonathan Doe", false, "Should return false when first name is similar but not exact"),
                arguments("John Doesmith", false, "Should return false when last name is similar but not exact"),

                // Null, empty and whitespace cases - should return false
                arguments(null, false, "Should return false when selectedPartyName is null"),
                arguments("", false, "Should return false when selectedPartyName is empty"),
                arguments("   ", false, "Should return false when selectedPartyName is whitespace"),
                arguments("\t", false, "Should return false when selectedPartyName is tab"),
                arguments("\n", false, "Should return false when selectedPartyName is newline")
            );
        }
    }

    @Nested
    @DisplayName("isOrderServedViaStatementOfService Tests")
    class IsOrderServedViaStatementOfServiceTests {

        @Test
        @DisplayName("Should return false when statement of service list is null")
        void shouldReturnFalseWhenStatementOfServiceListIsNull() {
            boolean result = validationService.isOrderServedViaStatementOfService(order, null, caseData);

            assertFalse(result, "Should return false when statement of service list is null");
        }

        @Test
        @DisplayName("Should return false when statement of service list is empty")
        void shouldReturnFalseWhenStatementOfServiceListIsEmpty() {
            List<Element<StmtOfServiceAddRecipient>> emptyList = List.of();

            boolean result = validationService.isOrderServedViaStatementOfService(order, emptyList, caseData);

            assertFalse(result, "Should return false when statement of service list is empty");
        }

        @Test
        @DisplayName("Should return true when service is completed and respondent is included")
        void shouldReturnTrueWhenServiceIsCompletedAndRespondentIsIncluded() {
            StmtOfServiceAddRecipient completedSos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .selectedPartyName("John Doe")
                .build();

            Element<StmtOfServiceAddRecipient> element = Element.<StmtOfServiceAddRecipient>builder()
                .value(completedSos)
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(element);

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertTrue(result, "Should return true when service is completed and respondent is included");
        }

        @Test
        @DisplayName("Should return false when service is completed but respondent is not included")
        void shouldReturnFalseWhenServiceIsCompletedButRespondentIsNotIncluded() {
            StmtOfServiceAddRecipient completedSos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .selectedPartyName("Jane Smith")
                .build();

            Element<StmtOfServiceAddRecipient> element = Element.<StmtOfServiceAddRecipient>builder()
                .value(completedSos)
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(element);

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertFalse(result, "Should return false when service is completed but respondent is not included");
        }

        @Test
        @DisplayName("Should return false when respondent is included but service is not completed")
        void shouldReturnFalseWhenRespondentIsIncludedButServiceIsNotCompleted() {
            StmtOfServiceAddRecipient incompleteSos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John Doe")
                .build();

            Element<StmtOfServiceAddRecipient> element = Element.<StmtOfServiceAddRecipient>builder()
                .value(incompleteSos)
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(element);

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertFalse(result, "Should return false when respondent is included but service is not completed");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should validate complete order service workflow")
        void shouldValidateCompleteOrderServiceWorkflow() {
            PartyDetails respondent = createTestRespondent("Michael", "Johnson");
            AcroCaseData caseData = createTestCaseData(respondent);
            StmtOfServiceAddRecipient completedSos = createCompletedSos("Mr. Michael Johnson (Respondent)");
            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(createElement(completedSos));
            OrderDetails order = createTestOrder();

            assertAll(
                "Complete order service validation workflow",
                () -> assertTrue(
                    validationService.statementOfServiceHasServedSubmittedTime(completedSos),
                    "Statement of service should be completed"
                ),
                () -> assertTrue(
                    validationService.isRespondentIncludedInService(completedSos, caseData),
                    "Respondent should be included in service"
                ),
                () -> assertTrue(
                    validationService.isOrderServedViaStatementOfService(order, sosList, caseData),
                    "Order should be served via statement of service"
                )
            );
        }

        @Test
        @DisplayName("Should handle edge case with multiple SOS records")
        void shouldHandleMultipleSosRecords() {
            PartyDetails respondent = createTestRespondent("John", "Doe");
            AcroCaseData caseData = createTestCaseData(respondent);

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(
                createElement(createIncompleteSos("John Doe")), // Incomplete but correct respondent
                createElement(createCompletedSos("Jane Smith")), // Complete but wrong respondent
                createElement(createCompletedSos("John Doe")) // Complete and correct respondent
            );

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertTrue(result, "Should return true when at least one SOS record is valid");
        }
    }

    private PartyDetails createTestRespondent(String firstName, String lastName) {
        return PartyDetails.builder()
            .firstName(firstName)
            .lastName(lastName)
            .address(Address.builder()
                         .addressLine1("456 Test Avenue")
                         .postCode("T1 2ST")
                         .build())
            .build();
    }

    private AcroCaseData createTestCaseData(PartyDetails respondent) {
        return AcroCaseData.builder()
            .id(987654321L)
            .respondent(respondent)
            .build();
    }

    private StmtOfServiceAddRecipient createCompletedSos(String selectedPartyName) {
        return StmtOfServiceAddRecipient.builder()
            .servedDateTimeOption(LocalDateTime.of(2024, 1, 15, 14, 30))
            .selectedPartyName(selectedPartyName)
            .build();
    }

    private StmtOfServiceAddRecipient createIncompleteSos(String selectedPartyName) {
        return StmtOfServiceAddRecipient.builder()
            .selectedPartyName(selectedPartyName)
            .build();
    }

    private OrderDetails createTestOrder() {
        return OrderDetails.builder()
            .orderType("FL404")
            .build();
    }

    private Element<StmtOfServiceAddRecipient> createElement(StmtOfServiceAddRecipient sos) {
        return Element.<StmtOfServiceAddRecipient>builder()
            .value(sos)
            .build();
    }
}
