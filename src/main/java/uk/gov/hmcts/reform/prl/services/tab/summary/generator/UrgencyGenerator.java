package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.Urgency;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Component
public class UrgencyGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().urgencyDetails(Urgency.builder().urgencyStatus(getUrgencyStatus(caseData)).build()).build();
    }

    private String getUrgencyStatus(CaseData caseData) {
        String urgencyStatus = PrlAppsConstants.BLANK_STRING;
        if (PrlAppsConstants.C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            String[] listOfValues = {
                YesOrNo.Yes.equals(caseData.getIsCaseUrgent()) ? "Urgent" : "Not urgent",
                YesOrNo.Yes.equals(caseData.getDoYouNeedAWithoutNoticeHearing()) ? "without notice" : "",
                YesOrNo.Yes.equals(caseData.getDoYouRequireAHearingWithReducedNotice()) ? "reduced notice" : "" };

            List<String> modifiableList = new ArrayList<>(Arrays.asList(listOfValues));
            modifiableList.removeAll(Arrays.asList("", null));
            urgencyStatus = String.join(", ", modifiableList);
        }
        if (PrlAppsConstants.FL401_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())) {
            Optional<WithoutNoticeOrderDetails> withoutNoticeOrderDetails = ofNullable(caseData.getOrderWithoutGivingNoticeToRespondent());
            if (withoutNoticeOrderDetails.isPresent()) {
                urgencyStatus =  YesOrNo.Yes.equals(withoutNoticeOrderDetails.get().getOrderWithoutGivingNotice()) ? PrlAppsConstants.WITHOUT_NOTICE :
                                                                                                                        PrlAppsConstants.WITH_NOTICE;
            }
        }

        return urgencyStatus;
    }
}
