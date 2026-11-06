package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
@AllArgsConstructor
public class CourtPrefix {
    String epimmsId;
    String refDataCourtName;
    String prefix;

    @JsonIgnore
    public static CourtPrefix map(Map<String, String> row) {
        return CourtPrefix.builder()
            .epimmsId(row.getOrDefault("EpimmsId", ""))
            .refDataCourtName(row.getOrDefault("RefDataCourtName", ""))
            .prefix(row.getOrDefault("Prefix", ""))
            .build();
    }
}
