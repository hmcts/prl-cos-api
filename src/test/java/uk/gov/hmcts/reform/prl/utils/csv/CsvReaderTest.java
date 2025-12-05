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
        assertThat(first.get("Local Authorities")).isEqualTo("LA1");
        assertThat(first.get("epimms")).isEqualTo("1001");
        assertThat(first.get("Status")).isEqualTo("Open");

        Map<String, String> second = results.get(1);
        assertThat(second.get("Local Authorities")).isEqualTo("LA2");
        assertThat(second.get("epimms")).isEqualTo("1002");
        assertThat(second.get("Status")).isEqualTo("Closed");
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
