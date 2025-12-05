package uk.gov.hmcts.reform.prl.services;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.LocalAuthorityCourt;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocalAuthorityCourtDataLoader {

    private static final String LOCAL_AUTHORITY_CSV_FILE_PATH = "council_court_epims_csv/Local_Authority_to_Court_Nov_25.csv";

    private final CsvReader csvReader;

    private final List<LocalAuthorityCourt> localAuthorityCourtList = new ArrayList<>();

    private List<LocalAuthorityCourt> loadCsv() {
        List<Map<String, String>> rows = csvReader.read(LOCAL_AUTHORITY_CSV_FILE_PATH);

        rows.forEach(line -> localAuthorityCourtList.add(LocalAuthorityCourt.map(line)));

        return localAuthorityCourtList;
    }

    public List<LocalAuthorityCourt> getLocalAuthorityCourtList() {
        if (localAuthorityCourtList.isEmpty()) {
            loadCsv();
        }
        return ImmutableList.copyOf(localAuthorityCourtList);
    }
}
