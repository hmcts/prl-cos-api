package uk.gov.hmcts.reform.prl.models.caseflags;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class PartyFlags {
    private Flags partyInternalFlags;
    private Flags partyExternalFlags;
}
