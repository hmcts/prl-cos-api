package uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllegationOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ApplicationTypeDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ConfidentialDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OrderAppliedFor;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedingEmptyTable;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.SpecialArrangements;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.Urgency;

import java.util.List;

@Builder
@Data
public class CaseSummary implements MappableObject {
    private final AllocatedJudge allocatedJudgeDetails;
    private final CaseStatus caseStatus;
    private final ConfidentialDetails confidentialDetails;
    private final Urgency urgencyDetails;
    private final AllegationOfHarm allegationOfHarm;
    private final AllegationOfHarmRevised allegationOfHarmRevised;
    //private final Hearings hearings;
    private final SpecialArrangements specialArrangement;
    private final OrderAppliedFor summaryTabForOrderAppliedFor;
    private final List<Element<OtherProceedings>> otherProceedingsForSummaryTab;
    private final OtherProceedingEmptyTable otherProceedingEmptyTable;
    private final DateOfSubmission dateOfSubmission;
    private final ApplicationTypeDetails applicationTypeDetails;

}
