package uk.gov.hmcts.reform.prl.utils.csv;

import com.opencsv.CSVReaderHeaderAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CsvReader {
    public List<Map<String, String>> read(String filePath) {

        try (CSVReaderHeaderAware reader =
                 new CSVReaderHeaderAware(new FileReader(ResourceUtils.getFile(filePath)))) {

            List<Map<String, String>> result = new ArrayList<>();
            Map<String, String> row;

            while ((row = reader.readMap()) != null) {
                result.add(row);
            }

            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Failed to read CSV: " + filePath, e);
        }
    }
}
