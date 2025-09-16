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
    @DisplayName("isStatementOfServiceCompleted Tests")
    class IsStatementOfServiceCompletedTests {

        @Test
        @DisplayName("Should return true when servedDateTimeOption is populated")
        void shouldReturnTrueWhenServedDateTimeOptionIsPopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertTrue(result, "Should return true when servedDateTimeOption is populated");
        }

        @Test
        @DisplayName("Should return true when submittedDateTime is populated")
        void shouldReturnTrueWhenSubmittedDateTimeIsPopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .submittedDateTime(LocalDateTime.now())
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertTrue(result, "Should return true when submittedDateTime is populated");
        }

        @Test
        @DisplayName("Should return true when partiesServedDateTime is populated")
        void shouldReturnTrueWhenPartiesServedDateTimeIsPopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .partiesServedDateTime("2024-01-15 10:30")
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertTrue(result, "Should return true when partiesServedDateTime is populated");
        }

        @Test
        @DisplayName("Should return true when multiple date fields are populated")
        void shouldReturnTrueWhenMultipleDateFieldsArePopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .submittedDateTime(LocalDateTime.now().minusDays(1))
                .partiesServedDateTime("2024-01-15 10:30")
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertTrue(result, "Should return true when multiple date fields are populated");
        }

        @Test
        @DisplayName("Should return false when no date fields are populated")
        void shouldReturnFalseWhenNoDateFieldsArePopulated() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John Doe")
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertFalse(result, "Should return false when no date fields are populated");
        }

        @Test
        @DisplayName("Should return false when all date fields are null")
        void shouldReturnFalseWhenAllDateFieldsAreNull() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(null)
                .submittedDateTime(null)
                .partiesServedDateTime(null)
                .selectedPartyName("John Doe")
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertFalse(result, "Should return false when all date fields are null");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should return false when partiesServedDateTime is null, empty or whitespace")
        void shouldReturnFalseWhenPartiesServedDateTimeIsNullEmptyOrWhitespace(String partiesServedDateTime) {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .partiesServedDateTime(partiesServedDateTime)
                .build();

            boolean result = validationService.isStatementOfServiceCompleted(sos);

            assertFalse(result, "Should return false when partiesServedDateTime is null, empty or whitespace");
        }
    }

    @Nested
    @DisplayName("isRespondentIncludedInService Tests")
    class IsRespondentIncludedInServiceTests {

        @Test
        @DisplayName("Should return true when selectedPartyName contains both first and last name")
        void shouldReturnTrueWhenSelectedPartyNameContainsBothNames() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John Doe")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertTrue(result, "Should return true when selectedPartyName contains both first and last name");
        }

        @Test
        @DisplayName("Should return true when selectedPartyName contains names in different order")
        void shouldReturnTrueWhenSelectedPartyNameContainsNamesInDifferentOrder() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("Doe, John")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertTrue(result, "Should return true when selectedPartyName contains names in different order");
        }

        @Test
        @DisplayName("Should return true when selectedPartyName contains names with extra text")
        void shouldReturnTrueWhenSelectedPartyNameContainsNamesWithExtraText() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("Mr. John Doe (Respondent)")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertTrue(result, "Should return true when selectedPartyName contains names with extra text");
        }

        @Test
        @DisplayName("Should be case insensitive")
        void shouldBeCaseInsensitive() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("JOHN DOE")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertTrue(result, "Should be case insensitive");
        }

        @Test
        @DisplayName("Should return false when selectedPartyName contains only first name")
        void shouldReturnFalseWhenSelectedPartyNameContainsOnlyFirstName() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertFalse(result, "Should return false when selectedPartyName contains only first name");
        }

        @Test
        @DisplayName("Should return false when selectedPartyName contains only last name")
        void shouldReturnFalseWhenSelectedPartyNameContainsOnlyLastName() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("Doe")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertFalse(result, "Should return false when selectedPartyName contains only last name");
        }

        @Test
        @DisplayName("Should return false when selectedPartyName contains different names")
        void shouldReturnFalseWhenSelectedPartyNameContainsDifferentNames() {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("Jane Smith")
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertFalse(result, "Should return false when selectedPartyName contains different names");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("Should return false when selectedPartyName is null, empty or whitespace")
        void shouldReturnFalseWhenSelectedPartyNameIsNullEmptyOrWhitespace(String selectedPartyName) {
            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName(selectedPartyName)
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, caseData);

            assertFalse(result, "Should return false when selectedPartyName is null, empty or whitespace");
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

        @ParameterizedTest
        @MethodSource("respondentNameVariations")
        @DisplayName("Should handle various respondent name scenarios")
        void shouldHandleVariousRespondentNameScenarios(String firstName, String lastName, String selectedPartyName, boolean expectedResult) {
            PartyDetails testRespondent = PartyDetails.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();

            AcroCaseData testCaseData = AcroCaseData.builder()
                .id(12345L)
                .respondent(testRespondent)
                .build();

            StmtOfServiceAddRecipient sos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName(selectedPartyName)
                .build();

            boolean result = validationService.isRespondentIncludedInService(sos, testCaseData);

            if (expectedResult) {
                assertTrue(result, String.format("Should return true for firstName='%s', lastName='%s', selectedPartyName='%s'",
                    firstName, lastName, selectedPartyName));
            } else {
                assertFalse(result, String.format("Should return false for firstName='%s', lastName='%s', selectedPartyName='%s'",
                    firstName, lastName, selectedPartyName));
            }
        }

        static Stream<Arguments> respondentNameVariations() {
            return Stream.of(
                // Valid matches
                arguments("John", "Doe", "John Doe", true),
                arguments("John", "Doe", "Doe, John", true),
                arguments("John", "Doe", "Mr John Doe", true),
                arguments("John", "Doe", "john doe", true),
                arguments("Mary", "Smith-Jones", "Mary Smith-Jones", true),
                arguments("José", "García", "José García", true),

                // Invalid matches
                arguments("John", "Doe", "John", false),
                arguments("John", "Doe", "Doe", false),
                arguments("John", "Doe", "Jane Doe", false),
                arguments("John", "Doe", "John Smith", false),
                arguments("John", "Doe", "Jane Smith", false),

                // Missing names
                arguments(null, "Doe", "John Doe", false),
                arguments("John", null, "John Doe", false),
                arguments(null, null, "John Doe", false),
                arguments("", "Doe", "John Doe", false),
                arguments("John", "", "John Doe", false)
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

        @Test
        @DisplayName("Should return true when at least one statement of service is valid")
        void shouldReturnTrueWhenAtLeastOneStatementOfServiceIsValid() {
            // First SOS - incomplete
            StmtOfServiceAddRecipient incompleteSos = StmtOfServiceAddRecipient.builder()
                .selectedPartyName("John Doe")
                .build();

            // Second SOS - complete and includes respondent
            StmtOfServiceAddRecipient completeSos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .selectedPartyName("John Doe")
                .build();

            // Third SOS - complete but different person
            StmtOfServiceAddRecipient otherPersonSos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .selectedPartyName("Jane Smith")
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(
                Element.<StmtOfServiceAddRecipient>builder().value(incompleteSos).build(),
                Element.<StmtOfServiceAddRecipient>builder().value(completeSos).build(),
                Element.<StmtOfServiceAddRecipient>builder().value(otherPersonSos).build()
            );

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertTrue(result, "Should return true when at least one statement of service is valid");
        }

        @Test
        @DisplayName("Should handle null element values gracefully")
        void shouldHandleNullElementValuesGracefully() {
            Element<StmtOfServiceAddRecipient> nullElement = Element.<StmtOfServiceAddRecipient>builder()
                .value(null)
                .build();

            StmtOfServiceAddRecipient validSos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.now())
                .selectedPartyName("John Doe")
                .build();

            Element<StmtOfServiceAddRecipient> validElement = Element.<StmtOfServiceAddRecipient>builder()
                .value(validSos)
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(nullElement, validElement);

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertTrue(result, "Should handle null element values gracefully and find valid ones");
        }

        @Test
        @DisplayName("Should return false when all elements have null values")
        void shouldReturnFalseWhenAllElementsHaveNullValues() {
            Element<StmtOfServiceAddRecipient> nullElement1 = Element.<StmtOfServiceAddRecipient>builder()
                .value(null)
                .build();

            Element<StmtOfServiceAddRecipient> nullElement2 = Element.<StmtOfServiceAddRecipient>builder()
                .value(null)
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(nullElement1, nullElement2);

            boolean result = validationService.isOrderServedViaStatementOfService(order, sosList, caseData);

            assertFalse(result, "Should return false when all elements have null values");
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("Should handle complete realistic scenario")
        void shouldHandleCompleteRealisticScenario() {
            // Create a realistic case with respondent
            PartyDetails realisticRespondent = PartyDetails.builder()
                .firstName("Michael")
                .lastName("Johnson")
                .address(Address.builder()
                    .addressLine1("456 Oak Avenue")
                    .addressLine2("Apartment 2B")
                    .postCode("M1 2AB")
                    .build())
                .phoneNumber("07700123456")
                .email("m.johnson@example.com")
                .build();

            AcroCaseData realisticCaseData = AcroCaseData.builder()
                .id(987654321L)
                .respondent(realisticRespondent)
                .build();

            // Create statement of service with realistic data
            StmtOfServiceAddRecipient realisticSos = StmtOfServiceAddRecipient.builder()
                .servedDateTimeOption(LocalDateTime.of(2024, 1, 15, 14, 30))
                .submittedDateTime(LocalDateTime.of(2024, 1, 15, 15, 0))
                .partiesServedDateTime("2024-01-15T14:30:00")
                .selectedPartyName("Mr. Michael Johnson (Respondent)")
                .selectedPartyId("12345")
                .build();

            Element<StmtOfServiceAddRecipient> element = Element.<StmtOfServiceAddRecipient>builder()
                .value(realisticSos)
                .build();

            List<Element<StmtOfServiceAddRecipient>> sosList = List.of(element);

            OrderDetails realisticOrder = OrderDetails.builder()
                .orderType("FL404")
                .build();

            // Test all methods with realistic data
            assertAll(
                "Complete realistic scenario validation",
                () -> assertTrue(validationService.isStatementOfServiceCompleted(realisticSos),
                    "Statement of service should be completed"),
                () -> assertTrue(validationService.isRespondentIncludedInService(realisticSos, realisticCaseData),
                    "Respondent should be included in service"),
                () -> assertTrue(validationService.isOrderServedViaStatementOfService(realisticOrder, sosList, realisticCaseData),
                    "Order should be served via statement of service")
            );
        }
    }
}
