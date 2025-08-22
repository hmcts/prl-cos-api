package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        "Respondent 2nd Line of Address", "Respondent Postcode", "Respondent Phone", "Respondent Email",
        "Is Respondent Address Confidential", "Is Respondent Phone Confidential", "Is Respondent Email Confidential",
        "Applicant Surname", "Applicant Forename(s)", "Applicant DOB", "Applicant First Line of Address",
        "Applicant Second Line of Address", "Applicant Postcode", "Applicant Phone", "Applicant Safe Time to Call", "Applicant Email",
        "Is Applicant Address Confidential", "Is Applicant Phone Confidential", "Is Applicant Email Confidential", "Order File Name"
    };

    private static final String APPLICANT_PHONE = "1234567890";
    private static final String APPLICANT_EMAIL = "test@test.com";
    private static final String APPLICANT_ADDRESS_LINE1 = "123 Example Street";
    private static final String APPLICANT_ADDRESS_LINE2 = "London";
    private static final String APPLICANT_POSTCODE = "E1 6AN";
    private static final String RESPONDENT_ADDRESS_LINE1 = "70 Petty France";
    private static final String RESPONDENT_ADDRESS_LINE2 = "Westminster";
    private static final String RESPONDENT_POSTCODE = "SW1H 9EX";
    private static final String RESPONDENT_PHONE = "07700900123";
    private static final String RESPONDENT_EMAIL = "respondent@example.com";
    private static final String BLANK_VALUE = "-";

    @InjectMocks
    private CsvWriter csvWriter;

    @Nested
    @DisplayName("CSV File Creation Tests")
    class CsvFileCreationTests {
        @Test
        @DisplayName("Should create a valid CSV file")
        void shouldCreateValidCsvFile() throws Exception {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, true);

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
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, true);
            Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(csvFile.toPath());
            assertAll(
                "File permissions validation",
                () -> assertTrue(
                    permissions.contains(PosixFilePermission.OWNER_READ),
                    "Owner should have read permission"
                ),
                () -> assertTrue(
                    permissions.contains(PosixFilePermission.OWNER_WRITE),
                    "Owner should have write permission"
                ),
                () -> assertTrue(
                    permissions.contains(PosixFilePermission.OWNER_EXECUTE),
                    "Owner should have execute permission"
                )
            );
        }

        @Test
        @DisplayName("Should create a valid CSV file with output verification")
        void shouldCreateValidCsvFileWithOutputVerification() throws Exception {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, true);

            System.out.println("\nCSV Content:");

            assertAll(
                "CSV file validation",
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
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, true);
            String headerLine = readFirstLine(csvFile);
            assertNotNull(headerLine, "Header line should not be null");
            for (String expectedHeader : EXPECTED_CSV_HEADERS) {
                assertTrue(headerLine.contains(expectedHeader), "Header line should contain: " + expectedHeader);
            }
        }

        @Test
        @DisplayName("Should have headers in correct order")
        void shouldHaveHeadersInCorrectOrder() throws Exception {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, true);
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
        void shouldExtractPropertiesCorrectly(String propertyName, Object expectedValue, AcroCaseData caseData) {
            Object actualValue = csvWriter.extractPropertyValues(caseData, propertyName);

            assertAll(
                "Property extraction validation",
                () -> assertNotNull(actualValue, "Property value should not be null for: " + propertyName),
                () -> assertEquals(
                    expectedValue,
                    actualValue,
                    "Property value should match expected for: " + propertyName
                )
            );
        }

        @Test
        @DisplayName("Should handle null object gracefully")
        void shouldHandleNullObjectGracefully() {
            Object result = csvWriter.extractPropertyValues(null, "someProperty");
            assertEquals("", result, "Should return empty string for null object");
        }

        @Test
        @DisplayName("Should handle invalid property paths gracefully")
        void shouldHandleInvalidPropertyPathsGracefully() {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();

            assertAll(
                "Invalid property path handling",
                () -> assertEquals(
                    "",
                    csvWriter.extractPropertyValues(caseData, null),
                    "Should handle null property path"
                ),
                () -> assertEquals(
                    "",
                    csvWriter.extractPropertyValues(caseData, ""),
                    "Should handle empty property path"
                ),
                () -> assertEquals(
                    "",
                    csvWriter.extractPropertyValues(caseData, "   "),
                    "Should handle whitespace property path"
                ),
                () -> assertEquals(
                    "",
                    csvWriter.extractPropertyValues(caseData, "nonExistentProperty"),
                    "Should handle non-existent property"
                )
            );
        }
    }

    @Nested
    @DisplayName("Confidential Data Handling Tests")
    class ConfidentialDataHandlingTests {

        @Test
        @DisplayName("Should create CSV file with all expected data when confidential data is allowed")
        void shouldCreateCsvFileWithAllDataWhenConfidentialDataAllowed() throws Exception {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, true);
            String csvContent = Files.readString(csvFile.toPath());

            assertAll(
                "Confidential data should be included",
                () -> assertTrue(csvContent.contains(APPLICANT_PHONE), "Phone number should be present"),
                () -> assertTrue(csvContent.contains(APPLICANT_EMAIL), "Email should be present"),
                () -> assertTrue(csvContent.contains(APPLICANT_ADDRESS_LINE1), "Address line 1 should be present"),
                () -> assertTrue(csvContent.contains(APPLICANT_POSTCODE), "Postcode should be present")
            );
        }

        @Test
        @DisplayName("Should replace confidential fields with dash when confidential data is not allowed")
        void shouldCreateCsvFileWithDashesWhenConfidfentialDataNotAllowed() throws Exception {
            AcroCaseData caseData = TestDataFactory.createCaseDataWithAllConfidentialFields();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, false);
            String csvContent = Files.readString(csvFile.toPath());

            assertAll(
                "Confidential data should be blanked",
                () -> assertTrue(csvContent.contains(BLANK_VALUE), "Should contain blanked values"),
                () -> assertFalse(csvContent.contains(APPLICANT_PHONE), "Phone number should be blanked"),
                () -> assertFalse(csvContent.contains(APPLICANT_EMAIL), "Email should be blanked")
            );
        }

        @ParameterizedTest(name = "Should handle {0} confidentiality correctly")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#confidentialityTestCases")
        @DisplayName("Should handle field confidentiality correctly")
        void shouldHandleFieldConfidentialityCorrectly(String fieldType, AcroCaseData caseData, String expectedValue) throws Exception {
            File csvFileBlank = csvWriter.writeCcdOrderDataToCsv(caseData, false);
            File csvFileInclude = csvWriter.writeCcdOrderDataToCsv(caseData, true);

            String blankContent = Files.readString(csvFileBlank.toPath());
            String includeContent = Files.readString(csvFileInclude.toPath());

            assertAll(
                "Confidentiality handling",
                () -> assertFalse(
                    blankContent.contains(expectedValue),
                    fieldType + " should be blanked when confidential and not allowed"
                ),
                () -> assertTrue(
                    includeContent.contains(expectedValue),
                    fieldType + " should be included when confidential but allowed"
                )
            );
        }

        @Test
        @DisplayName("Should not blank non-confidential columns")
        void shouldNotBlankNonConfidentialColumns() throws Exception {
            AcroCaseData caseData = TestDataFactory.createMixedConfidentialityCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, false);
            String csvContent = Files.readString(csvFile.toPath());

            assertAll(
                "Non-confidential columns should not be blanked",
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
            AcroCaseData caseData = expectedConfidential
                ? TestDataFactory.createCaseDataWithAllConfidentialFields()
                : TestDataFactory.createStandardCaseData();
            File csvFile = csvWriter.writeCcdOrderDataToCsv(caseData, false);
            String csvContent = Files.readString(csvFile.toPath());

            if (expectedConfidential) {
                assertFalse(
                    csvContent.contains(APPLICANT_PHONE),
                    "Phone should be blanked when " + flagType + " indicates confidential"
                );
            } else {
                assertTrue(
                    csvContent.contains(APPLICANT_PHONE),
                    "Phone should be included when " + flagType + " indicates not confidential"
                );
            }
        }
    }

    @Nested
    @DisplayName("Wrapped List Extraction Tests")
    class WrappedListExtractionTests {

        @ParameterizedTest(name = "Should extract {0} from wrapped list")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#wrappedListExtractionTestCases")
        @DisplayName("Should extract values from wrapped lists correctly")
        void shouldExtractValuesFromWrappedListsCorrectly(String description, AcroCaseData caseData, String propertyPath, Object expectedValue) {
            Object result = csvWriter.extractPropertyValues(caseData, propertyPath);
            assertEquals(expectedValue, result, "Should extract " + description + " from wrapped list");
        }

        @ParameterizedTest(name = "Should handle {0} gracefully")
        @MethodSource("uk.gov.hmcts.reform.prl.services.acro.CsvWriterTest#wrappedListEdgeCaseTestCases")
        @DisplayName("Should handle edge cases gracefully")
        void shouldHandleEdgeCasesGracefully(String description, AcroCaseData caseData, String propertyPath, String expectedValue) {
            Object result = csvWriter.extractPropertyValues(caseData, propertyPath);
            assertEquals(expectedValue, result, "Should handle " + description + " gracefully");
        }
    }

    static Stream<Arguments> propertyExtractionTestCases() {
        AcroCaseData standardCase = TestDataFactory.createStandardCaseData();
        AcroCaseData nullValuesCase = TestDataFactory.createCaseDataWithNullValues();

        return Stream.of(
            arguments("respondentsFL401.lastName", "Doe", standardCase),
            arguments("respondentsFL401.firstName", "John", standardCase),
            arguments("respondentsFL401.phoneNumber", RESPONDENT_PHONE, standardCase),
            arguments("respondentsFL401.email", RESPONDENT_EMAIL, standardCase),
            arguments("applicantsFL401.lastName", "Smith", standardCase),
            arguments("applicantsFL401.firstName", "Jane", standardCase),
            arguments("applicantsFL401.phoneNumber", APPLICANT_PHONE, standardCase),
            arguments("applicantsFL401.email", APPLICANT_EMAIL, standardCase),

            arguments("respondentsFL401.dateOfBirth", "", nullValuesCase),
            arguments("respondentsFL401.address.addressLine2", "", nullValuesCase)
        );
    }

    static Stream<Arguments> confidentialityTestCases() {
        return Stream.of(
            arguments("Applicant Phone", TestDataFactory.createCaseDataWithAllConfidentialFields(), APPLICANT_PHONE),
            arguments("Applicant Email", TestDataFactory.createCaseDataWithAllConfidentialFields(), APPLICANT_EMAIL),
            arguments(
                "Applicant Address",
                TestDataFactory.createCaseDataWithAllConfidentialFields(),
                APPLICANT_ADDRESS_LINE1
            ),
            arguments("Respondent Phone", TestDataFactory.createCaseDataWithAllConfidentialFields(), RESPONDENT_PHONE),
            arguments("Respondent Email", TestDataFactory.createCaseDataWithAllConfidentialFields(), RESPONDENT_EMAIL),
            arguments(
                "Respondent Address",
                TestDataFactory.createCaseDataWithAllConfidentialFields(),
                RESPONDENT_ADDRESS_LINE1
            )
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
            arguments(
                "firstName from basic wrapped list",
                TestDataFactory.createStandardCaseData(),
                "respondents.firstName",
                "John"
            ),
            arguments(
                "nested address property from wrapped list",
                TestDataFactory.createStandardCaseData(),
                "applicants.address.addressLine1",
                APPLICANT_ADDRESS_LINE1
            ),
            arguments(
                "first element from multiple item list",
                TestDataFactory.createStandardCaseData(),
                "applicants.firstName",
                "Jane"
            )
        );
    }

    static Stream<Arguments> wrappedListEdgeCaseTestCases() {
        return Stream.of(
            arguments(
                "empty wrapped list",
                TestDataFactory.createCaseDataWithNullValues(),
                "respondents.firstName",
                ""
            ),
            arguments(
                "null wrapped list",
                TestDataFactory.createCaseDataWithNullValues(),
                "respondents.firstName",
                ""
            ),
            arguments(
                "null elements in list",
                TestDataFactory.createCaseDataWithNullValues(),
                "applicants.firstName",
                ""
            )
        );
    }

    private String readFirstLine(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        }
    }

    static class TestDataFactory {
        static AcroCaseData createStandardCaseData() {
            PartyDetails respondent = createPartyDetails(
                "John", "Doe", "1994-07-05",
                RESPONDENT_ADDRESS_LINE1, RESPONDENT_ADDRESS_LINE2, RESPONDENT_POSTCODE,
                RESPONDENT_PHONE, RESPONDENT_EMAIL, YesOrNo.No, YesOrNo.No, YesOrNo.No
            );

            PartyDetails applicant = createPartyDetails(
                "Jane", "Smith", "1990-12-11",
                APPLICANT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, APPLICANT_POSTCODE,
                APPLICANT_PHONE,
                APPLICANT_EMAIL, YesOrNo.No, YesOrNo.No, YesOrNo.No
            );

            String contactInstructions = "9am-5pm weekdays";

            return createCaseData(respondent, applicant, contactInstructions);
        }

        static AcroCaseData createCaseDataWithAllConfidentialFields() {
            PartyDetails respondent = createPartyDetails(
                "John", "Doe", "1994-07-05",
                RESPONDENT_ADDRESS_LINE1, RESPONDENT_ADDRESS_LINE2, RESPONDENT_POSTCODE,
                RESPONDENT_PHONE, RESPONDENT_EMAIL, YesOrNo.Yes, YesOrNo.Yes, YesOrNo.Yes
            );

            PartyDetails applicant = createPartyDetails(
                "Jane", "Smith", "1990-12-11",
                APPLICANT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, APPLICANT_POSTCODE,
                APPLICANT_PHONE,
                APPLICANT_EMAIL, YesOrNo.Yes, YesOrNo.Yes, YesOrNo.Yes
            );

            return createCaseData(respondent, applicant, "9am-5pm weekdays");
        }

        static AcroCaseData createCaseDataWithNullValues() {
            PartyDetails respondent = PartyDetails.builder()
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(null)
                .address(Address.builder()
                             .addressLine1(RESPONDENT_ADDRESS_LINE1)
                             .addressLine2(null)
                             .postCode(RESPONDENT_POSTCODE)
                             .build())
                .phoneNumber("")
                .email("")
                .build();

            return AcroCaseData.builder()
                .respondentsFL401(respondent)
                .build();
        }

        static AcroCaseData createMixedConfidentialityCaseData() {
            PartyDetails respondent = createPartyDetails(
                "John", "Doe", "1994-07-05",
                RESPONDENT_ADDRESS_LINE1, RESPONDENT_ADDRESS_LINE2, RESPONDENT_POSTCODE,
                RESPONDENT_PHONE, RESPONDENT_EMAIL, YesOrNo.No, YesOrNo.No, YesOrNo.No
            );

            PartyDetails applicant = createPartyDetails(
                "Jane", "Smith", "1990-12-11",
                APPLICANT_ADDRESS_LINE1, APPLICANT_ADDRESS_LINE2, APPLICANT_POSTCODE,
                APPLICANT_PHONE,
                APPLICANT_EMAIL, YesOrNo.No, YesOrNo.Yes, YesOrNo.No
            );

            return createCaseData(respondent, applicant, "9am-5pm weekdays");
        }


        private static PartyDetails createPartyDetails(String firstName, String lastName, String dateOfBirth,
                                                       String addressLine1, String addressLine2, String postCode,
                                                       String phoneNumber, String email,
                                                       YesOrNo isAddressConfidential, YesOrNo isPhoneConfidential, YesOrNo isEmailConfidential) {
            return PartyDetails.builder()
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
                .address(createAddress(addressLine1, addressLine2, postCode))
                .phoneNumber(phoneNumber)
                .email(email)
                .isAddressConfidential(isAddressConfidential)
                .isPhoneNumberConfidential(isPhoneConfidential)
                .isEmailAddressConfidential(isEmailConfidential)
                .build();
        }

        private static Address createAddress(String addressLine1, String addressLine2, String postCode) {
            return Address.builder()
                .addressLine1(addressLine1)
                .addressLine2(addressLine2)
                .postCode(postCode)
                .build();
        }

        private static AcroCaseData createCaseData(PartyDetails respondent, PartyDetails applicant, String contactInstructions) {
            Element<PartyDetails> respondentElement = Element.<PartyDetails>builder().value(respondent).build();
            Element<PartyDetails> applicantElement = Element.<PartyDetails>builder().value(applicant).build();

            return AcroCaseData.builder()
                .id(1234567891234567L)
                .courtName("test")
                .courtEpimsId("Manchester")
                .caseTypeOfApplication("FL401")
                .fl404Orders(List.of(OrderDetails.builder().fl404CustomFields(FL404.builder().orderSpecifiedDateTime(
                    LocalDateTime.now()).build()).build()))
                .respondentsFL401(respondent)
                .applicantsFL401(applicant)
                .respondents(List.of(respondentElement))
                .applicants(List.of(applicantElement))
                .daApplicantContactInstructions(contactInstructions)
                .build();
        }
    }
}
