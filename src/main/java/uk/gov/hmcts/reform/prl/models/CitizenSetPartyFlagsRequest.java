package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.caseflags.PartyFlags;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class CitizenSetPartyFlagsRequest {

    private String partyIdamId;
    private PartyFlags partyFlags;
    private final String caseTypeOfApplication;
    private PartyEnum partyType;
}
