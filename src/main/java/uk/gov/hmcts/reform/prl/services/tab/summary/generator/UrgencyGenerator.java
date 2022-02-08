package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.Urgency;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class UrgencyGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().urgencyDetails(Urgency.builder().urgencyStatus(getUrgencyStatus(caseData)).build()).build();
    }

    private String getUrgencyStatus(CaseData caseData) {
        String[] listOfValues = {
            YesOrNo.Yes.equals(caseData.getIsCaseUrgent()) ? "Urgent" : "",
            YesOrNo.Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing()) ? "Without notice" : "",
            YesOrNo.Yes.equals(caseData.getDoYouRequireAHearingWithReducedNotice()) ? "Redued notice" : "" };
        List modifiableList = new ArrayList(Arrays.asList(listOfValues));
        modifiableList.removeAll(Arrays.asList("", null));
        return String.join(", ", modifiableList);
    }
}
