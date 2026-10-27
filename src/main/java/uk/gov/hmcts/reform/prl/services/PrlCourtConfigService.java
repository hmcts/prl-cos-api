package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.DfjArea;
import uk.gov.hmcts.reform.prl.models.court.PrlCourtMapping;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class PrlCourtConfigService {

    private static final String PRL_COURT_CONFIG = "prlCourtConfig.csv";

    private final CsvReader csvReader;
    private final ObjectMapper objectMapper;
    private List<PrlCourtMapping> prlCourtMapping;

    public PrlCourtConfigService(@Autowired CsvReader csvReader, @Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.csvReader = csvReader;

        loadPrlCourtMappings();
    }

    private void loadPrlCourtMappings() {
        try {
            List<Map<String, String>> rows = csvReader.read(PRL_COURT_CONFIG);
            prlCourtMapping = objectMapper.convertValue(rows, new TypeReference<>(){});
        } catch (Exception e) {
            log.error("Unable to parse prlCourtConfig.csv file.", e);
        }
    }

    public boolean isPathfinderCourt(String baseLocationId) {
        return getPrlCourtById(baseLocationId)
            .map(PrlCourtMapping::getDfjArea)
            .map(DfjArea::isPathFinderEnabled)
            .orElse(false);
    }

    private Optional<PrlCourtMapping> getPrlCourtById(String baseLocationId) {
        return prlCourtMapping.stream()
            .filter(mapping -> mapping.getCourtCode().equals(baseLocationId))
            .findFirst();
    }

    private Optional<PrlCourtMapping> getPrlCourtMappingFromName(String courtName) {
        return prlCourtMapping.stream()
            .filter(prlCourtDetails -> prlCourtDetails.getCourtName().equalsIgnoreCase(courtName))
            .findAny();
    }

    public Map<String, String> getDfjAreaFieldsByCourtId(String courtCode) {
        Optional<PrlCourtMapping> prlCourt = getPrlCourtById(courtCode);
        Map<String, String> dfjAreaFields = new HashMap<>();
        if (prlCourt.isPresent()) {
            DfjArea dfjArea = prlCourt.get().getDfjArea();
            dfjAreaFields.put("dfjArea", dfjArea.getName());
            dfjAreaFields.put(dfjArea.getCourtField(), courtCode);
        }
        return dfjAreaFields;
    }

    public Map<String, String> getDfjAreaFieldsByCourtName(String name) {
        Optional<PrlCourtMapping> prlCourt = getPrlCourtMappingFromName(name);
        Map<String, String> dfjAreaFields = new HashMap<>();
        if (prlCourt.isPresent()) {
            DfjArea dfjArea = prlCourt.get().getDfjArea();
            dfjAreaFields.put("dfjArea", dfjArea.getName());
            dfjAreaFields.put(dfjArea.getCourtField(), prlCourt.get().getCourtCode());
        }
        return dfjAreaFields;
    }

    public Set<String> getAllCourtFields() {
        return Stream.of(DfjArea.values())
                .map(DfjArea::getCourtField)
                .collect(toSet());
    }

}
