package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "applicantFamilyDetailsObject", generate = true)
@Data
@Builder(toBuilder = true)
public class ApplicantFamilyDetails {

    @CCD(
            label = "*Does the applicant have any children, have parental responsibility for any children or need to protect other children with this application?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("doesApplicantHaveChildren")
    private final YesOrNo doesApplicantHaveChildren;

    @JsonCreator
    public ApplicantFamilyDetails(YesOrNo doesApplicantHaveChildren) {
        this.doesApplicantHaveChildren  = doesApplicantHaveChildren;
    }

}
