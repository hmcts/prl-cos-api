package uk.gov.hmcts.reform.prl.utils.csv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReaderTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldReadCsvIntoHeaderMappedRows() throws Exception {
        Path csv = tempDir.resolve("test.csv");

        try (FileWriter writer = new FileWriter(csv.toFile())) {
            writer.write("Local Authorities,epimms,Status\n");
            writer.write("LA1,1001,Open\n");
        }

        CsvReader reader = new CsvReader();
        List<Map<String, String>> rows = reader.read(csv.toString());

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).get("Local Authorities")).isEqualTo("LA1");
        assertThat(rows.get(0).get("epimms")).isEqualTo("1001");
        assertThat(rows.get(0).get("Status")).isEqualTo("Open");
    }

}
