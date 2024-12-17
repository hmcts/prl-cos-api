package uk.gov.hmcts.reform.prl.models.caseflags.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CitizenPartyFlagsRequest {
    private final String caseTypeOfApplication;
    private String partyIdamId;
    private FlagsRequest partyExternalFlags;
}
