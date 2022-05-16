package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class ApplicantFamily {
    private final YesOrNo doesApplicantHaveChildren;
    private List<Element<ApplicantChild>> applicantChild;
}
