package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.court.DfjAreaCourtMapping;
import uk.gov.hmcts.reform.prl.utils.ResourceReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class DfjLookupService {

    private final ObjectMapper objectMapper;
    private List<DfjAreaCourtMapping> dfjCourtMapping;
    private Set<String> courtFields;


    public DfjLookupService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadDfjMappings();
        loadCourtFields();
    }

    private void loadDfjMappings() {
        try {
            final String jsonContent = ResourceReader.readString("dfjAreaCourtMapping.json");
            dfjCourtMapping = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse dfjAreaCourtMapping.json file.", e);
        }
    }

    private void loadCourtFields() {
        courtFields = dfjCourtMapping.stream()
            .map(DfjAreaCourtMapping::getCourtField)
            .collect(toSet());
    }

    public Optional<DfjAreaCourtMapping> getDfjArea(String courtCode) {
        return dfjCourtMapping.stream()
            .filter(dfjCourtMap -> dfjCourtMap.getCourtCode().equals(courtCode))
            .findAny();
    }

    public Map<String, String> getDfjAreaFields(String courtCode) {
        Optional<DfjAreaCourtMapping> dfjArea = getDfjArea(courtCode);
        Map<String, String> dfjAreaFields = new HashMap<>();
        if (dfjArea.isPresent()) {
            dfjAreaFields.put("dfjArea", dfjArea.get().getDfjArea());
            dfjAreaFields.put(dfjArea.get().getCourtField(), courtCode);
        }
        return dfjAreaFields;
    }

    public Set<String> getAllCourtFields() {
        return courtFields;
    }

}
