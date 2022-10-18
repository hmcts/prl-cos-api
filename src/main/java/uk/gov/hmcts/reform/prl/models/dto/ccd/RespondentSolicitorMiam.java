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
public class RespondentSolicitorMiam {

    @JsonProperty("whatIsMiamPlaceHolder")
    private final String whatIsMiamPlaceHolder;

    private final YesOrNo respondentSolicitorHaveYouAttendedMiam;

    @JsonProperty("helpMiamCostsExemptionsPlaceHolder")
    private final String helpMiamCostsExemptionsPlaceHolder;

    private final YesOrNo respondentSolicitorWillingnessToAttendMiam;

}
