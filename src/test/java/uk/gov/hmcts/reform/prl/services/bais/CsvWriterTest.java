package uk.gov.hmcts.reform.prl.services.bais;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CsvWriterTest {

    private static final String[] EXPECTED_HEADERS = {
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

    @Test
    void shouldReturnCsvFile() throws Exception {
        // Arrange: create a dummy CaseData (use a builder or mock as needed)
        CaseData caseData = CaseData.builder().build();

        // Act
        File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);

        // Assert
        assertNotNull(csvFile);
        assertTrue(csvFile.exists());
        assertTrue(csvFile.getName().endsWith(".csv"));
    }

    @Test
    void csvFileShouldHaveAllExpectedHeaders() throws Exception {
        CaseData caseData = CaseData.builder().build();
        File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {
            String headerLine = reader.readLine();
            assertNotNull(headerLine);
            for (String header : EXPECTED_HEADERS) {
                assertTrue(headerLine.contains(header), "Missing header: " + header);
            }
        }
    }

    @Test
    void csvFileShouldHaveHeadersInCorrectOrder() throws Exception {
        CaseData caseData = CaseData.builder().build();
        File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);

        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(csvFile))) {
            String headerLine = reader.readLine();
            String[] actualHeaders = headerLine.split(",");
            assertArrayEquals(EXPECTED_HEADERS, actualHeaders);
        }
    }

    @Test
    void csvFileShouldHaveCorrectPermissions() throws Exception {
        CaseData caseData = CaseData.builder().build();
        File csvFile = CsvWriter.writeCcdOrderDataToCsv(caseData);
        java.util.Set<java.nio.file.attribute.PosixFilePermission> perms = java.nio.file.Files.getPosixFilePermissions(csvFile.toPath());
        assertTrue(perms.contains(java.nio.file.attribute.PosixFilePermission.OWNER_READ));
        assertTrue(perms.contains(java.nio.file.attribute.PosixFilePermission.OWNER_WRITE));
        assertTrue(perms.contains(java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE));
    }
}
