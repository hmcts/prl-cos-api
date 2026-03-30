package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.court.PathFinderMapping;
import uk.gov.hmcts.reform.prl.utils.ResourceReader;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

@Slf4j
@Service
public class PathFinderLookupService {

    private final ObjectMapper objectMapper;
    private List<PathFinderMapping> pathFinderMapping;
    @Getter
    private Set<String> courtFields;

    public PathFinderLookupService(@Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        loadPathFinderMappings();
        loadCourtFields();
    }

    private void loadPathFinderMappings() {
        try {
            final String jsonContent = ResourceReader.readString("pathFinderMapping.json");
            pathFinderMapping = objectMapper.readValue(jsonContent, new TypeReference<>() {});
        } catch (IOException e) {
            log.error("Unable to parse pathFinderMapping.json file.", e);
        }
    }

    private void loadCourtFields() {
        courtFields = pathFinderMapping.stream()
            .map(PathFinderMapping::getCourtField)
            .collect(toSet());
    }

    public Optional<PathFinderMapping> getPathFinderMappingByCourtField(String baseLocationId) {
        return pathFinderMapping.stream()
            .filter(mapping -> mapping.getCourtCode().equals(baseLocationId))
            .findFirst();
    }

}
