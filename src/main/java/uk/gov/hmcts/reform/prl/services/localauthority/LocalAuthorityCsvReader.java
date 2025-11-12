package uk.gov.hmcts.reform.prl.services.localauthority;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LocalAuthorityCsvReader {

    @Value("${courtmapper.csv}")
    private String csvResourceFile;

    private List<LocalAuthorityCsvData> laData = new ArrayList<>();

    public LocalAuthorityCsvReader() throws IOException {

        List<List<String>> records = Files.readAllLines(Paths.get(csvResourceFile))
            .stream()
            .map(line -> Arrays.asList(line.split(",")))
            .collect(Collectors.toList());

        records.forEach(r -> addToMap(r));
    }

    private void addToMap(List<String> row) {
        LocalAuthorityCsvData localAuthorityCsvData = LocalAuthorityCsvData.builder()
            .localAuthority(row.get(0))
            .designatedFamilyCourt(row.get(1))
            .specificPostCodes(row.get(2))
            .excludes(row.get(3))
            .build();

        laData.add(localAuthorityCsvData);


    }
}
