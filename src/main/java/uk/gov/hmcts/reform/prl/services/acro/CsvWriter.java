package uk.gov.hmcts.reform.prl.services.acro;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CsvWriter {
    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    private static final FileAttribute<Set<PosixFilePermission>> ATTRIBUTE = PosixFilePermissions
        .asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    public enum CsvColumn {
        CASE_NUMBER("Case No.", "id"),
        COURT_NAME("Court Name/Location", "CourtName"),
        COURT_ID("Court Code", "CourtId"),
        ORDER_NAME("Order Name", "caseTypeOfApplication"),
        COURT_DATE("Court Date DD/MM/YYYY", "dateOrderMade"),
        ORDER_EXPIRY_DATE("Order Expiry Date", "finalCaseClosedDate"), // Assuming this is a date field
        RESPONDENT_SURNAME("Respondent Surname", "respondentsFL401.lastName"),
        RESPONDENT_FORENAMES("Respondent Forename(s)", "respondentsFL401.firstName"),
        RESPONDENT_DOB("Respondent DOB", "respondentsFL401.dateOfBirth"),
        RESPONDENT_ADDRESS1("Respondent 1st Line of Address", "respondentsFL401.address.addressLine1"),
        RESPONDENT_ADDRESS2("Respondent 2nd Line of Address", "respondentsFL401.address.addressLine2"),
        RESPONDENT_POSTCODE("Respondent Postcode", "respondentsFL401.address.postCode"),
        APPLICANT_SURNAME("Applicant Surname", "applicantsFL401.lastName"),
        APPLICANT_FORENAMES("Applicant Forename(s)", "applicantsFL401.firstName"),
        APPLICANT_DOB("Applicant DOB", "applicantsFL401.dateOfBirth"),
        APPLICANT_ADDRESS1("Applicant First Line of Address", "applicantsFL401.address.addressLine1"),
        APPLICANT_ADDRESS2("Applicant Second Line of Address", "applicantsFL401.address.addressLine2"),
        APPLICANT_POSTCODE("Applicant Postcode", "applicantsFL401.address.postCode"),
        APPLICANT_PHONE("Applicant Phone", "applicantsFL401.phoneNumber"),
        APPLICANT_EMAIL("Applicant Email", "applicantsFL401.email"),
        APPLICANT_ADDRESS_CONFIDENTIAL("Is Applicant Address Confidential", "applicantsFL401.isAddressConfidential"),
        APPLICANT_EMAIL_CONFIDENTIAL("Is Applicant Email Confidential", "applicantsFL401.isEmailAddressConfidential"),
        APPLICANT_PHONE_CONFIDENTIAL("Is Applicant Phone Confidential", "applicantsFL401.isPhoneNumberConfidential"),
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

    private static final CsvColumn[] COLUMNS = CsvColumn.values();

    public static File writeCcdOrderDataToCsv(CaseData ccdOrderData, boolean confidentialAllowed) throws IOException {
        Path path = Files.createTempFile("AcroReport", ".csv", ATTRIBUTE);
        File file = path.toFile();
        String[] headers = Arrays.stream(COLUMNS).map(CsvColumn::getHeader).toArray(String[]::new);
        CSVFormat csvFileHeader = CSVFormat.DEFAULT.withHeader(headers);

        try (FileWriter fileWriter = new FileWriter(file);
             CSVPrinter printer = new CSVPrinter(fileWriter, csvFileHeader)) {
            List<String> record = new ArrayList<>();
            for (CsvColumn column : COLUMNS) {
                Object value = extractPropertyValues(ccdOrderData, column.getProperty());

                if (!confidentialAllowed && shouldBlankConfidentialValue(ccdOrderData, column)) {
                    value = "-";
                }

                if (value == null || value.toString().isEmpty()) {
                    logger.warn("Missing value for CSV column '{}' (property '{}')", column.getHeader(), column.getProperty());
                }
                record.add(value != null ? value.toString() : "");
            }
            printer.printRecord(record);
        }
        return file;
    }

    private static boolean shouldBlankConfidentialValue(CaseData caseData, CsvColumn column) {
        return switch (column) {
            case APPLICANT_PHONE -> isConfidential(caseData, "applicantsFL401.isPhoneNumberConfidential");
            case APPLICANT_EMAIL -> isConfidential(caseData, "applicantsFL401.isEmailAddressConfidential");
            case APPLICANT_ADDRESS1, APPLICANT_ADDRESS2, APPLICANT_POSTCODE ->
                isConfidential(caseData, "applicantsFL401.isAddressConfidential");
            case RESPONDENT_ADDRESS1, RESPONDENT_ADDRESS2, RESPONDENT_POSTCODE ->
                isConfidential(caseData, "respondentsFL401.isAddressConfidential");
            default -> false;
        };
    }

    private static boolean isConfidential(CaseData caseData, String confidentialityProperty) {
        Object value = extractPropertyValues(caseData, confidentialityProperty);
        if (value instanceof YesOrNo) {
            return YesOrNo.Yes.equals(value);
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return "Yes".equalsIgnoreCase(String.valueOf(value)) || "true".equalsIgnoreCase(String.valueOf(value));
    }

    public static Object extractPropertyValues(Object obj, String propertyPath) {
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

        return currentValue != null ? currentValue : "";
    }

    private static Object getPropertyValue(Object obj, String propertyName) {
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

    private static Method createGetter(Class<?> clazz, String propertyName) throws NoSuchMethodException {
        String getterName = "get" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
        return clazz.getMethod(getterName);
    }

    private static boolean isWrappedListProperty(String propertyName) {
        Set<String> wrappedListProperties = Set.of("respondents", "applicants");
        return wrappedListProperties.contains(propertyName.toLowerCase());
    }

    private static Object extractFromWrappedList(Object value) {
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

    private static boolean isEmpty(Object value) {
        return value == null || "".equals(value);
    }
}
