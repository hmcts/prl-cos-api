package uk.gov.hmcts.reform.prl.services.bais;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CsvWriter {
    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

    // enum field name to combine ACRO_REPORT_CSV_HEADERS and propertyNames

    private static final FileAttribute<Set<PosixFilePermission>> ATTRIBUTE = PosixFilePermissions
        .asFileAttribute(PosixFilePermissions.fromString("rwx------"));

    private static final String[] ACRO_REPORT_CSV_HEADERS = {
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

    private static final List<String> propertyNames = Arrays.asList(
        "CaseId", "CourtName", "CourtId", "CourtDate",
        "OrderExpiryDate", "Respondent.Surname", "Respondent.FirstName", "Respondent.Address1",
        "Respondent.Address2", "Respondent.Postcode", "Applicant.Surname", "Applicant.FirstName",
        "Applicant.Address1", "Applicant.Address2", "Applicant.Postcode",
        "PdfIdentifier", "IsConfidential", "ForceCode"
    );


    public static File writeCcdOrderDataToCsv(
        CaseData ccdOrderData
    ) throws java.io.IOException {
        Path path = Files.createTempFile("AcroReport", ".csv", ATTRIBUTE);
        File file = path.toFile();
        CSVFormat csvFileHeader = CSVFormat.DEFAULT.withHeader(ACRO_REPORT_CSV_HEADERS);

        try (java.io.FileWriter fileWriter = new java.io.FileWriter(file);
             CSVPrinter printer = new CSVPrinter(fileWriter, csvFileHeader)) {
            List<String> record = new java.util.ArrayList<>();
            for (String pathName : propertyNames) {
                Object value = extractPropertyValues(ccdOrderData, pathName);
                record.add(value != null ? value.toString() : "");
            }
        }
        return file;
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
                return "-";
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
