package uk.gov.hmcts.reform.prl.services.tab.summary.generator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.CaseSummary;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.DateOfSubmission;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

@Component
public class DateOfSubmissionGenerator implements FieldGenerator {
    @Override
    public CaseSummary generate(CaseData caseData) {
        return CaseSummary.builder().dateOfSubmission(DateOfSubmission.builder()
                                                          .dateOfSubmission(
                                                              CommonUtils.getIsoDateToSpecificFormat(
                                                                      caseData.getDateSubmitted(),
                                                                      CommonUtils.DATE_OF_SUBMISSION_FORMAT))
                                                          .build()).build();
    }
}
