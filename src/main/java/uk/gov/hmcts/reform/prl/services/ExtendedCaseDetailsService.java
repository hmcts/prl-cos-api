package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Service
@RequiredArgsConstructor
public class ExtendedCaseDetailsService {
    private final FormattedTitleDisplayService formattedTitleDisplayService;

    public String getExtendedDetails(CaseData caseData) {
        String extendedCaseDetailsTitle = formattedTitleDisplayService.getFormattedTitleDisplay(
            caseData.getApplicantCaseName(),
            caseData.getCaseNameHmctsInternal(),
            caseData.getFamilymanCaseNumber(),
            caseData.getId(),
            caseData.getIssueDate()
        );
        return extendedCaseDetailsTitle;
    }
}
