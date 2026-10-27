package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.court.PrlCourtMapping;
import uk.gov.hmcts.reform.prl.utils.csv.CsvReader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Slf4j
@Service
public class PrlCourtConfigService {

    private static final String PRL_COURT_CONFIG = "prlCourtConfig.csv";

    private final CsvReader csvReader;
    private final ObjectMapper objectMapper;
    private List<PrlCourtMapping> prlCourtMapping;
    @Getter
    private Set<String> courtFields;

    public PrlCourtConfigService(@Autowired CsvReader csvReader, @Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.csvReader = csvReader;

        loadPrlCourtMappings();
        loadCourtFields();
    }

    private void loadPrlCourtMappings() {
        try {
            List<Map<String, String>> rows = csvReader.read(PRL_COURT_CONFIG);
            prlCourtMapping = objectMapper.convertValue(rows, new TypeReference<>(){});
        } catch (Exception e) {
            log.error("Unable to parse prlCourtConfig.csv file.", e);
        }
    }

    private void loadCourtFields() {
        if (isNotEmpty(prlCourtMapping)) {
            courtFields = prlCourtMapping.stream()
                .map(PrlCourtMapping::getCourtField)
                .collect(toSet());
        }
    }

    public Optional<PrlCourtMapping> getPathFinderMappingByCourtField(String baseLocationId) {
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
        Optional<PrlCourtMapping> dfjArea = getPathFinderMappingByCourtField(courtCode);
        Map<String, String> dfjAreaFields = new HashMap<>();
        if (dfjArea.isPresent()) {
            dfjAreaFields.put("dfjArea", dfjArea.get().getDfjArea());
            dfjAreaFields.put(dfjArea.get().getCourtField(), courtCode);
        }
        return dfjAreaFields;
    }

    public Map<String, String> getDfjAreaFieldsByCourtName(String name) {
        Optional<PrlCourtMapping> dfjArea = getPrlCourtMappingFromName(name);
        Map<String, String> dfjAreaFields = new HashMap<>();
        if (dfjArea.isPresent()) {
            dfjAreaFields.put("dfjArea", dfjArea.get().getDfjArea());
            dfjAreaFields.put(dfjArea.get().getCourtField(), dfjArea.get().getCourtCode());
        }
        return dfjAreaFields;
    }

    public Set<String> getAllCourtFields() {
        return courtFields;
    }

}
