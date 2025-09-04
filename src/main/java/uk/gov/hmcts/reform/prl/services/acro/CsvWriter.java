package uk.gov.hmcts.reform.prl.services.acro;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class CsvWriter {

    private static final FileAttribute<Set<PosixFilePermission>> ATTRIBUTE = PosixFilePermissions
        .asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    @Value("${acro.source-directory}")
    private String sourceDirectory;

    public enum CsvColumn {
        CASE_NUMBER("Case No.", "id"),
        COURT_NAME("Court Name/Location", "courtName"),
        COURT_ID("Court Code", "courtEpimsId"),
        ORDER_NAME("Order Name", "caseTypeOfApplication"),
        COURT_DATE("Court Date", "dateOrderMade"),
        ORDER_EXPIRY_DATE("Order Expiry Date", "finalCaseClosedDate"), // Assuming this is a date field
        RESPONDENT_SURNAME("Respondent Surname", "respondentsFL401.lastName"),
        RESPONDENT_FORENAMES("Respondent Forename(s)", "respondentsFL401.firstName"),
        RESPONDENT_DOB("Respondent DOB", "respondentsFL401.dateOfBirth"),
        RESPONDENT_ADDRESS1("Respondent 1st Line of Address", "respondentsFL401.address.addressLine1"),
        RESPONDENT_ADDRESS2("Respondent 2nd Line of Address", "respondentsFL401.address.addressLine2"),
        RESPONDENT_POSTCODE("Respondent Postcode", "respondentsFL401.address.postCode"),
        RESPONDENT_PHONE("Respondent Phone", "respondentsFL401.phoneNumber"),
        RESPONDENT_EMAIL("Respondent Email", "respondentsFL401.email"),
        RESPONDENT_ADDRESS_CONFIDENTIAL("Is Respondent Address Confidential", "respondentsFL401.isAddressConfidential"),
        RESPONDENT_PHONE_CONFIDENTIAL("Is Respondent Phone Confidential", "respondentsFL401.isPhoneNumberConfidential"),
        RESPONDENT_EMAIL_CONFIDENTIAL(
            "Is Respondent Email Confidential",
            "respondentsFL401.isEmailAddressConfidential"
        ),
        APPLICANT_SURNAME("Applicant Surname", "applicantsFL401.lastName"),
        APPLICANT_FORENAMES("Applicant Forename(s)", "applicantsFL401.firstName"),
        APPLICANT_DOB("Applicant DOB", "applicantsFL401.dateOfBirth"),
        APPLICANT_ADDRESS1("Applicant 1st Line of Address", "applicantsFL401.address.addressLine1"),
        APPLICANT_ADDRESS2("Applicant 2nd Line of Address", "applicantsFL401.address.addressLine2"),
        APPLICANT_POSTCODE("Applicant Postcode", "applicantsFL401.address.postCode"),
        APPLICANT_PHONE("Applicant Phone", "applicantsFL401.phoneNumber"),
        APPLICANT_SAFE_TIME_TO_CALL("Applicant Safe Time to Call", "daApplicantContactInstructions"),
        APPLICANT_EMAIL("Applicant Email", "applicantsFL401.email"),
        APPLICANT_ADDRESS_CONFIDENTIAL("Is Applicant Address Confidential", "applicantsFL401.isAddressConfidential"),
        APPLICANT_PHONE_CONFIDENTIAL("Is Applicant Phone Confidential", "applicantsFL401.isPhoneNumberConfidential"),
        APPLICANT_EMAIL_CONFIDENTIAL("Is Applicant Email Confidential", "applicantsFL401.isEmailAddressConfidential"),
        PDF_IDENTIFIER("Order File Name", "PdfIdentifier");

        private final String header;
        private final String property;

        CsvColumn(String header, String property) {
            this.header = header;
            this.property = property;
        }

        public String getHeader() {
            return header;
        }

        public String getProperty() {
            return property;
        }
    }

    public static final CsvColumn[] COLUMNS = CsvColumn.values();

    /**
     * Creates a new CSV file with headers in the configured output directory.
     * This should be called first before appending data rows.
     *
     * @return the created CSV file with headers
     * @throws IOException if file creation fails
     */
    public File createCsvFileWithHeaders() throws IOException {
        File csvFile = createCsvFile();
        String[] headers = Arrays.stream(COLUMNS).map(CsvColumn::getHeader).toArray(String[]::new);
        CSVFormat csvFileHeader = CSVFormat.DEFAULT.builder().setHeader(headers).build();

        try (FileWriter fileWriter = new FileWriter(csvFile);
             CSVPrinter ignored = new CSVPrinter(fileWriter, csvFileHeader)) {
            log.info("Created CSV file with headers: {}", csvFile.getAbsolutePath());
        }
        return csvFile;
    }

    /**
     * Creates CSV row data for a single case.
     * Returns a list of strings that can be written to a CSV file by another service.
     *
     * @param ccdOrderData        the case data to convert to CSV row
     * @param confidentialAllowed toggle to include confidential data or replace with "-"
     * @param orderFilename       the filename to be added to the Order File Name column
     * @return list of strings representing the CSV row data
     */
    public List<String> createCsvRowData(AcroCaseData ccdOrderData, boolean confidentialAllowed, String orderFilename) {
        List<String> record = new ArrayList<>();
        for (CsvColumn column : COLUMNS) {
            Object value;

            if (column == CsvColumn.PDF_IDENTIFIER) {
                value = orderFilename;
            } else {
                value = extractPropertyValues(ccdOrderData, column.getProperty());
            }

            if (!confidentialAllowed && isConfidentialField(ccdOrderData, column)) {
                value = "-";
            }

            if (value == null || value.toString().isEmpty()) {
                log.warn("Missing value for CSV column '{}' (property '{}')", column.getHeader(), column.getProperty());
            }
            record.add(value != null ? value.toString() : "");
        }
        return record;
    }

    /**
     * Appends a CSV row to an existing CSV file.
     * Uses the createCsvRowData method to generate the row data and writes it to the file.
     *
     * @param csvFile             the CSV file to append to
     * @param caseData            the case data to convert to CSV row
     * @param confidentialAllowed toggle to include confidential data or replace with "-"
     * @param filename            the filename to be added to the Order File Name column
     * @throws IOException if writing to the file fails
     */
    public void appendCsvRowToFile(File csvFile, AcroCaseData caseData, boolean confidentialAllowed, String filename) throws IOException {
        List<String> rowData = createCsvRowData(caseData, confidentialAllowed, filename);

        CSVFormat csvFormat = CSVFormat.DEFAULT;
        try (FileWriter fileWriter = new FileWriter(csvFile, true);
             CSVPrinter printer = new CSVPrinter(fileWriter, csvFormat)) {
            printer.printRecord(rowData);
        }
    }

    /**
     * Creates a CSV file in the configured output directory with a date-stamped filename.
     *
     * @return the created CSV file
     * @throws IOException if directory creation or file creation fails
     */
    private File createCsvFile() throws IOException {
        Path outputPath = Paths.get(sourceDirectory);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        return new File(sourceDirectory, generateCsvFilename());
    }

    private String generateCsvFilename() {
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = currentDate.format(formatter);

        return "manifest-" + formattedDate + ".csv";
    }

    private boolean isConfidentialField(AcroCaseData caseData, CsvColumn column) {
        return switch (column) {
            case APPLICANT_PHONE -> isConfidential(caseData, "applicantsFL401.isPhoneNumberConfidential");
            case APPLICANT_EMAIL -> isConfidential(caseData, "applicantsFL401.isEmailAddressConfidential");
            case APPLICANT_SAFE_TIME_TO_CALL -> true; // Always blank when confidentialAllowed is false
            case APPLICANT_ADDRESS1, APPLICANT_ADDRESS2, APPLICANT_POSTCODE ->
                isConfidential(caseData, "applicantsFL401.isAddressConfidential");

            case RESPONDENT_PHONE -> isConfidential(caseData, "respondentsFL401.isPhoneNumberConfidential");
            case RESPONDENT_EMAIL -> isConfidential(caseData, "respondentsFL401.isEmailAddressConfidential");
            case RESPONDENT_ADDRESS1, RESPONDENT_ADDRESS2, RESPONDENT_POSTCODE ->
                isConfidential(caseData, "respondentsFL401.isAddressConfidential");
            default -> false;
        };
    }

    private boolean isConfidential(AcroCaseData caseData, String confidentialityProperty) {
        Object value = extractPropertyValues(caseData, confidentialityProperty);
        if (value instanceof YesOrNo) {
            return YesOrNo.Yes.equals(value);
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "Yes".equalsIgnoreCase(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    public Object extractPropertyValues(Object obj, String propertyPath) {
        if (obj == null || propertyPath == null || propertyPath.trim().isEmpty()) {
            return "";
        }

        String[] properties = propertyPath.split("\\.");
        Object currentValue = obj;

        for (String property : properties) {
            currentValue = getPropertyValue(currentValue, property);
            if (isEmpty(currentValue)) {
                return "";
            }
        }

        return currentValue;
    }

    private Object getPropertyValue(Object obj, String propertyName) {
        if (obj == null) {
            return "";
        }
        try {
            Method getter = createGetter(obj.getClass(), propertyName);
            Object value = getter.invoke(obj);

            return isWrappedListProperty(propertyName)
                ? extractFromWrappedList(value) : value;

        } catch (Exception e) {
            return "";
        }
    }

    private Method createGetter(Class<?> clazz, String propertyName) throws NoSuchMethodException {
        String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return clazz.getMethod(getterName);
    }

    private boolean isWrappedListProperty(String propertyName) {
        Set<String> wrappedListProperties = Set.of("respondents", "applicants");
        return wrappedListProperties.contains(propertyName.toLowerCase());
    }

    private Object extractFromWrappedList(Object value) {
        if (!(value instanceof List)) {
            return value;
        }

        List<?> list = (List<?>) value;
        if (list.isEmpty()) {
            return "";
        }

        try {
            Object element = list.get(0);
            java.lang.reflect.Method valueGetter = element.getClass().getMethod("getValue");
            return valueGetter.invoke(element);
        } catch (Exception e) {
            return "";
        }
    }

    private boolean isEmpty(Object value) {
        return value == null || "".equals(value);
    }
}
