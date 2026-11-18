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
    String location;
    String prefix;
    String locationCode;

    @JsonIgnore
    public static CourtPrefix map(Map<String, String> row) {
        return CourtPrefix.builder()
            .epimmsId(row.getOrDefault("\uFEFFEpimmsId", ""))
            .refDataCourtName(row.getOrDefault("RefDataCourtName", ""))
            .location(row.getOrDefault("Location", ""))
            .prefix(row.getOrDefault("Prefix", ""))
            .locationCode(row.getOrDefault("LocationCode", ""))
            .build();
    }
}
