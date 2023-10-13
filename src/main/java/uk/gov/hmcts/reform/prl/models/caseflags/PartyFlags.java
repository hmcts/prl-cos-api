package uk.gov.hmcts.reform.prl.models.caseflags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class PartyFlags {
    private Flags partyInternalFlags;
    private Flags partyExternalFlags;
}
