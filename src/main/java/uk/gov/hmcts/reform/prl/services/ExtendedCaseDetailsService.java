package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExtendedCaseDetailsService {
    private FormattedTitleDisplayService formattedTitleDisplayService;

    public void setExtendedDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        String extendedCaseDetailsTitle = formattedTitleDisplayService.getFormattedTitleDisplay(
            caseData.getApplicantCaseName(),
            caseData.getCaseNameHmctsInternal(),
            caseData.getFamilymanCaseNumber(),
            caseData.getId(),
            caseData.getIssueDate()
        );
        HashMap injectedData = new HashMap();
        injectedData.put("extendedCaseDetailsTitle", extendedCaseDetailsTitle);
        caseDataUpdated.put("INJECTED_DATA", injectedData);
    }
}
