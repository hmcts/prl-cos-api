package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class ApplicantFamilyDetails {

    @JsonProperty("doesApplicantHaveChildren")
    private final YesOrNo doesApplicantHaveChildren;

    @JsonCreator
    public ApplicantFamilyDetails(YesOrNo doesApplicantHaveChildren) {
        this.doesApplicantHaveChildren  = doesApplicantHaveChildren;
    }

}
