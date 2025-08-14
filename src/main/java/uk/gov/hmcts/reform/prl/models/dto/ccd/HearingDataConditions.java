package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HearingDataConditions {

    @JsonProperty("isApplicant1Present")
    private final YesOrNo isApplicant1Present;
    @JsonProperty("isApplicant4Present")
    private final YesOrNo isApplicant4Present;
    @JsonProperty("isApplicant1SolicitorPresent")
    private final YesOrNo isApplicant1SolicitorPresent;
    @JsonProperty("isApplicant4SolicitorPresent")
    private final YesOrNo isApplicant4SolicitorPresent;
}
