package uk.gov.hmcts.reform.prl.utils.csv;

import com.opencsv.CSVReaderHeaderAware;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CsvReader {

    public List<Map<String, String>> read(String resourcePath) {

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new IllegalStateException("Resource not found: " + resourcePath);
            }

            CSVReaderHeaderAware csv = new CSVReaderHeaderAware(new InputStreamReader(is, StandardCharsets.UTF_8));

            List<Map<String, String>> results = new ArrayList<>();
            Map<String, String> row;
            while ((row = csv.readMap()) != null) {
                results.add(row);
            }
            return results;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to read resource CSV: " + resourcePath, e);
        }
    }
}
