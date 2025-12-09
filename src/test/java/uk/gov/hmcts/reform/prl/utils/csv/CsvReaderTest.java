package uk.gov.hmcts.reform.prl.utils.csv;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvReaderTest {

    private final CsvReader csvReader = new CsvReader();

    @Test
    void shouldReadCsvFromClasspathResource() {
        List<Map<String, String>> results =
            csvReader.read("sample.csv");

        assertThat(results).hasSize(2);

        Map<String, String> first = results.get(0);
        assertThat(first).containsEntry("Local Authorities","LA1")
            .containsEntry("epimms","1001")
            .containsEntry("Status","Open");

        Map<String, String> second = results.get(1);
        assertThat(second).containsEntry("Local Authorities","LA2")
            .containsEntry("epimms","1002")
            .containsEntry("Status","Closed");
    }

    @Test
    void shouldThrowWhenCsvIsMalformed() {
        assertThatThrownBy(() ->
                               csvReader.read("sampleBad.csv")
        )
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Failed to read resource CSV");
    }

}
