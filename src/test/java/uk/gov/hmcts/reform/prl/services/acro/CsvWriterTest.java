package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
        "Case No.", "Court Name/Location", "Court Code", "Order Name", "Court Date DD/MM/YYYY", "Order Expiry Date",
        "Respondent Surname", "Respondent Forename(s)", "Respondent DOB", "Respondent 1st Line of Address",
        "Respondent 2nd Line of Address", "Respondent Postcode", "Applicant Surname", "Applicant Forename(s)",
        "Applicant DOB", "Applicant First Line of Address", "Applicant Second Line of Address", "Applicant Postcode",
        "Applicant Phone", "Applicant Email", "Is Applicant Address Confidential", "Is Applicant Email Confidential",
        "Is Applicant Phone Confidential", "Order File Name"
    };

    private CaseData createCaseData() {
        PartyDetails respondent = createPartyDetails("John", "Doe", "1994-07-05", "70 Petty France", "London", "SW1H 9EX", "", "");
        PartyDetails applicant = PartyDetails.builder()
            .firstName("Jane")
            .lastName("Smith")
            .dateOfBirth(LocalDate.parse("1990-12-11"))
            .address(Address.builder().addressLine1("123 Example Street").addressLine2("London").postCode("E1 6AN").build())
            .phoneNumber("1234567890")
            .email("test@test.com")
            .isAddressConfidential(YesOrNo.No)
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .build();
        return CaseData.builder()
            .id(1234567891234567L)
            .courtName("test")
            .courtId("Manchester")
            .caseTypeOfApplication("FL401")
            .dateOrderMade(LocalDate.now())
            .finalCaseClosedDate("02-10-2026") //orderExpiryDate(LocalDate.now().plusYears(1))
            .respondentsFL401(respondent)
            .applicantsFL401(applicant)
            .build();
    }

    @Nested
    @DisplayName("CSV File Creation Tests")
    class CsvFileCreationTests {
        @Test
        @DisplayName("Should create a valid CSV file")
        void shouldCreateValidCsvFile() throws Exception {
            CaseData caseData = createCaseData();
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
            CaseData caseData = createCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(csvFile.toPath());
            assertAll("File permissions validation",
                () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_READ), "Owner should have read permission"),
                () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_WRITE), "Owner should have write permission"),
                () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_EXECUTE), "Owner should have execute permission")
            );
        }
    }

    @Nested
    @DisplayName("CSV Header Tests")
    class CsvHeaderTests {
        @Test
        @DisplayName("Should contain all expected headers")
        void shouldContainAllExpectedHeaders() throws Exception {
            CaseData caseData = createCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
            String headerLine = readFirstLine(csvFile);
            assertNotNull(headerLine, "Header line should not be null");
            for (String expectedHeader : EXPECTED_CSV_HEADERS) {
                assertTrue(headerLine.contains(expectedHeader), "Header line should contain: " + expectedHeader);
            }
        }

        @Test
        @DisplayName("Should have headers in correct order")
        void shouldHaveHeadersInCorrectOrder() throws Exception {
            CaseData caseData = createCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
            String headerLine = readFirstLine(csvFile);
            String[] actualHeaders = headerLine.split(",");
            assertArrayEquals(EXPECTED_CSV_HEADERS, actualHeaders, "Headers should be in the expected order");
        }
    }

    @Nested
    @DisplayName("Nested Property Extraction Tests")
    class NestedPropertyExtractionTests {
        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("respondentPropertyTestCases")
        @DisplayName("Respondent property extraction")
        void shouldExtractRespondentProperties(String propertyName, Object expectedValue) {
            CaseData caseData = createCaseData();
            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);
            assertAll("Property extraction validation",
                () -> assertNotNull(actualValue, "Property value should not be null for: " + propertyName),
                () -> assertEquals(expectedValue, actualValue, "Property value should match expected for: " + propertyName)
            );
        }

        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("applicantPropertyTestCases")
        @DisplayName("Applicant property extraction")
        void shouldExtractApplicantProperties(String propertyName, Object expectedValue) {
            CaseData caseData = createCaseDataWithParties();
            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);
            assertAll("Property extraction validation",
                () -> assertNotNull(actualValue, "Property value should not be null for: " + propertyName),
                () -> assertEquals(expectedValue, actualValue, "Property value should match expected for: " + propertyName)
            );
        }

        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("respondentWithNullValuesPropertyTestCases")
        @DisplayName("Respondent with null values property extraction")
        void shouldExtractRespondentPropertiesWhenNullValuesPresent(String propertyName, Object expectedValue) {
            CaseData caseData = createCaseDataWithPartiesNullValues();
            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);
            assertAll("Property extraction validation",
                () -> assertNotNull(actualValue, "Property value should not be null for: " + propertyName),
                () -> assertEquals(expectedValue, actualValue, "Property value should match expected for: " + propertyName)
            );
        }

        private static Stream<Arguments> respondentPropertyTestCases() {
            return Stream.of(
                arguments("respondentsFL401.lastName", "Doe"),
                arguments("respondentsFL401.firstName", "John"),
                arguments("respondentsFL401.dateOfBirth", LocalDate.parse("1994-07-05")),
                arguments("respondentsFL401.address.addressLine1", "70 Petty France"),
                arguments("respondentsFL401.address.addressLine2", "London"),
                arguments("respondentsFL401.address.postCode", "SW1H 9EX")
            );
        }

        private static Stream<Arguments> applicantPropertyTestCases() {
            return Stream.of(
                arguments("applicantsFL401.lastName", "Smith"),
                arguments("applicantsFL401.firstName", "Jane"),
                arguments("applicantsFL401.dateOfBirth", LocalDate.parse("1990-12-11")),
                arguments("applicantsFL401.address.addressLine1", "123 Example Street"),
                arguments("applicantsFL401.address.addressLine2", "London"),
                arguments("applicantsFL401.address.postCode", "E1 6AN"),
                arguments("applicantsFL401.phoneNumber", "1234567890"),
                arguments("applicantsFL401.email", "test@test.com")
            );
        }

        private static Stream<Arguments> applicantConfidentialPropertyTestCases() {
            return Stream.of(
                arguments("applicantsFL401.isAddressConfidential", false),
                arguments("applicantsFL401.isEmailAddressConfidential", true),
                arguments("applicantsFL401.isPhoneNumberConfidential", true)
            );
        }

        private static Stream<Arguments> respondentWithNullValuesPropertyTestCases() {
            return Stream.of(
                arguments("respondentsFL401.lastName", "Doe"),
                arguments("respondentsFL401.firstName", "John"),
                arguments("respondentsFL401.dateOfBirth", ""),
                arguments("respondentsFL401.address.addressLine1", "70 Petty France"),
                arguments("respondentsFL401.address.addressLine2", ""),
                arguments("respondentsFL401.address.postCode", "SW1H 9EX")
            );
        }
    }

    private static PartyDetails createPartyDetails(String firstName, String lastName, String dateOfBirth,
                                                   String addressLine1, String addressLine2, String postCode,
                                                   String phoneNumber, String email) {
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
            .phoneNumber(phoneNumber)
            .email(email)
            .build();
    }

    private static CaseData createCaseDataWithParties() {
        PartyDetails respondent = createPartyDetails("John", "Doe", "1994-07-05",
                                                     "70 Petty France", "London", "SW1H 9EX",
                                                     "", "");
        PartyDetails applicant = createPartyDetails("Jane", "Smith", "1990-12-11",
                                                    "123 Example Street", "London", "E1 6AN",
                                                    "1234567890", "test@test.com");
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        return CaseData.builder()
            .respondentsFL401(respondent)
            .applicantsFL401(applicant)
            .build();
    }

    private static CaseData createCaseDataWithPartiesNullValues() {
        PartyDetails respondent = createPartyDetails("John", "Doe", null, "70 Petty France", null, "SW1H 9EX", "", "");
        Element<PartyDetails> wrappedRespondent = Element.<PartyDetails>builder().value(respondent).build();
        return CaseData.builder()
            .respondentsFL401(respondent)
            .build();
    }

    private String readFirstLine(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }

    @Test
    @DisplayName("Should create a valid CSV file with saved output")
    void shouldCreateValidCsvFile() throws Exception {
        CaseData caseData = createCaseData();
        File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);

        // Quick save for colleague review
        // File savedCsv = new File("test-output.csv");
        // Files.copy(csvFile.toPath(), savedCsv.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // System.out.println("CSV saved to: " + savedCsv.getAbsolutePath());
        System.out.println("\nCSV Content:");
        // Files.lines(savedCsv.toPath()).forEach(System.out::println);

        // Your existing assertions
        assertNotNull(csvFile);
        assertTrue(csvFile.exists());
        assertTrue(csvFile.getName().endsWith(".csv"));
    }
}
