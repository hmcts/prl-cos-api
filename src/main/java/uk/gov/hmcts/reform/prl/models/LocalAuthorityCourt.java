package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Value
@Builder
@AllArgsConstructor
public class LocalAuthorityCourt {
    String localAuthority;
    String familyCourtName;
    String refDataCourtName;
    String localCustodianCode;
    List<String> specificPostCodes;
    String excludes;
    String epimmsId;
    String address;
    String emailAddress;
    String status;

    @JsonIgnore
    public static LocalAuthorityCourt map(Map<String, String> row) {
        return LocalAuthorityCourt.builder()
            .localAuthority(row.getOrDefault("Local Authorities", ""))
            .familyCourtName(row.getOrDefault("Designated Family Ct", ""))
            .refDataCourtName(row.getOrDefault("RefData Court Name", ""))
            .localCustodianCode(row.getOrDefault("Local Custodian code", ""))
            .specificPostCodes(Arrays.asList(row.getOrDefault("Specific PostCodes", "").split(";")))
            .excludes(row.getOrDefault("Excludes", ""))
            .epimmsId(row.getOrDefault("epimms", ""))
            .address(row.getOrDefault("Address", ""))
            .emailAddress(row.getOrDefault("emailAddress", ""))
            .status(row.getOrDefault("Status", ""))
            .build();
    }
}
