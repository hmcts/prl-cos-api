package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class ApplicantFamily {
    @CCD(
            label = "Does the applicant have any children (or parental responsibility over any children) who need to be protected as part of this application?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo doesApplicantHaveChildren;
    @CCD(
            label = "Child",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ChildToBeProtected"
    )
    private List<Element<ApplicantChild>> applicantChild;
}
