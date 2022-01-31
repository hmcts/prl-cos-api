package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ApplicantFamilyDetails {

    private final YesOrNo doesApplicantHaveChildren;
    @JsonProperty("applicantChild")
    private final List<Element<ApplicantChild>> applicantChildren;
}
