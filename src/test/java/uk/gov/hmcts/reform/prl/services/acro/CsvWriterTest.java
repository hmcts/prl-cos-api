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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private static final String TEST_PHONE = "1234567890";
    private static final String TEST_EMAIL = "test@test.com";
    private static final String APPLICANT_ADDRESS_LINE1 = "123 Example Street";
    private static final String APPLICANT_ADDRESS_LINE2 = "London";
    private static final String APPLICANT_POSTCODE = "E1 6AN";
    private static final String RESPONDENT_ADDRESS_LINE1 = "70 Petty France";
    private static final String RESPONDENT_ADDRESS_LINE2 = "Westminster";
    private static final String RESPONDENT_POSTCODE = "SW1H 9EX";
    private static final String BLANK_VALUE = "-";

    private CaseData createCaseData() {
        PartyDetails respondent = TestDataFactory.createPartyDetails("John", "Doe", "1994-07-05", "70 Petty France", "London", "SW1H 9EX", "", "");
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
            .finalCaseClosedDate("02-10-2026")
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
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, true);

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

        @Test
        @DisplayName("Should have correct file permissions")
        void shouldHaveCorrectFilePermissions() throws Exception {
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, true);
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(csvFile.toPath());
            assertAll("File permissions validation",
                () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_READ), "Owner should have read permission"),
                () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_WRITE), "Owner should have write permission"),
                () -> assertTrue(permissions.contains(PosixFilePermission.OWNER_EXECUTE), "Owner should have execute permission")
            );
        }

        @Test
        @DisplayName("Should create a valid CSV file with output verification")
        void shouldCreateValidCsvFileWithOutputVerification() throws Exception {
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, true);

            System.out.println("\nCSV Content:");

            assertAll("CSV file validation",
                () -> assertNotNull(csvFile, "CSV file should not be null"),
                () -> assertTrue(csvFile.exists(), "CSV file should exist"),
                () -> assertTrue(csvFile.getName().endsWith(".csv"), "File should have .csv extension")
            );
        }
    }

    @Nested
    @DisplayName("CSV Header Tests")
    class CsvHeaderTests {
        @Test
        @DisplayName("Should contain all expected headers")
        void shouldContainAllExpectedHeaders() throws Exception {
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, true);
            String headerLine = readFirstLine(csvFile);
            assertNotNull(headerLine, "Header line should not be null");
            for (String expectedHeader : EXPECTED_CSV_HEADERS) {
                assertTrue(headerLine.contains(expectedHeader), "Header line should contain: " + expectedHeader);
            }
        }

        @Test
        @DisplayName("Should have headers in correct order")
        void shouldHaveHeadersInCorrectOrder() throws Exception {
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, true);
            String headerLine = readFirstLine(csvFile);
            String[] actualHeaders = headerLine.split(",");
            assertArrayEquals(EXPECTED_CSV_HEADERS, actualHeaders, "Headers should be in the expected order");
        }
    }

    @Nested
    @DisplayName("Property Extraction Tests")
    class PropertyExtractionTests {
        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#propertyExtractionTestCases")
        @DisplayName("Should extract properties correctly")
        void shouldExtractPropertiesCorrectly(String propertyName, Object expectedValue, CaseData caseData) {
            Object actualValue = CsvWriter.extractPropertyValues(caseData, propertyName);

            assertAll("Property extraction validation",
                () -> assertNotNull(actualValue, "Property value should not be null for: " + propertyName),
                () -> assertEquals(expectedValue, actualValue, "Property value should match expected for: " + propertyName)
            );
        }

        @Test
        @DisplayName("Should handle null object gracefully")
        void shouldHandleNullObjectGracefully() {
            Object result = CsvWriter.extractPropertyValues(null, "someProperty");
            assertEquals("", result, "Should return empty string for null object");
        }

        @Test
        @DisplayName("Should handle invalid property paths gracefully")
        void shouldHandleInvalidPropertyPathsGracefully() {
            CaseData caseData = TestDataFactory.createStandardCaseData();

            assertAll("Invalid property path handling",
                () -> assertEquals("", CsvWriter.extractPropertyValues(caseData, null), "Should handle null property path"),
                () -> assertEquals("", CsvWriter.extractPropertyValues(caseData, ""), "Should handle empty property path"),
                () -> assertEquals("", CsvWriter.extractPropertyValues(caseData, "   "), "Should handle whitespace property path"),
                () -> assertEquals("", CsvWriter.extractPropertyValues(caseData, "nonExistentProperty"), "Should handle non-existent property")
            );
        }
    }

    @Nested
    @DisplayName("Confidential Data Handling Tests")
    class ConfidentialDataHandlingTests {

        @Test
        @DisplayName("Should include confidential data when allowed")
        void shouldIncludeConfidentialDataWhenAllowed() throws Exception {
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, true);
            String csvContent = Files.readString(csvFile.toPath());

            assertAll("Confidential data should be included",
                () -> assertTrue(csvContent.contains(TEST_PHONE), "Phone number should be present"),
                () -> assertTrue(csvContent.contains(TEST_EMAIL), "Email should be present"),
                () -> assertTrue(csvContent.contains(APPLICANT_ADDRESS_LINE1), "Address line 1 should be present"),
                () -> assertTrue(csvContent.contains(APPLICANT_POSTCODE), "Postcode should be present")
            );
        }

        @Test
        @DisplayName("Should blank confidential data when not allowed")
        void shouldBlankConfidentialDataWhenNotAllowed() throws Exception {
            CaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, false);
            String csvContent = Files.readString(csvFile.toPath());

            assertAll("Confidential data should be blanked",
                () -> assertTrue(csvContent.contains(BLANK_VALUE), "Should contain blanked values"),
                () -> assertFalse(csvContent.contains(TEST_PHONE), "Phone number should be blanked"),
                () -> assertFalse(csvContent.contains(TEST_EMAIL), "Email should be blanked")
            );
        }

        @ParameterizedTest(name = "Should handle {0} confidentiality correctly")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#confidentialityTestCases")
        @DisplayName("Should handle field confidentiality correctly")
        void shouldHandleFieldConfidentialityCorrectly(String fieldType, CaseData caseData, String expectedValue) throws Exception {
            File csvFileBlank = CsvWriter.writeCcdOrderDataToCsv(caseData, false);
            File csvFileInclude = CsvWriter.writeCcdOrderDataToCsv(caseData, true);

            String blankContent = Files.readString(csvFileBlank.toPath());
            String includeContent = Files.readString(csvFileInclude.toPath());

            assertAll("Confidentiality handling",
                () -> assertFalse(blankContent.contains(expectedValue),
                    fieldType + " should be blanked when confidential and not allowed"),
                () -> assertTrue(includeContent.contains(expectedValue),
                    fieldType + " should be included when confidential but allowed")
            );
        }

        @Test
        @DisplayName("Should not blank non-confidential columns")
        void shouldNotBlankNonConfidentialColumns() throws Exception {
            CaseData caseData = TestDataFactory.createMixedConfidentialityCaseData();
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, false);
            String csvContent = Files.readString(csvFile.toPath());

            assertAll("Non-confidential columns should not be blanked",
                () -> assertTrue(csvContent.contains("John"), "Respondent first name should not be blanked"),
                () -> assertTrue(csvContent.contains("Doe"), "Respondent last name should not be blanked"),
                () -> assertTrue(csvContent.contains("Jane"), "Applicant first name should not be blanked"),
                () -> assertTrue(csvContent.contains("Smith"), "Applicant last name should not be blanked")
            );
        }

        @ParameterizedTest(name = "Should correctly identify confidential flag: {0}")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#confidentialFlagTestCases")
        @DisplayName("Should handle different confidential flag types")
        void shouldHandleConfidentialFlagTypes(String flagType, YesOrNo flagValue, boolean expectedConfidential) throws Exception {
            CaseData caseData = TestDataFactory.createCaseDataWithPhoneConfidentiality(flagValue);
            File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData, false);
            String csvContent = Files.readString(csvFile.toPath());

            if (expectedConfidential) {
                assertFalse(csvContent.contains(TEST_PHONE),
                    "Phone should be blanked when " + flagType + " indicates confidential");
            } else {
                assertTrue(csvContent.contains(TEST_PHONE),
                    "Phone should be included when " + flagType + " indicates not confidential");
            }
        }
    }

    @Nested
    @DisplayName("Wrapped List Extraction Tests")
    class WrappedListExtractionTests {

        @ParameterizedTest(name = "Should extract {0} from wrapped list")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#wrappedListExtractionTestCases")
        @DisplayName("Should extract values from wrapped lists correctly")
        void shouldExtractValuesFromWrappedListsCorrectly(String description, CaseData caseData, String propertyPath, Object expectedValue) {
            Object result = CsvWriter.extractPropertyValues(caseData, propertyPath);
            assertEquals(expectedValue, result, "Should extract " + description + " from wrapped list");
        }

        @ParameterizedTest(name = "Should handle {0} gracefully")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#wrappedListEdgeCaseTestCases")
        @DisplayName("Should handle edge cases gracefully")
        void shouldHandleEdgeCasesGracefully(String description, CaseData caseData, String propertyPath, String expectedValue) {
            Object result = CsvWriter.extractPropertyValues(caseData, propertyPath);
            assertEquals(expectedValue, result, "Should handle " + description + " gracefully");
        }
    }

    static Stream<Arguments> propertyExtractionTestCases() {
        CaseData standardCase = TestDataFactory.createStandardCaseData();
        CaseData nullValuesCase = TestDataFactory.createCaseDataWithNullValues();

        return Stream.of(
            arguments("respondentsFL401.lastName", "Doe", standardCase),
            arguments("respondentsFL401.firstName", "John", standardCase),
            arguments("applicantsFL401.lastName", "Smith", standardCase),
            arguments("applicantsFL401.firstName", "Jane", standardCase),
            arguments("applicantsFL401.phoneNumber", TEST_PHONE, standardCase),
            arguments("applicantsFL401.email", TEST_EMAIL, standardCase),

            arguments("respondentsFL401.dateOfBirth", "", nullValuesCase),
            arguments("respondentsFL401.address.addressLine2", "", nullValuesCase)
        );
    }

    static Stream<Arguments> confidentialityTestCases() {
        return Stream.of(
            arguments("Applicant Phone", TestDataFactory.createCaseDataWithPhoneConfidentiality(YesOrNo.Yes), TEST_PHONE),
            arguments("Applicant Email", TestDataFactory.createCaseDataWithEmailConfidentiality(YesOrNo.Yes), TEST_EMAIL),
            arguments("Applicant Address", TestDataFactory.createCaseDataWithAddressConfidentiality(YesOrNo.Yes), APPLICANT_ADDRESS_LINE1),
            arguments("Respondent Address", TestDataFactory.createCaseDataWithRespondentAddressConfidentiality(YesOrNo.Yes), RESPONDENT_ADDRESS_LINE1)
        );
    }

    static Stream<Arguments> confidentialFlagTestCases() {
        return Stream.of(
            arguments("YesOrNo.Yes", YesOrNo.Yes, true),
            arguments("YesOrNo.No", YesOrNo.No, false),
            arguments("null value", null, false)
        );
    }

    static Stream<Arguments> wrappedListExtractionTestCases() {
        return Stream.of(
            arguments("firstName from basic wrapped list",
                TestDataFactory.createCaseDataWithWrappedList(),
                "respondents.firstName",
                "John"),
            arguments("nested address property from wrapped list",
                TestDataFactory.createCaseDataWithWrappedListAndAddress(),
                "applicants.address.addressLine1",
                "123 Test Street"),
            arguments("first element from multiple item list",
                TestDataFactory.createCaseDataWithMultipleWrappedElements(),
                "applicants.firstName",
                "First")
        );
    }

    static Stream<Arguments> wrappedListEdgeCaseTestCases() {
        return Stream.of(
            arguments("empty wrapped list",
                TestDataFactory.createCaseDataWithEmptyWrappedList(),
                "respondents.firstName",
                ""),
            arguments("null wrapped list",
                TestDataFactory.createCaseDataWithNullWrappedList(),
                "respondents.firstName",
                ""),
            arguments("null elements in list",
                TestDataFactory.createCaseDataWithNullElements(),
                "applicants.firstName",
                "")
        );
    }

    private String readFirstLine(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }

    static class TestDataFactory {

        static CaseData createStandardCaseData() {
            PartyDetails respondent = createPartyDetails("John", "Doe", "1994-07-05",
                RESPONDENT_ADDRESS_LINE1, "London", RESPONDENT_POSTCODE, "", "");
            PartyDetails applicant = PartyDetails.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.parse("1990-12-11"))
                .address(createAddress(APPLICANT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, APPLICANT_POSTCODE))
                .phoneNumber(TEST_PHONE)
                .email(TEST_EMAIL)
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
                .finalCaseClosedDate("02-10-2026")
                .respondentsFL401(respondent)
                .applicantsFL401(applicant)
                .build();
        }

        static CaseData createCaseDataWithNullValues() {
            PartyDetails respondent = createPartyDetails("John", "Doe", null,
                RESPONDENT_ADDRESS_LINE1, null, RESPONDENT_POSTCODE, "", "");
            return CaseData.builder().respondentsFL401(respondent).build();
        }

        static CaseData createMixedConfidentialityCaseData() {
            PartyDetails respondent = PartyDetails.builder()
                .firstName("John")
                .lastName("Doe")
                .address(createAddress(RESPONDENT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, RESPONDENT_POSTCODE))
                .isAddressConfidential(YesOrNo.No)
                .build();

            PartyDetails applicant = PartyDetails.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber(TEST_PHONE)
                .email(TEST_EMAIL)
                .address(createAddress(APPLICANT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, APPLICANT_POSTCODE))
                .isPhoneNumberConfidential(YesOrNo.Yes)
                .isEmailAddressConfidential(YesOrNo.No)
                .isAddressConfidential(YesOrNo.No)
                .build();

            return CaseData.builder()
                .respondentsFL401(respondent)
                .applicantsFL401(applicant)
                .build();
        }

        static CaseData createCaseDataWithPhoneConfidentiality(YesOrNo confidential) {
            PartyDetails applicant = PartyDetails.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phoneNumber(TEST_PHONE)
                .isPhoneNumberConfidential(confidential)
                .build();
            return CaseData.builder().applicantsFL401(applicant).build();
        }

        static CaseData createCaseDataWithEmailConfidentiality(YesOrNo confidential) {
            PartyDetails applicant = PartyDetails.builder()
                .firstName("Jane")
                .lastName("Smith")
                .email(TEST_EMAIL)
                .isEmailAddressConfidential(confidential)
                .build();
            return CaseData.builder().applicantsFL401(applicant).build();
        }

        static CaseData createCaseDataWithAddressConfidentiality(YesOrNo confidential) {
            PartyDetails applicant = PartyDetails.builder()
                .firstName("Jane")
                .lastName("Smith")
                .address(createAddress(APPLICANT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, APPLICANT_POSTCODE))
                .isAddressConfidential(confidential)
                .build();
            return CaseData.builder().applicantsFL401(applicant).build();
        }

        static CaseData createCaseDataWithRespondentAddressConfidentiality(YesOrNo confidential) {
            PartyDetails respondent = PartyDetails.builder()
                .firstName("John")
                .lastName("Doe")
                .address(createAddress(RESPONDENT_ADDRESS_LINE1, RESPONDENT_ADDRESS_LINE2, RESPONDENT_POSTCODE))
                .isAddressConfidential(confidential)
                .build();
            return CaseData.builder().respondentsFL401(respondent).build();
        }

        static CaseData createCaseDataWithWrappedList() {
            PartyDetails partyDetails = PartyDetails.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

            Element<PartyDetails> wrappedParty = Element.<PartyDetails>builder()
                .value(partyDetails)
                .build();

            return CaseData.builder()
                .respondents(List.of(wrappedParty))
                .build();
        }

        static CaseData createCaseDataWithWrappedListAndAddress() {
            PartyDetails partyDetails = PartyDetails.builder()
                .firstName("Jane")
                .lastName("Smith")
                .address(createAddress("123 Test Street", "Test City", "AB1 2CD"))
                .build();

            Element<PartyDetails> wrappedParty = Element.<PartyDetails>builder()
                .value(partyDetails)
                .build();

            return CaseData.builder()
                .applicants(List.of(wrappedParty))
                .build();
        }

        static CaseData createCaseDataWithEmptyWrappedList() {
            return CaseData.builder()
                .respondents(List.of())
                .build();
        }

        static CaseData createCaseDataWithNullWrappedList() {
            return CaseData.builder()
                .respondents(null)
                .build();
        }

        static CaseData createCaseDataWithNullElements() {
            List<Element<PartyDetails>> applicantsList = new ArrayList<>();
            applicantsList.add(null);
            return CaseData.builder()
                .applicants(applicantsList)
                .build();
        }

        static CaseData createCaseDataWithMultipleWrappedElements() {
            PartyDetails firstParty = PartyDetails.builder()
                .firstName("First")
                .lastName("Party")
                .build();

            PartyDetails secondParty = PartyDetails.builder()
                .firstName("Second")
                .lastName("Party")
                .build();

            Element<PartyDetails> firstElement = Element.<PartyDetails>builder()
                .value(firstParty)
                .build();

            Element<PartyDetails> secondElement = Element.<PartyDetails>builder()
                .value(secondParty)
                .build();

            return CaseData.builder()
                .applicants(List.of(firstElement, secondElement))
                .build();
        }

        private static PartyDetails createPartyDetails(String firstName, String lastName, String dateOfBirth,
                                                      String addressLine1, String addressLine2, String postCode,
                                                      String phoneNumber, String email) {
            return PartyDetails.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
                .address(createAddress(addressLine1, addressLine2, postCode))
                .phoneNumber(phoneNumber)
                .email(email)
                .build();
        }

        private static Address createAddress(String addressLine1, String addressLine2, String postCode) {
            return Address.builder()
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .postCode(postCode)
                .build();
        }
    }
}
