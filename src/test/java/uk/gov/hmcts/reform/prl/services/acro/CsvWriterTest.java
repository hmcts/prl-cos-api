package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CSV Writer Tests")
class CsvWriterTest {

    private static final String ACRO_CONFIDENTIAL_DATA_FEATURE_FLAG = "acro-confidential-data-allowed";
    private static final String TEST_ORDER_FILENAME = "test-order.pdf";

    private static final String[] EXPECTED_CSV_HEADERS = {
        "Case No.", "Court Name/Location", "Court Code", "Order Name", "Court Date", "Order Expiry Date",
        "Respondent Surname", "Respondent Forename(s)", "Respondent DOB", "Respondent 1st Line of Address",
        "Respondent 2nd Line of Address", "Respondent Postcode", "Respondent Phone", "Respondent Email",
        "Is Respondent Address Confidential", "Is Respondent Phone Confidential", "Is Respondent Email Confidential",
        "Applicant Surname", "Applicant Forename(s)", "Applicant DOB", "Applicant 1st Line of Address",
        "Applicant 2nd Line of Address", "Applicant Postcode", "Applicant Phone", "Applicant Safe Time to Call", "Applicant Email",
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

    @InjectMocks
    private CsvWriter csvWriter;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @BeforeEach
    void setUp() throws IOException {
        ReflectionTestUtils.setField(csvWriter, "sourceDirectory", Files.createTempDirectory("csvwriter-test").toString());
    }

    @Nested
    @DisplayName("CSV File Creation Tests")
    class CsvFileCreationTests {

        @Test
        @DisplayName("Should create CSV file with headers")
        void shouldCreateCsvFileWithHeaders() throws IOException {
            File csvFile = csvWriter.createCsvFileWithHeaders();

            assertAll(
                "CSV file with headers validation",
                () -> assertNotNull(csvFile, "CSV file should not be null"),
                () -> assertTrue(csvFile.exists(), "CSV file should exist"),
                () -> assertTrue(csvFile.getName().startsWith("manifest-"), "File should start with 'manifest-'"),
                () -> assertTrue(csvFile.getName().endsWith(".csv"), "File should have .csv extension")
            );

            List<String> lines = Files.readAllLines(csvFile.toPath());
            assertEquals(1, lines.size(), "CSV should have only header line");

            String[] actualHeaders = lines.get(0).split(",");
            assertArrayEquals(EXPECTED_CSV_HEADERS, actualHeaders, "Headers should match expected format");
        }

        @Test
        @DisplayName("Should generate correct filename format")
        void shouldGenerateCorrectFilenameFormat() throws IOException {
            File csvFile = csvWriter.createCsvFileWithHeaders();

            String expectedDateFormat = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String expectedFilename = "manifest-" + expectedDateFormat + ".csv";

            assertEquals(expectedFilename, csvFile.getName(), "Filename should match expected format");
        }
    }

    @Nested
    @DisplayName("CSV Row Data Creation Tests")
    class CsvRowDataCreationTests {

        @BeforeEach
        void setUpLaunchDarkly() {
            when(launchDarklyClient.isFeatureEnabled(ACRO_CONFIDENTIAL_DATA_FEATURE_FLAG)).thenReturn(false);
        }

        @Test
        @DisplayName("Should create CSV row data with filename")
        void shouldCreateCsvRowDataWithFilename() {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            String filename = TEST_ORDER_FILENAME;

            List<String> rowData = csvWriter.createCsvRowData(caseData, filename);

            assertAll(
                "CSV row data validation",
                () -> assertNotNull(rowData, "Row data should not be null"),
                () -> assertEquals(EXPECTED_CSV_HEADERS.length, rowData.size(), "Row data should have correct number of columns"),
                () -> assertEquals(filename, rowData.get(rowData.size() - 1), "Last column should contain the filename")
            );
        }

        @Test
        @DisplayName("Should create CSV row data without filename")
        void shouldCreateCsvRowDataWithoutFilename() {
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();

            List<String> rowData = csvWriter.createCsvRowData(caseData, null);

            assertAll(
                "CSV row data validation",
                () -> assertNotNull(rowData, "Row data should not be null"),
                () -> assertEquals(EXPECTED_CSV_HEADERS.length, rowData.size(), "Row data should have correct number of columns"),
                () -> assertEquals("", rowData.get(rowData.size() - 1), "Last column should be empty when no filename provided")
            );
        }

        @ParameterizedTest(name = "Should handle {0} confidentiality correctly")
        @MethodSource("confidentialityTestCases")
        @DisplayName("Should handle field confidentiality correctly")
        void shouldHandleFieldConfidentialityCorrectly(String fieldType, AcroCaseData caseData, String expectedValue) {
            // Test with feature flag disabled (confidential data should be masked)
            when(launchDarklyClient.isFeatureEnabled(ACRO_CONFIDENTIAL_DATA_FEATURE_FLAG)).thenReturn(false);
            List<String> maskedRowData = csvWriter.createCsvRowData(caseData, TEST_ORDER_FILENAME);
            String maskedContent = String.join(",", maskedRowData);

            // Test with feature flag enabled (confidential data should be included)
            when(launchDarklyClient.isFeatureEnabled(ACRO_CONFIDENTIAL_DATA_FEATURE_FLAG)).thenReturn(true);
            List<String> unmaskedRowData = csvWriter.createCsvRowData(caseData, TEST_ORDER_FILENAME);
            String unmaskedContent = String.join(",", unmaskedRowData);

            assertAll(
                "Confidentiality handling for " + fieldType,
                () -> assertFalse(maskedContent.contains(expectedValue),
                    fieldType + " should be masked when feature flag is disabled"),
                () -> assertTrue(unmaskedContent.contains(expectedValue),
                    fieldType + " should be included when feature flag is enabled"),
                () -> assertTrue(maskedContent.contains("-"),
                    "Masked content should contain '-' replacement")
            );
        }

        static Stream<Arguments> confidentialityTestCases() {
            return Stream.of(
                arguments("Applicant Phone", TestDataFactory.createCaseDataWithAllConfidentialFields(), APPLICANT_PHONE),
                arguments("Applicant Email", TestDataFactory.createCaseDataWithAllConfidentialFields(), APPLICANT_EMAIL),
                arguments("Respondent Phone", TestDataFactory.createCaseDataWithAllConfidentialFields(), RESPONDENT_PHONE),
                arguments("Respondent Email", TestDataFactory.createCaseDataWithAllConfidentialFields(), RESPONDENT_EMAIL)
            );
        }
    }

    @Nested
    @DisplayName("CSV File Writing Tests")
    class CsvFileWritingTests {

        @BeforeEach
        void setUpLaunchDarkly() {
            when(launchDarklyClient.isFeatureEnabled(ACRO_CONFIDENTIAL_DATA_FEATURE_FLAG)).thenReturn(false);
        }

        @Test
        @DisplayName("Should append single CSV row to file")
        void shouldAppendSingleCsvRowToFile() throws IOException {
            File csvFile = csvWriter.createCsvFileWithHeaders();
            AcroCaseData caseData = TestDataFactory.createStandardCaseData();
            String filename = TEST_ORDER_FILENAME;

            csvWriter.appendCsvRowToFile(csvFile, caseData, filename);

            List<String> lines = Files.readAllLines(csvFile.toPath());

            assertAll(
                "Single row append validation",
                () -> assertEquals(2, lines.size(), "Should have header + 1 data row"),
                () -> assertTrue(lines.get(1).contains("John"), "Should contain respondent first name"),
                () -> assertTrue(lines.get(1).contains("Jane"), "Should contain applicant first name"),
                () -> assertTrue(lines.get(1).contains(filename), "Should contain the filename")
            );
        }

        @Test
        @DisplayName("Should append multiple CSV rows to file")
        void shouldAppendMultipleCsvRowsToFile() throws IOException {
            File csvFile = csvWriter.createCsvFileWithHeaders();

            List<AcroCaseData> cases = List.of(
                TestDataFactory.createStandardCaseData(),
                TestDataFactory.createCaseDataWithAllConfidentialFields()
            );

            for (int i = 0; i < cases.size(); i++) {
                csvWriter.appendCsvRowToFile(csvFile, cases.get(i), "order-" + (i + 1) + ".pdf");
            }

            List<String> lines = Files.readAllLines(csvFile.toPath());
            assertAll(
                "Multiple rows append validation",
                () -> assertEquals(3, lines.size(), "Should have header + 2 data rows"),
                () -> assertTrue(lines.get(1).contains("order-1.pdf"), "First row should contain first filename"),
                () -> assertTrue(lines.get(2).contains("order-2.pdf"), "Second row should contain second filename")
            );
        }
    }

    @Nested
    @DisplayName("Property Extraction Tests")
    class PropertyExtractionTests {

        @ParameterizedTest(name = "Should extract {0} with value: {1}")
        @MethodSource("propertyExtractionTestCases")
        @DisplayName("Should extract properties correctly")
        void shouldExtractPropertiesCorrectly(String propertyName, Object expectedValue, AcroCaseData caseData) {
            Object actualValue = csvWriter.extractPropertyValues(caseData, propertyName);

            assertEquals(expectedValue, actualValue, "Property value should match expected for: " + propertyName);
        }

        @Test
        @DisplayName("Should handle null inputs gracefully")
        void shouldHandleNullInputsGracefully() {
            assertAll(
                "Null input handling",
                () -> assertEquals("", csvWriter.extractPropertyValues(null,
                        "someProperty"), "Should handle null object"),
                () -> assertEquals("", csvWriter.extractPropertyValues(TestDataFactory.createStandardCaseData(),
                        null), "Should handle null property path"),
                () -> assertEquals("", csvWriter.extractPropertyValues(TestDataFactory.createStandardCaseData(),
                        ""), "Should handle empty property path")
            );
        }

        static Stream<Arguments> propertyExtractionTestCases() {
            AcroCaseData standardCase = TestDataFactory.createStandardCaseData();

            return Stream.of(
                arguments("respondent.lastName", "Doe", standardCase),
                arguments("respondent.firstName", "John", standardCase),
                arguments("applicant.lastName", "Smith", standardCase),
                arguments("applicant.firstName", "Jane", standardCase),
                arguments("applicant.phoneNumber", APPLICANT_PHONE, standardCase),
                arguments("applicant.email", APPLICANT_EMAIL, standardCase)
            );
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
                APPLICANT_PHONE, APPLICANT_EMAIL, YesOrNo.No, YesOrNo.No, YesOrNo.No
            );

            return createCaseData(respondent, applicant, "9am-5pm weekdays");
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
                APPLICANT_PHONE, APPLICANT_EMAIL, YesOrNo.Yes, YesOrNo.Yes, YesOrNo.Yes
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
                .address(Address.builder()
                    .addressLine1(addressLine1)
                    .addressLine2(addressLine2)
                    .postCode(postCode)
                    .build())
                .phoneNumber(phoneNumber)
                .email(email)
                .isAddressConfidential(isAddressConfidential)
                .isPhoneNumberConfidential(isPhoneConfidential)
                .isEmailAddressConfidential(isEmailConfidential)
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
                .respondent(respondent)
                .applicant(applicant)
                .respondents(List.of(respondentElement))
                .applicants(List.of(applicantElement))
                .daApplicantContactInstructions(contactInstructions)
                .build();
        }
    }
}
