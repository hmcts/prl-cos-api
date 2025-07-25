package uk.gov.hmcts.reform.prl.services.bais;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class CsvWriter {
    private static final Logger logger = LoggerFactory.getLogger(CsvWriter.class);

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

    public static File writeCcdOrderDataToCsv(
        CaseData CcdOrderData
    ) throws java.io.IOException {
        Path path = Files.createTempFile("AcroReport", ".csv", ATTRIBUTE);
        File file = path.toFile();
        CSVFormat csvFileHeader = CSVFormat.DEFAULT.builder().setHeader(ACRO_REPORT_CSV_HEADERS).build();

        try (java.io.FileWriter fileWriter = new java.io.FileWriter(file);
             CSVPrinter printer = new CSVPrinter(fileWriter, csvFileHeader)) {

        }
        return file;
    }
}
