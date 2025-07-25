package uk.gov.hmcts.reform.prl.services.bais;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CsvWriterTest {

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
}
