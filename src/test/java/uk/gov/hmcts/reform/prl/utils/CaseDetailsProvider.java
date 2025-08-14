package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;

import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;

@NoArgsConstructor
public class CaseDetailsProvider {

    public static CaseDetails empty() {
        return CaseDetails.builder().caseData(CaseDataProvider.empty()).build();
    }

    public static CaseDetails full() {
        return CaseDetails.builder().caseId(TEST_CASE_ID).caseData(CaseDataProvider.full()).build();
    }

    public static CaseDetails of(CaseData caseData) {
        return CaseDetails.builder().caseData(caseData).build();
    }
}
