package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Service
@RequiredArgsConstructor
public class ExtendedCaseDetailsService {
    private final FormattedTitleDisplayService formattedTitleDisplayService;
    private final String PREFIX_INJECTED_DATA = "INJECTED_DATA.";

    public String getExtendedDetails(CaseData caseData) {
        String extendedCaseDetailsTitle = formattedTitleDisplayService.getFormattedTitleDisplay(
            caseData.getApplicantCaseName(),
            caseData.getCaseNameHmctsInternal(),
            caseData.getFamilymanCaseNumber(),
            caseData.getId(),
            caseData.getIssueDate()
        );
        return PREFIX_INJECTED_DATA + extendedCaseDetailsTitle;
    }
}
