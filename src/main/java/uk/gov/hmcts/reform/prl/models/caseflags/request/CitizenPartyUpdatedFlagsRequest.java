package uk.gov.hmcts.reform.prl.models.caseflags.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CitizenPartyUpdatedFlagsRequest {
    private final String caseTypeOfApplication;
    private String partyIdamId;
    private FlagsUpdatedRequest partyExternalFlags;
}
