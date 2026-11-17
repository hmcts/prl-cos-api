package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.CourtPrefix;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourtPrefixDataLoader {

    private static final String COURT_PREFIX_CSV_FILE_PATH = "council_court_epims_csv/court_prefix_version_1.0.csv";

    private final CsvReader csvReader;

    private final List<CourtPrefix> courtPrefixList = new ArrayList<>();
    private Map<String, String> courtPrefixMap = new HashMap<>();

    private void loadCourtPrefixCsv() {
        List<Map<String, String>> rows = csvReader.read(COURT_PREFIX_CSV_FILE_PATH);
        log.info("CourtPrefix csv file rows {}", rows.size());
        rows.forEach(line -> courtPrefixList.add(CourtPrefix.map(line)));
        courtPrefixMap = courtPrefixList.stream().collect(Collectors.toMap(CourtPrefix::getEpimmsId,
                                                                           CourtPrefix::getPrefix));
        log.info("Loaded {} court prefixes", courtPrefixMap.size());

    }

    public String getPrefixForGivenEpimmsId(String epimmsId) {
        if (courtPrefixMap.isEmpty()) {
            loadCourtPrefixCsv();
        }
        return courtPrefixMap.get(epimmsId);
    }
}
