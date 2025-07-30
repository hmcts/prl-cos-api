package uk.gov.hmcts.reform.prl.services.bais;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@ExtendWith(MockitoExtension.class)
@DisplayName("CSV Writer Tests")
class CsvWriterTest {

    private static final String[] EXPECTED_CSV_HEADERS = {
        "Case Number",
        "Court Name/Location",
        "Court ID",
        "Court Date DD/MM/YYYY",
        "Order Expiry Date DD/MM/YYYY",
        "Respondent Surname",
        "Respondent Forename(s)",
        "Respondent First Line of Address",
        "Respondent Second Line of Address",
        "Respondent Postcode",
        "Applicant Surname",
        "Applicant Forename(s)",
        "Applicant First Line of Address",
        "Applicant Second Line of Address",
        "Applicant Postcode",
        "PDF Identifier",
        "Is Confidential",
        "Force Code"
    };

    @Nested
    @DisplayName("CSV File Creation Tests")
    class CsvFileCreationTests {

        @Test
        @DisplayName("Should create a valid CSV file")
        void shouldCreateValidCsvFile() throws Exception {
            CaseData caseData = CaseData.builder().build();

            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);

            assertAll("CSV file validation",
                      () -> assertNotNull(csvFile, "CSV file should not be null"),
                      () -> assertTrue(csvFile.exists(), "CSV file should exist"),
                      () -> assertTrue(csvFile.getName().endsWith(".csv"), "File should have .csv extension")
            );
        }

        @Test
        @DisplayName("Should have correct file permissions")
        void shouldHaveCorrectFilePermissions() throws Exception {
            CaseData caseData = CaseData.builder().build();

            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(csvFile.toPath());

            assertAll("File permissions validation",
                      () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_READ),
                                       "Owner should have read permission"),
                      () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_WRITE),
                                       "Owner should have write permission"),
                      () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_EXECUTE),
                                       "Owner should have execute permission")
            );
        }
    }

    @Nested
    @DisplayName("CSV Header Tests")
    class CsvHeaderTests {

        @Test
        @DisplayName("Should contain all expected headers")
        void shouldContainAllExpectedHeaders() throws Exception {
            CaseData caseData = CaseData.builder().build();

            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
            String headerLine = readFirstLine(csvFile);

            assertNotNull(headerLine, "Header line should not be null");
            for (String expectedHeader : EXPECTED_CSV_HEADERS) {
                assertTrue(headerLine.contains(expectedHeader),
                           "Header line should contain: " + expectedHeader);
            }
        }

        @Test
        @DisplayName("Should have headers in correct order")
        void shouldHaveHeadersInCorrectOrder() throws Exception {
            CaseData caseData = CaseData.builder().build();

            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
            String headerLine = readFirstLine(csvFile);
            String[] actualHeaders = headerLine.split(",");

            assertArrayEquals(EXPECTED_CSV_HEADERS, actualHeaders,
                              "Headers should be in the expected order");
        }
    }

    @Nested
    @DisplayName("Nested Property Extraction Tests")
    class NestedPropertyExtractionTests {

        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("respondentPropertyTestCases")
        @DisplayName("Respondent property extraction")
        void shouldExtractRespondentProperties(String propertyName, Object expectedValue) {
            CaseData caseData = createCaseDataWithParties();

            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);

            assertAll("Property extraction validation",
                      () -> assertNotNull(actualValue,
                                          "Property value should not be null for: " + propertyName),
                      () -> assertEquals(expectedValue, actualValue,
                                         "Property value should match expected for: " + propertyName)
            );
        }

        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("applicantPropertyTestCases")
        @DisplayName("Applicant property extraction")
        void shouldExtractApplicantProperties(String propertyName, Object expectedValue) {
            CaseData caseData = createCaseDataWithParties();

            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);

            assertAll("Property extraction validation",
                      () -> assertNotNull(actualValue,
                                          "Property value should not be null for: " + propertyName),
                      () -> assertEquals(expectedValue, actualValue,
                                         "Property value should match expected for: " + propertyName)
            );
        }

        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("respondentWithNullValuesPropertyTestCases")
        @DisplayName("Respondent with null values property extraction")
        void shouldExtractRespondentPropertiesWhenNullValuesPresent(String propertyName, Object expectedValue) {
            CaseData caseData = createCaseDataWithPartiesNullValues();

            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);

            assertAll("Property extraction validation",
                      () -> assertNotNull(actualValue,
                                          "Property value should not be null for: " + propertyName),
                      () -> assertEquals(expectedValue, actualValue,
                                         "Property value should match expected for: " + propertyName)
            );
        }

        private static Stream<Arguments> respondentPropertyTestCases() {
            return Stream.of(
                arguments("respondents.lastName", "Doe"),
                arguments("respondents.firstName", "John"),
                arguments("respondents.dateOfBirth", LocalDate.parse("1994-07-05")),
                arguments("respondents.address.addressLine1", "70 Petty France"),
                arguments("respondents.address.addressLine2", "London"),
                arguments("respondents.address.postCode", "SW1H 9EX")
            );
        }

        private static Stream<Arguments> applicantPropertyTestCases() {
            return Stream.of(
                arguments("applicants.lastName", "Smith"),
                arguments("applicants.firstName", "Jane"),
                arguments("applicants.dateOfBirth", LocalDate.parse("1990-12-11")),
                arguments("applicants.address.addressLine1", "123 Example Street"),
                arguments("applicants.address.addressLine2", "London"),
                arguments("applicants.address.postCode", "E1 6AN")
            );
        }

        private static Stream<Arguments> respondentWithNullValuesPropertyTestCases() {
            return Stream.of(
                arguments("respondents.lastName", "Doe"),
                arguments("respondents.firstName", "John"),
                arguments("respondents.dateOfBirth", "-"),
                arguments("respondents.address.addressLine1", "70 Petty France"),
                arguments("respondents.address.addressLine2", "-"),
                arguments("respondents.address.postCode", "SW1H 9EX")
            );
        }
    }

    private static PartyDetails createPartyDetails(String firstName, String lastName,
                                                   String dateOfBirth, String addressLine1,
                                                   String addressLine2, String postCode) {
        Address address = Address.builder()
            .addressLine1(addressLine1)
            .addressLine2(addressLine2)
            .postCode(postCode)
            .build();

        return PartyDetails.builder()
            .firstName(firstName)
            .lastName(lastName)
            .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
            .address(address)
            .build();
    }

    private static CaseData createCaseDataWithParties() {
        PartyDetails respondent = createPartyDetails(
            "John", "Doe", "1994-07-05",
            "70 Petty France", "London", "SW1H 9EX"
        );

        PartyDetails applicant = createPartyDetails(
            "Jane", "Smith", "1990-12-11",
            "123 Example Street", "London", "E1 6AN"
        );

        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder()
            .value(respondent)
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder()
            .value(applicant)
            .build();

        return CaseData.builder()
            .respondents(List.of(wrappedRespondent))
            .applicants(List.of(wrappedApplicant))
            .build();
    }

    private static CaseData createCaseDataWithPartiesNullValues() {
        PartyDetails respondent = createPartyDetails(
            "John", "Doe", null,
            "70 Petty France", null, "SW1H 9EX"
        );

        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder()
            .value(respondent)
            .build();

        return CaseData.builder()
            .respondents(List.of(wrappedRespondent))
            .build();
    }

    private String readFirstLine(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }
}
